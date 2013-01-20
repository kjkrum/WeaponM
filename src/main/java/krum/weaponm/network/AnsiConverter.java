package krum.weaponm.network;

/**
 * Converts a character sequence containing ANSI escape codes into a format
 * suitable for use as a lexer pattern.  Used for developing the the lexer
 * rules. 
 */
public class AnsiConverter {
	
	protected static final StringBuilder sb = new StringBuilder();

	public static String convert(CharSequence seq, int off, int len) {
		sb.setLength(0);
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if (c == '[' || c == ']' || c == '?' || c == '.' || c == '(' || c == ')') {
				sb.append('\\');
				sb.append(c);
			}
			else if(c == 10 || c == 13) {
				sb.append("&#");
				sb.append((int) c);
				sb.append(';');
				sb.append(c);
			}
			else if (c == '<') {
				sb.append("\\&lt;");
			}
			else if(c >= 32 && c <= 125) {
				sb.append(c);
			}
			else {
				sb.append("&#");
				sb.append((int) c);
				sb.append(';');
			}
		}
		return sb.toString();
	}

}
