package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

public class Emulator implements CallbackStop {
	private DCPU dcpu;

	private LinkedList<DCPUHardware> hardware = new LinkedList<>();
	private LinkedList<LemDisplay> lemDisplays = new LinkedList<>();
	private LinkedList<KeyboardDisplay> keyboardDisplays = new LinkedList<>();
	private LinkedList<EdcDisplay> edcDisplays = new LinkedList<>();

	private TickingThread ticking;

	private DebuggerInterface debuggerInterface;

	public Emulator(String[] args) {
		String input_file = args[1];

		boolean big_endian = true;

		HardwareTracker hardwareTracker = new HardwareTracker();
		dcpu = hardwareTracker.requestDCPU();

		ticking = new TickingThread(hardware);

		CPUControl cpuControl = hardwareTracker.requestCPUControl(this);
		hardware.add(cpuControl);
		cpuControl.connectTo(dcpu);
		cpuControl.powerOn();

		boolean assemble = false;
		boolean debugger = false;
		boolean optimize_shortLiterals = true;

		if(args.length > 2) {
			for(int i = 2; i < args.length; ++i) {
				if (args[i].equalsIgnoreCase("--assemble")) {
					assemble = true;
				} else if (args[i].equalsIgnoreCase("--debugger")) {
					debugger = true;
				} else if (args[i].equalsIgnoreCase("--little-endian")) {
					big_endian = false;
				} else if(args[i].equalsIgnoreCase("--disable-shortLiterals")) {
					optimize_shortLiterals = false;
				} else if(args[i].equalsIgnoreCase("--LEM1802")) {
					hardware.add(hardwareTracker.requestLem());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();

					LemDisplay lemDisplay = new LemDisplay((LEM1802) hardware.getLast());
					lemDisplay.start();
					lemDisplays.add(lemDisplay);
				} else if(args[i].equalsIgnoreCase("--CLOCK")) {
					hardware.add(hardwareTracker.requestClock());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();
				} else if(args[i].equalsIgnoreCase("--KEYBOARD")) {
					hardware.add(hardwareTracker.requestKeyboard());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();

					KeyboardDisplay keyboardDisplay = new KeyboardDisplay((GenericKeyboard) hardware.getLast(), cpuControl);
					keyboardDisplays.add(keyboardDisplay);
				} else if(args[i].equalsIgnoreCase("--EDC")) {
					hardware.add(hardwareTracker.requestEDC());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();

					EdcDisplay edcDisplay = new EdcDisplay((EDC) hardware.getLast());
					edcDisplays.add(edcDisplay);
				} else {
					String[] splitted = args[i].split("=");
					if (splitted.length != 2) {
						DCPUToolChain.usage();
					}

					if(splitted[0].equalsIgnoreCase("--M35FD")) {
						try {
							String disk_path = splitted[1];

							if (!disk_path.isEmpty()) {
								if ((new File(disk_path)).length() / 2 > M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS) {
									System.err.println("The provided disk file is longer than " + M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS + " words!\n");
									DCPUToolChain.usage();
								}

								byte[] disk_b = Files.readAllBytes(Paths.get(disk_path));
								char[] disk = new char[M35FD.WORDS_PER_SECTOR * M35FD.SECTORS_PER_TRACK * M35FD.TRACKS];
								for (int j = 0; j < disk_b.length / 2; ++j) {
									disk[j] = (char) (disk_b[j * 2] << 8);
									disk[j] |= (char) (disk_b[j * 2 + 1] & 0xFF);
								}

								M35FD m35FD = hardwareTracker.requestM35FD(disk_path);
								m35FD.connectTo(dcpu);
								m35FD.powerOn();
								hardware.add(m35FD);
							} else {
								M35FD m35FD = hardwareTracker.requestM35FD();
								m35FD.connectTo(dcpu);
								m35FD.powerOn();
								hardware.add(m35FD);
							}
						} catch (NoSuchFileException e) {
							System.err.println("Error: File not found: " + e.getFile() + ".\n");
							DCPUToolChain.usage();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if(splitted[0].equalsIgnoreCase("--M525HD")) {
						try {
							String disk_path = splitted[1];

							if (!disk_path.isEmpty()) {
								if ((new File(disk_path)).length() / 2 > M525HD.WORDS_PER_SECTOR * M525HD.SECTORS_PER_TRACK * M525HD.TRACKS) {
									System.err.println("The provided disk file is longer than " + M525HD.WORDS_PER_SECTOR * M525HD.SECTORS_PER_TRACK * M525HD.TRACKS + " words!\n");
									DCPUToolChain.usage();
								}

								M525HD m525HD = hardwareTracker.requestM525HD(disk_path);
								m525HD.connectTo(dcpu);
								m525HD.powerOn();
								hardware.add(m525HD);
							} else {
								System.err.println("Please specify a path for the M525HD!\n");
								DCPUToolChain.usage();
							}
						} catch (NoSuchFileException e) {
							System.err.println("Error: File not found: " + e.getFile() + ".\n");
							DCPUToolChain.usage();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						DCPUToolChain.usage();
					}
				}
			}
		} else {
			LEM1802 lem1802 = hardwareTracker.requestLem();
			GenericClock clock = hardwareTracker.requestClock();
			GenericKeyboard genericKeyboard = hardwareTracker.requestKeyboard();
			lem1802.connectTo(dcpu);
			lem1802.powerOn();
			clock.connectTo(dcpu);
			clock.powerOn();
			genericKeyboard.connectTo(dcpu);
			genericKeyboard.powerOn();

			hardware.add(lem1802);
			hardware.add(clock);
			hardware.add(genericKeyboard);
		}

		debuggerInterface = new DebuggerInterface(dcpu, ticking, this);

		try {
			if(assemble) {
				File tmpFile = File.createTempFile("DCPUToolchain", Long.toString(System.currentTimeMillis()));
				AssemblerManager assemblerManager;
				if(optimize_shortLiterals)
					assemblerManager = new AssemblerManager(new String[] {"assemble", input_file, tmpFile.getAbsolutePath()});
				else
					assemblerManager = new AssemblerManager(new String[] {"assemble", input_file, tmpFile.getAbsolutePath(), "--disable-shortLiterals"});
				boolean success = assemblerManager.assemble();
				if(!success)
					System.exit(-1);
				dcpu.setRam(tmpFile.getAbsolutePath(), true);
			} else {
				dcpu.setRam(input_file, big_endian);
			}
			dcpu.start();
			ticking.start();

			System.out.println("Emulator started. Stop it with command \"stop\" in this console (this is the only way modifications on a disk will be saved, unless an EDC shutdown is sent, or the debugger is closed).");

			new Thread() {
				public void run() {
					Scanner sc = new Scanner(System.in);
					String s = "";
					do {
						s = sc.nextLine();
					} while(!s.equalsIgnoreCase("stop"));

					this.stop();
				}
			}.start();
		} catch (NoSuchFileException e) {
			System.err.println("Error: File not found: " + e.getFile() + ".\n");

			DCPUToolChain.usage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopCallback() {
		System.out.println("Stopping emulator...");

		for(DCPUHardware dcpuHardware : hardware) {
			dcpuHardware.powerOff();
			dcpuHardware.onDestroy();
		}

		dcpu.setStopped();
		for(LemDisplay lemDisplay : lemDisplays)
			lemDisplay.close();
		for(KeyboardDisplay keyboardDisplay : keyboardDisplays)
			keyboardDisplay.close();
		for(EdcDisplay edcDisplay : edcDisplays)
			edcDisplay.close();
		ticking.setStopped();

		if(debuggerInterface != null)
			debuggerInterface.close();

		try {
			Thread.sleep(1000);
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
