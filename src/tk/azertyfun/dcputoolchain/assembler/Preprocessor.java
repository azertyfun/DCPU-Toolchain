package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.arguments.Parser;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.util.HashMap;

public class Preprocessor {
	private SourceManager sourceManager;

	private HashMap<String, String> defines = new HashMap<>();

	public Preprocessor(SourceManager sourceManager) {
		this.sourceManager = sourceManager;
	}

	public void processDefines() throws ParsingException {
		for(Line line : sourceManager.getLines()) {
			String[] splitted = line.getLine().split(" ");
			if(splitted[0].equalsIgnoreCase(".define") || splitted[0].equalsIgnoreCase("#define")) {
				String define_name = splitted[1];
				String define_body = "";
				for(int i = 2; i < splitted.length; ++i)
					define_body += splitted[2];

				defines.put(define_name, define_body);
				line.setLine(""); //Set the line for deletion
			} else if(splitted[0].equalsIgnoreCase(".magic") || splitted[0].equalsIgnoreCase("#magic")) {
				if(splitted.length == 3)
					sourceManager.addMagic(Parser.parseNumber(splitted[1], line), Parser.parseNumber(splitted[2], line));
				else
					throw new ParsingException("Error: Excpected 2 arguments for magic directive, got " + (splitted.length - 1) + " at " + line.getFile() + ":" + line.getLine() + " (" + line.getOriginal_line() + ")");
				line.setLine(""); //Set the line for deletion
			} else if(splitted[0].equalsIgnoreCase(".org") || splitted[0].equalsIgnoreCase("#org")) {
				if(splitted.length == 2)
					sourceManager.setOffset(Parser.parseNumber(splitted[1], line));
				else
					throw new ParsingException("Error: Expected 1 arguments for org directive, got " + (splitted.length - 1) + " at " + line.getFile() + ":" + line.getLine() + " (" + line.getOriginal_line() + ")");
				line.setLine(""); //Set the line for deletion
			}
		}

		for(String key : defines.keySet()) {
			sourceManager.replace(key, defines.get(key));
		}

		sourceManager.removeEmptyLines();
	}
}
