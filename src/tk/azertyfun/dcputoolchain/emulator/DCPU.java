/*
 * Credits to HerobrinesArmy on github for the code ! https://github.com/HerobrinesArmy/DevCPU
 * TODO IF THIS THING GETS ANYWHERE
 * Ask herobrinesArmy if it's okay to use his code (https://github.com/HerobrinesArmy/DevCPU), the license page is down and not archived by archive.org...
 */

package tk.azertyfun.dcputoolchain.emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
public class DCPU extends Thread implements Identifiable {

	public static final int MAX_DEVICES = 0xFFFF;
	public static final int MAX_QUEUE_SIZE = 256;
	public static final int RAM_SIZE = 0x10000;
	public static final int TOTAL_REGS = 8;

	public int speed_hz = 100000; //100 KHz
	public int batchSize = 100000/30;

	private String id;

	protected char[] ram = new char[RAM_SIZE], ram_init = new char[RAM_SIZE];
	protected char[] registers = new char[TOTAL_REGS];
	protected char pc, sp, ex, ia;
	protected long cycles;
	protected ArrayList<DCPUHardware> hardware = new ArrayList<>();

	protected boolean stopped = false, pausing = false, paused = false;
	protected boolean isSkiping = false, isOnFire = false, isQueueingEnabled = false, sleeping = false;

	protected LinkedList<Character> interrupts = new LinkedList<>();
	private LinkedList<InterruptListener> interruptListeners = new LinkedList<>();

	private boolean tickRequested = false;

	public DCPU(String id) {
		this.id = id;
	}

	public void setStopped() {
		stopped = true;
	}

	public void setRam(String path, boolean big_endian) throws NoSuchFileException, IOException {
		byte[] ram_b = Files.readAllBytes(Paths.get(path));
		char[] ram = new char[ram_b.length / 2];
		for(int i = 0; i < ram.length; ++i) {
			if(big_endian) {
				ram[i] = (char) (ram_b[i * 2] << 8);
				ram[i] |= (char) (ram_b[i * 2 + 1] & 0xFF);
			} else {
				ram[i] = (char) (ram_b[i * 2 + 1] << 8);
				ram[i] |= (char) (ram_b[i * 2] & 0xFF);
			}
		}

		if(ram.length < RAM_SIZE) {
			for(int i = 0; i < RAM_SIZE; ++i)
				this.ram[i] = 0;

			for(int i = 0; i < ram.length; ++i)
				this.ram[i] = ram[i];
		} else {
			for(int i = 0; i < RAM_SIZE; ++i)
				this.ram[i] = ram[i];
		}

		for(int i = 0; i < RAM_SIZE; ++i)
			this.ram_init[i] = this.ram[i];
	}

	@Override
	public void run() {
		for(int i = 0; i < TOTAL_REGS; ++i) {
			registers[i] = 0;
		}
		pc = 0;
		sp = 0;
		ex = 0;
		ia = 0;
		cycles = 0;
		cycles = 0;

		while(!stopped) {
			long start_ns = System.nanoTime();
			for(int i = 0; i < batchSize; ++i) {
				if(!sleeping)
					tick();
			}
			long end_ns = System.nanoTime();
			long waitTime_ns = ((long) ((batchSize * (1f / (float) speed_hz)) * 1000000000L) - (end_ns - start_ns));
			if(waitTime_ns > 0) {
				try {
					Thread.sleep(waitTime_ns / 1000000L, (int) (waitTime_ns % 1000000L));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				(new Thread() {
					public void run() {
						System.err.println("Error: DCPU is lagging behind schedule, currently " + (-waitTime_ns) + " ns late on a " + batchSize + " cycles batch.");
					}
				}).start();
			}
			if(pausing) {
				paused = true;
				while (pausing) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(tickRequested) {
						if(!sleeping)
							tick();
						tickRequested = false;
					}
				}
				paused = false;
			}
		}
	}

	public void tick() {
		cycles++;
		if(isOnFire) {
			corruptRam();
		}

		if(isSkiping) {
			tick_skip();
			return;
		}

		if(!isQueueingEnabled) {
			if(interrupts.size() > 0) {
				char a = interrupts.getFirst();
				if(ia > 0) {
					isQueueingEnabled = true;
					ram[--sp & 0xFFFF] = pc;
					ram[--sp & 0xFFFF] = registers[0];
					registers[0] = a;
					pc = ia;
					interrupts.removeFirst();
				}
			}
		}

		char opcode = ram[pc++];
		int cmd = opcode & 0x1F;
		if(cmd == 0) { //Special opcode
			cmd = opcode >> 5 & 0x1F;
			if(cmd != 0) {
				int atype = opcode >> 10 & 0x3F;
				int aaddr = getAddrA(atype);
				char a = get(aaddr);
				switch(cmd) {
					case 0x01: //JSR
						cycles += 2;
						ram[--sp & 0xFFFF] = pc;
						pc = a;
						break;
					case 0x07: //HCF (Halt and Catch Fire) : Not in the original doc, but widely used among emulators. Can be reproduced by overflowing the interrupt queue though.
						cycles += 8;
						isOnFire = true;
						break;
					case 0x08: //INT
						cycles += 3;
						interrupt(a);
						break;
					case 0x09: //IAG
						set(aaddr, ia);
						break;
					case 0x0a: //IAS
						ia = a;
						if(ia != 0)
							isQueueingEnabled = false;
						break;
					case 0x0b: //RFI
						cycles += 2;
						isQueueingEnabled = false;
						registers[0] = ram[sp++ & 0xFFFF];
						pc = ram[sp++ & 0xFFFF];
						break;
					case 0x0c: //IAQ
						cycles++;
						if(a == 0)
							isQueueingEnabled = false;
						else
							isQueueingEnabled = true;
						break;
					case 0x10: //HWN
						cycles++;
						set(aaddr, (char) hardware.size());
						break;
					case 0x11: //HWQ
						cycles += 3;
						synchronized (hardware) {
							if(a >= 0 && a < hardware.size()) {
								((DCPUHardware) hardware.get(a)).query();
							}
						}
						break;
					case 18: //HWI
						cycles += 3;
						synchronized (hardware) {
							if(a >= 0 && a < hardware.size())
								((DCPUHardware) hardware.get(a)).interrupt();
						}
						break;
					default:
						break;
				}
			}
		} else {
			int atype = opcode >> 10 & 0x3F;
			char a = getValA(atype);
			int btype = opcode >> 5 & 0x1F;
			int baddr = getAddrB(btype);
			char b = get(baddr);

			int val;

			switch(cmd) {
				case 0x01: //SET
					b = a;
					break;
				case 0x02: //ADD
					cycles++;
					val = b + a;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x03: //SUB
					cycles++;
					val = b - a;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x04: //MUL
					cycles++;
					val = b * a;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x05: //MLI
					cycles++;
					val = (short) b * (short) a;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x06: //DIV
					cycles += 2;
					if(a == 0) {
						b = ex = 0;
					} else {
						b /= a;
						ex = (char) ((b << 16) / a);
					}
					break;
				case 0x07: //DVI
					cycles += 2;
					if(a == 0) {
						b = ex = 0;
					} else {
						b = (char) ((short) b / (short) a);
						ex = (char) (((short) b  << 16) / (short) a);
					}
					break;
				case 0x08: //MOD
					cycles += 2;
					if(a == 0)
						b = 0;
					else
						b = (char) (b % a);
					break;
				case 0x09: //MDI
					cycles += 2;
					if(a == 0)
						b = 0;
					else
						b = (char) ((short) b % (short) a);
					break;
				case 0xa: //AND
					b = (char) (b & a);
					break;
				case 0xb: //BOR
					b = (char) (b | a);
					break;
				case 0xc: //XOR
					b = (char) (b ^ a);
					break;
				case 0xd: //SHR
					ex = (char) (b << 16 >> a);
					b = (char) (b >>> a);
					break;
				case 0xe: //ASR
					ex = (char) ((short) b << 16 >>> a);
					b = (char) ((short) b >> a);
					break;
				case 0xf: //SHL
					ex = (char) (b << a >> 16);
					b = (char) (b << a);
					break;
				case 0x10: //IFB
					cycles++;
					if((b & a) == 0)
						skip();
					return;
				case 0x11: //IFC
					cycles++;
					if((b & a) != 0)
						skip();
					return;
				case 0x12: //IFE
					cycles++;
					if(b != a)
						skip();
					return;
				case 0x13: //IFN
					cycles++;
					if(b == a)
						skip();
					return;
				case 0x14: //IFG
					cycles++;
					if(b <= a) skip();
					return;
				case 0x15: //IFA
					cycles++;
					if((short) b <= (short) a)
						skip();
					return;
				case 0x16: //IFL
					cycles++;
					if(b >= a) skip();
					return;
				case 0x17: //IFU
					cycles++;
					if((short) b >= (short) a)
						skip();
					return;
				case 0x1a: //ADX
					cycles++;
					val = b + a + ex;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x1b: //SBX
					cycles++;
					val = b - a + ex;
					b = (char) val;
					ex = (char) (val >> 16);
					break;
				case 0x1e: //STI
					b = a;
					set(baddr, b);
					registers[6]++;
					registers[7]++;
					return;
				case 0x1f: //STD
					b = a;
					set(baddr, b);
					registers[6]--;
					registers[7]--;
					return;
				default:
			}
			set(baddr, b);
		}
	}

	public void interrupt(char a) {
		interrupts.add(a);
		if(interrupts.size() > MAX_QUEUE_SIZE) { //Woops, overflowed the interrupt queue - catching fire (insert evil laugh here)
			interrupts.removeLast();
			isOnFire = true;
		}

		for(InterruptListener i : interruptListeners)
			i.interrupted();
	}

	public void skip() {
		isSkiping = true;
	}

	public int getInstructionLength(char opcode) {
		int length = 1;
		int cmd = opcode & 0x1F;
		if(cmd == 0) { //Special opcode
			cmd =  opcode >> 5 & 0x1F;
			if(cmd > 0) {
				int atype = opcode >> 10 & 0x3F;
				if((atype & 0xF8) == 16 || atype == 31 || atype == 30)
					length++;
			}
		} else {
			int atype = opcode >> 5 & 0x1F;
			int btype = opcode >> 10 & 0x3F;
			if((atype & 0xF8) == 16 || atype == 31 || atype == 30)
				length++;
			if((btype & 0xF8) == 16 || btype == 31 || btype == 30)
				length++;
		}

		return length;
	}

	public char get(int address) {
		if(address < 0x10000)
			return ram[address & 0xFFFF];
		if(address < 0x10008)
			return registers[address & 0x7];
		if(address >= 0x20000)
			return (char) address;
		if(address == 0x10008)
			return sp;
		if(address == 0x10009)
			return pc;
		if(address == 0x1000a)
			return ex;
		if(address == 0x1000b)
			return ia;
		throw new IllegalStateException("Illegal address " + Integer.toHexString(address) + " ! That definitely should not happen.");
	}

	public void set(int address, char value) {
		if(address < 0x10000)
			ram[address & 0xFFFF] = value;
		else if(address < 0x10008)
			registers[address & 0x7] = value;
		else if(address < 0x20000) {
			switch (address) {
				case  0x10008:
					sp = value;
					break;
				case 0x10009:
					pc = value;
					break;
				case 0x1000a:
					ex = value;
					break;
				default:
					throw new IllegalStateException("Illegal address" + Integer.toHexString(address) + " ! That definitely should not happen.");
			}
		}
	}

	public int getAddrA(int type) {
		if(type >= 0x20) {
			return 0x20000 | (type & 0x1F) + 0xFFFF & 0xFFFF;
		}

		switch(type & 0xF8) {
			case 0x00:
				return 0x10000 + (type & 0x7);
			case 0x08:
				return registers[type & 0x7];
			case 0x10:
				cycles++;
				return ram[pc++] + registers[type & 0x7] & 0xFFFF;
			case 0x18:
				switch(type & 0x7) {
					case 0x0:
						return sp++ & 0xFFFF;
					case 0x1:
						return sp & 0xFFFF;
					case 0x2:
						cycles++;
						return ram[pc++] + sp & 0xFFFF;
					case 0x3:
						return 0x10008;
					case 0x4:
						return 0x10009;
					case 0x5:
						return 0x1000a; // Was 0x10010 in Herobrine's code, not sure that's how you hexadecimal tho
					case 0x6:
						cycles++;
						return ram[pc++];
				}
				cycles++;
				return 0x20000 | ram[pc++];
		}

		throw new IllegalStateException("Illegal A value type " + Integer.toHexString(type) + " ! That definitely should not happen.");
	}

	public int getAddrB(int type) {
		switch(type & 0xF8) {
			case 0x00:
				return 0x10000 + (type & 0x7);
			case 0x08:
				return registers[type & 0x7];
			case 0x10:
				cycles++;
				return ram[pc++] + registers[type & 0x7] & 0xFFFF;
			case 0x18:
				switch(type & 0x7) {
					case 0x0:
						return (--sp) & 0xFFFF;
					case 0x1:
						return sp & 0xFFFF;
					case 0x2:
						cycles++;
						return ram[pc++] + sp & 0xFFFF;
					case 0x3:
						return 0x10008;
					case 0x4:
						return 0x10009;
					case 0x5:
						return 0x1000a; // Was 0x10010 in Herobrine's code, not sure that's how you hexadecimal tho
					case 0x6:
						cycles++;
						return ram[pc++];
				}
				cycles++;
				return 0x20000 | ram[pc++];
		}

		throw new IllegalStateException("Illegal B value type " + Integer.toHexString(type) + " ! That definitely should not happen.");
	}

	public char getValA(int type) {
		if(type >= 0x20) {
			return (char) ((type & 0x1F) + 0xFFFF);
		}
		switch (type & 0xF8) {
			case 0x00:
				return registers[type & 0x7];
			case 0x08:
				return ram[registers[type & 0x7]];
			case 0x10:
				cycles++;
				return ram[ram[pc++] + registers[type & 0x7] & 0xFFFF];
			case 0x18:
				switch(type & 0x7) {
					case 0x0:
						return ram[sp++ & 0xFFFF];
					case 0x1:
						return ram[sp & 0xFFFF];
					case 0x2:
						cycles++;
						return ram[ram[pc++] + sp & 0xFFFF];
					case 0x3:
						return sp;
					case 0x4:
						return pc;
					case 0x5:
						return ex;
					case 0x6:
						cycles++;
						return ram[ram[pc++]];
				}
				cycles++;
				return ram[pc++];
		}

		throw new IllegalStateException("Illegal A value type " + Integer.toHexString(type) + " !  That definitely should not happen.");
	}

	public void tick_skip() {
		char opcode = ram[pc];
		int cmd = opcode & 0x1F;
		pc = (char) (pc + getInstructionLength(opcode));
		if(cmd >= 16 && cmd <= 23)
			isSkiping = true;
		else
			isSkiping = false;
	}

	protected void corruptRam() {
		int pos = (int) (Math.random() * 0x10000) & 0xFFFF;
		char val = (char) ((int) (Math.random() * 0x10000) & 0xFFFF);
		int len = (int) (1 / (Math.random() + 0.001f)) - 0x50;
		for(int i = 0; i < len; ++i) {
			ram[(pos + i) & 0xFFFF] = val;
		}
	}

	//Hardware related functions
	public boolean addHardware(DCPUHardware hw) {
		synchronized (hardware) {
			if(hardware.size() < MAX_DEVICES)
				return hardware.add(hw);
			else
				return false;
		}
	}

	public boolean removeHardware(DCPUHardware hw) {
		synchronized (hardware) {
			return hardware.remove(hw);
		}
	}

	public List<DCPUHardware> getHardware() {
		synchronized (hardware) {
			return new ArrayList<DCPUHardware>(hardware);
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	public void reset() {
		pausing = true;
		while(!paused) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for(int i = 0; i < registers.length; ++i)
			registers[i] = 0;
		for(int i = 0; i < RAM_SIZE; ++i)
			ram[i] = ram_init[i];

		pc = 0;
		sp = 0;
		ex = 0;
		ia = 0;
		isOnFire = false;
		isQueueingEnabled = false;
		isSkiping = false;
		interrupts.clear();

		for(DCPUHardware h : hardware) {
			h.powerOff();
			h.powerOn();
		}
		pausing = false;
	}

	public void addInterruptListener(InterruptListener interruptListener) {
		interruptListeners.add(interruptListener);
	}

	public void runpause() {
		pausing = !pausing;
	}

	public void step() {
		if(pausing) {
			tickRequested = true;
		}
	}

	public boolean isPausing() {
		return pausing;
	}
}
