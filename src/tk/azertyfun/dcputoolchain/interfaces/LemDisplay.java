package tk.azertyfun.dcputoolchain.interfaces;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import tk.azertyfun.dcputoolchain.emulator.LEM1802;
import tk.azertyfun.dcputoolchain.emulator.Texture;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LemDisplay extends Thread {

	public final int WIDTH = 128;
	public final int HEIGHT = 96;

	public final int SCALE = 5;
	public final int REAL_WIDTH = (WIDTH +8) * SCALE;
	public final int REAL_HEIGHT = (HEIGHT +8) * SCALE;

	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;

	private long window;

	protected LEM1802 lem1802;

	public LemDisplay(LEM1802 lem1802) {
		this.lem1802 = lem1802;
	}

	public void run() {
		try {
			init();
			loop();
		} finally {
			glfwTerminate();
			errorCallback.release();
		}
	}

	private void init() {
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		if(glfwInit() != GLFW_TRUE)
			throw new IllegalStateException("Could not initialize GLFW. Screen will not be displayed.");

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		window = glfwCreateWindow(REAL_WIDTH, REAL_HEIGHT, "DCPU Emulator Display for techcompliant", NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Could not create the GLFW window. Screen will not be displayed.");

		/* glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
			}
		}); */

		//Center the window on the screen. Not ideal but afaik there is no way to let the display manager handle things.
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(
				window,
				(vidMode.width() - REAL_WIDTH) / 2,
				(vidMode.height() - REAL_HEIGHT) / 2
		);

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}

	private void loop() {
		GL.createCapabilities();

		glClearColor(0, 0, 0, 0);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, REAL_WIDTH, REAL_HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		while(glfwWindowShouldClose(window) == GLFW_FALSE) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			Texture texture = lem1802.getTexture();

			if(texture != null) {
				for (int x = 0; x < texture.getWidth(); ++x) {
					for (int y = 0; y < texture.getHeight(); ++y) {
						glColor3f(texture.getColors(x, y).red(), texture.getColors(x, y).green(), texture.getColors(x, y).blue());
						glBegin(GL_QUADS);
							glVertex2f(SCALE * x, SCALE * y);
							glVertex2f(SCALE * (x + 1), SCALE * y);
							glVertex2f(SCALE * (x + 1), SCALE * (y + 1));
							glVertex2f(SCALE * x, SCALE * (y + 1));
						glEnd();
					}
				}
			}

			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}

	public void close() {
		if(glfwWindowShouldClose(window) != GLFW_TRUE)
			glfwSetWindowShouldClose(window, GLFW_TRUE);
	}
}
