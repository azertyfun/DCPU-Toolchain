package tk.azertyfun.dcputoolchain.interfaces;

import biz.source_code.utils.RawConsoleInput;
import tk.azertyfun.dcputoolchain.emulator.CPUControl;
import tk.azertyfun.dcputoolchain.emulator.CallbackStop;
import tk.azertyfun.dcputoolchain.emulator.GenericKeyboard;

import java.io.IOException;


/*
 * IMPORTANT NOTICE: This implementation is far from ideal and severly limited. Java only allows so much to be done with the console.
 * The RawConsoleInput class from Christian d'Heureuse (http://www.source-code.biz/snippets/java/RawConsoleInput) is used to even allow reading a single char from the console.
 * Most control keys can't be captured that way. It is also impossible to distinguish key press and key release, meaning CHECK_KEY won't work.
 * If the program you are trying to use doesn't work because of these limitations, well tough luck. Unless you can setup a graphical environment, there is nothing that can be done in a portable fashion.
 */

public class KeyboardConsole extends KeyboardInterface implements Runnable {

	private boolean stopped = false;
	private CallbackStop callbackStop;

	public KeyboardConsole(GenericKeyboard keyboard, CPUControl cpuControl, CallbackStop callbackStop) {
		super(keyboard, cpuControl);
		this.callbackStop = callbackStop;
	}

	@Override
	public void run() {
		while(!stopped) {
			try {
				int key = RawConsoleInput.read(true);
				if(key == 3) //CTRL+C
					callbackStop.stopCallback();

				switch(key) { // /!\ IMPORTANT NOTICE: It is impossible in console mode to capture insert, delete, arrow keys, shift or control.
					case 0x8: //BACKSPACE
						keyboard.pressedKeyCode(0x10);
						break;
					case 0xa: //LF
						keyboard.pressedKeyCode(0x11);
						break;
					default:
						keyboard.pressedKey((char) key);
				}

				System.err.println(keys.getLast());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		stopped = true;

		try {
			RawConsoleInput.resetConsoleMode();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
