package tk.azertyfun.dcputoolchain.assembler.arguments;

public class ArgumentPeek extends Argument {
	public ArgumentPeek() {

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
