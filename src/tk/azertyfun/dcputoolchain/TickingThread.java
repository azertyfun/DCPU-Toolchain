package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.DCPUHardware;

import java.util.LinkedList;

public class TickingThread extends Thread {
	private boolean stop = false;

	private LinkedList<DCPUHardware> hardware;

	public TickingThread(LinkedList<DCPUHardware> hardware) {
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
