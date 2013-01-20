package krum.weaponm;

import java.util.prefs.Preferences;

public class AppSettings {
	protected static final String PREFS_NODE = "krum/weaponm";
	
	protected static final String LOOK_AND_FEEL = "LookAndFeel";
	protected static final String DEFAULT_LOOK_AND_FEEL = "Nimbus";
	
	protected static final String BUFFER_LINES = "BufferLines";
	protected static final int DEFAULT_BUFFER_LINES = 10000;
	
	protected static final String SCRIPT_CLASSPATH = "ScriptClasspath";
	protected static final String DEFAULT_SCRIPT_CLASSPATH = "./scripts";
	
	protected static final String AUTO_LOAD_SCRIPTS = "AutoLoadScripts";
	protected static final String DEFAULT_AUTO_LOAD_SCRIPTS = "";
	
	protected static final String GIANT_FONT = "GiantFont";
	protected static final boolean DEFAULT_GIANT_FONT = false;
	
	//protected static final String LOG_FILE = "LogFile";
	//protected static final String DEFAULT_LOG_FILE = "WeaponM.log";
	
	protected static final Preferences prefs;
	
	private AppSettings() { }
	
	static {
		prefs = Preferences.userRoot().node(PREFS_NODE);
		
		// this otherwise pointless code ensures that the preferences node is created
		// so the user can at least edit it by hand until I write some kind of dialog
		setLookAndFeel(getLookAndFeel());
		setBufferLines(getBufferLines());
		setScriptClasspath(getScriptClasspath());
		setAutoLoadScripts(getAutoLoadScripts());
		setGiantFont(getGiantFont());
		//setLogFile(getLogFile());
	}
	
	public static String getLookAndFeel() {
		return prefs.get(LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL);
	}
	
	public static void setLookAndFeel(String name) {
		prefs.put(LOOK_AND_FEEL, name);
	}
	
	public static int getBufferLines() {
		return prefs.getInt(BUFFER_LINES, DEFAULT_BUFFER_LINES);
	}
	
	public static void setBufferLines(int lines) {
		prefs.putInt(BUFFER_LINES, lines);
	}
	
	public static String getScriptClasspath() {
		return prefs.get(SCRIPT_CLASSPATH, DEFAULT_SCRIPT_CLASSPATH);
	}
	
	public static void setScriptClasspath(String classpath) {
		prefs.put(SCRIPT_CLASSPATH, classpath);
	}
	
	public static String getAutoLoadScripts() {
		return prefs.get(AUTO_LOAD_SCRIPTS, DEFAULT_AUTO_LOAD_SCRIPTS);
	}
	
	/**
	 * 
	 * @param autoLoadScripts a comma-separated list of fully qualified class names
	 */
	public static void setAutoLoadScripts(String autoLoadScripts) {
		prefs.put(AUTO_LOAD_SCRIPTS, autoLoadScripts);
	}
	
	public static boolean getGiantFont() {
		return prefs.getBoolean(GIANT_FONT, DEFAULT_GIANT_FONT);
	}
	
	public static void setGiantFont(Boolean giantFont) {
		prefs.putBoolean(GIANT_FONT, giantFont);
	}
	
	/*
	public static String getLogFile() {
		return prefs.get(LOG_FILE, DEFAULT_LOG_FILE);
	}
	
	public static void setLogFile(String logFile) {
		prefs.put(LOG_FILE, logFile);
	}
	*/
}
