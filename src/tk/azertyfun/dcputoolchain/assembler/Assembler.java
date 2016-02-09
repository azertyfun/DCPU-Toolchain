package tk.azertyfun.dcputoolchain.assembler;

import tk.azertyfun.dcputoolchain.assembler.exceptions.ParsingException;

import java.util.LinkedList;

public class Assembler {

	private LinkedList<Token> tokens;

	public Assembler(LinkedList<Token> tokens) {
		this.tokens = tokens;
	}

	public LinkedList<Byte> assemble(boolean big_endian) throws ParsingException {
		LinkedList<Byte> bytes = new LinkedList<>();

		//Now let's create the final words!

		for(Token token : tokens) {
			if(!token.isDat() && !token.isLabel()) {
				char c = '\0';

				if (!token.isSpecial()) {
					c |= token.getOpcode() & 0b11111;
					c |= (token.getB().getValue() & 0b11111) << 5;
					c |= (token.getA().getValue() & 0b111111) << 10;
				} else {
					c |= (token.getOpcode() & 0b11111) << 5;
					c |= (token.getA().getValue() & 0b111111) << 10;
				}

				if (big_endian) {
					bytes.add((byte) ((c >> 8) & 0b11111111));
					bytes.add((byte) (c & 0b11111111));
				} else {
					bytes.add((byte) (c & 0b11111111));
					bytes.add((byte) ((c >> 8) & 0b11111111));
				}

				if (token.getA().hasNextWordValue()) {
					char nextWord = token.getA().getNextWordValue();
					if (big_endian) {
						bytes.add((byte) ((nextWord >> 8) & 0b11111111));
						bytes.add((byte) (nextWord & 0b11111111));
					} else {
						bytes.add((byte) (nextWord & 0b11111111));
						bytes.add((byte) ((nextWord >> 8) & 0b11111111));
					}
				}

				if (token.getB().hasNextWordValue()) {
					char nextWord = token.getB().getNextWordValue();
					if (big_endian) {
						bytes.add((byte) ((nextWord >> 8) & 0b11111111));
						bytes.add((byte) (nextWord & 0b11111111));
					} else {
						bytes.add((byte) (nextWord & 0b11111111));
						bytes.add((byte) ((nextWord >> 8) & 0b11111111));
					}
				}
			} else if(!token.isLabel()) {
				if (big_endian) {
					bytes.add((byte) ((token.getDatValue().getLiteral() >> 8) & 0b11111111));
					bytes.add((byte) (token.getDatValue().getLiteral() & 0b11111111));
				} else {
					bytes.add((byte) (token.getDatValue().getLiteral() & 0b11111111));
					bytes.add((byte) ((token.getDatValue().getLiteral() >> 8) & 0b11111111));
				}
			}
		}

		return bytes;
	}
}
