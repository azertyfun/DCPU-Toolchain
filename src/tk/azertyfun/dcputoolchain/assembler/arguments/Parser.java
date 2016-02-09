package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

public class Parser {

	public static char parseNumber(String s, Line line) throws ParsingException {
		if(s.length() == 1) {
			try {
				return (char) Integer.parseInt(s);
			} catch(NumberFormatException e) {
				throw new ParsingException("Error: Can't parse decimal number: \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
			}
		}

		char c2 = s.charAt(1);
		String s_;
		switch (c2) {
			case 'x': //Hex
				s_ = s.substring(2, s.length());
				try {
					return (char) Integer.parseInt(s_, 16);
				} catch(NumberFormatException e) {
					throw new ParsingException("Error: Can't parse hexadecimal number \"" + s + "\": \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
				}
			case 'b': //Binary
				s = s.substring(2, s.length());
				try {
					return (char) Integer.parseInt(s, 2);
				} catch(NumberFormatException e) {
					throw new ParsingException("Error: Can't parse binary number \"" + s + "\": \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
				}
			case 'o': //Octal
				s = s.substring(2, s.length());
				try {
					return (char) Integer.parseInt(s, 8);
				} catch(NumberFormatException e) {
					throw new ParsingException("Error: Can't parse octal number \"" + s + "\": \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
				}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': //Decimal
				try {
					return (char) Integer.parseInt(s, 10);
				} catch(NumberFormatException e) {
					throw new ParsingException("Error: Can't parse decimal number \"" + s + "\": \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
				}
			default:
				throw new ParsingException("Error: Can't parse number \"" + s + "\": \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
		}
	}

	public static char parseRegister(String s, Line line) throws ParsingException {
		switch(s.charAt(0)) {
			case 'A':
			case 'a':
				return 0x00;
			case 'B':
			case 'b':
				return 0x01;

			case 'C':
			case 'c':
				return 0x02;

			case 'X':
			case 'x':
				return 0x03;

			case 'Y':
			case 'y':
				return 0x04;

			case 'Z':
			case 'z':
				return 0x05;

			case 'I':
			case 'i':
				return 0x06;

			case 'J':
			case 'j':
				return 0x07;
			default:
				throw new ParsingException("Error: Can't parse register \"" + s + "\": '" + line.getOriginal_line() + "' in " + line.getFile() + ":" + line.getLineNumber());
		}
	}

	public static boolean isNumber(String s) {
		if(s.length() == 1 && s.charAt(0) >= '0' && s.charAt(0) <= '9')
			return true;
		else if(s.length() == 1)
			return false;

		char c2 = s.charAt(1);
		String s_;
		switch (c2) {
			case 'x': //Hex
				s_ = s.substring(2, s.length());
				try {
					Integer.parseInt(s_, 16);
					break;
				} catch(NumberFormatException e) {
					return false;
				}
			case 'b': //Binary
				s = s.substring(2, s.length());
				try {
					Integer.parseInt(s, 2);
					break;
				} catch(NumberFormatException e) {
					return false;
				}
			case 'o': //Octal
				s = s.substring(2, s.length());
				try {
					Integer.parseInt(s, 8);
					break;
				} catch(NumberFormatException e) {
					return false;
				}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': //Decimal
				try {
					Integer.parseInt(s, 10);
					break;
				} catch(NumberFormatException e) {
					return false;
				}
			default:
				return false;
		}

		return true;
	}
}
