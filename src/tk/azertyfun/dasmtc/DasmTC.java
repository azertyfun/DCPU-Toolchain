package tk.azertyfun.dasmtc;

import tk.azertyfun.dasmtc.emulator.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;

public class DasmTC {

	public static void main(String[] args) {
		if(args.length == 0 || args[0].equals("-h")) {
			usage();
		} else if(args.length == 2 && args[0].equals("compile")) {
			compile(args[1]);
		} else if(args.length == 2 && args[0].equals("run")) {
			run(args[1], "");
		} else if(args.length == 3 && args[0].equals("run")) {
			run(args[1], args[2]);
		} else {
			usage();
		}
	}

	public static void compile(String source_path) {
		System.out.println("TODO");
	}

	public static void run(String binary_path, String disk_path) {
		try {
			HardwareTracker hardwareTracker = new HardwareTracker();
			final DCPU dcpu = hardwareTracker.requestDCPU();
			LEM1802 lem1802 = hardwareTracker.requestLem();
			GenericClock clock = hardwareTracker.requestClock();
			GenericKeyboard genericKeyboard = hardwareTracker.requestKeyboard();
			M35FD m35FD;
			if(!disk_path.isEmpty()) {
				if((new File(disk_path)).length() / 2 > M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS) {
					System.err.println("The provided disk file is longer than " + M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS + " words!\n");
					usage();
				}

				byte[] disk_b = Files.readAllBytes(Paths.get(disk_path));
				char[] disk = new char[M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS];
				for(int i = 0; i < disk_b.length / 2; ++i) {
					disk[i] = (char) (disk_b[i * 2] << 8);
					disk[i] |= (char) (disk_b[i * 2 + 1] & 0xFF);
				}
				m35FD = hardwareTracker.requestM35FD(disk);
			} else
				m35FD = hardwareTracker.requestM35FD();

			lem1802.connectTo(dcpu);
			lem1802.powerOn();
			clock.connectTo(dcpu);
			clock.powerOn();
			genericKeyboard.connectTo(dcpu);
			genericKeyboard.powerOn();
			m35FD.connectTo(dcpu);
			m35FD.powerOn();

			byte[] ram_b = Files.readAllBytes(Paths.get(binary_path));
			char[] ram = new char[0x10000];
			for(int i = 0; i < 0x10000; ++i) {
				ram[i] = (char) (ram_b[i * 2] << 8);
				ram[i] |= (char) (ram_b[i * 2 + 1] & 0xFF);
			}
			dcpu.setRam(ram);
			dcpu.start();
			TickingThread ticking = new TickingThread(new DCPUHardware[] {lem1802, genericKeyboard, m35FD});
			ticking.start();
			LemKeyboard lemKeyboard = new LemKeyboard(genericKeyboard);
			LemDisplay lemDisplay = new LemDisplay(lem1802);
			lemDisplay.start();

			System.out.println("Emulator started. Stop it with command \"stop\" in this console (this is the only way modifications on a disk will be saved).");

			new Thread() {
				public void run() {
					Scanner sc = new Scanner(System.in);
					String s = "";
					do {
						s = sc.nextLine();
					} while(!s.equalsIgnoreCase("stop"));

					char[] disk = m35FD.getDisk();
					byte[] disk_b = new byte[disk.length * 2];
					for(int i = 0; i < disk.length; ++i) {
						disk_b[i * 2] = (byte) ((disk[i] >> 8) & 0xFF);
						disk_b[i * 2 + 1] = (byte) (disk[i] & 0xFF);
					}

					try {
						FileOutputStream fos = new FileOutputStream(disk_path);
						fos.write(disk_b);
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					dcpu.setStopped();
					lemKeyboard.close();
					lemDisplay.close();
					ticking.setStopped();
				}
			}.start();
		} catch (NoSuchFileException e) {
			System.err.println("Error: File not found.\n");
			usage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		System.out.println("Usage: \n" +
				"java -jar DasmTC.jar <Action> [Options]\n" +
				"\tACTIONS:\n" +
				"\t\t- compile <file>: Compiles <file> to <file>.o\n" +
				"\t\t- run <file> [floppy disk]: Runs emulator for <file> (binary format) with [floppy disk] loaded into M35FD if specified.");

		System.exit(-1);
	}
}
