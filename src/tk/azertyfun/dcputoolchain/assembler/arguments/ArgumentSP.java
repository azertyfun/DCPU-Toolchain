package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentSP extends Argument {
	@Override
	public char getValue() {
		return 0x1B;
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
