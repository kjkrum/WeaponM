package krum.weaponm.database;

import java.io.Serializable;

public class ShipType implements Serializable, Constants {
	private static final long serialVersionUID = 1L;

	private final String name;
	private volatile int initialHolds = UNKNOWN;
	private volatile int maxHolds = UNKNOWN;
	private volatile int fighters = UNKNOWN;
	private volatile int shields = UNKNOWN;
	private volatile int turnsPerWarp = UNKNOWN;
	private volatile float offensiveOdds = UNKNOWN;
	private volatile float defensiveOdds = UNKNOWN;
	private volatile int fighterWave = UNKNOWN;
	private volatile int transportRange = UNKNOWN;
	private volatile int longRangeScan = UNKNOWN;
	private volatile Ternary combatScan = Ternary.UNKNOWN;
	private volatile Ternary planetScan = Ternary.UNKNOWN;
	private volatile int transWarpLevel = UNKNOWN;
	private volatile Ternary interdictor = Ternary.UNKNOWN;
	private volatile Ternary landable = Ternary.UNKNOWN;
	private volatile Ternary guardianBonus = Ternary.UNKNOWN;
	private volatile Ternary fedOnly = Ternary.UNKNOWN;
	private volatile Ternary corpOnly = Ternary.UNKNOWN;
	private volatile Ternary ceoOnly = Ternary.UNKNOWN;
	private volatile Ternary pod = Ternary.UNKNOWN;
	private volatile int price = UNKNOWN;
	private volatile int mines = UNKNOWN;
	private volatile int photons = UNKNOWN;
	private volatile int genTorps = UNKNOWN;
	private volatile int atomics = UNKNOWN;
	private volatile int cloaks = UNKNOWN;
	private volatile int probes = UNKNOWN;
	private volatile int disruptors = UNKNOWN;
	private volatile int corbomite = UNKNOWN;
	private volatile int beacons = UNKNOWN;
	
	ShipType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public int getInitialHolds() {
		return initialHolds;
	}

	void setInitialHolds(int initialHolds) {
		this.initialHolds = initialHolds;
	}

	public int getMaxHolds() {
		return maxHolds;
	}

	void setMaxHolds(int maxHolds) {
		this.maxHolds = maxHolds;
	}

	public int getFighters() {
		return fighters;
	}

	void setFighters(int fighters) {
		this.fighters = fighters;
	}

	public int getShields() {
		return shields;
	}

	void setShields(int shields) {
		this.shields = shields;
	}

	public int getTurnsPerWarp() {
		return turnsPerWarp;
	}

	void setTurnsPerWarp(int turnsPerWarp) {
		this.turnsPerWarp = turnsPerWarp;
	}

	public float getOffensiveOdds() {
		return offensiveOdds;
	}

	void setOffensiveOdds(float offensiveOdds) {
		this.offensiveOdds = offensiveOdds;
	}

	public float getDefensiveOdds() {
		return defensiveOdds;
	}

	void setDefensiveOdds(float defensiveOdds) {
		this.defensiveOdds = defensiveOdds;
	}

	public int getFighterWave() {
		return fighterWave;
	}

	void setFighterWave(int fighterWave) {
		this.fighterWave = fighterWave;
	}

	public int getTransportRange() {
		return transportRange;
	}

	void setTransportRange(int transportRange) {
		this.transportRange = transportRange;
	}
	
	public int getLongRangeScan() {
		return longRangeScan;
	}
	
	void setLongRangeScan(int longRangeScan) {
		this.longRangeScan = longRangeScan;
	}

	public Ternary hasCombatScan() {
		return combatScan;
	}

	void setCombatScan(Ternary combatScan) {
		this.combatScan = combatScan;
	}

	public Ternary hasPlanetScan() {
		return planetScan;
	}

	void setPlanetScan(Ternary planetScan) {
		this.planetScan = planetScan;
	}

	/**
	 * Currently, if ships can equip any TransWarp drive, they can equip both
	 * types.  However, this capability is represented as an <tt>int</tt>
	 * instead of a <tt>Ternary</tt> in anticipation of an option to restrict
	 * TransWarp-capable ships to Type I drives.
	 */
	public int getTransWarpLevel() {
		return transWarpLevel;
	}

	void setTransWarpLevel(int transWarpLevel) {
		if(this.transWarpLevel < transWarpLevel) this.transWarpLevel = transWarpLevel;
	}

	public Ternary hasInterdictor() {
		return interdictor;
	}

	void setInterdictor(Ternary interdictor) {
		this.interdictor = interdictor;
	}

	public Ternary isLandable() {
		return landable;
	}

	void setLandable(Ternary landable) {
		this.landable = landable;
	}

	public Ternary hasGuardianBonus() {
		return guardianBonus;
	}

	void setGuardianBonus(Ternary guardianBonus) {
		this.guardianBonus = guardianBonus;
	}

	public Ternary isFedOnly() {
		return fedOnly;
	}

	void setFedOnly(Ternary commissionOnly) {
		this.fedOnly = commissionOnly;
	}
	
	public Ternary isCorpOnly() {
		return corpOnly;
	}
	
	public void setCorpOnly(Ternary corpOnly) {
		this.corpOnly = corpOnly;
	}

	public Ternary isCeoOnly() {
		return ceoOnly;
	}

	void setCeoOnly(Ternary ceoOnly) {
		this.ceoOnly = ceoOnly;
	}

	public Ternary hasPod() {
		return pod;
	}

	void setPod(Ternary hasPod) {
		this.pod = hasPod;
	}

	public int getPrice() {
		return price;
	}

	void setPrice(int price) {
		this.price = price;
	}

	public int getMines() {
		return mines;
	}

	void setMines(int mines) {
		this.mines = mines;
	}

	public int getPhotons() {
		return photons;
	}

	void setPhotons(int photons) {
		if(this.photons < photons) this.photons = photons;
	}

	public int getGenTorps() {
		return genTorps;
	}

	void setGenTorps(int genTorps) {
		this.genTorps = genTorps;
	}

	public int getAtomics() {
		return atomics;
	}

	void setAtomics(int atomics) {
		this.atomics = atomics;
	}

	public int getCloaks() {
		return cloaks;
	}

	void setCloaks(int cloaks) {
		this.cloaks = cloaks;
	}

	public int getProbes() {
		return probes;
	}

	void setProbes(int probes) {
		this.probes = probes;
	}

	public int getDisruptors() {
		return disruptors;
	}

	void setDisruptors(int disruptors) {
		this.disruptors = disruptors;
	}

	public int getCorbomite() {
		return corbomite;
	}

	void setCorbomite(int corbomite) {
		this.corbomite = corbomite;
	}

	public int getBeacons() {
		return beacons;
	}

	void setBeacons(int beacons) {
		this.beacons = beacons;
	}	
	
	@Override
	public String toString() {
		return name;
	}
}
