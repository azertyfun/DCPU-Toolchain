package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.CallbackStop;
import tk.azertyfun.dcputoolchain.emulator.DCPU;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class DebuggerInterface extends JFrame {
	private Action goToAddressAction = new AbstractAction("Go to address (Ctrl+G)") {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			goToAddress();
		}
	};

	private JButton goToAddress = new JButton(goToAddressAction);
	private JTextArea ramDump = new JTextArea("Ram viewer not loaded yet.");

	private JLabel regs = new JLabel("<html>" +
			"<head></head>" +
			"<body>" +
			"A: -, B: -, C: -<br />" +
			"X: -, Y: -, Z: -<br />" +
			"I: -, J: -<br />" +
			"PC: -, SP: -, EX: -, IA: -" +
			"</body>" +
			"</html>");
	private DCPU dcpu;

	private char currentAddress;

	public DebuggerInterface(DCPU dcpu, CallbackStop callbackStop) {
		//runpause.addActionListener(actionEvent -> runpause());
		JButton stop = new JButton("Stop");
		stop.addActionListener(actionEvent -> callbackStop.stopCallback());
		Action stepAction = new AbstractAction("Step (Ctrl+S)") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				step();
			}
		};
		JButton step = new JButton(stepAction);
		step.addActionListener(actionEvent -> dcpu.step());

		this.dcpu = dcpu;

		setTitle("DCPU Emulator Debugger for techcompliant");

		Panel buttonsPanel = new Panel();
		buttonsPanel.setLayout(new GridLayout());

		Action runpauseAction = new AbstractAction("Run/Pause (Ctrl+R)") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				runpause();
			}
		};
		runpauseAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		JButton runpause = new JButton(runpauseAction);
		runpause.getActionMap().put("runpause", runpauseAction);
		runpause.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) runpauseAction.getValue(Action.ACCELERATOR_KEY), "runpause");
		buttonsPanel.add(runpause);

		buttonsPanel.add(stop);

		stepAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		step.getActionMap().put("step", stepAction);
		step.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) stepAction.getValue(Action.ACCELERATOR_KEY), "step");
		buttonsPanel.add(step);

		goToAddress.setEnabled(false);
		goToAddressAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		goToAddress.getActionMap().put("goToAddress", goToAddressAction);
		goToAddress.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) goToAddressAction.getValue(Action.ACCELERATOR_KEY), "goToAddress");
		buttonsPanel.add(goToAddress);

		getContentPane().add(buttonsPanel, BorderLayout.NORTH);
		regs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		getContentPane().add(regs);

		ramDump.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		ramDump.setColumns(66);
		ramDump.setRows(16);
		ramDump.setEditable(false);
		getContentPane().add(ramDump, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public void runpause() {
		dcpu.runpause();
		updateDebugInfo();
	}

	public void step() {
		dcpu.step();
		updateDebugInfo();
	}

	public void goToAddress() {
		String message = JOptionPane.showInputDialog("Please input an address in RAM.");
		//TODO
	}

	public void updateDebugInfo() {
		ramDump.setText("");
		if(dcpu.isPausing()) {
			String text = "0x0000: ";
			for(int i = 0; i < 16 * 8; ++i) {
				if((i + 1) % 8 == 0 && i != 16 * 8 - 1) {
					text += "0x" + String.format("%04x", (int) dcpu.get(i)) + "\n0x" + String.format("%04x", i + 1) + ": ";
				} else if((i + 1) % 8 == 0) {
					text += "0x" + String.format("%04x", (int) dcpu.get(i));
				} else {
					text += "0x" + String.format("%04x", (int) dcpu.get(i)) + ", ";
				}
			}
			ramDump.setText(text);

			goToAddress.setEnabled(true);

			regs.setText("<html>" +
					"<head></head>" +
					"<body>" +
					"A: 0x" + String.format("%04x", (int) dcpu.get(0x10000)) + ", B: 0x" + String.format("%04x", (int) dcpu.get(0x10001)) + ", C: 0x" + String.format("%04x", (int) dcpu.get(0x10002)) + "<br />" +
					"X: 0x" + String.format("%04x", (int) dcpu.get(0x10003)) + ", Y: 0x" + String.format("%04x", (int) dcpu.get(0x10004)) + ", Z: 0x" + String.format("%04x", (int) dcpu.get(0x10005)) + "<br />" +
					"I: 0x" + String.format("%04x", (int) dcpu.get(0x10006)) + ", J: 0x" + String.format("%04x", (int) dcpu.get(0x10007)) + "<br />" +
					"PC: 0x" + String.format("%04x", (int) dcpu.get(0x10009)) + ", SP: 0x" + String.format("%04x", (int) dcpu.get(0x10008)) + ", EX: 0x" + String.format("%04x", (int) dcpu.get(0x1000a)) + ", IA: 0x" + String.format("%04x", (int) dcpu.get(0x1000b)) +
					"</body>" +
					"</html>");
		} else {
			goToAddress.setEnabled(false);
		}
	}
}
