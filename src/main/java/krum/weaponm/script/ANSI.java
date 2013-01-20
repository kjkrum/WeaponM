package krum.weaponm.script;

/**
 * A utility class for producing ANSI strings that can be printed to the
 * terminal.  The constants defined in this class correspond to the values
 * recognized by the emulation parser.
 */
public class ANSI {
	public static final int RESET = 0;
	public static final int BRIGHT = 1;
	public static final int DIM = 2;
	public static final int UNDERLINE = 4;
	public static final int BLINK = 5;
	public static final int INVERSE = 7;
	
	public static final int FG_BLACK = 30;
	public static final int FG_RED = 31;
	public static final int FG_GREEN = 32;
	public static final int FG_YELLOW = 33;
	public static final int FG_BLUE = 34;
	public static final int FG_MAGENTA = 35;
	public static final int FG_CYAN = 36;
	public static final int FG_WHITE = 37;
	
	public static final int BG_BLACK = 40;
	public static final int BG_RED = 41;
	public static final int BG_GREEN = 42;
	public static final int BG_YELLOW = 43;
	public static final int BG_BLUE = 44;
	public static final int BG_MAGENTA = 45;
	public static final int BG_CYAN = 46;
	public static final int BG_WHITE = 47;
	
	public static final char UP = 'A';
	public static final char DOWN = 'B';
	public static final char RIGHT = 'C';
	public static final char LEFT = 'D';
	
	private ANSI() {}
	
	/**
	 * Produces a string that changes the text attributes.
	 * 
	 * @param attributes one or more of the color and attribute constants defined in this class
	 */
	public static String setText(int... attributes) {
		for(int a : attributes) {
			if(!isValidAttribute(a)) throw new IllegalArgumentException("invalid attribute: " + a);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("\033[");
		if(attributes.length == 0) {
			sb.append('0');
		}
		else {
			sb.append(attributes[0]);
			for(int i = 1; i < attributes.length; ++i) {
				sb.append(';');
				sb.append(attributes[i]);
			}
		}
		sb.append('m');
		return sb.toString();
	}
	
	private static boolean isValidAttribute(int a) {
		if(a == 0) return true;
		if(a == 1) return true;
		if(a == 2) return true;
		if(a == 4) return true;
		if(a == 5) return true;
		if(a == 7) return true;
		if(a >= 30 && a <= 37) return true;
		if(a >= 40 && a <= 47) return true;
		return false;
	}
	
	/**
	 * Produces a string that resets the text attributes.
	 */
	public static String setText() {
		return "\033[0m";
	}
	
	/**
	 * Produces a string that moves the cursor.
	 * 
	 * @param direction one of the direction constants defined in this class
	 * @param distance the number of rows or columns
	 */
	public static String moveCursor(char direction, int distance) {
		if(distance < 0) throw new IllegalArgumentException("invalid distance: " + distance);
		if(direction < 'A' || direction > 'D') throw new IllegalArgumentException("invalid direction: " + direction);
		return "\033[" + distance + direction;
	}
}
