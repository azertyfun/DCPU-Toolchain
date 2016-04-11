package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.LEM1802;

public class LemDisplayConsole extends LemDisplay {

	private boolean stopped = false;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BG_BLACK = "\u001B[40m";
	public static final String ANSI_BG_RED = "\u001B[41m";
	public static final String ANSI_BG_GREEN = "\u001B[42m";
	public static final String ANSI_BG_YELLOW = "\u001B[43m";
	public static final String ANSI_BG_BLUE = "\u001B[44m";
	public static final String ANSI_BG_PURPLE = "\u001B[45m";
	public static final String ANSI_BG_CYAN = "\u001B[46m";
	public static final String ANSI_BG_WHITE = "\u001B[47m";

	public static final String ANSI_HOME = "\u001B[H";
	public static final String ANSI_CLEAR= "\u001B[2J";

	private static int offset;

	public LemDisplayConsole(LEM1802 lem1802, int offset) {
		super(lem1802);
		this.offset = offset;
	}

	@Override
	public void run() {

		while(!stopped) {
			long start = System.nanoTime();

			char[] ram = lem1802.getVideoRam();

			System.out.print(ANSI_HOME); //CURSOR_HOME ANSI escape code.
			System.out.print("\u001B[" + offset + "B"); //Cursor down offset lines.

			for (int i = 0; i < 32 * 12; ++i) {
				if(i % 32 == 0 && i != 0)
					System.out.println();

				char c = (char) (ram[i] & 0b1111111);
				char bgColor = (char) ((ram[i] >> 8) & 0b1111);
				char fgColor = (char) ((ram[i] >> 12) & 0b1111);
				String ansi_fgColor = getColor(fgColor, true);
				String ansi_bgColor = getColor(bgColor, false);
				char ansi_c = getChar(c);

				System.out.print(ansi_fgColor + ansi_bgColor + ansi_c);
			}
			System.out.println(ANSI_RESET);

			long end = System.nanoTime();
			long dt = end - start;
			long sleep_ns = (long) ((float) (100000000 - dt) / 30f);

			System.out.println("dt=" + dt + ", " + "sleep_ns=" + sleep_ns);

			if(sleep_ns > 0) {
				try {
					Thread.sleep((long) ((float) sleep_ns / 1000000f));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void close() {
		this.stopped = true;
	}
	
	private String getColor(char c, boolean fg) {
		switch (c) {
			case 0:
				return fg ? ANSI_BLACK : ANSI_BG_BLACK;
			case 1:
				return fg ? ANSI_BLUE : ANSI_BG_BLUE;
			case 2:
				return fg ? ANSI_GREEN : ANSI_BG_GREEN;
			case 3:
				return fg ? ANSI_CYAN : ANSI_BG_CYAN;
			case 4:
				return fg ? ANSI_RED : ANSI_BG_RED;
			case 5:
				return fg ? ANSI_PURPLE : ANSI_BG_PURPLE;
			case 6:
				return fg ? ANSI_RED : ANSI_BG_RED; //Brown doesn't exist
			case 7:
				return fg ? ANSI_CYAN : ANSI_BG_CYAN; //Light cyan doesn't exist
			case 8:
				return fg ? ANSI_WHITE : ANSI_BG_WHITE; //Grey doesn't exist
			case 9:
				return fg ? ANSI_RED : ANSI_BG_RED; //Strawberry (or whatever this is) doesn't exist
			case 0xa:
				return fg ? ANSI_GREEN : ANSI_BG_GREEN; //Light green doesn't exist
			case 0xb:
				return fg ? ANSI_CYAN : ANSI_BG_CYAN; //Flashy light blue doesn't exist
			case 0xc:
				return fg ? ANSI_RED : ANSI_BG_RED; //Salmon doesn't exist
			case 0xd:
				return fg ? ANSI_RED : ANSI_BG_RED; //Pink doesn't exist
			case 0xe:
				return fg ? ANSI_YELLOW : ANSI_BG_YELLOW;
			case 0xf:
				return fg ? ANSI_WHITE : ANSI_BG_WHITE;
		}
		throw new IllegalStateException("This shouldn't happen!");
	}

	private char getChar(char c) {
		if(c >= 0x20 && c < 0x7f)
			return c;
		else
			return ' ';
	}
}
