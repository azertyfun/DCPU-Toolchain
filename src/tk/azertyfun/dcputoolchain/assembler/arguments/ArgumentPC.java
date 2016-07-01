package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

public class ArgumentPC extends Argument {

	public ArgumentPC(Line line) {
		super(line);
	}

	@Override
	public char getValue() {
		return 0x1C;
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
