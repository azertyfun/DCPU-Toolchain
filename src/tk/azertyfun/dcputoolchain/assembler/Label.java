package tk.azertyfun.dcputoolchain.assembler;

public class Label {

	private String label, file;
	private int line;

	public Label(String label, String file, int line) {
		this.label = label;
		this.file = file;
		this.line = line;
	}

	public String getLabel() {
		return label;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}
}
