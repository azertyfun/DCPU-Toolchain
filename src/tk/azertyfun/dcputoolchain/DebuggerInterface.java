package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private JEditorPane ramDump = new JEditorPane("text/html", ""), ramChar = new JEditorPane("text/html", "");

	private JLabel regs = new JLabel(), stack = new JLabel();

	private DCPU dcpu;
	private TickingThread tickingThread;

	private char currentAddress;

	public DebuggerInterface(DCPU dcpu, TickingThread tickingThread, CallbackStop callbackStop) {
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
		this.tickingThread = tickingThread;

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

		JPanel regsAndStack = new JPanel();
		regsAndStack.setLayout(new BorderLayout());

		regs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		regsAndStack.add(regs, BorderLayout.WEST);

		stack.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		regsAndStack.add(stack, BorderLayout.EAST);

		getContentPane().add(regsAndStack);

		Panel viewers = new Panel();
		BorderLayout layout = new BorderLayout();
		layout.setHgap(10);
		viewers.setLayout(layout);
		ramDump.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		//ramDump.setColumns(68);
		//ramDump.setRows(32);
		ramDump.setEditable(false);
		viewers.add(ramDump, BorderLayout.WEST);

		ramChar.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		//ramChar.setColumns(10);
		//ramChar.setRows(32);
		ramChar.setEditable(false);
		viewers.add(ramChar, BorderLayout.CENTER);

		clearDebugInfo();

		Panel hardwarePanel = new Panel();
		hardwarePanel.setLayout(new GridLayout(0, 1));
		for(DCPUHardware dcpuHardware : tickingThread.getHardware()) {
			JPanel panel = new JPanel();
			GridLayout gridLayout = new GridLayout(0, 2);
			gridLayout.setHgap(20);
			panel.setLayout(gridLayout);

			JLabel label = new JLabel();
			panel.add(label);

			JCheckBox ticking = new JCheckBox("Ticking", true);
			ticking.setToolTipText("Note: Disabling ticking is not disabling the device. It will mostly disable interruptions from the hardware to the DCPU.");
			final boolean[] enabledWhenRunning = {true};
			final boolean[] enabledWhenPausing = {false};
			ticking.addActionListener(actionEvent -> {
				if(tickingThread.isPausing())
					enabledWhenPausing[0] = !enabledWhenPausing[0];
				else
					enabledWhenRunning[0] = !enabledWhenRunning[0];
				dcpuHardware.setTicking(ticking.isSelected());
			});
			tickingThread.addCallback(() -> {
				ticking.setSelected(tickingThread.isPausing() ? enabledWhenPausing[0] : enabledWhenRunning[0]);
				dcpuHardware.setTicking(ticking.isSelected());
			});
			panel.add(ticking);

			if (dcpuHardware instanceof EDC) {
				label.setText("EDC: ");
			} else if(dcpuHardware instanceof GenericClock) {
				label.setText("Generic clock: ");

				JLabel ticksLabel = new JLabel("Ticks: ");
				((GenericClock) dcpuHardware).addCallback(ticks -> ticksLabel.setText("Ticks: " + ticks));
				panel.add(ticksLabel);
				JButton addTick = new JButton("Add tick to clock");
				addTick.addActionListener(actionEvent -> ((GenericClock) dcpuHardware).addTick());
				panel.add(addTick);
			} else if(dcpuHardware instanceof GenericKeyboard) {
				label.setText("Generic Keyboard: ");
				JLabel keyboardInfoKey = new JLabel("Last pressed key: ");
				JLabel keyboardInfoKeyCode = new JLabel("Last pressed key code:");
				((GenericKeyboard) dcpuHardware).addCallback(new GenericKeyboard.KeyboardCallback() {

					@Override
					public void pressedKey(char key) {
						keyboardInfoKey.setText("Last pressed key: '" + key + "' (" + (int) key + ")");
					}

					@Override
					public void pressedKeyCode(int key) {
						keyboardInfoKeyCode.setText("Last pressed key code: " + key);
					}
				});

				panel.add(keyboardInfoKey);
				panel.add(keyboardInfoKeyCode);
			} else if(dcpuHardware instanceof LEM1802) {
				label.setText("LEM1802: ");
			} else if(dcpuHardware instanceof M35FD) {
				label.setText("M35FD: ");

				JLabel statusLabel = new JLabel("Status: STATE_READY");
				((M35FD) dcpuHardware).addCallback(status -> statusLabel.setText("Status: " + status));

				panel.add(statusLabel);
			} else if(dcpuHardware instanceof M525HD) {
				label.setText("M525HD: ");

				JLabel statusLabel = new JLabel("Status: STATE_PARKED");
				((M525HD) dcpuHardware).addCallback(status -> statusLabel.setText("Status: " + status));

				panel.add(statusLabel);
			} else if(dcpuHardware instanceof CPUControl) {
				label.setText("CPU Control: ");

				JLabel modeLabel = new JLabel("Mode: 0");
				((CPUControl) dcpuHardware).addCallback(mode -> modeLabel.setText("Mode: " + mode));

				panel.add(modeLabel);
			}
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			hardwarePanel.add(panel);
		}
		viewers.add(hardwarePanel, BorderLayout.EAST);
		getContentPane().add(viewers, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public void runpause() {
		tickingThread.setPausing(!dcpu.isPausing());
		dcpu.runpause();
		updateDebugInfo();
	}

	public void step() {
		dcpu.step();
		updateDebugInfo();
	}

	public void goToAddress() {
		// String message = JOptionPane.showInputDialog("Please input an address in RAM."); //Makes X kill the app on dispose(), for some reason. Google tells me to change the parent, which doesn't help. Oh, well.
		JLabel label = new JLabel("Please input an address in RAM (or a register).");
		JTextField textField = new JTextField("");
		JButton button = new JButton("OK");

		JFrame inputDialog = new JFrame("Go to address");
		inputDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		inputDialog.getContentPane().setLayout(new BorderLayout());
		inputDialog.getContentPane().add(label, BorderLayout.NORTH);
		inputDialog.getContentPane().add(textField, BorderLayout.CENTER);
		inputDialog.getContentPane().add(button, BorderLayout.SOUTH);
		inputDialog.setLocationByPlatform(true);
		inputDialog.pack();
		inputDialog.setVisible(true);

		ActionListener actionListener = actionEvent -> {
			String input = textField.getText();
			if(input.equalsIgnoreCase("PC")) {
				currentAddress = dcpu.get(0x10009);
			} else if(input.equalsIgnoreCase("A")) {
				currentAddress = dcpu.get(0x10000);
			} else if(input.equalsIgnoreCase("B")) {
				currentAddress = dcpu.get(0x10001);
			} else if(input.equalsIgnoreCase("C")) {
				currentAddress = dcpu.get(0x10002);
			} else if(input.equalsIgnoreCase("X")) {
				currentAddress = dcpu.get(0x10003);
			} else if(input.equalsIgnoreCase("Y")) {
				currentAddress = dcpu.get(0x10004);
			} else if(input.equalsIgnoreCase("Z")) {
				currentAddress = dcpu.get(0x10005);
			} else if(input.equalsIgnoreCase("I")) {
				currentAddress = dcpu.get(0x10006);
			} else if(input.equalsIgnoreCase("J")) {
				currentAddress = dcpu.get(0x10007);
			} else if(input.equalsIgnoreCase("SP")) {
				currentAddress = dcpu.get(0x10008);
			} else if(input.equalsIgnoreCase("EX")) {
				currentAddress = dcpu.get(0x1000a);
			} else if(input.equalsIgnoreCase("IA")) {
				currentAddress = dcpu.get(0x1000b);
			} else {
				try {
					if(input.length() > 2 && input.substring(0, 2).equalsIgnoreCase("0x")) {
						System.out.println(input.substring(2, input.length()));
						currentAddress = (char) Integer.parseInt(input.substring(2, input.length()), 16);
						System.out.println(Integer.parseInt(input.substring(2, input.length()), 16));
					} else {
						currentAddress = dcpu.get(Integer.parseInt(input));
					}
				} catch(NumberFormatException e) {
					//A JOptionPane should display an error, but since they crash on me I'm too lazy to implement one myself.
				}
			}

			while(currentAddress % 8 != 0) {
				currentAddress--;
			}

			inputDialog.setVisible(false); //I'd use dispose() if I could, but it makes X kill the JVM. Not cool.
			updateDebugInfo();
		};

		textField.addActionListener(actionListener);
		button.addActionListener(actionListener);
	}

	public void updateDebugInfo() {
		if(dcpu.isPausing()) {
			ramDump.setText("<html><head><style>body{font-family:monospace;font-size:10px;}</style></head><body>");
			ramChar.setText("<html><head><style>body{font-family:monospace;font-size:10px;}</style></head><body>");
			String text = "0x" + String.format("%04x", (int) currentAddress) + ": ";
			String charText = "";
			for(int i = currentAddress; i < currentAddress + 32 * 8; ++i) {
				String pcHighlighterOpen = i == dcpu.get(0x10009) ? "<strong>" : "";
				String pcHighlighterClose = i == dcpu.get(0x10009) ? "</strong>" : "";
				if((i + 1) % 8 == 0 && i != currentAddress + 32 * 8 - 1) {
					text += pcHighlighterOpen + "0x" + String.format("%04x", (int) dcpu.get(i)) + pcHighlighterClose + "<br />0x" + String.format("%04x", i + 1) + ": ";
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i) + "<br />";
					else
						charText += ".<br />";
				} else if((i + 1) % 8 == 0) {
					text += pcHighlighterOpen + "0x" + String.format("%04x", (int) dcpu.get(i)) + pcHighlighterClose;
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i);
					else
						charText += ".";
				} else {
					text += pcHighlighterOpen + "0x" + String.format("%04x", (int) dcpu.get(i)) + pcHighlighterClose + ", ";
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i);
					else
						charText += ".";
				}


			}
			text += "</body></html>";
			charText += "</body></html>";
			ramDump.setText(text);
			ramChar.setText(charText);

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

			String stackText = "<html>" +
					"<head></head>" +
					"<body>" +
					"Stack: (0x" + String.format("%04x", (int) dcpu.get(0x10008)) + ") ";
			for(int i = dcpu.get(0x10008); i < dcpu.get(0x10008) + 10 && i != 0xFFFF; ++i) {
				stackText += "0x" + String.format("%04x", (int) dcpu.get(i)) + ((i == dcpu.get(0x10008) + 9 || i == 0xFFFE) ? " (0x" + String.format("%04x", i) + ")" : ", ");
			}
			stackText += "</body>" +
					"</html>";

			stack.setText(stackText);
		} else {
			goToAddress.setEnabled(false);

			clearDebugInfo();
		}
	}

	public void clearDebugInfo() {
		ramDump.setText("<html><head><style>body{font-family:monospace;font-size:10px;}</style></head><body>");
		ramChar.setText("<html><head><style>body{font-family:monospace;font-size:10px;}</style></head><body>");

		String text = "0x0000: ";
		String charText = "";
		for(int i = 0; i < 32 * 8; ++i) {
			if((i + 1) % 8 == 0 && i != 32 * 8 - 1) {
				text += "0x0000<br />0x0000: ";
				charText += ".<br />";
			} else if((i + 1) % 8 == 0) {
				text += "0x0000";
				charText += ".";
			} else {
				text += "0x0000, ";
				charText += ".";
			}

		}
		text += "</body></html>";
		charText += "</body></html>";
		ramDump.setText(text);
		ramChar.setText(charText);

		goToAddress.setEnabled(true);

		regs.setText("<html>" +
				"<head></head>" +
				"<body>" +
				"A: -, B: -, C: -<br />" +
				"X: -, Y: -, Z: -<br />" +
				"I: -, J: -<br />" +
				"PC: -, SP: -, EX: -, IA: -" +
				"</body>" +
				"</html>");
		stack.setText("<html>" +
				"<head></head>" +
				"<body>" +
				"Stack: (0xFFFF) 0x0000 (0xFFFF)" +
				"</body>" +
				"</html>");
	}
}
