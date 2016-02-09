package tk.azertyfun.dasmtc.assembler;

import tk.azertyfun.dasmtc.assembler.sourceManagement.Line;
import tk.azertyfun.dasmtc.assembler.sourceManagement.SourceManager;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelAggregator {
	private SourceManager sourceManager;

	public LabelAggregator(SourceManager sourceManager) {
		this.sourceManager = sourceManager;
	}

	public void aggregateLabels() {
		LinkedList<String> labels = new LinkedList<>();

		for(Line l : sourceManager.getLines()) {
			String[] splitted = l.getLine().split(" ");

			if(splitted[0].equalsIgnoreCase(".LBL") || splitted[0].equalsIgnoreCase("#LBL")) {
				sourceManager.addLabel(splitted[1], l);
			}
		}
	}
}
