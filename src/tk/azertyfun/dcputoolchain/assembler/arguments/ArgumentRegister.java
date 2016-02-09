package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

public class ArgumentRegister extends Argument  {
	char register;

	public ArgumentRegister(String argument, Line line) throws ParsingException {
		register = Parser.parseRegister(argument, line);
	}

	@Override
	public char getValue() {
		return register;
	}

	@Override
	public boolean hasNextWordValue() {
		return false;
	}

	@Override
	public char getNextWordValue() {
		return 0;
	}
}
