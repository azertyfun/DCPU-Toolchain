package tk.azertyfun.dcputoolchain.disassembler;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Disassembler {

	public static HashMap<Character, String> opcodes = new HashMap<>();
	static {
		opcodes.put((char) 0x01, "SET");
		opcodes.put((char) 0x02, "ADD");
		opcodes.put((char) 0x03, "SUB");
		opcodes.put((char) 0x04, "MUL");
		opcodes.put((char) 0x05, "MLI");
		opcodes.put((char) 0x06, "DIV");
		opcodes.put((char) 0x07, "DVI");
		opcodes.put((char) 0x08, "MOD");
		opcodes.put((char) 0x09, "MDI");
		opcodes.put((char) 0x0a, "AND");
		opcodes.put((char) 0x0b, "BOR");
		opcodes.put((char) 0x0c, "XOR");
		opcodes.put((char) 0x0d, "SHR");
		opcodes.put((char) 0x0e, "ASR");
		opcodes.put((char) 0x0f, "SHL");
		opcodes.put((char) 0x10, "IFB");
		opcodes.put((char) 0x11, "IFC");
		opcodes.put((char) 0x12, "IFE");
		opcodes.put((char) 0x13, "IFN");
		opcodes.put((char) 0x14, "IFG");
		opcodes.put((char) 0x15, "IFA");
		opcodes.put((char) 0x16, "IFL");
		opcodes.put((char) 0x17, "IFU");
		opcodes.put((char) 0x18, "0x0018");
		opcodes.put((char) 0x19, "0x0019");
		opcodes.put((char) 0x1a, "ADX");
		opcodes.put((char) 0x1b, "SBX");
		opcodes.put((char) 0x1c, "0x001c");
		opcodes.put((char) 0x1d, "0x001d");
		opcodes.put((char) 0x1e, "STI");
		opcodes.put((char) 0x1f, "STD");
	}
	public static HashMap<Character, String> specialOpcodes = new HashMap<>();
	static {
		specialOpcodes.put((char) 0x00, "0x0000");
		specialOpcodes.put((char) 0x01, "JSR");
		specialOpcodes.put((char) 0x02, "0x0002");
		specialOpcodes.put((char) 0x03, "0x0003");
		specialOpcodes.put((char) 0x04, "0x0004");
		specialOpcodes.put((char) 0x05, "0x0005");
		specialOpcodes.put((char) 0x06, "0x0006");
		specialOpcodes.put((char) 0x07, "0x0007");
		specialOpcodes.put((char) 0x08, "INT");
		specialOpcodes.put((char) 0x09, "IAG");
		specialOpcodes.put((char) 0x0a, "IAS");
		specialOpcodes.put((char) 0x0b, "RFI");
		specialOpcodes.put((char) 0x0c, "IAQ");
		specialOpcodes.put((char) 0x0d, "0x000d");
		specialOpcodes.put((char) 0x0e, "0x000e");
		specialOpcodes.put((char) 0x0f, "0x000f");
		specialOpcodes.put((char) 0x10, "HWN");
		specialOpcodes.put((char) 0x11, "HWQ");
		specialOpcodes.put((char) 0x12, "HWI");
		specialOpcodes.put((char) 0x13, "LOG");
		specialOpcodes.put((char) 0x14, "BRK");
		specialOpcodes.put((char) 0x15, "0x0015");
		specialOpcodes.put((char) 0x16, "0x0016");
		specialOpcodes.put((char) 0x17, "0x0017");
		specialOpcodes.put((char) 0x18, "0x0018");
		specialOpcodes.put((char) 0x19, "0x0019");
		specialOpcodes.put((char) 0x1a, "0x001a");
		specialOpcodes.put((char) 0x1b, "0x001b");
		specialOpcodes.put((char) 0x1c, "0x001c");
		specialOpcodes.put((char) 0x1d, "0x001d");
		specialOpcodes.put((char) 0x1e, "0x001e");
		specialOpcodes.put((char) 0x1f, "0x001f");
	}

	private char[] ram;
	private char offset;
	private char pc = 0;
	private char wordToHighlight;
	private boolean wordHighlit = false;

	public Disassembler(char[] ram, char wordToHighlight, char offset) {
		this.ram = ram;
		this.offset = offset;
		this.wordToHighlight = wordToHighlight;
	}

	public LinkedHashMap<String, Boolean> disassemble() {
		LinkedHashMap<String, Boolean> instructions = new LinkedHashMap<>();

		while(pc < ram.length) {
			String instruction = "";
			char currentInstruction = pc;

			instruction += "0x" + String.format("%04x", (int) (currentInstruction + offset)) + ": ";

			char word = ram[pc++];
			char opcode = (char) (word & 0b11111);
			char a = (char) ((word >> 10) & 0b111111);
			char b = (char) ((word >> 5) & 0b11111);

			if(opcode == 0) { //Special opcode
				opcode = b;
				instruction += specialOpcodes.get(opcode);
				instruction += " " + get(a, true);
			} else {
				instruction += opcodes.get(opcode);
				instruction += " " + get(b, false) + ", " + get(a, true);
			}

			boolean highlight = false;
			if(currentInstruction + offset >= wordToHighlight && !wordHighlit) {
				highlight = true;
				wordHighlit = true;
			}

			instructions.put(instruction, highlight);
		}

		return instructions;
	}

	public String get(char a, boolean isA) {
		if(a <= 0x07) {
			switch (a) {
				case 0:
					return "A";
				case 1:
					return "B";
				case 2:
					return "C";
				case 3:
					return "X";
				case 4:
					return "Y";
				case 5:
					return "Z";
				case 6:
					return "I";
				case 7:
					return "J";
				default:
					return "0x" + String.format("%04x", (int) a);
			}
		} else if(a <= 0x0F) {
			switch (a - 0x08) {
				case 0:
					return "[A]";
				case 1:
					return "[B]";
				case 2:
					return "[C]";
				case 3:
					return "[X]";
				case 4:
					return "[Y]";
				case 5:
					return "[Z]";
				case 6:
					return "[I]";
				case 7:
					return "[J]";
				default:
					return "0x" + String.format("%04x", (int) a);
			}
		} else if(a <= 0x17) {
			switch (a - 0x10) {
				case 0:
					return "[A + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 1:
					return "[B + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 2:
					return "[C + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 3:
					return "[X + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 4:
					return "[Y + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 5:
					return "[Z + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 6:
					return "[I + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				case 7:
					return "[J + 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
				default:
					return String.format("%04x", (int) a);
			}
		} else if(a == 0x18) {
			if(isA)
				return "POP";
			else
				return "PUSH";
		} else if(a == 0x19) {
			return "PEEK";
		} else if(a == 0x1A) {
			return "PICK 0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????");
		} else if(a == 0x1B) {
			return "SP";
		} else if(a == 0x1C) {
			return "PC";
		} else if(a == 0x1D) {
			return "EX";
		} else if(a == 0x1E) {
			return "[0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????") + "]";
		} else if(a == 0x1F) {
			return "0x" + ((pc < ram.length) ? String.format("%04x", (int) ram[pc++]) : "0x????");
		} else if(a <= 0x3F) {
			if(a == 0x20)
				return "0xffff";
			else
				return "0x" + String.format("%04x", a - 0x21);
		} else {
			return "0x" + String.format("%04x", (int) a);
		}
	}
}
