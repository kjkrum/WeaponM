package krum.weaponm.database;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around a map containing the game stats.  Game stats are the
 * properties displayed when you enter '*' at the game prompt.
 */
public class GameStats implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected final Map<String, String> statsMap = Collections.synchronizedMap(new HashMap<String, String>());
	
	GameStats() { }
	
	/**
	 * Gets any game stat as a string.
	 * 
	 * @param name the name of the stat
	 * @return the value of the stat
	 */
	public String getStat(String name) {
		return statsMap.get(name);
	}
	
	/**
	 * Returns true if the stats map is empty.
	 */
	public boolean isEmpty() {
		return statsMap.isEmpty();
	}
	
	/**
	 * Updates the game stats with a freshly downloaded map.
	 * 
	 * @param newStats
	 * @throws DatabaseIntegrityException if the game appears to have been rebanged
	 */
	protected void updateAll(Map<String, String> newStats) throws DatabaseIntegrityException {
		String dateKey = "Start Day";
		synchronized(statsMap) {
			if(statsMap.containsKey(dateKey) && !statsMap.get(dateKey).equals(newStats.get(dateKey))) {
				throw new DatabaseIntegrityException("Start Day does not match database.");
			}
			statsMap.putAll(newStats);
		}
	}
	
	// convenience methods to parse specific properties
	
	public int majorVersion() {
		return Integer.parseInt(statsMap.get("Major Version"));
	}
	
	public int minorVersion() {
		return Integer.parseInt(statsMap.get("Minor Version"));
	}
	
	// TODO: access schedule stuff
	
	public boolean goldEnabled() {
		return Boolean.parseBoolean(statsMap.get("Gold Enabled"));
	}
	
	public boolean mbbsCompatible() {
		return Boolean.parseBoolean(statsMap.get("MBBS Compatibility"));
	}
	
	public boolean goldBubbles() {
		return Boolean.parseBoolean(statsMap.get("Bubbles"));
	}
	
	public Date startDay() {
		return ParserUtils.parseShortDate(statsMap.get("Start Day"));
	}
	
	public int gameAge() {
		String str = statsMap.get("Game Age");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public Date lastExternDay() {
		return ParserUtils.parseShortDate(statsMap.get("Last Extern Day"));
	}
	
	public boolean internalAliens() {
		return Boolean.parseBoolean(statsMap.get("Internal Aliens"));
	}
	
	public boolean internalFerrengi() {
		return Boolean.parseBoolean(statsMap.get("Internal Ferrengi"));
	}
	
	public boolean isClosedGame() {
		return Boolean.parseBoolean(statsMap.get("Closed Game"));
	}
	
	public boolean showStardock() {
		return Boolean.parseBoolean(statsMap.get("Show Stardock"));
	}
	
	public int turnBase() {
		String str = statsMap.get("Turn Base");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	// TODO: Time Online=Unlimited or ???
	
	/**
	 * Gets the inactivity timeout in seconds.
	 */
	public int inactivityTimeout() {
		String str = statsMap.get("Inactive Time");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public Date lastBustClearDay() {
		return ParserUtils.parseShortDate(statsMap.get("Last Bust Clear Day"));
	}
	
	public int initialFighters() {
		return Integer.parseInt(statsMap.get("Initial Fighters"));
	}
	
	public int initialCredits() {
		return Integer.parseInt(statsMap.get("Initial Credits"));
	}
	
	public int initialHolds() {
		return Integer.parseInt(statsMap.get("Initial Holds"));
	}
	
	public boolean newPlayerPlanets() {
		return Boolean.parseBoolean(statsMap.get("New Player Planets"));
	}
	
	public int daysUntilDeletion() {
		String str = statsMap.get("Days Til Deletion");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public int colonistRegenRate() {
		return Integer.parseInt(statsMap.get("Colonist Regen Rate"));
	}
	
	public int maxPlanetsPerSector() {
		return Integer.parseInt(statsMap.get("MaxPlanetSector"));
	}
	
	public int maxCorpMembers() {
		return Integer.parseInt(statsMap.get("Max Corp Members"));
	}
	
	public int fedspaceParkingLimit() {
		return Integer.parseInt(statsMap.get("FedSpace Ship Limit"));
	}
	
	/**
	 * Gets the photon duration in seconds.
	 */
	public int photonDuration() {
		String str = statsMap.get("Photon Missile Duration");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	/**
	 * True if photons can be fired from FedSpace.  Photons can never be fired
	 * into FedSpace.
	 */
	public boolean fedspacePhotons() {
		return Boolean.parseBoolean(statsMap.get("FedSpace Photons"));
	}
	
	public boolean photonsDisablePlayers() {
		return Boolean.parseBoolean(statsMap.get("Photons Disable Players"));
	}
	
	public int cloakFailPercent() {
		String str = statsMap.get("Cloak Fail Percent");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}
	
	public int debrisLossPercent() {
		String str = statsMap.get("Debris Loss Percent");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}
	
	public int planetTradePercent() {
		String str = statsMap.get("Trade Percent");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}
	
	public boolean stealFromBuyPorts() {
		return Boolean.parseBoolean(statsMap.get("Steal Buy"));
	}
	
	public int portRegenRate() {
		return Integer.parseInt(statsMap.get("Production Rate"));
	}
	
	public int maxPortRegen() {
		return Integer.parseInt(statsMap.get("Max Production Regen"));
	}
	
	public boolean multiplePhotons() {
		return Boolean.parseBoolean(statsMap.get("Multiple Photons"));
	}
	
	/**
	 * Gets the number of days between bust clears.  Individual busts do not
	 * take this long to clear; rather, all busts are cleared every so many
	 * days.
	 * 
	 * @see #lastBustClearDay()
	 */
	public int clearBustDays() {
		String str = statsMap.get("Clear Bust Days");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public int stealFactor() {
		String str = statsMap.get("Steal Factor");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}	

	public int robFactor() {
		String str = statsMap.get("Rob Factor");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}
	
	public int maxPortProduction() {
		return Integer.parseInt(statsMap.get("Port Production Max"));
	}
	
	public int radiationLifetime() {
		String str = statsMap.get("Radiation Lifetime");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public int fighterLockDecay() {
		String str = statsMap.get("Fighter Lock Decay");
		return Integer.parseInt(str.substring(0, str.indexOf(' ')));
	}
	
	public boolean invincibleFerrengal() {
		return Boolean.parseBoolean(statsMap.get("Invincible Ferrengal"));
	}
	
	public boolean mbbsCombat() {
		return Boolean.parseBoolean(statsMap.get("MBBS Combat"));
	}
	
	public boolean deathDelay() {
		return Boolean.parseBoolean(statsMap.get("Death Delay"));
	}
	
	public int deathsPerDay() {
		return Integer.parseInt(statsMap.get("Deaths per Day"));
	}
	
	// TODO: Startup Asset Dropoff=No dropoff
	
	public boolean showWhoIsOnline() {
		return Boolean.parseBoolean(statsMap.get("Show Whos Online"));
	}
	
	public boolean interactiveSubPrompts() {
		return Boolean.parseBoolean(statsMap.get("Interactive Sub-prompts"));
	}
	
	public boolean allowAliases() {
		return Boolean.parseBoolean(statsMap.get("Allow Aliases"));
	}
	
	// TODO: Alien Sleep Mode=Active
	
	// TODO: Allow MBBS MegaRob Bug=N/A
	
	public int maxTerraColonists() {
		return Integer.parseInt(statsMap.get("Max Terra Colonists"));
	}
	
	// TODO: Minimum Login Time=None
	
	public int turnBankDays() {
		return Integer.parseInt(statsMap.get("Turn Accumulation Days"));
	}
	
	// TODO: Podless Captures=Always
	
	public int captureFailPercent() {
		String str = statsMap.get("Capture Fail Percent");
		return Integer.parseInt(str.substring(0, str.indexOf('%')));
	}
	
	public int maxBankCredits() {
		String str = statsMap.get("Max Bank Credits");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	
	/* TODO
	[Reports]
	High Score Mode=On demand
	High Score Type=Values
	Rankings Mode=On demand
	Rankings Type=Values & Titles
	Entry Log Blackout=None
	Game Log Blackout=None
	Port Report Delay=No Delay
	[Emulation]
	Input Bandwidth=1 Mps Broadband
	Output Bandwidth=1 Mps Broadband
	Latency=150 ms
	[Timing]
	Ship Delay=Third (1/3 s/t)
	Planet Delay=None
	Other Attacks Delay=None
	EProbe Delay=None
	Crime Delay=Constant (2 s)
	Photon Launch Delay=None
	Photon Wave Delay=None
	Genesis Launch Delay=None
	IC Powerup Delay=None
	PIG Powerup Delay=None
	Planet Landing/Takeoff Delay=None
	Port Dock/Depart Delay=None
	Ship Transporter Delay=None
	Planet Transporter Delay=None
	Take/Drop Fighters Delay=None
	Drop/Take Mines Delay=None
	 */
	
	public int tavernAnnouncementCost() {
		String str = statsMap.get("Tavern Announcement");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int limpetRemovalCost() {
		String str = statsMap.get("Limpet Removal");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}	

	public int renameShipCost() {
		String str = statsMap.get("Reregister Ship");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int bwarpCost() {
		String str = statsMap.get("Citadel Transport Unit");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int bwarpUpgradeCost() {
		String str = statsMap.get("Citadel Transport Upgrade");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int genTorpCost() {
		String str = statsMap.get("Genesis Torpedo");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int armidCost() {
		String str = statsMap.get("Armid Mine");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int limpetCost() {
		String str = statsMap.get("Limpet Mine");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int beaconCost() {
		String str = statsMap.get("Beacon");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int transWarp1Cost() {
		String str = statsMap.get("Type I TWarp");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int transWarp2Cost() {
		String str = statsMap.get("Type II TWarp");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}

	public int transWarpUpgradeCost() {
		String str = statsMap.get("TWarp Upgrade");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int psyProbeCost() {
		String str = statsMap.get("Psychic Probe");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int planetScanCost() {
		String str = statsMap.get("Planet Scanner");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int atomicCost() {
		String str = statsMap.get("Atomic Detonator");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int corbomiteCost() {
		String str = statsMap.get("Corbomite");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int probeCost() {
		String str = statsMap.get("Ether Probe");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int photonCost() {
		String str = statsMap.get("Photon Missile");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int cloakCost() {
		String str = statsMap.get("Cloaking Device");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int disruptorCost() {
		String str = statsMap.get("Mine Disruptor");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int holoScannerCost() {
		String str = statsMap.get("Holographic Scanner");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	public int densityScannerCost() {
		String str = statsMap.get("Density Scanner");
		return ParserUtils.parseThousands(str.substring(0, str.indexOf(' ')));
	}
	
	// universe	
		
	public int sectors() {
		return Integer.parseInt(statsMap.get("Sectors"));
	}
	
	public int maxTraders() {
		return Integer.parseInt(statsMap.get("Users"));
	}
	
	public int maxAliens() {
		return Integer.parseInt(statsMap.get("Aliens"));
	}
	
	public int maxShips() {
		return Integer.parseInt(statsMap.get("Ships"));
	}

	public int maxPorts() {
		return Integer.parseInt(statsMap.get("Ports"));
	}
	
	public int maxPlanets() {
		return Integer.parseInt(statsMap.get("Planets"));
	}
	
	public int maxCourseLength() {
		return Integer.parseInt(statsMap.get("Max Course Length"));
	}
	
	/* TODO
	[Tournament]
	Tournament Mode=0
	Days To Enter=0 Days
	Lockout Mode=None
	Max Times Blown Up=N/A
	 */
	
	// dynamic
	
	/**
	 * The fictional game date.  In recent versions of TWGSv2, this is always
	 * twelve years in the future.
	 */
	public Date gameDate() {
		return ParserUtils.parseShortDate(statsMap.get("Local Game Time"));
	}
	
	public int activeTraders() {
		return Integer.parseInt(statsMap.get("Active Players"));
	}
	
	public int goodTraderPercent() {
		String str = statsMap.get("Percent Players Good");
		if("N/A".equals(str)) return 0;
		else return Integer.parseInt(str);
	}
	
	public int activeAliens() {
		return Integer.parseInt(statsMap.get("Active Aliens"));
	}	
	
	public int goodAlienPercent() {
		String str = statsMap.get("Percent Aliens Good");
		if("N/A".equals(str)) return 0;
		else return Integer.parseInt(str);
	}
	
	public int activePorts() {
		return Integer.parseInt(statsMap.get("Active Ports"));
	}

	public int portValue() {
		return Integer.parseInt(statsMap.get("Port Value"));
	}
	
	public int activePlanets() {
		return Integer.parseInt(statsMap.get("Active Planets"));
	}
	
	public int planetCitadelPercent() {
		String str = statsMap.get("Percent Planet Citadels");
		if("N/A".equals(str)) return 0;
		else return Integer.parseInt(str);
	}
	
	public int activeShips() {
		return Integer.parseInt(statsMap.get("Active Ships"));
	}
	public int activeCorps() {
		return Integer.parseInt(statsMap.get("Active Corps"));
	}
	public int activeFighters() {
		return Integer.parseInt(statsMap.get("Active Figs"));
	}
	public int activeMines() {
		return Integer.parseInt(statsMap.get("Active Mines"));
	}
}

