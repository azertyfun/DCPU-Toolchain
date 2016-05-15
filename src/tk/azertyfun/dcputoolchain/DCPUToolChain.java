package tk.azertyfun.dcputoolchain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

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
		for(String s : args) {
			String[] splitted = s.split("=");
			if(splitted.length == 2) {
				if(splitted[0].equalsIgnoreCase("--bootloader")) {
					try {
						char[] bootloader_data = new char[512];
						String bootloader_path = splitted[1];

						if ((new File(bootloader_path)).length() / 2 > 512) {
							System.err.println("The provided bootloader is longer than 512 words!\n");
							DCPUToolChain.usage();
						}

						byte[] bootloader_b = Files.readAllBytes(Paths.get(bootloader_path));
						for (int j = 0; j < bootloader_b.length / 2; ++j) {
							bootloader_data[j] = (char) (bootloader_b[j * 2] << 8);
							bootloader_data[j] |= (char) (bootloader_b[j * 2 + 1] & 0xFF);
						}

						AssemblerManager assembler = new AssemblerManager(args, bootloader_data);
						assembler.assemble();
						System.exit(0);
					} catch (NoSuchFileException e) {
						System.err.println("Error: File not found: " + e.getFile() + ".\n");
						DCPUToolChain.usage();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
				"  java -jar DCPU-Toolchain.jar assemble <input file> <output file> [--bootloader=/path/to/file] [--little-endian] [--disable-shortLiterals]\n" +
				"  java -jar DCPU-Toolchain.jar run <file> [--assemble] [--bootloader=/path/to/file] [--debugger] [--little-endian] [--clock] [--keyboard] [--lem1802] [--edc] [--M35FD=/path/to/file] [--M525HD=/path/to/file] [--console]\n" +
				"\n" +
				"Options:\n" +
				"  --little-endian            Treat files as little endian instead of big endian by default.\n" +
				"  --disable-shortLiterals    Disables optimization of short literals (-1 -> 30) to be included in the opcode instead of the next word.\n" +
				"  --assemble                 The specified input file is assembly instead of binary and must be assembled at runtime.\n" +
				"  --bootloader=path/to/file  If assembling, adds a bootloader at the beggining of the output floppy. If not, adds a bootloader internally to run a binary that doesn't have one.\n" +
				"  --debugger                 Enable the debugger interface.\n" +
				"  --clock                    Adds a clock device.\n" +
				"  --keyboard                 Adds a keyboard device.\n" +
				"  --lem1802                  Adds a LEM1802 device.\n" +
				"  --edc                      Adds an EDC device.\n" +
				"  --M35FD=path/to/file       Adds an M35FD device with a floppy stored in path/to/file.\n" +
				"  --M525HD=path/to/file      Adds an M525HD device with a hard disk stored in path/to/file.\n" +
				"  --console                  Disables debugger and EDC, and creates a server on port 25570 that can be used by remoteConsole.py to control the LEM1802 and keyboard via a remote console. Useful if you want to run the emulator headless.");

		System.exit(-1);
	}
}
