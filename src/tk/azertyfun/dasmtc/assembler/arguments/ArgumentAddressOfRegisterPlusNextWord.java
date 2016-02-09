package tk.azertyfun.dasmtc.assembler.arguments;

import tk.azertyfun.dasmtc.assembler.exceptions.ParsingException;
import tk.azertyfun.dasmtc.assembler.sourceManagement.Line;

import java.util.LinkedList;

public class ArgumentAddressOfRegisterPlusNextWord extends Argument {

	private char register;

	public ArgumentAddressOfRegisterPlusNextWord(String argument, Line line, LinkedList<String> labels) throws ParsingException {
		String[] splitted = argument.substring(1, argument.length() - 1).split("\\+");

		if(Parser.isNumber(splitted[0])) {
			register = Parser.parseRegister(splitted[1], line);

			boolean isLabel = false;
			for (String label : labels) {
				if (label.equalsIgnoreCase(splitted[0])) {
					isLabel = true;
					break;
				}
			}

			if (isLabel)
				value = new Value(splitted[0]);
			else
				value = new Value(Parser.parseNumber(splitted[0], line));
		} else {
			register = Parser.parseRegister(splitted[0], line);

			boolean isLabel = false;
			for (String label : labels) {
				if (label.equalsIgnoreCase(splitted[1])) {
					isLabel = true;
					break;
				}
			}

			if (isLabel)
				value = new Value(splitted[1]);
			else
				value = new Value(Parser.parseNumber(splitted[1], line));
		}
	}

	@Override
	public char getValue() {
		return (char) (register + 0x10);
	}

	@Override
	public boolean hasNextWordValue() {
		return true;
	}

	@Override
	public char getNextWordValue() {
		return value.literal;
	}
}
