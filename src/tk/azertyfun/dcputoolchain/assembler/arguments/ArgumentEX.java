package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentEX extends Argument {

	@Override
	public char getValue() {
		return 0x1D;
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
