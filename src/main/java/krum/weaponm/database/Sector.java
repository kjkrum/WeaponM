package krum.weaponm.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Sector implements Serializable, Constants {
	private static final long serialVersionUID = 1L;
	
	transient Database db;
	private final int number;
	// warp access is synched
	private int[] warpsOut = new int[0];
	private boolean warpsOutSorted = true;
	private transient int[] warpsIn = new int[0];
	private transient boolean warpsInSorted = true;
	// synch related fields
	private int density = UNKNOWN;
	private int warpDensity = UNKNOWN; // number of warps reported by density scan
	private int navhaz = UNKNOWN;
	private boolean anomaly;
	private Date densityDate; // date of last density scan
	private volatile Date holoDate; // date of last holo scan, ether probe, or presence
	private volatile boolean fullyMapped;
	private volatile boolean explored;
	private volatile boolean avoided;
	private volatile String nebula;
	private final Map<String, String> notes = new HashMap<String, String>();
	//private final Map<String, Object> userData = new HashMap<String, Object>();
	private volatile Port port;
	private volatile String beaconMessage;
	// fighter stuff synched
	private int fighters;
	private Owner fighterOwner;
	private FighterMode fighterMode;
	private Date fighterDate;
	// fig hit stuff synched
	private Date figHitDate;
	private String figHitName;
	// mine stuff synched
	private int armids = UNKNOWN;
	private Owner armidOwner;
	private int limpets;
	private Owner limpetOwner;
	
	transient List<Trader> traders = new LinkedList<Trader>();
	transient List<Alien> aliens = new LinkedList<Alien>();
	transient List<Ship> ships = new LinkedList<Ship>();
	transient List<Planet> planets = new LinkedList<Planet>();


	protected Sector(Database db, int number) {
		this.db = db;
		this.number = number;
	}
	
	/**
	 * Returns the sector number.
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Returns the number of warps this sector is known to have.  This is the
	 * larger of the number of known warps and the number of warps reported by
	 * a density scan.
	 * 
	 * @see #getWarpDensity()
	 * @see #getNumKnownWarpsOut()
	 */
	synchronized public int getNumWarpsOut() {
		return Math.max(warpDensity, warpsOut.length);
	}
	
	/**
	 * Returns the number of known warps out.  This differs from
	 * {@link #getNumWarpsOut()} in that this method returns the number of
	 * warps with known destinations.  This may be less than the number of
	 * warps the sector is known to have if, for example, your only
	 * knowledge of the sector is a density scan.
	 * 
	 * @see #getNumWarpsOut()
	 */
	synchronized public int getNumKnownWarpsOut() {
		return warpsOut.length;
	}
	
	/**
	 * Returns the number of known warps into the sector.
	 */
	synchronized public int getNumWarpsIn() {
		return warpsIn.length;
	}
	
	/**
	 * Returns true if this sector is known to have a warp to the specified
	 * sector.
	 */
	synchronized public boolean hasWarpTo(int sector) {
		for(int i = 0; i < warpsOut.length; ++i) {
			if(warpsOut[i] == sector) return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this sector is known to have a warp from the specified
	 * sector.
	 */
	synchronized public boolean hasWarpFrom(int sector) {
		for(int i = 0; i < warpsIn.length; ++i) {
			if(warpsIn[i] == sector) return true;
		}
		return false;
	}
	
	void addWarpTo(int sector) {
			synchronized (this) {
				if(fullyMapped || hasWarpTo(sector)) return;
				warpsOut = Arrays.copyOf(warpsOut, warpsOut.length + 1);
				warpsOut[warpsOut.length - 1] = sector;
				if(warpsOut.length > 1 && warpsOut[warpsOut.length - 1] < warpsOut[warpsOut.length - 2]) {
					warpsOutSorted = false;
				}
				if(warpsOut.length == 6 || warpsOut.length == warpDensity) fullyMapped = true;
			}
			Sector warp = db.getSector(sector);
			warp.addWarpFrom(number);
	}
	
	synchronized void addWarpFrom(int sector) {
			if(hasWarpFrom(sector)) return;
			warpsIn = Arrays.copyOf(warpsIn, warpsIn.length + 1);
			warpsIn[warpsIn.length - 1] = sector;
			if(warpsIn.length > 1 && warpsIn[warpsIn.length - 1] < warpsIn[warpsIn.length - 2]) {
				warpsInSorted = false;
			}
	}
	
	void setWarpsOut(int[] warpsOut) {
		synchronized (this) {
			if(this.warpsOut.length == warpsOut.length && warpsOutSorted) return;
			this.warpsOut = warpsOut;
			warpsOutSorted = true;
			explored = true;
			fullyMapped = true;
		}
		for(int i = 0; i < warpsOut.length; ++i) {
			Sector warp = db.getSector(warpsOut[i]);
			warp.addWarpFrom(number);
		}
	}
	
	/**
	 * Gets the known warps out.  The array will be sorted in ascending order.
	 */
	synchronized public int[] getWarpsOut() {
		if(!warpsOutSorted) {
			Arrays.sort(warpsOut);
			warpsOutSorted = true;
		}
		return Arrays.copyOf(warpsOut, warpsOut.length);
	}
	
	/**
	 * Gets the known warps out as Sectors.
	 * 
	 * @see #getWarpsOut()
	 */
	/*
	synchronized public Sector[] getWarpSectors() {
		int[] warps = getWarpsOut();
		Sector[] sectors = new Sector[warps.length];
		for(int i = 0; i < sectors.length; ++i) {
			sectors[i] = this.db.getSector(warps[i]);
		}
		return sectors;
	}
	*/
	
	/**
	 * Gets the known warps in.  The array will be sorted in ascending order.
	 */
	synchronized public int[] getWarpsIn() {
		if(!warpsInSorted) {
			Arrays.sort(warpsIn);
			warpsInSorted = true;
		}
		return Arrays.copyOf(warpsIn, warpsIn.length);
	}
	
	/**
	 * Returns true if this sector has been density scanned.
	 */
	synchronized public boolean hasDensityData() {
		return densityDate != null;
	}
	
	/**
	 * Returns the sector's density.  Returns <tt>UNKNOWN</TT> if the sector
	 * has not been density scanned.
	 */
	synchronized public int getDensity() {
		return density;
	}
	
	/**
	 * Returns the number of warps reported by a density scan.  Returns
	 * <tt>UNKNOWN</tt> if the sector has not been density scanned.
	 * 
	 * @see #getNumWarpsOut()
	 */
	synchronized public int getWarpDensity() {
		return warpDensity;
	}
	
	/**
	 * Returns the last known navhaz value, as recorded by a density scan,
	 * holo scan, ether probe, or presence.  Returns <tt>UNKNOWN</tt> if the
	 * navhaz level has never been recorded.
	 */
	synchronized public int getNavhaz() {
		return navhaz;
	}
	
	/**
	 * Returns true if an anomaly was detected on the last density scan.
	 */
	synchronized public boolean containsAnomaly() {
		return anomaly;
	}
	
	/**
	 * Returns the date of the last density scan.  Returns null if the sector
	 * has not been density scanned.
	 */
	synchronized public Date getDensityDate() {
		return densityDate;
	}
	
	synchronized protected void setDensityData(int density, int warpDensity, int navhaz, boolean anomaly) {
		this.density = density;
		this.warpDensity = warpDensity;
		this.navhaz = navhaz;
		this.anomaly = anomaly;
		this.densityDate = new Date();
		if(warpDensity == warpsOut.length) fullyMapped = true;
	}
	
	/**
	 * Returns the date of your last holo scan, ether probe report, or
	 * presence in the sector.  Returns null if no holo data has been
	 * recorded.  The holo date is not updated when your probe is destroyed
	 * in the sector.
	 */
	public Date getHoloDate() {
		return holoDate;
	}
	
	void setHoloDate(Date holoDate) {
		this.holoDate = holoDate;
	}
	
	/**
	 * Returns true if the sector is explored.
	 */
	public boolean isExplored() {
		return explored;
	}
	
	protected void setExplored(boolean explored) {
		if(this.explored != explored) this.explored = explored;
	}
	
	/**
	 * Returns true if it is certain that all the sector's outbound warps are
	 * known.  This is true if the sector's warps have been parsed, or it has
	 * six known warps, or it has been density scanned and the number of known
	 * warps equals the number reported by the density scan.
	 */
	public boolean isFullyMapped() {
		return fullyMapped;
	}
	
	protected void setFullyMapped(boolean fullyMapped) {
		this.fullyMapped = fullyMapped;
	}
	
	/**
	 * Returns true if the sector is avoided.
	 */
	public boolean isAvoided() {
		return avoided;
	}
	
	protected void setAvoided(boolean avoided) {
		this.avoided = avoided;
	}
	
	/**
	 * Returns the sector's nebula name.  Returns null if the sector is not
	 * part of a nebula.
	 */
	public String getNebula() {
		return nebula;
	}
	
	void setNebula(String nebula) {
		if(nebula != null) this.nebula = nebula;
	}	
	
	/**
	 * Gets the specified note for this sector.
	 * 
	 * @param name the name of the note
	 * @return the content of the note
	 */
	synchronized public String getNote(String name) {
		return notes.get(name);
	}
	
	/**
	 * Sets the specified note for this sector.  Note names must be unique.
	 * To avoid name conflicts, it is recommended that note names include the
	 * fully qualified class name of the script that created them, e.g.,
	 * <tt>"mypackage.MyScript#noteName"</tt>.
	 * 
	 * @param name the name of the note
	 * @param note the content of the note
	 */
	synchronized public void setNote(String name, String note) {
		notes.put(name, note);
	}
	
	/**
	 * Removes the specified note from this sector.
	 * 
	 * @param name the name of the note
	 */
	synchronized public void removeNote(String name) {
		notes.remove(name);
	}
	
	
	/**
	 * Returns true if this sector is known to contain a port.
	 */
	public boolean hasPort() {
		return port != null;
	}
	
	/**
	 * Returns the port in this sector.  Returns null if no port is known.
	 */
	public Port getPort() {
			return port;
	}
	
	protected void setPort(Port port) {
			this.port = port;
	}
	
	/**
	 * Returns the date of your last density scan, holo scan, ether probe
	 * report, or presence in the sector.  Returns null if the sector has not
	 * been scanned.  This date is not updated when your probe is destroyed in
	 * the sector.
	 */
	synchronized public Date getScanDate() {
		if(holoDate == null) return densityDate;
		if(densityDate == null) return holoDate;
		return densityDate.after(holoDate) ? densityDate : holoDate; 
	}
	
	/**
	 * Returns the number of fighters the sector is known to contain.  Unlike
	 * most other fields, this value is initialized to zero.  A return value
	 * of <tt>UNKNOWN</tt> indicates that enemy fighters destroyed one of your
	 * probes in the sector.
	 */
	synchronized public int getFighters() {
		return fighters;
	}
	
	/**
	 * Gets the owner of the fighters in the sector.  Returns null if there
	 * are no fighters in the sector.
	 */
	synchronized public Owner getFighterOwner() {
		return fighterOwner;
	}
	
	/**
	 * Returns the mode of the fighters in this sector.  Returns null if there
	 * are no fighters in the sector.
	 */
	synchronized public FighterMode getFighterMode() {
		return fighterMode;
	}
	
	/**
	 * Returns the date the fighters in this sector were placed or discovered.
	 * Returns null if there are no fighters in the sector.
	 */
	synchronized public Date getFighterDate() {
		return fighterDate;
	}
	
	synchronized void setFighters(int fighters, Owner owner, FighterMode mode) {
		// don't update fighter date if nothing has changed
		if(this.fighters == fighters && fighterOwner == owner && fighterMode == mode) return;
		this.fighters = fighters;
		fighterOwner = owner;
		fighterMode = mode;
		fighterDate = (fighters == 0) ? null : new Date();
	}
	
	public String getBeaconMessage() {
		return beaconMessage;
	}
	
	void setBeaconMessage(String message) {
		beaconMessage = message;
	}
	
	/**
	 * Returns the name of the last entity to hit your fighters in this
	 * sector.  Includes hits by traders, aliens, probes, and Feds.  Returns
	 * null if no fighter hit has been recorded in this sector.
	 */
	synchronized public String getFigHitName() {
		return figHitName;
	}
	
	/**
	 * Returns the date of the last fighter hit in this sector.  Returns null
	 * if no fighter hit has been recorded.
	 */
	synchronized public Date getFigHitDate() {
		return figHitDate;
	}
	
	synchronized void setFigHit(String name, Date date) {
		figHitName = name;
		figHitDate = date;
	}
	
	/**
	 * Returns the number of limpets in this sector.  Only returns information
	 * about your own limpets.
	 */
	synchronized public int getLimpets() {
		return limpets;
	}
	
	/**
	 * Returns the owner of the limpets in this sector.  Returns null if there
	 * are no limpets in this sector.  Only returns information about your own
	 * limpets.
	 */
	synchronized public Owner getLimpetOwner() {
		return limpetOwner;
	}
	
	synchronized void setLimpets(int limpets, Owner owner) {
		this.limpets = limpets;
		limpetOwner = owner;
	}
	
	/**
	 * Returns the number of armid mines in this sector.
	 */
	synchronized public int getArmids() {
		return armids;
	}
	
	/**
	 * Returns the owner of the armid mines in this sector.  Returns null if
	 * there are no armid mines in this sector.
	 */
	synchronized public Owner getArmidOwner() {
		return armidOwner;
	}
	
	synchronized void setArmids(int armids, Owner owner) {
		this.armids = armids;
		armidOwner = owner;
	}
	
	public boolean isFedSpace() {
		return(number <= 10 || this == db.getStardockSector());
	}
	
	public boolean isYourLocation() {
		return this == db.getYou().getSector();
	}
	
	/************************************************************************/
	
	@Override
	public String toString() {
		return Integer.toString(number);
	}
		
	synchronized private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
	}
	
	synchronized private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		warpsIn = new int[0];
		warpsInSorted = true;
		traders = new LinkedList<Trader>();
		aliens = new LinkedList<Alien>();
		ships = new LinkedList<Ship>();
		planets = new LinkedList<Planet>();
	}
}
