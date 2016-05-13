package tk.azertyfun.dcputoolchain.interfaces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tk.azertyfun.dcputoolchain.emulator.DCPU;
import tk.azertyfun.dcputoolchain.emulator.GenericKeyboard;
import tk.azertyfun.dcputoolchain.emulator.LEM1802;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

public class ConsoleServer extends Thread {

	private boolean stopped = false;
	private int port;
	private StoppableThread readThread, writeThread;
	private LinkedList<GenericKeyboard> keyboards = new LinkedList<>();
	private LinkedList<LEM1802> lem1802s = new LinkedList<>();
	private DCPU dcpu;

	public ConsoleServer(int port, DCPU dcpu) {
		this.port = port;
		this.dcpu = dcpu;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while(!stopped) {
				System.out.println("Waiting for connection from console...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client console connected!");
				OutputStream out = clientSocket.getOutputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				(readThread = new StoppableThread() {
					public void run() {
						while(!stopped) {
							try {
								if(in.ready()) {
									for (GenericKeyboard genericKeyboard : keyboards) {
										int key = in.read();

										switch(key) { // /!\ IMPORTANT NOTICE: It is impossible in console mode to capture insert, delete, arrow keys, shift or control.
											case 0:
												break;
											case 0x8:
											case 0x7F: //BACKSPACE-DEL (ncurses doesn't seem to differenciate)
												genericKeyboard.pressedKeyCode(0x10);
												break;
											case 0xa: //LF
												genericKeyboard.pressedKeyCode(0x11);
												break;
											default:
												genericKeyboard.pressedKey((char) key);
										}
									}
								} else {
									Thread.sleep(10);
								}
							} catch (InterruptedException | IOException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();

				(writeThread = new StoppableThread() {
					public void run() {
						StatusPacket statusPacket = new StatusPacket();

						Gson json = (new GsonBuilder()).create();

						while(!stopped) {
							try {
								char[] ram = lem1802s.getFirst().getVideoRam();
								for (int i = 0; i < 12*32; ++i)
									statusPacket.ram[i] = ram[i];

								for (int i = 0; i < 8; ++i)
									statusPacket.registers[i] = dcpu.getRegisters()[i];
								statusPacket.pc = dcpu.getPc();
								statusPacket.sp = dcpu.getSp();
								statusPacket.ex = dcpu.getEx();
								statusPacket.ia = dcpu.getIa();

								out.write(json.toJson(statusPacket).getBytes());
								out.write(3); // End Of Text

								Thread.sleep(100);
							} catch (SocketException e) {
								readThread.stopped = true;
								stopped = true;
							} catch (InterruptedException | IOException e) {
								e.printStackTrace();
							}
						}

						try {
							out.write(4); // End of Transmission
						} catch (SocketException e) {
							readThread.stopped = true;
							stopped = true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStopped() {
		stopped = true;
		readThread.stopped = true;
		writeThread.stopped = true;
	}

	public void addLemDisplayConsole(LEM1802 lem1802) {
		lem1802s.add(lem1802);
	}

	public void addKeyboard(GenericKeyboard genericKeyboard) {
		keyboards.add(genericKeyboard);
	}

	private abstract class StoppableThread extends Thread {
		public boolean stopped = false;
	}
}
