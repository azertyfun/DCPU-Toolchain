package tk.azertyfun.dcputoolchain.emulator;

import java.util.LinkedList;

public class GenericKeyboard extends DCPUHardware {

	public static final int TYPE = 0x30cf7406, REVISION = 1, MANUFACTURER = 0;

	protected char[] buffer = new char[64];
	protected int buffer_pointer = -1, interruptMessage = 0;

	private CallbackIsKeyDown callbackIsKeyDown;

	private LinkedList<KeyboardCallback> keyboardCallbacks = new LinkedList<>();

	protected GenericKeyboard(String id) {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;
	}

	public void interrupt() {
		int a = dcpu.registers[0];
		switch(a) {
			case 0: // CLEAR_BUFFER
				buffer_pointer = -1;
				for(int i = 0; i < buffer.length; ++i) {
					buffer[i] = 0;
				}

				for(KeyboardCallback keyboardCallback : keyboardCallbacks) {
					keyboardCallback.changedBuffer(buffer);
				}
				break;
			case 1: // GET_NEXT
				if(buffer_pointer == -1)
					dcpu.registers[2] = 0;
				else {
					dcpu.registers[2] = buffer[buffer_pointer & 0x3F];
					buffer[buffer_pointer-- & 0x3F] = 0;
				}

				for(KeyboardCallback keyboardCallback : keyboardCallbacks) {
					keyboardCallback.changedBuffer(buffer);
				}
				break;
			case 2: // CHECK_KEY
				if(callbackIsKeyDown != null) {
					char b = dcpu.registers[1];
					dcpu.registers[2] = callbackIsKeyDown.isKeyDown(b) ? (char) 1 : 0;
				} else {
					dcpu.registers[2] = 0;
				}
				break;
			case 3: // SET_INTERRUPT
				interruptMessage = dcpu.registers[1];
				break;
		}
	}

	public void pressedKey(char keyChar) {
		buffer[++buffer_pointer] = keyChar;
		if(interruptMessage != 0)
			dcpu.interrupt((char) interruptMessage);

		for(KeyboardCallback keyboardCallback : keyboardCallbacks) {
			keyboardCallback.pressedKey(keyChar);
			keyboardCallback.changedBuffer(buffer);
		}
	}

	public void pressedKeyCode(int keyCode) {
		if(buffer_pointer + 1 < buffer.length) {
			buffer[++buffer_pointer] = (char) keyCode;
			if (interruptMessage != 0)
				dcpu.interrupt((char) interruptMessage);

			for (KeyboardCallback keyboardCallback : keyboardCallbacks) {
				keyboardCallback.pressedKeyCode(keyCode);
			}
		}
	}

	public void setCallbackIsKeyDown(CallbackIsKeyDown callbackIsKeyDown) {
		this.callbackIsKeyDown = callbackIsKeyDown;
	}

	public void addCallback(KeyboardCallback keyboardCallback) {
		keyboardCallbacks.add(keyboardCallback);
	}

	public interface KeyboardCallback {
		void pressedKey(char key);
		void pressedKeyCode(int key);
		void changedBuffer(char[] buffer);
	}
}
