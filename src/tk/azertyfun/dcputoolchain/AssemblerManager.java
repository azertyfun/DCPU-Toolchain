package tk.azertyfun.dcputoolchain;

import tk.azertyfun.dcputoolchain.assembler.*;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class AssemblerManager {

	private boolean little_endian = true, optimize_shortLiterals = true;
	private String inputFile_path;
	private String outputFile_path;
	private boolean bootloader = false;
	private boolean bootloader_little_endian = true;
	private char[] bootloader_data;

	public AssemblerManager(String[] args) {
		inputFile_path = args[1];
		outputFile_path = args[2];

		if(args.length > 3) {
			for(int i = 3; i < args.length; ++i) {
				if(args[i].equalsIgnoreCase("--big-endian")) {
					little_endian = false;
				} else if(args[i].equalsIgnoreCase("--disable-shortLiterals"))
					optimize_shortLiterals = false;
			}
		}
	}

	public AssemblerManager(String[] args, char[] bootloader_data, boolean bootloader_little_endian) {
		inputFile_path = args[1];
		outputFile_path = args[2];

		if(args.length > 3) {
			for(int i = 3; i < args.length; ++i) {
				if(args[i].equalsIgnoreCase("--big-endian")) {
					little_endian = false;
				} else if(args[i].equalsIgnoreCase("--disable-shortLiterals"))
					optimize_shortLiterals = false;
			}
		}

		bootloader = true;
		this.bootloader_little_endian = bootloader_little_endian;
		this.bootloader_data = bootloader_data;
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
			LinkedList<Byte> bytes = assembler.assemble(little_endian);

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");

			System.out.print("Replacing magic numbers... ");
			start = System.currentTimeMillis();

			for(Character location : sourceManager.getMagic().keySet()) {
				if(location + 1 > bytes.size() / 2) { // The bytes list is not as long as the ram itself, so if a magic word is outside it we need to biggen it
					for(int i = bytes.size(); i < location * 2 + 2; ++i) {
						bytes.add((byte) 0);
					}
				}

				char magic = sourceManager.getMagic().get(location);
				System.out.println("Writing " + Integer.toHexString(magic) + " to " + Integer.toHexString(location));

				if(little_endian) {
					bytes.set(location * 2, (byte) (magic & 0xFF));
					bytes.set(location * 2 + 1, (byte) ((magic >> 8) & 0xFF));
				} else {
					bytes.set(location * 2, (byte) ((magic >> 8) & 0xFF));
					bytes.set(location * 2 + 1, (byte) (magic & 0xFF));
				}
			}

			System.out.println("Done in " + (System.currentTimeMillis() - start) + " ms.");

			byte[] bytes_array;
			if(bootloader) {
				System.out.println("Appending bootloader.");
				bytes_array = new byte[bytes.size() + 1024];
				for (int i = 0; i < 512; ++i) {
					if(bootloader_little_endian) {
						bytes_array[i * 2] = (byte) (bootloader_data[i] & 0xFF);
						bytes_array[i * 2 + 1] = (byte) ((bootloader_data[i] >> 8) & 0xFF);
					} else {
						bytes_array[i * 2] = (byte) ((bootloader_data[i] >> 8) & 0xFF);
						bytes_array[i * 2 + 1] = (byte) (bootloader_data[i] & 0xFF);
					}
				}
				for (int i = 1024; i < bytes.size() + 1024; ++i) {
					bytes_array[i] = bytes.get(i - 1024);
				}
			} else {
				bytes_array = new byte[bytes.size()];
				for (int i = 0; i < bytes.size(); ++i) {
					bytes_array[i] = bytes.get(i);
				}
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
