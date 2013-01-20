package krum.weaponm.database;

import java.io.Serializable;

/**
 * Represents a ternary value.
 */
public class Ternary implements Serializable, Comparable<Ternary> {
	private static final long serialVersionUID = 1L;
	
	public static final Ternary TRUE = new Ternary();
	public static final Ternary FALSE = new Ternary();
	public static final Ternary UNKNOWN = new Ternary();
	
	private Ternary() { }
	
	/**
	 * Behaves as if <tt>TRUE > UNKNOWN > FALSE</tt>.
	 */
	@Override
	public int compareTo(Ternary other) {
		if(this == other) return 0;
		if(this == TRUE) return 1;
		if(this == FALSE) return -1;
		return other == TRUE ? -1 : 1;
	}
}
