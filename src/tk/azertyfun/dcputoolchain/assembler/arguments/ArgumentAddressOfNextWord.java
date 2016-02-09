package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

import java.util.LinkedList;

public class ArgumentAddressOfNextWord extends Argument {
	boolean isA;

	public ArgumentAddressOfNextWord(String argument, Line line, LinkedList<String> labels, boolean isA) throws ParsingException {
		this.isA = isA;

		String content = argument.substring(1, argument.length() - 1);

		boolean isLabel = false;
		for(String label : labels) {
			if(label.equalsIgnoreCase(content)) {
				isLabel = true;
				break;
			}
		}

		if(isLabel)
			value = new Value(content);
		else
			value = new Value(Parser.parseNumber(content, line));
	}

	@Override
	public char getValue() {
		return 0x1E;
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
