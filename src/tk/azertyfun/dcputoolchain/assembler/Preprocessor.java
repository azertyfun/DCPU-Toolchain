package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.util.HashMap;

public class Preprocessor {
	private SourceManager sourceManager;

	private HashMap<String, String> defines = new HashMap<>();

	public Preprocessor(SourceManager sourceManager) {
		this.sourceManager = sourceManager;
	}

	public void processDefines() {
		for(Line line : sourceManager.getLines()) {
			String[] splitted = line.getLine().split(" ");
			if(splitted[0].equalsIgnoreCase(".define") || splitted[0].equalsIgnoreCase("#define")) {
				String define_name = splitted[1];
				String define_body = "";
				for(int i = 2; i < splitted.length; ++i)
					define_body += splitted[2];

				defines.put(define_name, define_body);
				line.setLine(""); //Set the line for deletion
			}
		}

		for(String key : defines.keySet()) {
			sourceManager.replace(key, defines.get(key));
		}

		sourceManager.removeEmptyLines();
	}
}
