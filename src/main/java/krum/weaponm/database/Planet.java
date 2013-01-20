package krum.weaponm.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Planet implements Serializable, Constants {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final PlanetType type;
	private volatile int number = UNKNOWN;
	private volatile Sector sector;
	private volatile Date timestamp;
	private volatile boolean missing;
	private final int[] populations = new int[3];
	private final int[] products = new int[3];
	private volatile int fighters;
	private volatile Owner owner;
	private volatile int citadelLevel;
	private volatile int constructionDays;
	private volatile int milReact;
	private volatile int quasarAtmos;
	private volatile int quasarSector;
	private volatile boolean interdictOn;
	private volatile int bwarpRange;

	
	Planet(String name, PlanetType type, Sector sector) {
		this.name = name;
		this.type = type;
		this.sector = sector;
	}
	
	/** 
	 * Returns the planet's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the planet number, or <tt>UNKNOWN</tt>.
	 */
	public int getNumber() {
		return number;
	}
	
	void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Returns the planet's type.
	 */
	public PlanetType getType() {
		return type;
	}
	
	/**
	 * Returns the last known location of this planet. 
	 */
	public Sector getSector() {
		return sector;
	}
	
	void setSector(Sector sector) {
		this.sector = sector;
	}
	
	/**
	 * A planet is "missing" if it is found to no longer be in its last known
	 * location.  Identifying planets is tricky, so a planet is not removed
	 * from the database unless it is positively known to have been destroyed
	 * or it is discovered to be a duplicate entry.
	 */
	public boolean isMissing() {
		return missing;
	}
	
	void setMissing(boolean missing) {
		this.missing = missing;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getFighters() {
		return fighters;
	}

	void setFighters(int fighters) {
		this.fighters = fighters;
	}

	public Owner getOwner() {
		return owner;
	}

	void setOwner(Owner owner) {
		this.owner = owner;
	}

	public int getCitadelLevel() {
		return citadelLevel;
	}

	void setCitadelLevel(int citadelLevel) {
		this.citadelLevel = citadelLevel;
	}

	public int getConstructionDays() {
		return constructionDays;
	}

	void setConstructionDays(int constructionDays) {
		this.constructionDays = constructionDays;
	}

	public int getMilReact() {
		return milReact;
	}

	void setMilReact(int milReact) {
		this.milReact = milReact;
	}

	public int getQuasarAtmos() {
		return quasarAtmos;
	}

	void setQuasarAtmos(int quasarAtmos) {
		this.quasarAtmos = quasarAtmos;
	}

	public int getQuasarSector() {
		return quasarSector;
	}

	void setQuasarSector(int quasarSector) {
		this.quasarSector = quasarSector;
	}

	public boolean isInterdictOn() {
		return interdictOn;
	}

	void setInterdictOn(boolean interdictOn) {
		this.interdictOn = interdictOn;
	}

	public int getBwarpRange() {
		return bwarpRange;
	}

	void setBwarpRange(int bwarpRange) {
		this.bwarpRange = bwarpRange;
	}

	synchronized public int[] getPopulations() {
		return Arrays.copyOf(populations, populations.length);
	}
	
	synchronized public int getPopulation(int product) {
		return populations[product];
	}
	
	synchronized void setPopulation(int product, int population) {
		populations[product] = population;
	}

	synchronized public int[] getProducts() {
		return Arrays.copyOf(products, products.length);
	}
	
	synchronized public int getProduct(int product) {
		return products[product];
	}
	
	synchronized public void setProduct(int product, int inventory) {
		products[product] = inventory;
	}

	synchronized private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	
	synchronized private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
}
