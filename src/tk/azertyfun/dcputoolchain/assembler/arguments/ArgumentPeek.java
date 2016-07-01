package tk.azertyfun.dcputoolchain.assembler.arguments;

import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;

public class ArgumentPeek extends Argument {
	public ArgumentPeek(Line line) {
		super(line);
	}

	@Override
	public char getValue() {
		return 0x19;
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
