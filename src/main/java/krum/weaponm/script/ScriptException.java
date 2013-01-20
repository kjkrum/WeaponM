package krum.weaponm.script;

public class ScriptException extends Exception {
	private static final long serialVersionUID = 1L;

	public ScriptException() {
		super();
	}

	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(Throwable cause) {
		super(cause);
	}
	
}
