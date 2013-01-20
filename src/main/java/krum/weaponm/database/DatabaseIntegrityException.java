package krum.weaponm.database;

/**
 * Thrown when something tries to update the database with invalid data.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
class DatabaseIntegrityException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatabaseIntegrityException() {
		super();
	}

	public DatabaseIntegrityException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseIntegrityException(String message) {
		super(message);
	}

	public DatabaseIntegrityException(Throwable cause) {
		super(cause);
	}

	
}
