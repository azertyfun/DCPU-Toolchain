package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

import java.util.LinkedList;

public class ArgumentNextWordLiteral extends Argument implements ArgumentOptimizable {

	boolean hasNextWordValue = true;
	char literalValue;
	boolean isA;

	public ArgumentNextWordLiteral(String argument, Line line, LinkedList<String> labels, boolean isA) throws ParsingException {
		this.isA = isA;

		boolean isLabel = false;
		for(String label : labels) {
			if(label.equalsIgnoreCase(argument)) {
				isLabel = true;
				break;
			}
		}

		if(isLabel)
			value = new Value(argument);
		else
			value = new Value(Parser.parseNumber(argument, line));
	}

	@Override
	public char getValue() {
		if(hasNextWordValue)
			return 0x1F;
		else
			return literalValue;
	}

	@Override
	public boolean hasNextWordValue() {
		return hasNextWordValue;
	}

	@Override
	public char getNextWordValue() {
		return value.literal;
	}

	public void optimizeLiteral() {
		if(isA && (value.literal == 0xFFFF || value.literal <= 0x1E)) {
			hasNextWordValue = false;
			literalValue = (char) (value.literal + 0x21);
		}
	}
}
