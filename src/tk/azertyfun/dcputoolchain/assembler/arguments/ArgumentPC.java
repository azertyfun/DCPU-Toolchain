package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentPC extends Argument {

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
