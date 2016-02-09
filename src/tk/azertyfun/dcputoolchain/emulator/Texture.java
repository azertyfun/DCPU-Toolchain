package tk.azertyfun.dcputoolchain.emulator;

public class Texture {
	private int width, height;
	private Color[][] colors;

	public Texture(int width, int height, Color[][] colors) {
		this.width = width;
		this.height = height;
		this.colors = colors;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Color[][] getColors() {
		return colors;
	}

	public Color getColors(int x, int y) {
		return colors[x][y];
	}

	public static class Color {
		float r, g, b;

		public Color(float r, float g, float b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public float red() {
			return r;
		}

		public float green() {
			return g;
		}

		public float blue() {
			return b;
		}
	}
}
