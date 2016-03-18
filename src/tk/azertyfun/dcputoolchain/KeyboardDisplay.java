package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.CPUControl;
import tk.azertyfun.dcputoolchain.emulator.CallbackIsKeyDown;
import tk.azertyfun.dcputoolchain.emulator.GenericKeyboard;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class KeyboardDisplay extends JFrame implements KeyListener, CallbackIsKeyDown, ActionListener {

	private JButton powerButton = new JButton("PWR");
	private JButton modeButton = new JButton("MDE");
	private CustomPanel kbPanel;

	private LinkedList<Integer> keys = new LinkedList<>();

	private GenericKeyboard keyboard;
	private CPUControl cpuControl;

	public KeyboardDisplay(GenericKeyboard keyboard, CPUControl cpuControl) {
		this.keyboard = keyboard;
		this.cpuControl = cpuControl;

		keyboard.setCallbackIsKeyDown(this);

		this.setTitle("DCPU Emulator Keyboard for techcompliant");
		this.setSize(new Dimension(356, 250));
		this.setResizable(false);

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();

		panel.add(powerButton);
		powerButton.setBounds(0, 0, 356/2, 50);
		powerButton.addActionListener(this);

		panel.add(modeButton);
		modeButton.setBounds(356/2, 0, 356, 50);
		modeButton.addActionListener(this);

		kbPanel = new CustomPanel();
		kbPanel.setBounds(0, 50, 356, 250);
		panel.add(kbPanel);
		getContentPane().add(panel);

		addKeyListener(this);

		this.setLocationByPlatform(true);
		this.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		keys.add(e.getKeyCode());
		switch(e.getKeyCode()) {
			case KeyEvent.VK_BACK_SPACE:
				keyboard.pressedKeyCode(0x10);
				break;
			case KeyEvent.VK_ENTER:
				keyboard.pressedKeyCode(0x11);
				break;
			case KeyEvent.VK_INSERT:
				keyboard.pressedKeyCode(0x12);
				break;
			case KeyEvent.VK_DELETE:
				keyboard.pressedKeyCode(0x13);
				break;
			case KeyEvent.VK_UP:
				keyboard.pressedKeyCode(0x80);
				break;
			case KeyEvent.VK_DOWN:
				keyboard.pressedKeyCode(0x81);
				break;
			case KeyEvent.VK_LEFT:
				keyboard.pressedKeyCode(0x82);
				break;
			case KeyEvent.VK_RIGHT:
				keyboard.pressedKeyCode(0x83);
				break;
			case KeyEvent.VK_SHIFT:
				keyboard.pressedKeyCode(0x90);
				break;
			case KeyEvent.VK_CONTROL:
				keyboard.pressedKeyCode(0x91);
				break;
			default:
				keyboard.pressedKey(e.getKeyChar());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for(int i = 0; i < keys.size(); ++i) {
			if (keys.get(i) == e.getKeyCode())
				keys.remove(i);
		}
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	@Override
	public boolean isKeyDown(int key) {
		return keys.contains(key);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if(actionEvent.getSource() instanceof JButton && ((JButton) actionEvent.getSource()).getText().equals("PWR")) {
			cpuControl.powerButton();
		} else if(actionEvent.getSource() instanceof JButton && ((JButton) actionEvent.getSource()).getText().equals("MDE")) {
			cpuControl.modeButton();
		}
	}

	private class CustomPanel extends JPanel implements FocusListener {
		private Image img;
		private Image img_nofocus;

		public CustomPanel() {
			super();
			try {
				img = ImageIO.read(new File("res/keyboard.png"));
				img_nofocus = ImageIO.read(new File("res/keyboard_nofocus.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			setFocusable(true);
			addKeyListener(KeyboardDisplay.this);
			addFocusListener(this);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(356, 200);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			if(hasFocus())
				graphics.drawImage(img, 0, 0, null);
			else {
				graphics.drawImage(img_nofocus, 0, 0, null);
				requestFocusInWindow();
			}
		}

		@Override
		public void focusGained(FocusEvent focusEvent) {
			this.repaint();
		}

		@Override
		public void focusLost(FocusEvent focusEvent) {
			this.repaint();
		}
	}
}
