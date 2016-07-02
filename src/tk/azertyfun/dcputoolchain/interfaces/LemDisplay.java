package tk.azertyfun.dcputoolchain.interfaces;

import tk.azertyfun.dcputoolchain.emulator.LEM1802;
import tk.azertyfun.dcputoolchain.emulator.Texture;

import javax.swing.*;
import java.awt.*;

public class LemDisplay extends JFrame {

	public static final int BORDER_WIDTH = 4;

	public final int SCALE = 5;
	public final int WIDTH = (128 + BORDER_WIDTH * 2) * SCALE;
	public final int HEIGHT = (96 + BORDER_WIDTH * 2) * SCALE;

	private RepaintThread repaintThread;

	private CustomPanel customPanel = new CustomPanel();

	protected LEM1802 lem1802;

	public LemDisplay(LEM1802 lem1802) {
		this.lem1802 = lem1802;

		this.setResizable(false);
		//this.setSize(WIDTH, HEIGHT);
		this.setTitle("DCPU Emulator Display for techcompliant");

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		customPanel.setBounds(0, 0, WIDTH, HEIGHT);
		this.setSize(WIDTH, HEIGHT);
		repaintThread = new RepaintThread();
		repaintThread.start();
		this.add(customPanel);

		getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));

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
			return new Dimension(LemDisplay.this.WIDTH, LemDisplay.this.WIDTH);
		}

		@Override
		public void paintComponent(Graphics graphics) {
			Texture texture = lem1802.getTexture();

			if(texture != null) {
				for (int x = 0; x < texture.getWidth(); ++x) {
					for (int y = 0; y < texture.getHeight(); ++y) {
						try {
							graphics.setColor(new Color(texture.getColors(x, y).red(), texture.getColors(x, y).green(), texture.getColors(x, y).blue()));
							graphics.fillRect(SCALE * x, SCALE * y, SCALE * (x + 1), SCALE * (y + 1));
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}
	}

	private class RepaintThread extends Thread {
		public boolean stopped = false;

		public RepaintThread() {
		}

		public void run() {
			float expectedTime = 1000f / 10f;

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
					System.err.println("woops " + (expectedTime - execTime));
				}
			}
		}
	}
}
