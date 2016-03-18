package tk.azertyfun.dcputoolchain.emulator;

import java.util.LinkedList;
import java.util.Random;

public class CPUControl extends DCPUHardware implements InterruptListener {
	public static final int TYPE = 0x11E0DACC, REVISION = 0x0004, MANUFACTURER = 0x00000000; //MANUFACTURER not specified (various)
	public static final int CLOCK_RATE = 100000, REDUCED_CLOCK_RATE = CLOCK_RATE / 10;

	private int mode = Modes.FULL_RATE;
	private char runTime = 10; //In .1 second increments
	private char sleepTime = 10; //In .1 second increments
	private boolean sleeping = false;

	private char power_message = 0, mode_message = 0;

	private CallbackStop callback;
	private LinkedList<CPUControlCallback> cpuControlCallbacks = new LinkedList<>();

	protected CPUControl(String id, CallbackStop callback) {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;

		this.callback = callback;
	}

	@Override
	public void interrupt() {
		int a = dcpu.registers[0];
		int b = dcpu.registers[1];
		int c = dcpu.registers[2];
		switch(a) {
			case 0x0000: //SET_MODE
				if(b < 0 || b > 4) {
					Random r = new Random();
					b = r.nextInt(5);
				}

				this.mode = b;
				switch(b) {
					case Modes.FULL_RATE:
						dcpu.speed_hz = CLOCK_RATE;
						dcpu.batchSize = CLOCK_RATE / 30;
						break;
					case Modes.REDUCED_RATE:
						dcpu.speed_hz = REDUCED_CLOCK_RATE;
						dcpu.batchSize = REDUCED_CLOCK_RATE / 30;
						break;
					case Modes.REDUCED_RATE_SLEEP:
						dcpu.speed_hz = REDUCED_CLOCK_RATE;
						dcpu.batchSize = REDUCED_CLOCK_RATE / 30;
						break;
					case Modes.SLEEP:
						sleeping = true;
						dcpu.sleeping = true;
						break;
					case Modes.POWER_OFF:
						System.out.println("DCPU shut down by guest.");
						callback.stopCallback();
						break;
				}

				break;
			case 0x0001: //SET_RUNTIME
				runTime = (char) b;
				break;
			case 0x0002: //SET_SLEEPTIME
				sleepTime = (char) b;
				break;
			case 0x0003: //SET_INTERRUPT_MESSAGE
				if(c == 0)
					power_message = (char) b;
				else if(c == 1)
					mode_message = (char) b;
				break;
			case 0x0004: //GET_CLOCK_RATE
				dcpu.registers[1] = (char) (CLOCK_RATE & 0xFFFF);
				dcpu.registers[2] = (char) ((CLOCK_RATE >> 16) & 0xFFFF);
				break;
			case 0x0005: //GET_R_CLOCK_RATE
				dcpu.registers[1] = (char) (REDUCED_CLOCK_RATE & 0xFFFF);
				dcpu.registers[2] = (char) ((REDUCED_CLOCK_RATE >> 16) & 0xFFFF);
				break;
			case 0x0505: //RESET_EVERYTHING
				dcpu.reset();
				break;
		}
	}

	@Override
	public void tick60hz() {
		super.tick60hz();
	}

	@Override
	public void powerOn() {
		super.powerOn();
	}

	@Override
	public void interrupted() {
		if(sleeping) {
			sleeping = false;
			dcpu.sleeping = false;
			mode = Modes.REDUCED_RATE;
			dcpu.speed_hz = REDUCED_CLOCK_RATE;
			dcpu.batchSize = REDUCED_CLOCK_RATE / 30;
		}
	}

	@Override
	public DCPUHardware connectTo(DCPU dcpu) {
		DCPUHardware h = super.connectTo(dcpu);
		this.dcpu.addInterruptListener(this);
		this.dcpu.speed_hz = 10000;
		dcpu.batchSize = 500;
		return h;
	}

	public void powerButton() {
		if(power_message != 0)
			dcpu.interrupt(power_message);
		for(CPUControlCallback cpuControlCallback : cpuControlCallbacks)
			cpuControlCallback.modeChanged(mode);
	}

	public void modeButton() {
		if(mode_message != 0)
			dcpu.interrupt(mode_message);
		for(CPUControlCallback cpuControlCallback : cpuControlCallbacks)
			cpuControlCallback.modeChanged(mode);
	}

	public static class Modes {
		public static final int FULL_RATE = 0;
		public static final int REDUCED_RATE = 1;
		public static final int REDUCED_RATE_SLEEP = 2;
		public static final int SLEEP = 3;
		public static final int POWER_OFF = 4;
	}

	public void addCallback(CPUControlCallback cpuControlCallback) {
		cpuControlCallbacks.add(cpuControlCallback);
	}

	public interface CPUControlCallback {
		public void modeChanged(int mode);
	}
}
