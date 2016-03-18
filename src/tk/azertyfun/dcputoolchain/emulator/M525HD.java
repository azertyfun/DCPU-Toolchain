package tk.azertyfun.dcputoolchain.emulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class M525HD extends DCPUHardware {
	public static final int TYPE = 0x525d4ac5, REVISION = 0x0001, MANUFACTURER = 0x1eb37e91;

	public static final int TRACKS = 64*4, SECTORS_PER_TRACK = 20, WORDS_PER_SECTOR = 512;

	public static final int WORDS_PER_SECOND = 30700;
	public static final float SEEKING_TIME_MS = 2.4f;

	protected char state = 0x0000, error = 0x0000;
	protected boolean interrupt = false;

	protected char[] disk = new char[WORDS_PER_SECTOR * SECTORS_PER_TRACK * TRACKS];

	protected boolean reading = false;
	protected boolean writing = false;
	protected char readFrom, readTo, writeFrom, writeTo;
	protected int ticksSkip = 0;
	protected boolean spinning = false;
	protected boolean spinningUp = false;
	protected long spinUpStart = 0;

	private String disk_path;

	private LinkedList<M525HDCallback> m525HDCallbacks = new LinkedList<>();

	protected M525HD(String id, String path) throws IOException {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;

		this.disk_path = path;

		byte[] disk_b = Files.readAllBytes(Paths.get(path));
		char[] disk = new char[WORDS_PER_SECTOR * SECTORS_PER_TRACK * TRACKS];
		for (int j = 0; j < disk_b.length / 2; ++j) {
			disk[j] = (char) (disk_b[j * 2] << 8);
			disk[j] |= (char) (disk_b[j * 2 + 1] & 0xFF);
		}

		if(disk.length > this.disk.length) {
			throw new IllegalArgumentException("M35FD #" + id + ": given disk file is bigger than " + this.disk.length + " words!");
		} else {
			for(int i = 0; i < disk.length; ++i) {
				this.disk[i] = disk[i];
			}

			for(int i = disk.length; i < this.disk.length; ++i) {
				this.disk[i] = 0;
			}
		}

		state = States.STATE_PARKED;
		error = Errors.ERROR_NONE;
	}

	@Override
	public void interrupt() {

		int a = dcpu.registers[0];
		switch(a) {
			case 0: //POLL_DEVICE
				dcpu.registers[1] = state;
				dcpu.registers[2] = error;
				error = Errors.ERROR_NONE;
				break;
			case 1: //SET_INTERRUPT
				char x = dcpu.registers[3];
				if(x != 0)
					interrupt = true;
				else
					interrupt = false;
				break;
			case 2: //READ_SECTOR
				readFrom = dcpu.registers[3];
				readTo = dcpu.registers[4];
				if(state != States.STATE_READY && state != States.STATE_READY_WP && readFrom >= SECTORS_PER_TRACK * TRACKS) {
					dcpu.registers[1] = 0;
					switch (state) {
						case States.STATE_BUSY:
						case States.STATE_INIT:
						case States.STATE_INIT_WP:
							error = Errors.ERROR_BUSY;
							break;
						case States.STATE_PARKED:
						case States.STATE_PARKED_WP:
							error = Errors.ERROR_PARKED;
							break;
						default:
							if(readFrom >= SECTORS_PER_TRACK * TRACKS)
								error = Errors.ERROR_BAD_ADDRESS;
							else
								error = Errors.ERROR_BROKEN; //We definitely should not have another value for state (that would be a bad programming error).
					}
					break;
				}

				dcpu.registers[1] = 1;
				state = States.STATE_BUSY;
				for(M525HDCallback m525HDCallback : m525HDCallbacks)
					m525HDCallback.statusChanged("STATE_BUSY");
				reading = true;
				ticksSkip = 5; //Skipping 5 ticks because of the specified 80ms seek time. Ideal behaviour would be checking distance between tracks, but it doesn't affect the emulation enough that I will worry about it any time soon.

				break;
			case 3: //WRITE_SECTOR
				writeFrom = dcpu.registers[4];
				writeTo = dcpu.registers[3];
				if(state != States.STATE_READY && writeTo >= SECTORS_PER_TRACK * TRACKS) {
					dcpu.registers[1] = 0;
					switch (state) {
						case States.STATE_BUSY:
						case States.STATE_INIT:
							error = Errors.ERROR_BUSY;
							break;
						case States.STATE_PARKED:
							error = Errors.ERROR_PARKED;
							break;
						case States.STATE_PARKED_WP:
						case States.STATE_INIT_WP:
						case States.STATE_READY_WP:
							error = Errors.ERROR_PROTECTED;
							break;
						default:
							if(writeTo >= SECTORS_PER_TRACK * TRACKS)
								error = Errors.ERROR_BAD_ADDRESS;
							else
								error = Errors.ERROR_BROKEN; //We definitely should not have another value for state (that would be a bad programming error).
					}
					break;
				}

				dcpu.registers[1] = 1;
				state = States.STATE_BUSY;
				for(M525HDCallback m525HDCallback : m525HDCallbacks)
					m525HDCallback.statusChanged("STATE_BUSY");
				writing = true;
				ticksSkip = 5; //Skipping 5 ticks because of the specified 80ms seek time. Ideal behaviour would be checking distance between tracks, but it doesn't affect the emulation enough that I will worry about it any time soon.

				break;
			case 4: //SPIN_DOWN
				spinning = false;
				state = States.STATE_PARKED;
				for(M525HDCallback m525HDCallback : m525HDCallbacks)
					m525HDCallback.statusChanged("STATE_PARKED");
				break;
			case 5:
				if(!spinning) {
					spinUpStart = System.currentTimeMillis();
					spinningUp = true;
					state = States.STATE_INIT;
					for(M525HDCallback m525HDCallback : m525HDCallbacks)
						m525HDCallback.statusChanged("STATE_INIT");
				}
		}
	}

	@Override
	public void tick60hz() {
		/**
		 * The reading should ideally be spanned over several ticks. However, the drive reads a sector per tick and seek time is way less than a tick, so we don't need to.
		 */

		if(spinningUp) {
			long time = System.currentTimeMillis();
			if(time - spinUpStart >= 15000) {
				spinningUp = false;
				spinning = true;
				state = States.STATE_READY;
				for(M525HDCallback m525HDCallback : m525HDCallbacks)
					m525HDCallback.statusChanged("STATE_READY");
			}
		}

		if(reading && (--ticksSkip == 0)) {
			for(int i = 0; i < WORDS_PER_SECTOR; ++i) {
				dcpu.ram[readTo + i] = disk[readFrom * WORDS_PER_SECTOR + i];
			}

			state = States.STATE_READY;
			for(M525HDCallback m525HDCallback : m525HDCallbacks)
				m525HDCallback.statusChanged("STATE_READY");
			reading = false;
		} else if(writing && (--ticksSkip == 0)) {
			for(int i = 0; i < WORDS_PER_SECTOR; ++i) {
				disk[writeTo * WORDS_PER_SECTOR + i] = dcpu.ram[writeFrom + i];
			}

			state = States.STATE_READY;
			for(M525HDCallback m525HDCallback : m525HDCallbacks)
				m525HDCallback.statusChanged("STATE_READY");
			writing = false;
		}
	}

	@Override
	public void powerOff() {
		state = 0x0000;
		error = 0x0000;
		interrupt = false;
	}

	@Override
	public void onDestroy() {
		byte[] disk_b = new byte[disk.length * 2];
		for(int i = 0; i < disk.length; ++i) {
			disk_b[i * 2] = (byte) ((disk[i] >> 8) & 0xFF);
			disk_b[i * 2 + 1] = (byte) (disk[i] & 0xFF);
		}

		try {
			FileOutputStream fos = new FileOutputStream(disk_path);
			fos.write(disk_b);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public char[] getDisk() {
		return disk;
	}

	public void addCallback(M525HDCallback m525HDCallback) {
		m525HDCallbacks.add(m525HDCallback);
	}

	public static class States {
		public static final int STATE_READY =       0x0001;
		public static final int STATE_READY_WP =    0x0002;
		public static final int STATE_BUSY =        0x0003;
		public static final int STATE_PARKED =      0x0004;
		public static final int STATE_PARKED_WP =   0x0005;
		public static final int STATE_INIT =        0x0006;
		public static final int STATE_INIT_WP =     0x0007;
	}

	public static final class Errors {
		public static final int ERROR_NONE =        0x0000;
		public static final int ERROR_BUSY =        0x0001;
		public static final int ERROR_BAD_ADDRESS = 0x0002;
		public static final int ERROR_PROTECTED =   0x0003;
		public static final int ERROR_PARKED =      0x0004;
		public static final int ERROR_BAD_SECTOR =  0x0005;
		public static final int ERROR_BROKEN =      0xFFFF;
	}

	public interface M525HDCallback {
		public void statusChanged(String status);
	}
}
