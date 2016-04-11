package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.LEM1802;

public abstract class LemDisplay extends Thread {
	public final int WIDTH = 128;
	public final int HEIGHT = 96;

	protected LEM1802 lem1802;

	public LemDisplay(LEM1802 lem1802) {
		this.lem1802 = lem1802;
	}

	@Override
	public abstract void run();

	public abstract void close();
}
