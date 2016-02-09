package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

import java.util.LinkedList;

public class ArgumentAddressOfRegisterPlusNextWord extends Argument {

	private char register;

	public ArgumentAddressOfRegisterPlusNextWord(String argument, Line line, LinkedList<String> labels) throws ParsingException {
		String[] splitted = argument.substring(1, argument.length() - 1).split("\\+");

		/*
		 * What this does:
		 * - If splitted[0] is a label => [next word + register]
		 * - If splitted[0] is a number => [next word + register]
		 * - Else => [register + next word]
		 */


		boolean isLabel = false;
		for (String label : labels) {
			if (label.equalsIgnoreCase(splitted[0])) {
				isLabel = true;
				break;
			}
		}

		if(isLabel) {
			register = Parser.parseRegister(splitted[1], line);

			value = new Value(splitted[0]);
		} else if(Parser.isNumber(splitted[0])) {
			register = Parser.parseRegister(splitted[1], line);

			value = new Value(Parser.parseNumber(splitted[0], line));
		} else {
			register = Parser.parseRegister(splitted[0], line);

			isLabel = false;
			for (String label : labels) {
				if (label.equalsIgnoreCase(splitted[1])) {
					isLabel = true;
					break;
				}
			}

			if (isLabel)
				value = new Value(splitted[0]);
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
