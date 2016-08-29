package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.LEM1802;

import javax.swing.*;
import java.awt.*;

public class LemDisplay extends JFrame {
	public final int SCALE = 5;
	public final int WIDTH = (LEM1802.WIDTH_PIXELS + LEM1802.BORDER_WIDTH * 2) * SCALE;
	public final int HEIGHT = (LEM1802.HEIGHT_PIXELS + LEM1802.BORDER_WIDTH * 2) * SCALE;

	private RepaintThread repaintThread;

	private CustomPanel customPanel = new CustomPanel();

	protected LEM1802 lem1802;

	private float fps = 10f;

	public LemDisplay(LEM1802 lem1802, float fps) {
		this.lem1802 = lem1802;
		this.fps = fps;

		this.setResizable(false);
		this.setTitle("DCPU Emulator Display for techcompliant");

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		customPanel.setBounds(0, 0, WIDTH, HEIGHT);
		repaintThread = new RepaintThread();
		repaintThread.start();
		this.add(customPanel);

		getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));

		customPanel.setDoubleBuffered(false);

		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
	}

	public void close() {
		repaintThread.stopped = true;
		setVisible(false);
		dispose();
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

	private class RepaintThread extends Thread {
		public boolean stopped = false;

		public RepaintThread() {
			this.setName("LEM1802 Display Render Thread");
		}

		public void run() {
			float expectedTime = 1000f / fps;

			while (!stopped) {
				long start = System.currentTimeMillis();
				customPanel.repaint();
				long execTime = System.currentTimeMillis() - start;
				if (expectedTime - execTime > 0) {
					try {
						Thread.sleep((long) (expectedTime - execTime));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("LEM1802 Display is lagging behind by " + (expectedTime - execTime) + " ms!");
				}
			}
		}
	}
}
