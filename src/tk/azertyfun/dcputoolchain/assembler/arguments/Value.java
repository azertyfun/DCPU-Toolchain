package tk.azertyfun.dcputoolchain.assembler.arguments;

public class Value {
	boolean isLiteral;
	char literal;
	String label;

	public Value(char literal) {
		this.isLiteral = true;
		this.literal = literal;
	}

	public Value(String label) {
		this.isLiteral = false;
		this.label = label;
	}

	public char getLiteral() {
		return literal;
	}

	public boolean isLiteral() {
		return isLiteral;
	}

	public String getLabel() {
		return label;
	}

	public void setLiteral(Character literal) {
		this.literal = literal;
	}
}
