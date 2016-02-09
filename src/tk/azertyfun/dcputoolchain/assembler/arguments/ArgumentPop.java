package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentPop extends Argument {

	@Override
	public char getValue() {
		return 0x18;
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
