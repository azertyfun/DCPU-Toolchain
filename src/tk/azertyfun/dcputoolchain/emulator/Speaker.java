package tk.azertyfun.dcputoolchain.emulator;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.*;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class Speaker extends DCPUHardware {
	public static final int TYPE = 0xC0F00001, REVISION = 0x0001, MANUFACTURER = 0x5672746B;

	public static final float SAMPLE_RATE = 11025;
	public static final int WAVELENGTHS = 40;

	private Synthesizer synth;

	private SquareOscillator squareOscillator0 = new SquareOscillator();
	private SquareOscillator squareOscillator1 = new SquareOscillator();
	private LineOut lineOut = new LineOut();


	protected Speaker(String id) {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;

		synth = JSyn.createSynthesizer();
		synth.add(lineOut);
		synth.add(squareOscillator0);
		synth.add(squareOscillator1);

		synth.start();
		lineOut.start();
		squareOscillator0.start();
		squareOscillator1.start();

		squareOscillator0.output.connect(0, lineOut.input, 0);
		squareOscillator0.output.connect(0, lineOut.input, 1);
		squareOscillator1.output.connect(0, lineOut.input, 0);
		squareOscillator1.output.connect(0, lineOut.input, 1);
	}

	@Override
	public void tick60hz() {

	}

	@Override
	public void interrupt() {
		int a = dcpu.registers[0];

		switch(a) {
			case 0: //SET_FREQUENCY_CHANNEL_1
				if(dcpu.registers[1] != 0) {
					squareOscillator0.amplitude.set(0.05);
					squareOscillator0.frequency.set(dcpu.registers[1]);
				} else {
					squareOscillator0.amplitude.set(0.0);
				}
				break;
			case 1: //SET_FREQUENCY_CHANNEL_2
				if(dcpu.registers[1] != 0) {
					squareOscillator1.amplitude.set(0.03);
					squareOscillator1.frequency.set(dcpu.registers[1]);
				} else {
					squareOscillator1.amplitude.set(0.0);
				}
				break;
		}
	}

	@Override
	public void powerOff() {
		squareOscillator0.amplitude.set(0);
		squareOscillator1.amplitude.set(0);
	}

	@Override
	public void powerOn() {
		squareOscillator0.amplitude.set(0);
		squareOscillator1.amplitude.set(0);
	}

	private byte getByteValue(double angle) {
		return (new Integer((int) Math.round(Math.sin(angle) * 63))).byteValue();
	}

	private class ThreadSound extends Thread {
		private Clip clip0, clip1;
		private boolean canPlayAudio = true;

		public boolean updateRequired_0;
		public boolean updateRequired_1;

		private char frequency_0 = 0, frequency_1 = 0;

		private boolean stopped;

		public ThreadSound() {
			try {
				clip0 = AudioSystem.getClip();
				clip1 = AudioSystem.getClip();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				canPlayAudio = false;
			}
		}

		@Override
		public void run() {
			while(!stopped) {
				synchronized (this) {
					if (updateRequired_0) {
						if (frequency_0 == 0) {
							clip0.stop();
						} else {
							playFrequency(clip0, frequency_0);
						}

						updateRequired_0 = false;
					}
				}

				synchronized (this) {
					if (updateRequired_1) {
						if (frequency_1 == 0) {
							clip1.stop();
						} else {
							playFrequency(clip1, frequency_1);
						}

						updateRequired_1 = false;
					}
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void setFrequency(int channel, char frequency) {
			if(canPlayAudio) {
				switch (channel) {
					case 0:
						frequency_0 = frequency;
						updateRequired_0 = true;
						break;
					case 1:
						frequency_1 = frequency;
						updateRequired_1 = true;
						break;
				}

			}
		}

		private void playFrequency(Clip clip, char frequency) {
			byte[] buf = new byte[2 * frequency * WAVELENGTHS];

			AudioFormat audioFormat = new AudioFormat(
					SAMPLE_RATE,
					8, // Sample size in bits
					2, // Channels
					true, // Signed
					false // Bid endian
			);

			for(int i = 0; i < frequency * WAVELENGTHS; i++) {
				double angle = ((float) (i * 2) / (SAMPLE_RATE / frequency)) * Math.PI;

				buf[i * 2] = getByteValue(2 * angle);
				buf[i * 2 + 1] = buf[i * 2];
			}

			try {
				byte[] b = buf;
				AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(b), audioFormat, buf.length / 2);

				clip.close();
				clip.open(ais);
				clip.setFramePosition(0);
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void setStopped() {
			stopped = true;
		}
	}
}
