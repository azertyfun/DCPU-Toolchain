package tk.azertyfun.dasmtc;

public class DasmTC {

	public static boolean isGlfwInitialized = false;

	public static void main(String[] args) {
		if(args.length == 0 || args[0].equals("-h")) {
			usage();
		} else if(args.length == 2 && args[0].equals("compile")) {
			compile(args[1]);
		} else if(args.length >= 2 && args[0].equals("run")) {
			run(args);
		} else {
			usage();
		}
	}

	public static void compile(String source_path) {
		System.out.println("TODO");
	}

	public static void run(String[] args) {
		Emulator emulator = new Emulator(args);

	}

	protected static void usage() {
		System.out.println("Usage: \n" +
				"java -jar DasmTC.jar <Action> [Options]\n" +
				"\tACTIONS:\n" +
				"\t\t- compile <file>: Compiles <file> to <file>.o\n" +
				"\t\t- run <file> [--clock] [--keyboard] [--lem1802] [--M35FD=/path/to/file] [--M525HD=/path/to/file]: Runs emulator for <file> (binary format) with specified hardware. If no hardware is specified, runs with clock, keyboard and LEM1802.");

		System.exit(-1);
	}
}
