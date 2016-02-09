package tk.azertyfun.dcputoolchain.assembler.sourceManagement;

public class Line {

	private String original_line;
	private String line;
	private String file;
	private int lineNumber;

	public Line(String line, String file, int lineNumber) {
		this.line = line;
		this.original_line = line;
		this.file = file;
		this.lineNumber = lineNumber;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getLine() {
		return line;
	}

	public String getOriginal_line() {
		return original_line;
	}

	public String getFile() {
		return file;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void replace_regexp(String search, String replace) {
		line = line.replaceAll(search, replace);
	}

	public void replace(String search, String replace) {
		line = line.replace(search, replace);
	}
}
