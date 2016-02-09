package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;

import java.util.HashMap;

public abstract class Argument {
	protected Value value = new Value('\0');

	public void makeValueLiteral(HashMap<String, Character> labels) throws ParsingException {
		if(!value.isLiteral) {
			if(labels.containsKey(value.label.toUpperCase()))
				value.literal = labels.get(value.label.toUpperCase());
			else
				throw new ParsingException("Error: Can't find label declaration for " + value.label);
			//value.isLiteral = true;
		}
	}

	public abstract char getValue();

	public abstract boolean hasNextWordValue();
	public abstract char getNextWordValue();
}
