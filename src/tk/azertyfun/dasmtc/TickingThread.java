package tk.azertyfun.dasmtc;

import tk.azertyfun.dasmtc.emulator.DCPUHardware;

public class TickingThread extends Thread {
	private boolean stop = false;

	private DCPUHardware[] hardware;

	public TickingThread(DCPUHardware[] hardware) {
		this.hardware = hardware;
	}

	@Override
	public void run() {
		while(!stop) {
			long startTime = System.currentTimeMillis();
			for(DCPUHardware h : hardware)
				h.tick60hz();
			long endTime = System.currentTimeMillis();
			try {
				if((int) (1000f / 60f - (endTime - startTime)) > 0)
					Thread.sleep((int) (1000f / 60f - (endTime - startTime)));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setStopped() {
		stop = true;
	}
}
