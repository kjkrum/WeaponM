package krum.weaponm.script;

import java.io.IOException;

/**
 * Thrown when a script tries to write to the network while it is locked by
 * another script.  Scripts can explicitly lock the network before sending a
 * burst of commands.  The network is also locked when a script sends a
 * command in response to an event, and unlocked after the event has been
 * processed by all scripts.
 */
public class NetworkLockedException extends IOException {
	private static final long serialVersionUID = 1L;

	public NetworkLockedException() {
		
	}

	public NetworkLockedException(String message) {
		super(message);
	}
}
