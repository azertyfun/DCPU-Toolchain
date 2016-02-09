package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.arguments.Argument;
import tk.azertyfun.dcputoolchain.assembler.arguments.Value;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;

import java.util.HashMap;

public class Token {

	private char opcode;
	private Argument A, B;
	private boolean special;
	private boolean isDat, isLabel;
	private Value datValue;
	private String label;

	public Token(Value value) {
		this.isDat = true;
		this.datValue = value;
	}

	public Token(char opcode, boolean special, Argument b, Argument a) {
		this.opcode = opcode;
		this.special = special;
		this.isDat = false;
		A = a;
		B = b;
	}

	public Token(String label) {
		this.label = label;
		isLabel = true;
	}

	public boolean isDat() {
		return isDat;
	}

	public boolean isLabel() {
		return isLabel;
	}

	public String getLabel() {
		return label;
	}

	public void makeDatValueLiteral(HashMap<String, Character> labels) throws ParsingException {
		if(!datValue.isLiteral()) {
			if(labels.containsKey(datValue.getLabel().toUpperCase()))
				datValue.setLiteral(labels.get(datValue.getLabel().toUpperCase()));
			else
				throw new ParsingException("Error: Can't find label declaration for " + datValue.getLabel());
			//value.isLiteral = true;
		}
	}

	public static final class Opcodes {
		public static final char SPECIAL_OPCODE = 0x00;
		public static final char SET = 0x01;
		public static final char ADD = 0x02;
		public static final char SUB = 0x03;
		public static final char MUL = 0x04;
		public static final char MLI = 0x05;
		public static final char DIV = 0x06;
		public static final char DVI = 0x07;
		public static final char MOD = 0x08;
		public static final char MDI = 0x09;
		public static final char AND = 0x0a;
		public static final char BOR = 0x0b;
		public static final char XOR = 0x0c;
		public static final char SHR = 0x0d;
		public static final char ASR = 0x0e;
		public static final char SHL = 0x0f;
		public static final char IFB = 0x10;
		public static final char IFC = 0x11;
		public static final char IFE = 0x12;
		public static final char IFN = 0x13;
		public static final char IFG = 0x14;
		public static final char IFA = 0x15;
		public static final char IFL = 0x16;
		public static final char IFU = 0x17;
		public static final char ADX = 0x1A;
		public static final char SBX = 0x1B;
		public static final char STI = 0x1E;
		public static final char STD = 0x1F;

		public static final HashMap<String, Character> strings = new HashMap<>();
		static {
			strings.put("SET", SET);
			strings.put("ADD", ADD);
			strings.put("SUB", SUB);
			strings.put("MUL", MUL);
			strings.put("MLI", MLI);
			strings.put("DIV", DIV);
			strings.put("DVI", DVI);
			strings.put("MOD", MOD);
			strings.put("MDI", MDI);
			strings.put("AND", AND);
			strings.put("BOR", BOR);
			strings.put("XOR", XOR);
			strings.put("SHR", SHR);
			strings.put("ASR", ASR);
			strings.put("SHL", SHL);
			strings.put("IFB", IFB);
			strings.put("IFC", IFC);
			strings.put("IFE", IFE);
			strings.put("IFN", IFN);
			strings.put("IFG", IFG);
			strings.put("IFA", IFA);
			strings.put("IFL", IFL);
			strings.put("IFU", IFU);
			strings.put("ADX", ADX);
			strings.put("SBX", SBX);
			strings.put("STI", STI);
			strings.put("STD", STD);
		}

	}

	public static final class SpecialOpcodes {
		public static final char JSR = 0x01;
		public static final char INT = 0x08;
		public static final char IAG = 0x09;
		public static final char IAS = 0x0A;
		public static final char RFI = 0x0B;
		public static final char IAQ = 0x0C;
		public static final char HWN = 0x10;
		public static final char HWQ = 0x11;
		public static final char HWI = 0x12;

		public static final HashMap<String, Character> strings = new HashMap<>();
		static {
			strings.put("JSR", JSR);
			strings.put("INT", INT);
			strings.put("IAG", IAG);
			strings.put("IAS", IAS);
			strings.put("RFI", RFI);
			strings.put("IAQ", IAQ);
			strings.put("HWN", HWN);
			strings.put("HWQ", HWQ);
			strings.put("HWI", HWI);
		}
	}

	public char getOpcode() {
		return opcode;
	}

	public Argument getA() {
		return A;
	}

	public Argument getB() {
		return B;
	}

	public boolean isSpecial() {
		return special;
	}

	public Value getDatValue() {
		return datValue;
	}
}
