package krum.weaponm.database;

import java.io.Serializable;
import java.util.Date;

public class Trader implements Owner, Serializable, Constants {
	private static final long serialVersionUID = 1L;
	
	private volatile String name;
	private volatile String rank;
	private volatile int xp;
	private volatile int align;
	private volatile Corporation corp;
	private volatile Ship ship = new Ship();
	private final Date firstObserved = new Date();
	private volatile Date lastObserved;
	private volatile boolean shipDestroyed;

	/**
	 * Gets this trader's name.
	 */
	@Override
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets this trader's rank name.
	 */
	public String getRank() {
		return rank;
	}

	void setRank(String rank) {
		this.rank = rank;
	}

	/**
	 * Gets this trader's experience points.
	 */
	public int getXp() {
		return xp;
	}

	void setXp(int xp) {
		this.xp = xp;
	}

	/**
	 * Gets this trader's alignment.
	 */
	public int getAlign() {
		return align;
	}

	void setAlign(int align) {
		this.align = align;
	}
	
	public boolean isFedsafe() {
		return xp < 1000 && align >= 0;
	}
	
	/**
	 * Returns this trader's location.  Actually a shortcut for
	 * <tt>getShip().getSector()</tt>.
	 */
	public Sector getSector() {
		if(ship == null) return null;
		else return ship.getSector();
	}
	
	void setSector(Sector sector) {
		if(ship == null) ship = new Ship();
		ship.setSector(sector);
	}

	/**
	 * Returns this trader's corporation, or null if they have none.
	 */
	public Corporation getCorp() {
		return corp;
	}

	void setCorp(Corporation corp) {
		this.corp = corp;
	}
	
	/**
	 * Returns this trader's ship.
	 */
	public Ship getShip() {
		return ship;
	}
	
	void setShip(Ship ship) {
		this.ship = ship;
	}
	
	/**
	 * True if this trader's ship is destroyed.
	 */
	public boolean isShipDestroyed() {
		return shipDestroyed;
	}
	
	void setShipDestroyed(boolean shipDestroyed) {
		this.shipDestroyed = shipDestroyed;
		// TODO: null or replace ship?
	}

	/**
	 * The date this trader was first observed.
	 */
	public Date getFirstObserved() {
		return firstObserved;
	}
	
	/**
	 * The date this trader was last observed.
	 */
	public Date getLastObserved() {
		return lastObserved;
	}
	
	void setLastObserved(Date lastObserved) {
		this.lastObserved = lastObserved;
	}
	
	@Override
	public String toString() {
		return rank + ' ' + name;
	}
}
