package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.CPUControl;
import tk.azertyfun.dcputoolchain.emulator.GenericKeyboard;

import javax.swing.*;
import java.util.LinkedList;

public abstract class KeyboardInterface extends JFrame {
	protected GenericKeyboard keyboard;
	protected CPUControl cpuControl;

	protected LinkedList<Integer> keys = new LinkedList<>();

	public KeyboardInterface(GenericKeyboard keyboard, CPUControl cpuControl) {
		this.keyboard = keyboard;
		this.cpuControl = cpuControl;
	}

	public abstract void close();
}
