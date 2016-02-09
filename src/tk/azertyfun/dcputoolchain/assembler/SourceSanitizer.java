package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceSanitizer {

	private SourceManager sourceManager;

	public SourceSanitizer(SourceManager sourceManager) {
		this.sourceManager = sourceManager;
	}

	public void sanitize() {
		LinkedList<Line> lines = sourceManager.getLines();
		String source_sanitized = "";

		LinkedList<String> string_literals = new LinkedList<>(); //We don't want to sanitize string literals!

		for(Line line : sourceManager.getLines()) {
			if(line.getLine().contains("\"")) {
				String[] lit_splitted = line.getLine().split("\"");
				boolean in_lit = false;
				for(int j = 0; j < lit_splitted.length; ++j) {
					if(in_lit) {
						string_literals.add(lit_splitted[j]);
						lit_splitted[j] = "__STRING__LIT__" + (string_literals.size() - 1) + "__";
					}

					in_lit = !in_lit;
				}

				String l = "";
				for(String s : lit_splitted)
					l += s;
				line.setLine(l);
			}

			line.replace("';'", "__TMP__SEMICOLON"); //We don't want to replace semicolon character constants
			line.replace_regexp(";.*", ""); //Remove comments
			line.replace("__TMP__SEMICOLON", ";");

			line.replace("\t", " "); //We don't neeed tabs

			line.replace_regexp("  +", " "); //Remove multiple spaces

			//Remove spaces before and after commas
			line.replace_regexp(" *, *", ",");

			line.replace_regexp("^ +", ""); //Remove spaces at the begginging of the line
			line.replace_regexp(" +$", ""); //Remove spaces at the end of the line

			line.replace_regexp("\n+$", ""); //Remove multiple \n

			//Change PICK n to [SP+n]
			Pattern p = Pattern.compile("[Pp][Ii][Cc][Kk] ([0-9]+)");
			Matcher m = p.matcher(line.getLine());
			while(m.find()) {
				line.replace_regexp("[Pp][Ii][Cc][Kk] ([0-9]+)", "[SP+" + m.group(1) + "]");
			}

			//Change [A + C], [C + D] n to [A+B],[C+D]. Otherwise, the next regex will replace both...
			p = Pattern.compile("\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\],\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\]");
			m = p.matcher(line.getLine());
			boolean replaced = false;
			while(m.find()) {
				replaced = true;
				line.replace_regexp("\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\],\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\]", "[" + m.group(1) + "+" + m.group(2) + "],[" + m.group(3) + "+" + m.group(4) + "]");
			}

			//Change [X + Y] n to [X+Y]
			if(!replaced) {
				p = Pattern.compile("\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\]");
				m = p.matcher(line.getLine());
				while (m.find()) {
					line.replace_regexp("\\[([^ ^\\[^\\]]+) *\\+ *([^ ^\\[^\\]]+)\\]", "[" + m.group(1) + "+" + m.group(2) + "]");
				}
			}
		}

		sourceManager.removeEmptyLines();

		//Let's restore string literals
		Pattern p = Pattern.compile("__STRING__LIT__([0-9]*)__");
		for(Line line : sourceManager.getLines()) {
			Matcher matcher = p.matcher(line.getLine());

			while(matcher.find()) {
				int lit_n = Integer.parseInt(matcher.group(1));

				String lit = string_literals.get(lit_n);
				char[] chars = lit.toCharArray();

				String lit_to_numbers = "";
				for(char c : chars) {
					lit_to_numbers += "0x" + Integer.toString((int) c, 16) + ",";
				}
				lit_to_numbers = lit_to_numbers.substring(0, lit_to_numbers.length() - 1);

				line.replace("__STRING__LIT__" + matcher.group(1) + "__", lit_to_numbers);
			}
		}
	}
}
