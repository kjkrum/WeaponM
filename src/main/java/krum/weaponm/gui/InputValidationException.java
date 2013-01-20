package krum.weaponm.gui;

/**
 * Thrown by various dialog input validators.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
public class InputValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public InputValidationException() {
		super();
	}

	public InputValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InputValidationException(String message) {
		super(message);
	}

	public InputValidationException(Throwable cause) {
		super(cause);
	}	
}
