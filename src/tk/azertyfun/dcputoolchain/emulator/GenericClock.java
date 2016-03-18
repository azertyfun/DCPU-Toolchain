package tk.azertyfun.dcputoolchain.emulator;

import java.util.LinkedList;

public class GenericClock extends DCPUHardware {
	public static final int TYPE = 0x12d0b402, REVISION = 1, MANUFACTURER = 0;

	protected int interval, intCount;
	protected char ticks, interruptMessage;

	private LinkedList<ClockCallback> clockCallbacks = new LinkedList<>();

	public GenericClock(String id) {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;
	}

	@Override
	public void interrupt() {
		int a = dcpu.registers[0];
		if(a == 0)
			interval = dcpu.registers[1];
		if(a == 1)
			dcpu.registers[2] = ticks;
		if(a == 2)
			interruptMessage = dcpu.registers[1];
	}

	@Override
	public void tick60hz() {
		if(interval == 0)
			return;
		if(++intCount >= interval) {
			if(interruptMessage != 0) {
				dcpu.interrupt(interruptMessage);
			}
			intCount = 0;
			addTick();
		}
	}

	@Override
	public void powerOff() {
		reset();
	}

	@Override
	public void powerOn() {
		reset();
	}

	public void reset() {
		intCount = 0;
		interruptMessage = 0;
		interval = 0;
		ticks = 0;
	}

	public void addCallback(ClockCallback clockCallback) {
		clockCallbacks.add(clockCallback);
	}

	public void addTick() {
		ticks++;
		for(ClockCallback clockCallback : clockCallbacks)
			clockCallback.ticksChanged(ticks);
	}

	public interface ClockCallback {
		public void ticksChanged(int ticks);
	}
}
