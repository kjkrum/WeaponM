package krum.weaponm.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ParserUtils {
	
	protected final static SimpleDateFormat shortDateFormat;
	protected final static SimpleDateFormat longDateFormat;
	protected final static StringBuilder sb = new StringBuilder();
	
	static {
		longDateFormat = new SimpleDateFormat("hh:mm:ss aa EEE MMM d, yyyy");
		shortDateFormat = new SimpleDateFormat("M/d/y");
		shortDateFormat.set2DigitYearStart(new Date());
	}
	
	/**
	 * Parses a date like "12/25/40".  The century range is set so game dates
	 * are parsed as being in the future.
	 * 
	 * @param date
	 * @return
	 */
	protected static Date parseShortDate(String date) {
		try {
			return shortDateFormat.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid date", e);
		}
	}
	
	/**
	 * Parses a date like "11:59:59 PM Mon Dec 31, 2040".
	 * 
	 * @param date
	 * @return
	 */
	protected static Date parseLongDate(String date) {
		try {
			return longDateFormat.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid date", e);
		}
	}
	
	/**
	 * Parses an integer value containing commas as thousands separators.
	 * Supports negative values.
	 * 
	 * @param thousands
	 * @return
	 */
	protected static int parseThousands(CharSequence thousands) {
		sb.setLength(0);
		for(int i = 0; i < thousands.length(); ++i) {
			if(thousands.charAt(i) != ',') {
				sb.append(thousands.charAt(i));
			}
		}
		return Integer.parseInt(sb.toString());
	}
	
	/**
	 * Finds and parses the first integer in the char sequence that isn't part
	 * of an escape sequence, starting at the specified offset.  Finds both
	 * contiguous and thousands-separated values.  Supports negative values.
	 * 
	 * @param seq
	 * @param off
	 * @return
	 * @throws ParseException 
	 */
	protected static int findInteger(CharSequence seq, int off, int len) {
		// Note: this will match oddly comma-separated patterns like 1,3243,22.  I don't think it matters.
		sb.setLength(0);
		boolean inEsc = false;
		boolean inNum = false;
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if(inNum) {
				if(Character.isDigit(c)) {
					sb.append(c);
					continue;
				}
				else if(c == ',') {
					continue;
				}
				else break;
			}
			else if(inEsc) {
				if(Character.isLetter(c)) {
					inEsc = false;
					continue;
				}
			}
			else if(c == '\033') {
				inEsc = true;
				continue;
			}
			else if(Character.isDigit(c)) {
				if(i > off && seq.charAt(i - 1) == '-') sb.append('-');
				sb.append(c);
				inNum = true;
				continue;
			}
		}
		if(!inNum) throw new IllegalArgumentException(new ParseException(seq.toString(), off));
		return Integer.parseInt(sb.toString());
	}
	
	
	/**
	 * Finds and parses the first float in the char sequence that isn't part
	 * of an escape sequence, starting at the specified offset.
	 * 
	 * @param seq
	 * @param off
	 * @return
	 * @throws ParseException 
	 */
	/*
	protected static float findFloat(CharSequence seq, int off, int len) {
		sb.setLength(0);
		boolean foundDecimal = false;
		boolean inEsc = false;
		boolean inNum = false;
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if(inNum) {
				if(Character.isDigit(c)) {
					sb.append(c);
					continue;
				}
				else if(c == '.' && !foundDecimal) {
					sb.append(c);
					foundDecimal = true;
					continue;
				}
				else break;
			}
			else if(inEsc) {
				if(Character.isLetter(c)) {
					inEsc = false;
					continue;
				}
			}
			else if(c == '\033') {
				inEsc = true;
				continue;
			}
			else if(Character.isDigit(c)) {
				sb.append(c);
				inNum = true;
				continue;
			}
		}
		if(!inNum) throw new IllegalArgumentException(new ParseException(seq.toString(), off));
		if(sb.charAt(sb.length() - 1) == '.') sb.append('0');
		return Float.parseFloat(sb.toString());
	}
	*/
	
	/**
	 * Scans a character sequence for integers that are not inside escape
	 * codes.  Finds both contiguous and thousands-separated values.  Supports
	 * negative values.
	 * 
	 * @param seq
	 * @return
	 */
	protected static List<Integer> findIntegers(CharSequence seq, int off, int len) {
		sb.setLength(0);
		ArrayList<Integer> values = new ArrayList<Integer>();
		boolean inEsc = false;
		boolean inNum = false;
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if(inNum) {
				if(Character.isDigit(c)) {
					sb.append(c);
					continue;
				}
				else if(c == ',') {
					continue;
				}
				else {
					values.add(Integer.parseInt(sb.toString()));
					sb.setLength(0);
					inNum = false;
					if(c == '\033') inEsc = true;
				}
			}
			else if(inEsc) {
				if(Character.isLetter(c)) {
					inEsc = false;
					continue;
				}
			}
			else if(c == '\033') {
				inEsc = true;
				continue;
			}
			else if(Character.isDigit(c)) {
				if(i > off && seq.charAt(i - 1) == '-') sb.append('-');
				sb.append(c);
				inNum = true;
				continue;
			}
		}
		// get last number if seq ends with number
		if(inNum) values.add(Integer.parseInt(sb.toString()));
		return values;
	}
	
	public static String findPrintable(CharSequence seq, int off, int len) {
		sb.setLength(0);
		boolean inEsc = false;
		boolean inPrintable = false;
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if(inPrintable) {
				if(c > 31 && c < 126) {
					sb.append(c);
					continue;
				}
				else if(c == ',') {
					continue;
				}
				else break;
			}
			else if(inEsc) {
				if(Character.isLetter(c)) {
					inEsc = false;
					continue;
				}
			}
			else if(c == 27) {
				inEsc = true;
				continue;
			}
			else if(c > 31 && c < 126) {
				sb.append(c);
				inPrintable = true;
				continue;
			}
		}
		if(!inPrintable) throw new IllegalArgumentException(new ParseException(seq.toString(), off));
		return sb.toString();
	}
	
	
	public static List<String> findPrintables(CharSequence seq, int off, int len) {
		sb.setLength(0);
		ArrayList<String> values = new ArrayList<String>();
		
		boolean inEsc = false;
		boolean inPrintable = false;
		for(int i = off; i < off + len; ++i) {
			char c = seq.charAt(i);
			if(inPrintable) {
				if(c > 31 && c < 126) {
					sb.append(c);
					continue;
				}
				else {
					values.add(sb.toString());
					sb.setLength(0);
					inPrintable = false;
					if(c == '\033') inEsc = true;
				}
			}
			else if(inEsc) {
				if(Character.isLetter(c)) {
					inEsc = false;
					continue;
				}
			}
			else if(c == '\033') {
				inEsc = true;
				continue;
			}
			else if(c > 31 && c < 126) {
				sb.append(c);
				inPrintable = true;
				continue;
			}
		}
		// get last printable if seq ends with printable
		if(inPrintable) values.add(sb.toString());
		
		return values;
	}
		
	public static String stripANSI(String input) {
		StringBuilder sb = new StringBuilder();
		boolean inANSI = false;
		for(int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			if(inANSI) {
				if(Character.isLetter(c)) {
					inANSI = false;
					continue;
				}
			}
			else {
				if(c == '\033') {
					inANSI = true;
					continue;
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Scans the input, removing all backspace sequences along with the
	 * preceding character that was backspaced over.  Returns the input as the
	 * game would have interpreted it.
	 */
	public static String stripBackspaces(String input) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			if(c == '\010') {
				sb.setLength(sb.length() - 1);
				i += 2;
			}
			else sb.append(c);
		}
		return sb.toString();
	}
	
	public static int[] toIntArray(List<Integer> numbers) {
		int[] ints = new int[numbers.size()];
		int i = 0;
		for(int value : numbers) {
			ints[i] = value;
			++i;
		}
		return ints;
	}
	
		private ParserUtils() { }
		
	//public static void main(String[] args) {
		//System.out.println(ParserUtils.stripANSI("\033[35mTurns left     \033[1;33m: \033[36m248"));
		//String sample = "\033[32mIncoming transmission from \033[1;36mTest Bitch With A Long Name\033[0;32m on channel \033[1;36m0\033[0;32m:\r\033[0m\n\033[1;33mSeriously, kill me now.";
		//String sample="\033[K\033[33mF \033[1;36mTest B \033[33mwoohoo";
		//List<String> strings = ParserUtils.findPrintables(sample, 0, sample.length());
		//System.out.println(strings);
		//String sample = "ABCXX\010 \010\010 \010D";
		//System.out.println(stripBackspaces(sample));
		//String sample = "5,000";
		//System.out.println(parseThousands(sample));
	//}
	
}
