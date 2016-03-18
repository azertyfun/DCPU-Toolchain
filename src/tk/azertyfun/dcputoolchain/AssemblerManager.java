package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.assembler.*;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class AssemblerManager {

	private boolean big_endian = true, optimize_shortLiterals = true;
	private String inputFile_path;
	private String outputFile_path;

	public AssemblerManager(String[] args) {
		inputFile_path = args[1];
		outputFile_path = args[2];

		if(args.length > 3) {
			for(int i = 3; i < args.length; ++i) {
				if(args[i].equalsIgnoreCase("--little-endian")) {
					big_endian = false;
				} else if(args[i].equalsIgnoreCase("--disable-shortLiterals"))
					optimize_shortLiterals = false;
			}
		}
	}

	public boolean assemble() {
		SourceAggregator sourceAggregator = new SourceAggregator(inputFile_path);
		try {
			System.out.print("Aggregating source... ");
			long start = System.currentTimeMillis();

			SourceManager sourceManager = sourceAggregator.getSourceManager();

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");


			System.out.print("Sanitizing code... ");
			start = System.currentTimeMillis();

			SourceSanitizer sourceSanitizer = new SourceSanitizer(sourceManager);
			sourceSanitizer.sanitize();

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");


			System.out.print("Preprocessing code... ");
			start = System.currentTimeMillis();

			Preprocessor preprocessor = new Preprocessor(sourceManager);
			preprocessor.processDefines();


			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");


			System.out.print("Aggregating labels... ");
			start = System.currentTimeMillis();

			LabelAggregator labelAggregator = new LabelAggregator(sourceManager);
			labelAggregator.aggregateLabels();

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");


			System.out.print("Tokenizing... ");
			start = System.currentTimeMillis();

			Tokenizer tokenizer = new Tokenizer(sourceManager, optimize_shortLiterals);
			LinkedList<Token> tokens = tokenizer.tokenize();

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");


			System.out.print("Optimizing... ");
			start = System.currentTimeMillis();

			FinalOptimizer finalOptimizer = new FinalOptimizer(tokens, optimize_shortLiterals);
			finalOptimizer.optimize();

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");

			System.out.print("Assembling... ");
			start = System.currentTimeMillis();

			Assembler assembler = new Assembler(tokens);
			LinkedList<Byte> bytes = assembler.assemble(big_endian);

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");

			byte[] bytes_array = new byte[bytes.size()];
			for(int i = 0; i < bytes.size(); ++i) {
				bytes_array[i] = bytes.get(i);
			}

			try {
				FileOutputStream fos = new FileOutputStream(outputFile_path);
				fos.write(bytes_array);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ParsingException e) {
			System.err.println("Parsing error.\n\t" + e.getMessage());
			return false;
		}

		return true;
	}
}
