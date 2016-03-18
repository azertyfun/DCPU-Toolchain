package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.emulator.EDC;
import tk.azertyfun.dcputoolchain.emulator.Texture;

import javax.swing.*;
import java.awt.*;

public class EdcDisplay extends JFrame {
	public final int SCALE = 5;
	public final int WINDOW_WIDTH = SCALE * (6 * EDC.TEXT_CELL_COLUMN_COUNT);
	public final int WINDOW_HEIGHT = SCALE * (8 * EDC.TEXT_CELL_LINE_COUNT);

	private EDC edc;

	private RepaintThread repaintThread;

	private CustomPanel customPanel = new CustomPanel();

	public EdcDisplay(EDC edc) {
		this.edc = edc;

		this.setTitle("DCPU Emulator Display for techcompliant");

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		customPanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		setContentPane(customPanel);

		repaintThread = new RepaintThread();
		repaintThread.start();

		this.pack();
		setResizable(false);

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
			return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
		}

		@Override
		public void paintComponent(Graphics graphics) {
			edc.render();
			Texture texture = edc.getTexture();

			if(texture != null) {
				for (int x = 0; x < texture.getWidth(); ++x) {
					for (int y = 0; y < texture.getHeight(); ++y) {
						graphics.setColor(new Color(texture.getColors(x, y).red(), texture.getColors(x, y).green(), texture.getColors(x, y).blue()));
						graphics.fillRect(SCALE * x, SCALE * y, SCALE * (x + 1), SCALE * (y + 1));
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
			float expectedTime = 1000f / 60f;

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
				}
			}
		}
	}
}
