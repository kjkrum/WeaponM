package krum.weaponm.gui;

/**
 * For passing values between threads.
 */
public class MutableInteger {

	public int value;
	
	public MutableInteger(int value) {
		this.value = value;
	}
	
	public MutableInteger() {
		this(0);
	}

}
