package krum.weaponm.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import krum.weaponm.script.BreadthFirstSearch;
import krum.weaponm.script.NodeMatcher;

public class Database implements Serializable, Constants {
	private static final long serialVersionUID = -4982847852694634925L;
	
	private final LoginOptions loginOptions = new LoginOptions();
	private final GameStats gameStats = new GameStats();
	private final PersonalSettings personalSettings = new PersonalSettings();
	private Sector[] sectors;
	private final Map<String, String> notes = new HashMap<String, String>();
	
	private volatile int stardockSector = UNKNOWN;
	private final ArrayList<Integer> zeroSectors = new ArrayList<Integer>(3);
	
	private volatile Date lastCimPortsDate;
	private volatile Date lastLogDate;
	
	private final RankNameResolver rankNameResolver = new RankNameResolver(); // serialized because it may cache non-standard ranks

	private final You you = new You();
	
	private final Map<Integer, Ship> shipNumberIndex = new HashMap<Integer, Ship>();
	
	private final Set<ShipType> shipTypes = new HashSet<ShipType>();
	private transient Map<String, ShipType> shipTypeNameIndex;

	private final Set<Trader> traders = new HashSet<Trader>();
	private transient Map<String, Trader> traderNameIndex;
	
	private final Map<Integer, Corporation> corpNumberIndex = new HashMap<Integer, Corporation>();
	
	/*
	private final Set<Boss> bosses = new HashSet<Boss>();
	private transient Map<String, Boss> bossIndex;
	
	private final Set<Alien> aliens = new HashSet<Alien>();
	private transient Map<String, Alien> alienIndex;
	
	private final Set<PlanetType> planetTypes = new HashSet<PlanetType>();
	private transient Map<String, PlanetType> planetTypeIndex;
	*/
	
	// TODO: ships, planets, corporations
	
	Database() {
		restoreTransients();
	}
	
	// builds indexes and stuff; called when db is created or deserialized
	void restoreTransients() {
		
		shipTypeNameIndex = new HashMap<String, ShipType>();
		for(ShipType shipType : shipTypes) {
			shipTypeNameIndex.put(shipType.getName(), shipType);
		}
		
		traderNameIndex = new HashMap<String, Trader>();
		for(Trader trader : traders) {
			String name = trader.getName();
			if (name.length() > 6) name = name.substring(0, 6);
			traderNameIndex.put(name, trader);
		}
		
		/*
		bossIndex = new HashMap<String, Boss>();
		for(Boss boss : bosses) {
			bossIndex.put(boss.getName(), boss);
		}
		
		alienIndex = new HashMap<String, Alien>();
		for(Alien alien : aliens) {
			alienIndex.put(alien.getName(), alien);
		}
		
		planetTypeIndex = new HashMap<String, PlanetType>();
		for(PlanetType planetType : planetTypes) {
			planetTypeIndex.put(planetType.getName(), planetType);
		}
		*/
		
		if(isInitialized()) {
			for(Sector sector : sectors) {
				sector.db = this;
				for(int w : sector.getWarpsOut()) {
					getSector(w).addWarpFrom(sector.getNumber());
				}
			}
		}		
	}	

	/**
	 * True when the number of sectors is known.
	 */
	synchronized public boolean isInitialized() {
		return sectors != null;
	}
	
	/**
	 * Creates the sectors array.
	 */
	synchronized protected void initialize() {
		if(isInitialized()) return;
		int s = gameStats.sectors();
		sectors = new Sector[s];
		for(int i = 0; i < s; ++i) {
			sectors[i] = new Sector(this, i + 1);
		}
	}
	
	/**
	 * Returns the special {@link Trader} that represents you.
	 */
	public You getYou() {
		return you;
	}
	
	/**
	 * Returns the game's login options.  This includes the host name, port,
	 * user name, password, and auto-login options.
	 */
	public LoginOptions getLoginOptions() {
		return loginOptions;
	}
	
	/**
	 * Returns the game stats.  This may be null, but only if you have never
	 * logged in as far as the game menu. 
	 */
	public GameStats getGameStats() {
		return gameStats;
	}
	
	/**
	 * Returns your personal game settings.
	 */
	public PersonalSettings getPersonalSettings() {
		return personalSettings;
	}
	
	/**
	 * Returns the sector where Stardock is located, or null if it is unknown.
	 */
	public Sector getStardockSector() {
		if(stardockSector == UNKNOWN) return null;
		else return sectors[stardockSector - 1];
	}
	
	protected void setStardockSector(int sector) {
		stardockSector = sector;
	}
	
	/**
	 * Returns the locations of known Class 0 ports.
	 */
	synchronized public Sector[] getZeroSectors() {
		//int[] ints = new int[zeroSectors.size()];
		//for(int i = 0; i < ints.length; ++i) {
		//	ints[i] = zeroSectors.get(i);
		//}
		//return ints;
		
		Sector[] zeros = new Sector[zeroSectors.size()];
		for(int i = 0; i < zeros.length; ++i) {
			zeros[i] = sectors[zeroSectors.get(i) - 1];
		}
		return zeros;
	}
	
	synchronized void addZeroSector(int sector) {
		if(!zeroSectors.contains(sector)) zeroSectors.add(sector);
	}
	
	/**
	 * Gets the specified note for this database.
	 * 
	 * @param name the name of the note
	 * @return the content of the note
	 */
	synchronized public String getNote(String name) {
		return notes.get(name);
	}
	
	/**
	 * Sets the specified note for this database.  Note names must be unique.
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
	 * Removes the specified note from this database.
	 */
	synchronized public void removeNote(String name) {
		notes.remove(name);
	}	
	
	/**
	 * Gets a Sector by its sector number.
	 */
	synchronized public Sector getSector(int number) {
		return sectors[number - 1];
	}

	/**
	 * Gets all the Sectors.  Remember that arrays in Java are zero-based, so
	 * sector 1 is at index 0.
	 */
	synchronized public Sector[] getSectors() {
		if(sectors == null) return null;
		return Arrays.copyOf(sectors, sectors.length);
	}
	
	/**
	 * Looks up a ship by its ship number.  Returns null if no ship with that
	 * number is in the database. 
	 */
	synchronized public Ship getShip(int number) {
		return shipNumberIndex.get(number);
	}
	
	synchronized void putShip(int number, Ship ship) {
		shipNumberIndex.put(number, ship);
	}
	
	synchronized void removeShip(int number) {
		if(shipNumberIndex == null) System.err.println("it's null");
		else shipNumberIndex.remove(number);
	}
	
	/**
	 * Parameter should already be stripped and trimmed.
	 */
	synchronized ShipType getOrCreateShipType(String name) {
		if(shipTypeNameIndex.containsKey(name)) {
			return shipTypeNameIndex.get(name);
		}
		else {
			ShipType shipType = new ShipType(name);
			shipTypes.add(shipType);
			shipTypeNameIndex.put(name, shipType);
			return shipType;
		}
	}
	
	/**
	 * Gets all known ship types.  Some of them may have no information except
	 * the type name.
	 */
	synchronized public Collection<ShipType> getShipTypes() {
		return new HashSet<ShipType>(shipTypes);
	}
	
	/**
	 * Returns the named trader, creating a new record if no matching record
	 * exists.  Updates trader records that were created with a shortened name
	 * when they are retrieved using the full name.
	 */
	synchronized Trader getOrCreateTrader(String name) {
		String key = name;
		if(key.length() > 6) key = key.substring(0, 6);
		if(traderNameIndex.containsKey(key)) {
			Trader trader = traderNameIndex.get(key);
			if(name.length() > trader.getName().length()) trader.setName(name);
			return trader;
		}
		else {
			Trader trader = new Trader();
			trader.setName(name);
			traders.add(trader);
			traderNameIndex.put(key, trader);
			return trader;
		}
	}
	
	/**
	 * Looks up the specified trader using at least the first six characters
	 * of their name, or their entire name if it is shorter than six
	 * characters.  Returns null if the trader is not in the database.
	 */
	synchronized public Trader getTrader(String name) {
		String key = name;
		if(key.length() > 6) key = key.substring(0, 6);
		if(traderNameIndex.containsKey(key)) {
			Trader trader = traderNameIndex.get(key);
			// don't return "StinkyDiver" if the user searches for "StinkyFace"
			// but do return "StinkyDiver" if the user searches for "Stinky"
			if(trader.getName().startsWith(name)) return trader;
		}
		return null;
	}	
	
	synchronized Corporation getOrCreateCorp(int number) {
		if(corpNumberIndex.containsKey(number)) {
			return corpNumberIndex.get(number);
		}
		else {
			Corporation corp = new Corporation(number);
			corpNumberIndex.put(number, corp);
			return corp;
		}
	}
	

	
	/**
	 * Returns the named planet type, creating a new record if no matching
	 * record exists.  Planet type names are stored with ANSI codes stripped,
	 * and should be of the form "M, Earth Type".
	 * 
	 * @param name

	synchronized PlanetType getPlanetType(String name) {
		name = ParserUtils.stripANSI(name);
		if(planetTypeIndex.containsKey(name)) return planetTypeIndex.get(name);
		else {
			PlanetType planetType = new PlanetType(name);
			planetTypes.add(planetType);
			planetTypeIndex.put(name, planetType);
			return planetType;
		}
	}
	
	synchronized public Collection<PlanetType> getPlanetTypes() {
		return new HashSet<PlanetType>(planetTypes);
	} */
	
	synchronized String resolveName(String rankName) {
		return rankNameResolver.resolveName(rankName);
	}

	synchronized String resolveRank(String rankName) {
		return rankNameResolver.resolveRank(rankName);
	}

	synchronized void addRank(String rank) {
		rankNameResolver.addRank(rank);
	}
	
	/**
	 * Returns the timestamp of the beginning of the last CIM port report.
	 * Any port with a report date earlier than this is blocked.
	 * 
	 * @see Port#getReportDate()
	 */
	public Date getLastCimPortsDate() {
		return lastCimPortsDate;
	}
	
	void setLastCimPortsDate(Date date) {
		lastCimPortsDate = date;
	}
	
	/**
	 * Returns the timestamp (in game time) of the last daily log entry
	 * parsed.
	 */
	public Date getLastLogDate() {
		return lastLogDate;
	}
	
	/**
	 * Returns the number of sectors in the universe.  Returns
	 * {@link Constants#UNKNOWN} if the database has not been initialized. 
	 */
	public int getNumSectors() {
		return sectors != null ? sectors.length : UNKNOWN;
	}
	
	/**
	 * Plots a course using warp information from the database.
	 * 
	 * @param from the origin sector
	 * @param to the destination sector
	 * @param ignoreAvoids true if the course should be plotted through avoids
	 * @return the calculated course, or null if no route was found
	 */
	public Sector[] plotCourse(Sector from, Sector to, boolean ignoreAvoids) {
		NodeMatcher include = ignoreAvoids ? NodeMatcher.ALL : NodeMatcher.NOT_AVOIDED;
		NodeMatcher target = NodeMatcher.sector(to);
		BreadthFirstSearch bfs = new BreadthFirstSearch(
				sectors,
				from,
				0,
				include,
				target,
				true
			);
		if(bfs.targetFound()) {
			return bfs.plotCourse(to);
		}
		else return null;
	}
	
	/**
	 * Plots a course using warp information from the database.
	 * 
	 * @param from the origin sector
	 * @param to the destination sector
	 * @param ignoreAvoids true if the course should be plotted through avoids
	 * @return the calculated course, or null if no route was found
	 */
	public int[] plotCourse(int from, int to, boolean ignoreAvoids) {
		NodeMatcher include = ignoreAvoids ? NodeMatcher.ALL : NodeMatcher.NOT_AVOIDED;
		NodeMatcher target = NodeMatcher.sector(to);
		BreadthFirstSearch bfs = new BreadthFirstSearch(
				sectors,
				from,
				0,
				include,
				target,
				true
			);
		if(bfs.targetFound()) {
			return bfs.plotCourse(to);
		}
		else return null;
	}
	
	/**
	 * Returns the named boss, creating a new record if no matching record
	 * exists.  The <tt>fed</tt> parameter is only used if a new record is
	 * created.
	 * 
	 * @param name the boss' name
	 * @param fed true if this boss is a Federal
	 
	synchronized Boss getBoss(String name, boolean fed) {
		if(bossIndex.containsKey(name)) return bossIndex.get(name);
		else {
			Boss boss = new Boss(name, fed);
			bosses.add(boss);
			bossIndex.put(name, boss);
			return boss;
		}
	}
	
	/**
	 * Returns the named alien, creating a new record if no matching record
	 * exists.  Alien names and ship types do not change unless edited by the
	 * sysop, but the uniqueness of names may not be guaranteed.  If a record
	 * exists with the same name and ship type, it is returned.  Otherwise, a
	 * new record is created.
	 * 
	 * @param name the alien's name
	 * @param shipType the type of the alien's ship

	synchronized Alien getAlien(String name, ShipType shipType) {
		Alien alien = alienIndex.get(name);
		if(alien == null || alien.getShipType() != shipType) {
			alien = new Alien(name, shipType);
			aliens.add(alien);
			alienIndex.put(name, alien);
		}
		return alien;
	}
	

	
	/************************************************************************/

	synchronized private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	synchronized private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		restoreTransients();
	}
}
