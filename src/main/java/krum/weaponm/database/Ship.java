package krum.weaponm.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Ship implements Serializable, Constants {
	private static final long serialVersionUID = 2808993374410741324L;
	
	private volatile ShipType type;
	private volatile Sector sector;
	private volatile String name;
	private volatile int fighters = 0;
	private volatile int shields = 0;	
	private volatile Owner owner;
	private volatile int number = UNKNOWN;
	private volatile Details details;
		
	
	public boolean hasDetails() {
		return details != null;
	}
	
	void resetDetails() {
		details = new Details();
	}
	
	public int getNumber() {
		return number;
	}

	void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public Owner getOwner() {
		return owner;
	}

	void setOwner(Owner owner) {
		this.owner = owner;
	}

	public Sector getSector() {
		return sector;
	}

	void setSector(Sector sector) {
		this.sector = sector;
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
	
	public ShipType getType() {
		return type;
	}
	
	void setType(ShipType type) {
		this.type = type;
	}
	
	public Date getBuildDate() {
		if(details == null) return null;
		else return details.buildDate;
	}

	void setBuildDate(Date buildDate) {
		if(details == null) details = new Details();
		details.buildDate = buildDate;
	}

	public int getHolds() {
		if(details == null) return 0;
		else return details.holds;
	}

	void setHolds(int holds) {
		if(details == null) details = new Details();
		details.holds = holds;
	}

	public int getTransWarp() {
		if(details == null) return 0;
		else return details.transWarp;
	}

	void setTransWarp(int transWarp) {
		if(details == null) details = new Details();
		if(details.transWarp < transWarp) details.transWarp = transWarp;
	}

	public int getLongRangeScan() {
		if(details == null) return 0;
		else return details.longRangeScan;
	}

	void setLongRangeScan(int longRangeScan) {
		if(details == null) details = new Details();
		details.longRangeScan = longRangeScan;
	}

	public boolean hasPlanetScan() {
		if(details == null) return false;
		else return details.planetScan;
	}

	void setPlanetScan(boolean planetScan) {
		if(details == null) details = new Details();
		details.planetScan = planetScan;
	}

	public boolean hasPsyProbe() {
		if(details == null) return false;
		else return details.psyProbe;
	}

	void setPsyProbe(boolean psyProbe) {
		if(details == null) details = new Details();
		details.psyProbe = psyProbe;
	}

	public int getPhotons() {
		if(details == null) return 0;
		else return details.photons;
	}

	void setPhotons(int photons) {
		if(details == null) details = new Details();
		details.photons = photons;
	}

	public int getBeacons() {
		if(details == null) return 0;
		else return details.beacons;
	}

	void setBeacons(int beacons) {
		if(details == null) details = new Details();
		details.beacons = beacons;
	}

	public int getProbes() {
		if(details == null) return 0;
		else return details.probes;
	}

	void setProbes(int probes) {
		if(details == null) details = new Details();
		details.probes = probes;
	}

	public int getArmids() {
		if(details == null) return 0;
		else return details.armids;
	}

	void setArmids(int armids) {
		if(details == null) details = new Details();
		details.armids = armids;
	}

	public int getLimpets() {
		if(details == null) return 0;
		else return details.limpets;
	}

	void setLimpets(int limpets) {
		if(details == null) details = new Details();
		details.limpets = limpets;
	}

	public int getDisruptors() {
		if(details == null) return 0;
		else return details.disruptors;
	}

	void setDisruptors(int disruptors) {
		if(details == null) details = new Details();
		details.disruptors = disruptors;
	}

	public int getGenTorps() {
		if(details == null) return 0;
		else return details.genTorps;
	}

	void setGenTorps(int genTorps) {
		if(details == null) details = new Details();
		details.genTorps = genTorps;
	}

	public int getAtomics() {
		if(details == null) return 0;
		else return details.atomics;
	}

	void setAtomics(int atomics) {
		if(details == null) details = new Details();
		details.atomics = atomics;
	}

	public int getCloaks() {
		if(details == null) return 0;
		else return details.cloaks;
	}

	void setCloaks(int cloaks) {
		if(details == null) details = new Details();
		details.cloaks = cloaks;
	}

	public int getCorbomite() {
		if(details == null) return 0;
		else return details.corbomite;
	}

	void setCorbomite(int corbomite) {
		if(details == null) details = new Details();
		details.corbomite = corbomite;
	}

	public boolean getInterdictOn() {
		if(details == null) return false;
		else return details.interdictOn;
	}

	void setInterdictOn(boolean interdictOn) {
		if(details == null) details = new Details();
		details.interdictOn = interdictOn;
	}
	
	public String getPassword() {
		return details.password;
	}
	
	void setPassword(String password) {
		if(details == null) details = new Details();
		details.password = password;
	}

	public int[] getCargo() {
		if(details != null) return details.getCargo();
		else return new int[4];
	}
	
	public int getCargo(int item) {
		if(details != null) return details.getCargo(item);
		else return 0;
	}
	
	void setCargo(int item, int quantity) {
		if(details == null) details = new Details();
		details.setCargo(item, quantity);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(name == null) sb.append("(unknown name)");
		else sb.append(name);
		if(type != null) {
			sb.append(" (");
			sb.append(type.getName());
			sb.append(')');
		}
		return sb.toString();
	}
	
	static class Details implements Serializable {
		private static final long serialVersionUID = -7018743235412541908L;
		// buildDate is mutable because details from quickstats or
		// other sources may be known before info screen is viewed
		public volatile Date buildDate;
		public volatile int holds;
		public volatile int transWarp;
		public volatile int longRangeScan;
		public volatile boolean planetScan;
		public volatile boolean psyProbe;
		public volatile int photons;
		public volatile int beacons;
		public volatile int probes;
		public volatile int armids;
		public volatile int limpets;
		public volatile int disruptors;
		public volatile int genTorps;
		public volatile int atomics;
		public volatile int cloaks;
		public volatile int corbomite;
		public volatile boolean interdictOn;
		public volatile String password;
		private final int cargo[] = new int[4];
		
		synchronized int[] getCargo() {
			return Arrays.copyOf(cargo, cargo.length);
		}
		
		synchronized int getCargo(int item) {
			return cargo[item];
		}
		
		synchronized void setCargo(int item, int quantity) {
			cargo[item] = quantity;
		}
		
		synchronized int getEmptyHolds() {
			int fullHolds = 0;
			for(int i = 0; i < cargo.length; ++i) {
				fullHolds += cargo[i];
			}
			return holds - fullHolds;
		}
		
		synchronized private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
		}
		
		synchronized private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
		}
	}
	
}
