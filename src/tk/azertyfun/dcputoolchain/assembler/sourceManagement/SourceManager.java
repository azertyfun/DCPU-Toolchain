package tk.azertyfun.dcputoolchain.assembler.sourceManagement;

import tk.azertyfun.dcputoolchain.assembler.Label;

import java.util.Iterator;
import java.util.LinkedList;

public class SourceManager {

	private LinkedList<Line> lines = new LinkedList<>();
	private LinkedList<String> s_labels = new LinkedList<>();
	private LinkedList<Label> labels = new LinkedList<>();

	public SourceManager() {

	}

	public LinkedList<Line> getLines() {
		return lines;
	}

	public void addLine(String line, String file, int lineNumber) {
		lines.add(new Line(line, file, lineNumber));
	}

	public void addSource(SourceManager source) {
		for(Line l : source.getLines()) {
			addLine(l.getLine(), l.getFile(), l.getLineNumber());
		}
	}

	public void replace(String key, String value) {
		for(Line l : lines) {
			l.replace(key, value);
		}
	}

	public void replace_regexp(String key, String value) {
		for(Line l : lines) {
			l.replace_regexp(key, value);
		}
	}

	public void removeEmptyLines() {
		Iterator<Line> iterator = lines.iterator();
		while(iterator.hasNext()) {
			if(iterator.next().getLine().isEmpty())
				iterator.remove();
		}
	}

	public void addLabel(String label, Line line) {
		s_labels.add(label);
		labels.add(new Label(label, line.getFile(), line.getLineNumber()));
	}

	public LinkedList<String> getS_labels() {
		return s_labels;
	}

	/*
	 * Dumps the whole processed source, can be used for debug purposes
	 */

	@Override
	public String toString() {
		String s = "";
		for(Line l : lines) {
			s += l.getLine() + "\n";
		}

		return s;
	}
}
