package krum.weaponm.database;

import java.io.Serializable;
import java.util.Arrays;

public class PlanetType implements Serializable, Constants {
	private static final long serialVersionUID = 1L;

	private final String name;
	private final int[] maxPopulation = new int[3];
	private final int[] production = new int[3];
	private volatile int figProduction = UNKNOWN;
	private final int[] storage = new int[3];
	private volatile int maxFighters = UNKNOWN;
	private volatile int maxShields = UNKNOWN;
	private volatile int maxCitLevel = UNKNOWN;
	private final int[] constructionDays = new int[6];
	private volatile float interestRate = UNKNOWN;
	
	/**
	 * The name parameter should be of the form <tt>"M, Earth Type"</tt>.
	 * 
	 * @param name
	 */
	PlanetType(String name) {
		this.name = name;
		Arrays.fill(maxPopulation, UNKNOWN);
		Arrays.fill(production, UNKNOWN);
		Arrays.fill(storage, UNKNOWN);
		Arrays.fill(constructionDays, UNKNOWN);
	}
	
	public String getName() {
		return name;
	}
	
	synchronized public int getMaxPopulation(int product) {
		return maxPopulation[product];		
	}
	
	synchronized void setMaxPopulation(int product, int maxPop) {
		maxPopulation[product] = maxPop;
	}
	
	synchronized public int getProduction(int product) {
		return production[product];
	}
	
	synchronized void setProduction(int product, int prod) {
		production[product] = prod;
	}
	
	public int getFigProduction() {
		return figProduction;
	}
	
	void setFigProduction(int figProduction) {
		this.figProduction = figProduction;
	}
	
	synchronized public int getStorage(int product) {
		return storage[product];
	}
	
	synchronized void setStorage(int product, int qty) {
		storage[product] = qty;
	}
	
	public int getMaxFighters() {
		return maxFighters;
	}
	
	void setMaxFighters(int maxFighters) {
		this.maxFighters = maxFighters;
	}
	
	public int getMaxShields() {
		return maxShields;
	}
	
	void setMaxShields(int maxShields) {
		this.maxShields = maxShields;
	}
	
	public int getMaxCitLevel() {
		return maxCitLevel;
	}
	
	void setMaxCitLevel(int level) {
		if(maxCitLevel < level) maxCitLevel = level;
	}
	
	synchronized public int getConstructionDays(int citLevel) {
		return constructionDays[citLevel - 1];
	}
	
	synchronized void setConstructionDays(int citLevel, int days) {
		constructionDays[citLevel - 1] = days; 
	}
	
	public float getInterestRate() {
		return interestRate;
	}
	
	void setInterestRate(float rate) {
		interestRate = rate;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
