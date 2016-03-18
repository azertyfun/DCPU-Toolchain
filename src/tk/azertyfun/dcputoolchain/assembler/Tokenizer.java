package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.arguments.*;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.Line;
import tk.azertyfun.dcputoolchain.assembler.sourceManagement.SourceManager;

import java.util.LinkedList;

public class Tokenizer {

	private SourceManager sourceManager;
	private boolean optimizeShortLiterals;

	public Tokenizer(SourceManager sourceManager, boolean optimizeShortLiterals) {
		this.sourceManager = sourceManager;
		this.optimizeShortLiterals = optimizeShortLiterals;
	}

	public LinkedList<Token> tokenize() throws ParsingException {
		LinkedList<Token> tokens = new LinkedList<>();
		for(Line line : sourceManager.getLines()) {
			String[] splitted = line.getLine().split(" ");

			String token = splitted[0].toUpperCase();
			if(Token.Opcodes.strings.containsKey(token)) {
				if(splitted.length != 2)
					throw new ParsingException("Error: " + (splitted.length < 2 ? "Not enough" : "Too many") + " arguments: \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());

				String[] arguments = splitted[1].split(",");
				if(arguments.length != 2)
					throw new ParsingException("Error: " + (arguments.length < 2 ? "Not enough" : "Too many") + " arguments: \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());

				tokens.add(new Token(Token.Opcodes.strings.get(token), false, getArgumentB(arguments[0], line), getArgumentA(arguments[1], line)));
			} else if(Token.SpecialOpcodes.strings.containsKey(token)) {
				if(splitted.length != 2)
					throw new ParsingException("Error: " + (splitted.length < 2 ? "Not enough" : "Too many") + " arguments: \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());

				tokens.add(new Token(Token.SpecialOpcodes.strings.get(token), true, new ArgumentNot(), getArgumentA(splitted[1], line)));
			} else if(token.equals("DAT") || token.equals(".DAT") || token.equals("#DAT")) {
				LinkedList<Value> values = new LinkedList<>();

				if(splitted.length != 2) { //DAT 0 1 2 3 syntax instead of DAT 0, 1, 2, 3
					for(int k = 1; k < splitted.length; ++k) {
						String argument = splitted[k];
						if(argument.matches("'.'")) {
							values.add(new Value(argument.charAt(1)));
						} else {
							boolean isLabel = false;
							for(String label : sourceManager.getS_labels()) {
								if(label.equalsIgnoreCase(argument)) {
									isLabel = true;
									break;
								}
							}

							if(isLabel)
								values.add(new Value(argument));
							else
								values.add(new Value(Parser.parseNumber(argument, line)));
						}
					}
				} else {
					String[] arguments = splitted[1].split(",");
					for(String argument : arguments) {
						if(argument.matches("'.'")) {
							values.add(new Value(argument.charAt(1)));
						} else {
							boolean isLabel = false;
							for(String label : sourceManager.getS_labels()) {
								if(label.equalsIgnoreCase(argument)) {
									isLabel = true;
									break;
								}
							}

							if(isLabel)
								values.add(new Value(argument));
							else
								values.add(new Value(Parser.parseNumber(argument, line)));
						}
					}
				}

				for(Value value: values)
					tokens.add(new Token(value));
			} else if(token.equals(".LBL") || token.equals("#LBL")) {
				if(splitted.length != 2)
					throw new ParsingException("Error: " + (splitted.length < 2 ? "Not enough" : "Too many") + " arguments: \"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
				tokens.add(new Token(splitted[1]));
			} else {
				throw new ParsingException("Error: Unkown opcode \"" + splitted[0] + "\": " + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
			}
		}

		return tokens;
	}

	private Argument getArgumentB(String b, Line line) throws ParsingException {
		if(b.charAt(0) >= '0' && b.charAt(0) <= '9') //B is a number
			return new ArgumentNextWordLiteral(b, line, sourceManager.getS_labels(), false);
		else if(b.length() == 1) //B is a register
			return new ArgumentRegister(b, line);
		else if(b.equalsIgnoreCase("SP"))
			return new ArgumentSP();
		else if(b.equalsIgnoreCase("PC"))
			return new ArgumentPC();
		else if(b.equalsIgnoreCase("EX"))
			return new ArgumentEX();
		else if(b.charAt(0) == '[') { //[register], [register + next word], [SP] (PEEK), [SP + next word] (PICK n) or [next word]
			if (b.length() < 3)
				throw new ParsingException("Error: Can't parse argument: \"" + b + "\"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
			if (b.charAt(2) == ']') { //[register]
				return new ArgumentAddressOfRegister(b, line);
			} else if (b.contains("+") && !b.contains("SP")) { //[register + next word]
				return new ArgumentAddressOfRegisterPlusNextWord(b, line, sourceManager.getS_labels());
			} else if (b.equalsIgnoreCase("[SP]") || b.equalsIgnoreCase("PEEK")) { //[SP] (PEEK)
				return new ArgumentPeek();
			} else if (b.substring(0, 3).equalsIgnoreCase("[SP")) {
				return new ArgumentPick(b, line, sourceManager.getS_labels());
			} else { //[next word]
				return new ArgumentAddressOfNextWord(b, line, sourceManager.getS_labels(), false);
			}
		} else if(b.equalsIgnoreCase("PUSH")) {
			return new ArgumentPush();
		} else if(b.equalsIgnoreCase("PEEK")) {
			return new ArgumentPeek();
		} else if(b.matches("'.'")) { //Character literal
			return new ArgumentLiteral(b.charAt(1));
		} else //next word (literal)
			return new ArgumentNextWordLiteral(b, line, sourceManager.getS_labels(), false);
	}

	private Argument getArgumentA(String a, Line line) throws ParsingException {
		if(a.charAt(0) >= '0' && a.charAt(0) <= '9') //A is a number
			return new ArgumentLiteral(a, line, sourceManager.getS_labels(), optimizeShortLiterals);
		else if(a.length() == 1) //A is a register
			return new ArgumentRegister(a, line);
		else if(a.equalsIgnoreCase("SP"))
			return new ArgumentSP();
		else if(a.equalsIgnoreCase("PC"))
			return new ArgumentPC();
		else if(a.equalsIgnoreCase("EX"))
			return new ArgumentEX();
		else if(a.charAt(0) == '[') { //[register], [register + next word], [SP] (PEEK), [SP + next word] (PICK n) or [next word]
			if(a.length() < 3)
				throw new ParsingException("Error: Can't parse argument: \"" + a + "\"" + line.getOriginal_line() + "\" in " + line.getFile() + ":" + line.getLineNumber());
			if(a.charAt(2) == ']') { //[register]
				return new ArgumentAddressOfRegister(a, line);
			} else if(a.contains("+") && !a.contains("SP")) { //[register + next word]
				return new ArgumentAddressOfRegisterPlusNextWord(a, line, sourceManager.getS_labels());
			} else if(a.equalsIgnoreCase("[SP]")) { //[SP] (PEEK)
				return new ArgumentPeek();
			} else if(a.substring(0, 3).equalsIgnoreCase("[SP")) {
				return new ArgumentPick(a, line, sourceManager.getS_labels());
			} else { //[next word]
				return new ArgumentAddressOfNextWord(a, line, sourceManager.getS_labels(), true);
			}
		} else if(a.equalsIgnoreCase("POP")) {
			return new ArgumentPop();
		} else if(a.equalsIgnoreCase("PEEK")) {
			return new ArgumentPeek();
		} else if(a.matches("'.'")) { //Character literal
			return new ArgumentLiteral(a.charAt(1));
		} else //next word (literal)
			return new ArgumentNextWordLiteral(a, line, sourceManager.getS_labels(), true);
	}
}
