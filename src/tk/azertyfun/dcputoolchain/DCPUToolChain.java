package tk.azertyfun.dcputoolchain;

public class DCPUToolChain {

	public static void main(String[] args) {
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
		System.out.println("Usage: \n" +
				"java -jar DasmTC.jar <Action> [Options]\n" +
				"\tACTIONS:\n" +
				"\t\t- assemble <input file> <output file> [--little-endian]: Assembles <input file> to <output file>. Big-endian by default, unless the --little-endian switch is present.\n" +
				"\t\t- run <file> [--assemble] [--little-endian] [--clock] [--keyboard] [--lem1802] [--edc] [--M35FD=/path/to/file] [--M525HD=/path/to/file]: Runs emulator for <file> (binary format) with specified hardware. If the --assemble flag is set, first assembles the file in the system temp directory. Big-endian by default, unless the --little-endian switch is present. If no hardware is specified, runs with clock, keyboard and LEM1802.");

		System.exit(-1);
	}
}
