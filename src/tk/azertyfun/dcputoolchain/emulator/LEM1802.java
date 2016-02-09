package tk.azertyfun.dcputoolchain.emulator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class LEM1802 extends DCPUHardware {

	public static final int TYPE = 0x7349f615, REVISION = 0x1802, MANUFACTURER = 0x1c6c8b36;
	public static final int WIDTH_PIXELS = 128;
	public static final int HEIGHT_PIXELS = 96;
	public static final int START_DURATION = 60;
	public static final int BORDER_WIDTH = 4;
	private final static int[][][] bootImage = new int[128][96][3];
	static {
		try {
			BufferedImage image = ImageIO.read(new File("res/boot.png"));
			byte[] bootImage_raw = ((DataBufferByte) image.getData().getDataBuffer()).getData();
			int pos = 0;
			for(int y = 0; y < 96; ++y) {
				for(int x = 0; x < 128; ++x) {
					bootImage[x][y][0] = bootImage_raw[pos * 3 + 2];
					bootImage[x][y][1] = bootImage_raw[pos * 3 + 1];
					bootImage[x][y][2] = bootImage_raw[pos * 3];

					pos++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean blinkOn;
	protected int blinkDelay;

	public static final char defaultFont[] = new char[] {
			0x000f, 0x0808, 0x080f, 0x0808, 0x08f8, 0x0808, 0x00ff, 0x0808, 0x0808, 0x0808, 0x08ff, 0x0808, 0x00ff, 0x1414,
			0xff00, 0xff08, 0x1f10, 0x1714, 0xfc04, 0xf414, 0x1710, 0x1714, 0xf404, 0xf414, 0xff00, 0xf714, 0x1414, 0x1414,
			0xf700, 0xf714, 0x1417, 0x1414, 0x0f08, 0x0f08, 0x14f4, 0x1414, 0xf808, 0xf808, 0x0f08, 0x0f08, 0x001f, 0x1414,
			0x00fc, 0x1414, 0xf808, 0xf808, 0xff08, 0xff08, 0x14ff, 0x1414, 0x080f, 0x0000, 0x00f8, 0x0808, 0xffff, 0xffff,
			0xf0f0, 0xf0f0, 0xffff, 0x0000, 0x0000, 0xffff, 0x0f0f, 0x0f0f, 0x0000, 0x0000, 0x005f, 0x0000, 0x0300, 0x0300,
			0x3e14, 0x3e00, 0x266b, 0x3200, 0x611c, 0x4300, 0x3629, 0x7650, 0x0002, 0x0100, 0x1c22, 0x4100, 0x4122, 0x1c00,
			0x2a1c, 0x2a00, 0x083e, 0x0800, 0x4020, 0x0000, 0x0808, 0x0800, 0x0040, 0x0000, 0x601c, 0x0300, 0x3e41, 0x3e00,
			0x427f, 0x4000, 0x6259, 0x4600, 0x2249, 0x3600, 0x0f08, 0x7f00, 0x2745, 0x3900, 0x3e49, 0x3200, 0x6119, 0x0700,
			0x3649, 0x3600, 0x2649, 0x3e00, 0x0024, 0x0000, 0x4024, 0x0000, 0x0814, 0x2241, 0x1414, 0x1400, 0x4122, 0x1408,
			0x0259, 0x0600, 0x3e59, 0x5e00, 0x7e09, 0x7e00, 0x7f49, 0x3600, 0x3e41, 0x2200, 0x7f41, 0x3e00, 0x7f49, 0x4100,
			0x7f09, 0x0100, 0x3e49, 0x3a00, 0x7f08, 0x7f00, 0x417f, 0x4100, 0x2040, 0x3f00, 0x7f0c, 0x7300, 0x7f40, 0x4000,
			0x7f06, 0x7f00, 0x7f01, 0x7e00, 0x3e41, 0x3e00, 0x7f09, 0x0600, 0x3e41, 0xbe00, 0x7f09, 0x7600, 0x2649, 0x3200,
			0x017f, 0x0100, 0x7f40, 0x7f00, 0x1f60, 0x1f00, 0x7f30, 0x7f00, 0x7708, 0x7700, 0x0778, 0x0700, 0x7149, 0x4700,
			0x007f, 0x4100, 0x031c, 0x6000, 0x0041, 0x7f00, 0x0201, 0x0200, 0x8080, 0x8000, 0x0001, 0x0200, 0x2454, 0x7800,
			0x7f44, 0x3800, 0x3844, 0x2800, 0x3844, 0x7f00, 0x3854, 0x5800, 0x087e, 0x0900, 0x4854, 0x3c00, 0x7f04, 0x7800,
			0x447d, 0x4000, 0x2040, 0x3d00, 0x7f10, 0x6c00, 0x417f, 0x4000, 0x7c18, 0x7c00, 0x7c04, 0x7800, 0x3844, 0x3800,
			0x7c14, 0x0800, 0x0814, 0x7c00, 0x7c04, 0x0800, 0x4854, 0x2400, 0x043e, 0x4400, 0x3c40, 0x7c00, 0x1c60, 0x1c00,
			0x7c30, 0x7c00, 0x6c10, 0x6c00, 0x4c50, 0x3c00, 0x6454, 0x4c00, 0x0836, 0x4100, 0x0077, 0x0000, 0x4136, 0x0800,
			0x0201, 0x0201, 0x704c, 0x7000
	};
	public static final char defaultPalette[] = new char[] {
			0x000, 0x00a, 0x0a0, 0x0aa, 0xa00, 0xa0a, 0xa50, 0xaaa, 0x555, 0x55f, 0x5f5, 0x5ff, 0xf55, 0xf5f, 0xff5, 0xfff
	};

	private int screenMemMap, fontMemMap, paletteMemMap, startDelay;
	private char borderColor = 0x0;
	private Texture texture;
	private int[] averageColor;
	private char[] videoRam;
	private char[] fontRam;
	private char[] paletteRam;
	private boolean useGivenBuffers = false;

	public LEM1802(String id) {
		super(TYPE, REVISION, MANUFACTURER);
		this.id = id;
	}

	public void render() {
		int avg_red = 0, avg_green = 0, avg_blue = 0;
		Texture.Color colors[][] = new Texture.Color[WIDTH_PIXELS + 2 * BORDER_WIDTH][HEIGHT_PIXELS + 2 * BORDER_WIDTH];

		if((screenMemMap != 0 || useGivenBuffers) && startDelay == 0) {
			/*
			 * This ram to texture algorithm is heavily inspired from mappum's in his javascript emulator. Check it out there : https://github.com/mappum/DCPU-16/blob/master/lib/LEM1802.js
			 */
			float colorBuffer2D[][][] = new float[128 + 2 * BORDER_WIDTH][96 + 2 * BORDER_WIDTH][3];
			int pos = 0;
			for(int y = 0; y < 12; ++y) {
				for(int x = 0; x < 32; ++x) {
					if((useGivenBuffers && videoRam[pos & 0xFFFF] != 0) || (!useGivenBuffers && dcpu.ram[(screenMemMap + pos) & 0xFFFF] != 0)) {
						char fgCol;
						char bgCol;
						boolean blink;
						char character;
						if(!useGivenBuffers) {
							fgCol = (char) ((dcpu.ram[(screenMemMap + pos) & 0xFFFF] & 0xF000) >> 12);
							bgCol = (char) ((dcpu.ram[(screenMemMap + pos) & 0xFFFF] & 0xF00) >> 8);
							blink = ((dcpu.ram[(screenMemMap + pos & 0xFFFF)] & 0x80) >> 7) == 1;
							character = (char) (dcpu.ram[(screenMemMap + pos) & 0xFFFF] & 0x7F);
						} else {
							fgCol = (char) ((videoRam[(pos) & 0xFFFF] & 0xF000) >> 12);
							bgCol = (char) ((videoRam[(pos) & 0xFFFF] & 0xF00) >> 8);
							blink = ((videoRam[(pos & 0xFFFF)] & 0x80) >> 7) == 1;
							character = (char) (videoRam[pos & 0xFFFF] & 0x7F);
						}

						char fontChar[] = new char[] {font(character * 2), font(character * 2 + 1)};

						if(!blink || !blinkOn) {
							for(int i = 0; i < 4; ++i) {
								int word = fontChar[((i >= 2) ? 1 : 0) * 1]; //java, plz, why can't you cast boolean to int ?
								int hword = (word >> (((i % 2) == 0 ? 1 : 0) * 8)) & 0xFF; //plz java plz
								for(int j = 0; j < 8; ++j) {
									int pixel = (hword >> j) & 1;
									int px = BORDER_WIDTH + x * 4 + i;
									int py = BORDER_WIDTH + y * 8 + j;
									if(pixel == 1) {
										colorBuffer2D[px][py][0] = red(palette(fgCol));
										colorBuffer2D[px][py][1] = green(palette(fgCol));
										colorBuffer2D[px][py][2] = blue(palette(fgCol));
									} else {
										colorBuffer2D[px][py][0] = red(palette(bgCol));
										colorBuffer2D[px][py][1] = green(palette(bgCol));
										colorBuffer2D[px][py][2] = blue(palette(bgCol));
									}
								}
							}
						}
					}
					pos++;
				}
			}
			for(int y = 95 + 2 * BORDER_WIDTH; y >=0 ; --y) {
				for(int x = 0; x < 128 + 2 * BORDER_WIDTH; ++x) {
					if(y < BORDER_WIDTH || (y < 96 + 2 * BORDER_WIDTH && y >= 96 + BORDER_WIDTH)) {
						colors[x][y] = new Texture.Color(red(palette(borderColor)), green(palette(borderColor)), blue(palette(borderColor)));
					} else if(x < BORDER_WIDTH || (x < 128 + 2 * BORDER_WIDTH && x >= 128 + BORDER_WIDTH)) {
						colors[x][y] = new Texture.Color(red(palette(borderColor)), green(palette(borderColor)), blue(palette(borderColor)));
					} else {
						colors[x][y] = new Texture.Color(colorBuffer2D[x][y][0], colorBuffer2D[x][y][1], colorBuffer2D[x][y][2]);
					}
				}
			}

			avg_red /= (float) colors.length / 3.0f;
			avg_green /= (float) colors.length / 3.0f;
			avg_blue /= (float) colors.length / 3.0f;
		} else {
			int pos = 0;
			for(int y = 95 + 2 * BORDER_WIDTH; y >=0 ; --y) {
				for(int x = 0; x < 128 + 2 * BORDER_WIDTH; ++x) {
					char borderColor = (char) (startDelay * 16 / START_DURATION);
					if(y < BORDER_WIDTH || (y < 96 + 2 * BORDER_WIDTH && y >= 96 + BORDER_WIDTH)) {
						colors[x][y] = new Texture.Color(red(palette(borderColor)), green(palette(borderColor)), blue(palette(borderColor)));
					} else if(x < BORDER_WIDTH || (x < 128 + 2 * BORDER_WIDTH && x >= 128 + BORDER_WIDTH)) {
						colors[x][y] = new Texture.Color(red(palette(borderColor)), green(palette(borderColor)), blue(palette(borderColor)));
					} else {
						colors[x][y] = new Texture.Color((char) bootImage[x - BORDER_WIDTH][y - BORDER_WIDTH][0], (char) bootImage[x - BORDER_WIDTH][y - BORDER_WIDTH][1], (char) bootImage[x - BORDER_WIDTH][y - BORDER_WIDTH][2]);
					}
					pos++;
				}
			}
			avg_red /= (float) colors.length / 3.0f;
			avg_green /= (float) colors.length / 3.0f;
			avg_blue /= (float) colors.length / 3.0f;
		}

		averageColor = new int[] {avg_red, avg_green, avg_blue};

		texture = new Texture(WIDTH_PIXELS + BORDER_WIDTH * 2, HEIGHT_PIXELS + BORDER_WIDTH * 2, colors);
	}

	public Texture getTexture() {
		return texture;
	}

	public int[] getAverageColor() {
		return averageColor;
	}

	public void interrupt() {
		int a = dcpu.registers[0];
		int offset = 0;
		switch(a) {
			case 0: //MEM_MAP_SCREEN
				if(screenMemMap == 0 && dcpu.registers[1] != 0) {
					startDelay = START_DURATION;
				}
				screenMemMap = dcpu.registers[1];
				break;
			case 1: //MEM_MAP_FONT
				fontMemMap = dcpu.registers[1];
				break;
			case 2: //MEM_MAP_PALETTE
				paletteMemMap = dcpu.registers[1];
				break;
			case 3: //SET_BORDER_COLOR
				borderColor = (char) (dcpu.registers[1] & 0xF);
				/* borderColor[0] = (char) (((palette(col) & 0xF00) >> 4) | 0xF);
				borderColor[1] = (char) ((palette(col) & 0xF0) | 0xF);
				borderColor[2] = (char) (((palette(col) & 0xF) << 4) | 0xF); */
				break;
			case 4: //MEM_DUMP_FONT
				offset = dcpu.registers[1];
				for(int i = 0; i < 256; ++i) {
					dcpu.ram[offset + i & 0xFFFF] = defaultFont[i];
				}
				dcpu.cycles += 256;
				break;
			case 5: //MEM_DUMP_PALETTE
				offset = dcpu.registers[1];
				for(int i = 0; i < 16; ++i) {
					dcpu.ram[offset + i & 0xFFFF] = defaultPalette[i];
				}
				dcpu.cycles += 16;
		}
	}

	protected char palette(int col) {
		if(useGivenBuffers)
			return paletteRam[col];

		if(paletteMemMap == 0) {
			return defaultPalette[col & 0xF];
		} else {
			return dcpu.ram[paletteMemMap + col];
		}
	}

	protected char font(int f) {
		if(useGivenBuffers)
			return fontRam[f];

		if(fontMemMap == 0) {
			return defaultFont[f & 0xFF];
		} else {
			return dcpu.ram[fontMemMap + f];
		}
	}

	protected float red(char color) {
		return ((float) (((color & 0xF00) >> 4) | 0xF)) / 255f;
	}

	protected float green(char color) {
		return ((float) ((color & 0xF0) | 0xF)) / 255f;
	}

	protected float blue(char color) {
		return ((float) (((color & 0xF) << 4) | 0xF)) / 255f;
	}

	@Override
	public void tick60hz() {
		if(startDelay > 0)
			startDelay -= 1;
		if(blinkDelay > 0)
			blinkDelay -= 1;
		if(blinkDelay == 0) {
			blinkOn = !blinkOn;
			blinkDelay = 30;
		}
	}

	@Override
	public void powerOff() {
		averageColor = new int[] {0, 0, 0};
		screenMemMap = 0;
		fontMemMap = 0;
		paletteMemMap = 0;
		borderColor = 0;
		startDelay = 0;
	}

	@Override
	public void powerOn() {
	}

	public char[] getVideoRam() {
		char ram[] = new char[384];
		for(int i = 0; i < 384; ++i) {
			if(screenMemMap != 0)
				ram[i] = dcpu.ram[screenMemMap + i];
			else
				ram[i] = 0;
		}
		return ram;
	}

	public char[] getFontRam() {
		char fontRam[] = new char[256];
		for(int i = 0; i < 256; ++i) {
			if(fontMemMap != 0)
				fontRam[i] = dcpu.ram[fontMemMap + i];
			else
				fontRam[i] = defaultFont[i];
		}
		return fontRam;
	}

	public char[] getPaletteRam() {
		char paletteRam[] = new char[16];
		for(int i = 0; i < 16; ++i) {
			if(paletteMemMap != 0)
				paletteRam[i] = dcpu.ram[paletteMemMap + i];
			else
				paletteRam[i] = defaultPalette[i];
		}
		return paletteRam;
	}

	public void setUseGivenBuffers(boolean useGivenBuffers) {
		this.useGivenBuffers = useGivenBuffers;
	}

	public void setVideoRam(char[] videoRam) {
		this.videoRam = videoRam;
	}

	public void setFontRam(char[] fontRam) {
		this.fontRam = fontRam;
	}

	public void setPaletteRam(char[] paletteRam) {
		this.paletteRam = paletteRam;
	}

	public char getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(char borderColor) {
		this.borderColor = borderColor;
	}
}
