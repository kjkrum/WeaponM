package krum.weaponm.database;

/** Fighter mode constants. */
public enum FighterMode {
	DEFENSIVE,
	OFFENSIVE,
	TOLL,
	UNKNOWN;
	
	/**
	 * @param c 'D', 'O', or 'T'
	 */
	static FighterMode getMode(char c) {
		switch (c) {
		case 'D': return DEFENSIVE;
		case 'O': return OFFENSIVE;
		case 'T': return TOLL;
		default: throw new IllegalArgumentException("Invalid fighter mode: '" + c + '\'');
		}
	}
}
