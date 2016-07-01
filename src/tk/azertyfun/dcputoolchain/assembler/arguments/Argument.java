package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

import java.util.HashMap;

public abstract class Argument {
	private Line line;

	public Argument(Line line) {
		this.line = line;
	}

	protected Value value = new Value('\0');

	public void makeValueLiteral(HashMap<String, Character> labels, char offset) throws ParsingException {
		if(!value.isLiteral) {
			if(labels.containsKey(value.label.toUpperCase()))
				value.literal = (char) (labels.get(value.label.toUpperCase()) + offset);
			else
				throw new ParsingException("Error: Can't find label declaration for " + value.label + " at " + line.getFile() + ":" + line.getLine() + " (" + line.getOriginal_line() + ")");
			//value.isLiteral = true;
		}
	}

	public abstract char getValue();

	public abstract boolean hasNextWordValue();
	public abstract char getNextWordValue();
}
