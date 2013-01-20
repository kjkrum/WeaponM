package krum.weaponm.database;

import java.io.Serializable;
import java.util.Date;

public class Alien implements Serializable, Constants {
	private static final long serialVersionUID = -7229512635982251963L;
	
	private volatile String name;
	private volatile String rank;
	private final Ship ship = new Ship();
	private volatile Date observed = new Date(); 

	/**
	 * Returns this alien's name.
	 */
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns this alien's rank.
	 */
	public String getRank() {
		return rank;
	}

	void setRank(String rank) {
		this.rank = rank;
	}

	/**
	 * Returns this alien's ship.
	 */
	public Ship getShip() {
		return ship;
	}
	
	/**
	 * Returns this alien's last known location.
	 */
	public Sector getSector() {
		return ship.getSector();
	}
	
	void setSector(Sector sector) {
		ship.setSector(sector);
	}
	
	/**
	 * Returns the date when this alien was last observed.
	 */
	public Date getObserved() {
		return observed;
	}
	
	void setObserved(Date timestamp) {
		this.observed = timestamp;
	}

}
