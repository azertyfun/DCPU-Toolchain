package tk.azertyfun.dcputoolchain.interfaces;

import java.util.LinkedList;

public class StatusPacket {

	public int ram[] = new int[12*32];

	public int registers[] = new int[8];

	public int pc;
	public int sp;
	public int ex;
	public int ia;

	public LinkedList<Integer> logs = new LinkedList<>();
}
