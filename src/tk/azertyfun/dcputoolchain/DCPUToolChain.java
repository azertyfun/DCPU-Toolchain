package tk.azertyfun.dcputoolchain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

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
		boolean bootloader_little_endian = true;

		for(String s : args) {
			if (s.equalsIgnoreCase("--bootloader-big-endian")) {
				bootloader_little_endian = false;
				break;
			}
		}

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

						AssemblerManager assembler = new AssemblerManager(args, bootloader_data, bootloader_little_endian);
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
				"  java -jar DCPU-Toolchain.jar assemble <input file> <output file> [--bootloader=/path/to/file] [--big-endian] [--disable-shortLiterals]\n" +
				"  java -jar DCPU-Toolchain.jar run <file> [--assemble] [--big-endian] [--rom-big-endian] [--bootloader=/path/to/file] [--debugger] [--clock] [--keyboard] [--lem1802] [--edc] [--M35FD=/path/to/file] [--M525HD=/path/to/file] [--console]\n" +
				"\n" +
				"Options:\n" +
				"  --assemble                 The specified input file is assembly instead of binary and must be assembled at runtime.\n" +
				"  --big-endian               Treat binary files as big endian instead of little endian by default (does not affect res/rom.bin).\n" +
				"  --rom-big-endian           Treat res/rom.bin as big endian instead of little endian by default.\n" +
				"  --bootloader-big-endian    Treat the bootloader (set with optional --bootloader flag) as big endian instead of little endian by default.\n" +
				"  --disks-big-endian         Treat the M35FD/M35HD as big endian instead of little endian by default.\n" +
				"  --bootloader=path/to/file  If assembling, adds a bootloader at the beggining of the output floppy. If not, adds a bootloader internally to run a binary that doesn't have one.\n" +
				"  --disable-shortLiterals    Disables optimization of short literals (-1 -> 30) to be included in the opcode instead of the next word.\n" +
				"  --debugger                 Enable the debugger interface.\n" +
				"  --clock                    Adds a clock device.\n" +
				"  --keyboard                 Adds a keyboard device.\n" +
				"  --lem1802                  Adds a LEM1802 device.\n" +
				"  --lem1802-fps=fps          Sets LEM1802 refresh rate to <fps> (if not in console mode). Default is 30.\n" +
				"  --edc                      Adds an EDC device.\n" +
				"  --M35FD=path/to/file       Adds an M35FD device with a floppy stored in path/to/file.\n" +
				"  --M525HD=path/to/file      Adds an M525HD device with a hard disk stored in path/to/file.\n" +
				"  --speaker                  Adds an speaker device.\n" +
				"  --console                  Disables debugger and EDC, and creates a server on port 25570 that can be used by remoteConsole.py to control the LEM1802 and keyboard via a remote console. Useful if you want to run the emulator headless.");

		System.exit(-1);
	}
}
