package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.CallbackStop;
import tk.azertyfun.dcputoolchain.emulator.DCPU;

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
	private JTextArea ramDump = new JTextArea(""), ramChar = new JTextArea("");

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

		Panel viewers = new Panel();
		viewers.setLayout(new BorderLayout());
		ramDump.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		ramDump.setColumns(68);
		ramDump.setRows(32);
		ramDump.setEditable(false);
		viewers.add(ramDump, BorderLayout.WEST);
		ramChar.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		ramChar.setColumns(15);
		ramChar.setRows(32);
		ramChar.setEditable(false);
		viewers.add(ramChar, BorderLayout.EAST);
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

		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
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
			}
		};

		textField.addActionListener(actionListener);
		button.addActionListener(actionListener);
	}

	public void updateDebugInfo() {
		ramDump.setText("");
		if(dcpu.isPausing()) {
			String text = "0x" + String.format("%04x", (int) currentAddress) + ": ";
			String charText = "";
			for(int i = currentAddress; i < currentAddress + 32 * 8; ++i) {
				String eventualAsterisk = i == dcpu.get(0x10009) ? "*" : "";
				if((i + 1) % 8 == 0 && i != currentAddress + 32 * 8 - 1) {
					text += eventualAsterisk + "0x" + String.format("%04x", (int) dcpu.get(i)) + eventualAsterisk + "\n0x" + String.format("%04x", i + 1) + ": ";
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i) + "\n";
					else
						charText += ".\n";
				} else if((i + 1) % 8 == 0) {
					text += eventualAsterisk + "0x" + String.format("%04x", (int) dcpu.get(i)) + eventualAsterisk;
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i);
					else
						charText += ".";
				} else {
					text += eventualAsterisk + "0x" + String.format("%04x", (int) dcpu.get(i)) + eventualAsterisk + ", ";
					if(dcpu.get(i) >= 0x20 && dcpu.get(i) <= 0x7f)
						charText += dcpu.get(i);
					else
						charText += ".";
				}


			}
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
		} else {
			goToAddress.setEnabled(false);
		}
	}
}
