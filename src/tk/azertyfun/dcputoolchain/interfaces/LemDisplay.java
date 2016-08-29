package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.LEM1802;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LemDisplay extends JFrame implements ActionListener {
	public final int SCALE = 5;
	public final int WIDTH = (LEM1802.WIDTH_PIXELS + LEM1802.BORDER_WIDTH * 2) * SCALE;
	public final int HEIGHT = (LEM1802.HEIGHT_PIXELS + LEM1802.BORDER_WIDTH * 2) * SCALE;

	private CustomPanel customPanel = new CustomPanel();

	protected LEM1802 lem1802;

	private float fps = 10f;

	private Timer timer;

	public LemDisplay(LEM1802 lem1802, float fps) {
		this.lem1802 = lem1802;
		this.fps = fps;
		timer = new Timer((int) (1000f / fps), this);
		timer.start();

		this.setResizable(false);
		this.setTitle("DCPU Emulator Display for techcompliant");

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		customPanel.setBounds(0, 0, WIDTH, HEIGHT);
		//repaintThread = new RepaintThread();
		//repaintThread.start();
		this.add(customPanel);

		getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));

		customPanel.setDoubleBuffered(false);

		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
	}

	public void close() {
		timer.stop();
		setVisible(false);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if(actionEvent.getSource() == timer) {
			customPanel.repaint();
		}
	}

	private class CustomPanel extends JPanel {
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(LemDisplay.this.WIDTH, LemDisplay.this.HEIGHT);
		}

		@Override
		public void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			lem1802.render(graphics, SCALE);
		}
	}
}
