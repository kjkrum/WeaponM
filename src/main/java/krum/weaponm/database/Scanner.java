package krum.weaponm.database;

/**
 * Long range scanner types.  They are represented as <tt>int</tt> constants
 * instead of as an enum to allow comparisons with inequality operators.
 */
public interface Scanner {
	public static final int UNKNOWN = -1;
	public static final int NONE = 0;
	public static final int DENSITY = 1;
	public static final int HOLOGRAPHIC = 2;
}
