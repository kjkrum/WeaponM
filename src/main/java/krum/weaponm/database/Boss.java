package krum.weaponm.database;

import java.io.Serializable;

/**
 * "Boss" is what John Pritchett calls Federals and Space Pirates.  All bosses
 * are invincible.
 */
public class Boss implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final boolean fed;
	private volatile Sector sector;
	
	Boss(String name, boolean fed) {
		this.name = name;
		this.fed = fed;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if this boss is a Federal.
	 */
	public boolean isFed() {
		return fed;
	}
	
	/**
	 * Returns this boss' last known location.
	 */
	public Sector getSector() {
		return sector;
	}
	
	void setSector(Sector sector) {
		this.sector = sector;
	}
	
}
