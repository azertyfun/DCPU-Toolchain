package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

import java.util.LinkedList;

public class ArgumentPick extends Argument {

	public ArgumentPick(String argument, Line line, LinkedList<String> labels) throws ParsingException {
		String[] splitted = argument.substring(1, argument.length() - 1).split("\\+");
		if(splitted.length != 2)
			throw new ParsingException("Error: Can't parse [SP + next word] \"" + argument + "\": '" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());

		boolean isLabel = false;
		for(String label : labels) {
			if(label.equalsIgnoreCase(argument)) {
				isLabel = true;
				break;
			}
		}

		if(isLabel)
			value = new Value(splitted[1]);
		else
			value = new Value(Parser.parseNumber(splitted[1], line));
	}

	@Override
	public char getValue() {
		return (char) (0x1A);
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
