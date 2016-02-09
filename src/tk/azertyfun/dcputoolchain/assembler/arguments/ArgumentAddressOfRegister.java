package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

public class ArgumentAddressOfRegister extends Argument {
	private char register;

	public ArgumentAddressOfRegister(String argument, Line line) throws ParsingException {
		register = (char) (Parser.parseRegister("" + argument.charAt(1), line) + 0x08);
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
