package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.DCPUToolChain;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceAggregator {

	private BufferedReader bufferedReader;
	private String file_path;

	public SourceAggregator(String file_path) {
		this.file_path = file_path;

		try {
			bufferedReader = new BufferedReader(new FileReader(new File(file_path)));
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file_path);
			DCPUToolChain.usage();
		}
	}

	public SourceManager getSourceManager() throws IOException, ParsingException {
		SourceManager sourceManager = new SourceManager();

		File current_dir = (new File(file_path)).getParentFile();

		String line;
		String original_line;
		int line_number = 0;
		while((line = bufferedReader.readLine()) != null) {
			line_number++;
			original_line = line;

			if(line.replaceAll(" ", "").replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").isEmpty())
				continue;

			String splitted[] = line.split(" ");

			for(int i = 0; i < splitted.length; ++i) {
				splitted[i] = splitted[i].replace(" ", "");
			}

			if(splitted[0].equalsIgnoreCase(".INCLUDE") || splitted[0].equalsIgnoreCase("#INCLUDE")) {
				String file = current_dir.getAbsolutePath() + System.getProperty("file.separator");
				if(splitted[1].charAt(0) != '"') { //For compatibility reasons, .include path/to/file instead of .include "path/to/file" is allowed.
					file += line.substring(line.indexOf(" ") + 1);
				} else {
					splitted[1] = splitted[1].replaceFirst("\"", "");

					if (splitted.length != 2) { //Either the file contains a space, or the expression is malformed.
						for (int i = 0; i < splitted.length; ++i) {
							if (i != splitted.length - 1 && splitted[i].contains("\"")) //If a quote is found that is not the last character, error
								throw new ParsingException("Error: Unexpected quote at " + file_path + ":" + line_number + " (" + original_line + ")");
							else if (i == splitted.length - 1 && splitted[i].charAt(splitted[i].length()) != '"') //If the last character is not a quote, error
								throw new ParsingException("Error: Expected quote at " + file_path + ":" + line_number + " (" + original_line + ")");
							else if (i == splitted.length - 1 && splitted[i].charAt(splitted[i].length()) == '"') //Remove the last quote
								splitted[i] = splitted[i].substring(0, splitted[i].length() - 1);
							else if (splitted[i].contains("\"")) //If we have a quote at that point, it is one quote too many
								throw new ParsingException("Error: Unexpected quote at " + file_path + ":" + line_number + " (" + original_line + ")");

							file += splitted[i];
						}
					} else {
						if (splitted[1].charAt(splitted[1].length()) != '"')
							throw new ParsingException("Error: Expected quote at " + file_path + ":" + line_number + " (" + original_line + ")");

						file += splitted[1].substring(0, splitted[1].length() - 1);

						if (splitted[1].contains("\""))
							throw new ParsingException("Error: Unexpected quote at " + file_path + ":" + line_number + " (" + original_line + ")");
					}
				}

				SourceAggregator sourceAggregator = new SourceAggregator(file);
				sourceManager.addSource(sourceAggregator.getSourceManager());
			} else {
				//Change :label to LBL label. We treat labels as an opcode because it's easier.
				Pattern p = Pattern.compile(":([A-Za-z0-9_]+)( |\\n|$|\\t)");
				Matcher m = p.matcher(line);
				while(m.find()) {
					sourceManager.addLine(".LBL " + m.group(1), file_path, line_number);
				}

				line = line.replaceAll(":([A-Za-z0-9_]+)( |\\n|$|\\t)", "");

				sourceManager.addLine(line, file_path, line_number);
			}
		}

		return sourceManager;
	}
}
