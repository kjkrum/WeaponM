package krum.weaponm.emulation;

import java.io.IOException;

import krum.jplex.UnderflowException;
import krum.weaponm.WeaponM;
import krum.weaponm.emulation.lexer.EmulationLexer;

public class Emulation {
	
	protected final EmulationLexer lexer;
	protected final EmulationParser parser;

	public Emulation(WeaponM weapon) throws IOException, ClassNotFoundException {
		lexer = new EmulationLexer();
		parser = new EmulationParser(weapon);
		lexer.addEventListener(parser);
	}
	
	public int write(CharSequence seq, int off, int len, boolean endOfInput) throws UnderflowException {
		return lexer.lex(seq, off, len, endOfInput);
	}
	
	/*
	public void reset() {
		lexer.reset();
		parser.reset();
	}
	*/
}
