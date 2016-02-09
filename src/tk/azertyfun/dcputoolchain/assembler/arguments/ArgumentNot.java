package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentNot extends Argument {
	@Override
	public char getValue() {
		return 0;
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
