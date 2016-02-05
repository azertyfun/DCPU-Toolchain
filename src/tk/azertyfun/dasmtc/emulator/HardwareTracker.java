package tk.azertyfun.dasmtc.emulator;

import java.util.LinkedList;

public class HardwareTracker {
	private LinkedList<DCPU> dcpus = new LinkedList<>();
	private LinkedList<GenericKeyboard> keyboards = new LinkedList<>();
	private LinkedList<LEM1802> lems = new LinkedList<>();
	private LinkedList<GenericClock> clocks = new LinkedList<>();
	private LinkedList<M35FD> m35fds = new LinkedList<>();

	public HardwareTracker() {

	}

	public DCPU requestDCPU() {
		dcpus.add(new DCPU("dcpu_" + (dcpus.size())));
		return dcpus.getLast();
	}

	public LEM1802 requestLem() {
		lems.add(new LEM1802("lem1802_" + (lems.size())));
		return lems.getLast();
	}

	public GenericClock requestClock() {
		clocks.add(new GenericClock("clock_" + (clocks.size())));
		return clocks.getLast();
	}

	public GenericKeyboard requestKeyboard() {
		keyboards.add(new GenericKeyboard("keyboard_" + keyboards.size()));
		return keyboards.getLast();
	}

	public M35FD requestM35FD() {
		m35fds.add(new M35FD("m35fd_" + m35fds.size()));
		return m35fds.getLast();
	}

	public M35FD requestM35FD(char[] ram) {
		m35fds.add(new M35FD("m35fd_" + m35fds.size(), ram));
		return m35fds.getLast();
	}

	public DCPU getDCPU(String id) {
		for(DCPU d : dcpus) {
			if(d.getID().equals(id))
				return d;
		}
		return null;
	}

	public GenericKeyboard getKeyboard(String id) {
		for(GenericKeyboard k : keyboards) {
			if(k.getID().equals(id))
				return k;
		}
		return null;
	}

	public LEM1802 getLem(String id) {
		for(LEM1802 l : lems) {
			if(l.getID().equals(id))
				return l;
		}
		return null;
	}

	public GenericClock getClock(String id) {
		for(GenericClock c : clocks) {
			if(c.getID().equals(id))
				return c;
		}
		return null;
	}

	public M35FD getM35FD(String id) {
		for(M35FD fd : m35fds) {
			if(fd.getID().equals(id))
				return fd;
		}
		return null;
	}
}
