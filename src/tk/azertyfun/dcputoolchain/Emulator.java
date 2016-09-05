package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.*;
import tk.azertyfun.dcputoolchain.interfaces.*;

import java.io.File;
import java.io.FileOutputStream;
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

	private boolean console = false;
	private ConsoleServer consoleServer;

	public Emulator(String[] args) {
		String input_file = args[1];

		boolean little_endian = true;
		boolean rom_little_endian = true;
		boolean bootloader_little_endian = true;
		boolean disks_little_endian = true;

		HardwareTracker hardwareTracker = new HardwareTracker();
		dcpu = hardwareTracker.requestDCPU();

		ticking = new TickingThread(hardware);

		CPUControl cpuControl = hardwareTracker.requestCPUControl(this);
		hardware.add(cpuControl);
		cpuControl.connectTo(dcpu);
		cpuControl.powerOn();

		boolean assemble = false;
		boolean bootloader = false;
		char[] bootloader_data = new char[512];
		boolean debugger = false;
		boolean optimize_shortLiterals = true;
		int nLems = 0; // We need to add the lems at the end because of the --LEM1802-FPS switch
		float lem_fps = 30f;

		if(args.length > 2) {

			for(int i = 2; i < args.length; ++i) {
				if(args[i].equalsIgnoreCase("--console")) {
					console = true;
					break;
				}
			}

			for(int i = 2; i < args.length; ++i) {
				if (args[i].equalsIgnoreCase("--assemble")) {
					assemble = true;
				} else if (args[i].equalsIgnoreCase("--debugger")) {
					if(!console)
						debugger = true;
					else
						System.out.println("WARNING: Ignored debugger flag, console flag specified.");
				} else if (args[i].equalsIgnoreCase("--big-endian")) {
					little_endian = false;
				} else if (args[i].equalsIgnoreCase("--rom-big-endian")) {
					rom_little_endian = false;
				} else if (args[i].equalsIgnoreCase("--bootloader-big-endian")) {
					bootloader_little_endian = false;
				} else if (args[i].equalsIgnoreCase("--disks-big-endian")) {
					disks_little_endian = false;
				} else if(args[i].equalsIgnoreCase("--disable-shortLiterals")) {
					optimize_shortLiterals = false;
				} else if(args[i].equalsIgnoreCase("--LEM1802")) {
					nLems++;
				} else if(args[i].equalsIgnoreCase("--CLOCK")) {
					hardware.add(hardwareTracker.requestClock());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();
				} else if(args[i].equalsIgnoreCase("--KEYBOARD")) {
					hardware.add(hardwareTracker.requestKeyboard());
					hardware.getLast().connectTo(dcpu);
					hardware.getLast().powerOn();

					if(!console) {
						KeyboardDisplay keyboardDisplay = new KeyboardDisplay((GenericKeyboard) hardware.getLast(), cpuControl);
						keyboardDisplays.add(keyboardDisplay);
					}
				} else if(args[i].equalsIgnoreCase("--EDC")) {
					if(!console) {
						hardware.add(hardwareTracker.requestEDC());
						hardware.getLast().connectTo(dcpu);
						hardware.getLast().powerOn();

						EdcDisplay edcDisplay = new EdcDisplay((EDC) hardware.getLast());
						edcDisplays.add(edcDisplay);
					} else {
						System.out.println("WARNING: Ignored edc flag, console flag specified.");
					}
				} else if(args[i].equalsIgnoreCase("--SPEAKER")) {
					if(!console) {
						hardware.add(hardwareTracker.requestSpeaker());
						hardware.getLast().connectTo(dcpu);
						hardware.getLast().powerOn();
					} else {
						System.out.println("WARNING: Ignored speaker flag, console flag specified.");
					}
				} else {
					if (!args[i].equalsIgnoreCase("--console")) {
						String[] splitted = args[i].split("=");
						if (splitted.length != 2) {
							DCPUToolChain.usage();
						}

						if(splitted[0].equalsIgnoreCase("--bootloader")) {
							try {
								bootloader = true;
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
							} catch (NoSuchFileException e) {
								System.err.println("Error: File not found: " + e.getFile() + ".\n");
								DCPUToolChain.usage();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (splitted[0].equalsIgnoreCase("--M35FD")) {
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

									M35FD m35FD = hardwareTracker.requestM35FD(disk_path, true);
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
						} else if (splitted[0].equalsIgnoreCase("--M525HD")) {
							try {
								String disk_path = splitted[1];

								if (!disk_path.isEmpty()) {
									if ((new File(disk_path)).length() / 2 > M525HD.WORDS_PER_SECTOR * M525HD.SECTORS_PER_TRACK * M525HD.TRACKS) {
										System.err.println("The provided disk file is longer than " + M525HD.WORDS_PER_SECTOR * M525HD.SECTORS_PER_TRACK * M525HD.TRACKS + " words!\n");
										DCPUToolChain.usage();
									}

									M525HD m525HD = hardwareTracker.requestM525HD(disk_path, disks_little_endian);
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
						} else if (splitted[0].equalsIgnoreCase("--LEM1802-FPS")) {
							try {
								lem_fps = Float.parseFloat(splitted[1]);
							} catch(NumberFormatException e) {
								System.err.println("Error: Could not parse FPS number \"" + splitted[1] + "\"");
							}
						} else {
							System.err.println("Error: Unknown flag \"" + splitted[0] + "\"");
							DCPUToolChain.usage();
						}
					}
				}
			}

			for(int i = 0; i < nLems; ++i) {
				hardware.add(hardwareTracker.requestLem());
				hardware.getLast().connectTo(dcpu);
				hardware.getLast().powerOn();

				if(!console) {
					LemDisplay lemDisplay = new LemDisplay((LEM1802) hardware.getLast(), lem_fps);
					lemDisplays.add(lemDisplay);
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

		if(console) {
			consoleServer = new ConsoleServer(25570, dcpu);
			for(GenericKeyboard genericKeyboard : hardwareTracker.getKeyboards()) {
				consoleServer.addKeyboard(genericKeyboard);
			}

			for(LEM1802 lem1802 : hardwareTracker.getLems()) {
				consoleServer.addLemDisplayConsole(lem1802);
			}

			consoleServer.start();
		}

		try {
			if(assemble) {
				File tmpFile = File.createTempFile("DCPUToolchain", Long.toString(System.currentTimeMillis()));
				AssemblerManager assemblerManager;

				String[] assembleCmdLine;
				if(optimize_shortLiterals)
					assembleCmdLine = new String[] {"assemble", input_file, tmpFile.getAbsolutePath()};
				else
					assembleCmdLine = new String[] {"assemble", input_file, tmpFile.getAbsolutePath(), "--disable-shortLiterals"};

				if(bootloader)
					assemblerManager = new AssemblerManager(assembleCmdLine, bootloader_data, bootloader_little_endian);
				else
					assemblerManager = new AssemblerManager(assembleCmdLine);
				boolean success = assemblerManager.assemble();
				if(!success)
					System.exit(-1);

				M35FD bootDrive = hardwareTracker.requestM35FD(tmpFile.getAbsolutePath(), true);
				bootDrive.connectTo(dcpu);
				bootDrive.powerOn();

				hardware.add(bootDrive);
			} else {
				if(!input_file.equalsIgnoreCase("none")) {
					M35FD bootDrive;
					if (bootloader) {
						File tmpFile = File.createTempFile("DCPUToolchain", Long.toString(System.currentTimeMillis()));
						byte[] input_file_data = Files.readAllBytes(Paths.get(input_file));

						byte[] bootloader_data_bytes = new byte[1024];
						for (int i = 0; i < 512; ++i) {
							if (little_endian != bootloader_little_endian) {
								bootloader_data_bytes[i * 2] = (byte) (bootloader_data[i] & 0xFF);
								bootloader_data_bytes[i * 2 + 1] = (byte) ((bootloader_data[i] >> 8) & 0xFF);
							} else {
								bootloader_data_bytes[i * 2] = (byte) ((bootloader_data[i] >> 8) & 0xFF);
								bootloader_data_bytes[i * 2 + 1] = (byte) (bootloader_data[i] & 0xFF);
							}
						}

						FileOutputStream fos = new FileOutputStream(tmpFile);
						fos.write(bootloader_data_bytes);
						fos.write(input_file_data);
						fos.close();

						bootDrive = hardwareTracker.requestM35FD(tmpFile.getAbsolutePath(), little_endian);
					} else {
						bootDrive = hardwareTracker.requestM35FD(input_file, little_endian);
					}
					bootDrive.connectTo(dcpu);
					bootDrive.powerOn();

					hardware.add(bootDrive);
				}
			}
			dcpu.setRam(getClass().getResourceAsStream("/rom.bin"), rom_little_endian);

			if(debugger) {
				debuggerInterface = new DebuggerInterface(dcpu, ticking, this);
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

					stopCallback();
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

		if(debuggerInterface != null)
			debuggerInterface.close();
		for(LemDisplay lemDisplay : lemDisplays)
			lemDisplay.close();
		for(KeyboardDisplay keyboardInterface : keyboardDisplays)
			keyboardInterface.close();
		for(EdcDisplay edcDisplay : edcDisplays)
			edcDisplay.close();
		ticking.setStopped();

		if(console)
			consoleServer.setStopped();

		try {
			Thread.sleep(1000);
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
