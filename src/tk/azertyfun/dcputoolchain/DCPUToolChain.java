package tk.azertyfun.dcputoolchain;

import java.io.File;

public class DCPUToolChain {

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());

		if(args.length == 0 || args[0].equals("-h")) {
			usage();
		} else if(args.length >= 3 && args[0].equals("assemble")) {
			assemble(args);
		} else if(args.length >= 2 && args[0].equals("run")) {
			run(args);
		} else {
			usage();
		}
	}

	public static void assemble(String[] args) {
		AssemblerManager assembler = new AssemblerManager(args);
		assembler.assemble();
	}

	public static void run(String[] args) {
		Emulator emulator = new Emulator(args);

	}

	public static void usage() {
		System.out.println("DCPU-Toolchain.\n" +
				"\n" +
				"Usage:\n" +
				"  java -jar DCPU-Toolchain.jar assemble <input file> <output file> [--little-endian] [--disable-shortLiterals]\n" +
				"  java -jar DCPU-Toolchain.jar run <file> [--assemble] [--debugger] [--little-endian] [--clock] [--keyboard] [--lem1802] [--edc] [--M35FD=/path/to/file] [--M525HD=/path/to/file] [--console]\n" +
				"\n" +
				"Options:\n" +
				"  --little-endian          Treat files as little endian instead of big endian by default.\n" +
				"  --disable-shortLiterals  Disables optimization of short literals (-1 -> 30) to be included in the opcode instead of the next word.\n" +
				"  --assemble               The specified input file is assembly instead of binary and must be assembled at runtime.\n" +
				"  --debugger               Enable the debugger interface.\n" +
				"  --clock                  Adds a clock device.\n" +
				"  --keyboard               Adds a keyboard device.\n" +
				"  --lem1802                Adds a LEM1802 device.\n" +
				"  --edc                    Adds an EDC device.\n" +
				"  --M35FD=path/to/file     Adds an M35FD device with a floppy stored in path/to/file.\n" +
				"  --M525HD=path/to/file    Adds an M525HD device with a hard disk stored in path/to/file.\n" +
				"  --console                Disables debugger and EDC, and creates a server on port 25570 that can be used by remoteConsole.py to control the LEM1802 and keyboard via a remote console. Useful if you want to run the emulator headless.");

		System.exit(-1);
	}
}
