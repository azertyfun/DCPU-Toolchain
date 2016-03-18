package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.arguments.ArgumentOptimizable;
import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;

import java.util.HashMap;
import java.util.LinkedList;

public class FinalOptimizer {

	private LinkedList<Token> tokens;

	private HashMap<String, Character> labelToValue = new HashMap<>();

	private boolean optimizeShortLiterals;

	public FinalOptimizer(LinkedList<Token> tokens, boolean optimizeShortLiterals) {
		this.tokens = tokens;
		this.optimizeShortLiterals = optimizeShortLiterals;
	}

	public void optimize() throws ParsingException {
		for(int i = 0; i < 0x40; ++i) {
			//First, let's replace labels by literals.
			char currentWord = 0;
			labelToValue.clear();
			for (Token token : tokens) {
				if(token.isLabel())
					labelToValue.put(token.getLabel().toUpperCase(), currentWord);
				else {
					currentWord++;

					if (!token.isDat() && !token.isSpecial() && token.getB().hasNextWordValue())
						currentWord++;
					if (!token.isDat() && token.getA().hasNextWordValue())
						currentWord++;
				}
			}
			for (Token token : tokens) {
				if (!token.isDat() && !token.isLabel()) {
					token.getA().makeValueLiteral(labelToValue);
					token.getB().makeValueLiteral(labelToValue);
				} else if(token.isDat()) {
					token.makeDatValueLiteral(labelToValue);
				}
			}
			if(optimizeShortLiterals) {
				for (int j = 0; j < i && j < tokens.size(); ++j) {
					Token token = tokens.get(j);

					if (token.getA() instanceof ArgumentOptimizable) {
						((ArgumentOptimizable) token.getA()).optimizeLiteral();
					}
				}
			}
		}
	}

	public HashMap<String, Character> getListing() {
		return labelToValue;
	}

	public String dumpListing() {
		String listing = "";

		for(String key : labelToValue.keySet())
			listing += key + ": 0x" + Integer.toHexString(labelToValue.get(key)) + "\n";

		return listing;
	}
}
