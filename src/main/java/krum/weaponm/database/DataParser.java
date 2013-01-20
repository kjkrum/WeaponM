package krum.weaponm.database;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import krum.jplex.UnderflowException;
import krum.weaponm.WeaponM;
import krum.weaponm.database.lexer.DataEventListener;
import krum.weaponm.database.lexer.DataEventLogger;
import krum.weaponm.database.lexer.DataLexer;
import krum.weaponm.database.lexer.DataState;
import krum.weaponm.database.lexer.DataStateLogger;
import krum.weaponm.gui.GUI;
import krum.weaponm.script.ScriptEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data parser.  Although this class is public, scripts cannot normally
 * obtain a reference to the Weapon's instance of it.
 */
public class DataParser implements DataEventListener, Constants {
	private static final Logger log = LoggerFactory.getLogger(DataParser.class);
	
	private final WeaponM weapon;
	private final DataLexer lexer;
	private final DataEventLogger dataLogger = new DataEventLogger();
	private final DataStateLogger stateLogger = new DataStateLogger();
	private final Database database;
	private final You you;
	
	// parser state
	private final Map<String, String> gameStatsMap = new HashMap<String, String>();
	private ScriptEvent currentPrompt; // major prompts only
	// sectors
	private Sector parsingSector; // sector being displayed, holo-scanned, or probed
	// ports
	private Port parsingPort; // port being docked at or reported in computer
	private final char[] parsingPortIndicators = new char[3]; // buying or selling
	private final int[] parsingPortLevels = new int[3];
	private final int[] parsingPortPercents = new int[3];
	private int lastCimPortSector = Integer.MAX_VALUE;
	// trades
	private final TradingState tradingState = new TradingState();
	// warp discovery
	private int[][] newWarps;
	private int newWarpCount = 0;
	// movement
	private MoveMode moveMode = null;
	private int moveTarget = NULL_SECTOR;
	private int moveDistance = UNKNOWN;	

	DataParser(WeaponM weapon, DataLexer lexer, Database database) {
		this.weapon = weapon;
		this.lexer = lexer;
		lexer.reset();
		lexer.addEventListener(this);
		if(WeaponM.DEBUG_LEXER) enableDebugLogging(true);
		this.database = database;
		you = database.getYou();
	}
	
	public void enableDebugLogging(boolean debug) {
		if(debug) {
			lexer.addEventListener(dataLogger);
			lexer.addStateListener(stateLogger);
		}
		else {
			lexer.removeEventListener(dataLogger);
			lexer.removeStateListener(stateLogger);			
		}
	}

	public int parse(CharSequence seq, int off, int len, boolean endOfInput) throws UnderflowException {
		return lexer.lex(seq, off, len, endOfInput);
	}
	
	// called by network thread after connect
	public void reset() {
		gameStatsMap.clear();
		currentPrompt = null;
		parsingSector = null;

		// reset lexer state without removing listeners
		lexer.jumpState(DataState.UNKNOWN);
	}
		
	/* lexer event methods */

	@Override
	public void pausePrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PAUSE_PROMPT);
	}

	@Override
	public void namePrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.NAME_PROMPT);
	}

	@Override
	public void passwordPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PASSWORD_PROMPT);		
	}

	@Override
	public void invalidPassword(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.INVALID_PASSWORD);		
	}

	@Override
	public void ansiPrompt(CharSequence seq, int off, int len) {
		try {
			weapon.network.write("Y");
		} catch (IOException e) {
			// not this class's problem
		}	
	}

	@Override
	public void gameSelected(CharSequence seq, int off, int len) {
		if(seq.charAt(off) == database.getLoginOptions().getGame()) {
			lexer.jumpState(DataState.CORRECT_GAME);
			log.info("entered correct game");
		}
		else {
			lexer.jumpState(DataState.WRONG_GAME);
			weapon.gui.threadSafeMessageDialog(
					"The selected game letter does not match the\n" +
					"database.  Data parsing has been shut down.",
					"Wrong Game Selected",
					JOptionPane.ERROR_MESSAGE);
			log.info("entered wrong game");
		}		
	}

	@Override
	public void gamePrompt(CharSequence seq, int off, int len) {
		if(gameStatsMap.isEmpty()) {
			try {
				weapon.network.write("*\r\n");
			} catch (IOException e) {
				// not this class's problem
			}
		}
		else {
			weapon.scripts.fireEvent(ScriptEvent.GAME_PROMPT);
		}
	}

	@Override
	public void gameStat(CharSequence seq, int off, int len) {		
		String stat = seq.subSequence(off, off + len).toString();
		int equalsSign = stat.indexOf('=');
		String key = stat.substring(1, equalsSign); // match begins with {LD}
		String value = stat.substring(equalsSign + 1);
		gameStatsMap.put(key, value);		
	}

	@Override
	public void endStats(CharSequence seq, int off, int len) {
		try {
			database.getGameStats().updateAll(gameStatsMap);
			newWarps = new int[database.getGameStats().maxCourseLength()][2];
			if(!database.isInitialized()) {
				database.initialize();
				weapon.gui.firePropertyChange(GUI.DATABASE_INITIALIZED, database, true);
			}
			lexer.jumpState(DataState.CORRECT_GAME);
		} catch(DatabaseIntegrityException e) {
			weapon.gui.threadSafeMessageDialog(
					"The game start day does not match\n" +
					"the date recorded in the database.\n" +
					"Data parsing has been shut down.",
					"Rebang Detected",
					JOptionPane.ERROR_MESSAGE);
			log.error("rebang detected");
			lexer.jumpState(DataState.WRONG_GAME);
		}
	}

	@Override
	public void clearAvoidsPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.CLEAR_AVOIDS_PROMPT);
	}
	
	@Override
	public void cimPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.CIM_PROMPT);
	}

	@Override
	public void cimPortInfo(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		// numbers: sector, level, pct, level, pct, level, pct
		int sectorWidth = Integer.toString(database.getGameStats().sectors()).length() + 2; // +1 for NL, +1 for space following
		char selling[] = new char[3];
		for(int i = 0; i < selling.length; ++i) {
			selling[i] = seq.charAt(off + sectorWidth + 12 * i);
			if(selling[i] == '-') numbers.set(i * 2 + 1, numbers.get(i * 2 + 1) * -1);
		}
		int s = numbers.get(0);
		
		if(s <= lastCimPortSector) { // began a new cim port report
			database.setLastCimPortsDate(new Date());
			for(int t = 1; t < s; ++t) {
				Sector sector = database.getSector(t);
				if(sector.hasPort() && sector.getPort().getTradingClass() != 0) {
					sector.getPort().setStatus(PortStatus.BLOCKED);
				}
			}
		}
		else { // continuing previous cim port report
			for(int t = lastCimPortSector + 1; t < s; ++t) {
				Sector sector = database.getSector(t);
				if(sector.hasPort() && sector.getPort().getTradingClass() != 0) {
					sector.getPort().setStatus(PortStatus.BLOCKED);
				}
			}
		}
		lastCimPortSector = s;

		Sector sector = database.getSector(s);
		sector.setExplored(true);
		Port port = sector.getPort();
		if(port == null) {
			port = new Port(sector);
			sector.setPort(port);
			weapon.gui.firePropertyChange(GUI.SECTOR_UPDATED, null, s);
		}
		port.setTradingClass(selling);
		int levels[] = new int[3];
		int percents[] = new int[3];
		for(int i = 0; i < 3; ++i) {
			levels[i] = numbers.get(i * 2 + 1);
			percents[i] = numbers.get(i * 2 + 2);
		}
		// TODO: check for trading activity
		port.setReport(levels, percents);
		calculateCapacities(port);
		port.setStatus(PortStatus.AVAILABLE);
	}

	@Override
	public void cimSectorInfo(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		int s = numbers.get(0);
		
		Sector sector = database.getSector(s);
		if(!sector.isExplored()) {
			sector.setExplored(true);
			weapon.gui.firePropertyChange(GUI.SECTOR_UPDATED, null, s);
		}
		
		int[] warps = new int[numbers.size() - 1];
		for(int i = 1; i < numbers.size(); ++i) {
			warps[i - 1] = numbers.get(i);
			if(!sector.hasWarpTo(warps[i - 1])) {
				newWarps[newWarpCount][0] = s;
				newWarps[newWarpCount][1] = warps[i - 1];
				++newWarpCount;
			}
		}
		
		if(newWarpCount > 0) {
			sector.setWarpsOut(warps);
			weapon.gui.firePropertyChange(GUI.WARPS_DISCOVERED, null, Arrays.copyOf(newWarps, newWarpCount));
			newWarpCount = 0;
		}
	}

	@Override
	public void cimCoursePlot(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		for(int i = 0; i < numbers.size() - 1; ++i) {
			int s = numbers.get(i);
			Sector sector = database.getSector(s);
			int t = numbers.get(i + 1);
			if(!sector.hasWarpTo(t)) {
				sector.addWarpTo(t);
				newWarps[newWarpCount][0] = s;
				newWarps[newWarpCount][1] = t;
				++newWarpCount;
			}
		}
		weapon.scripts.fireEvent(ScriptEvent.COURSE_PLOT, (Object) ParserUtils.toIntArray(numbers));
		if(newWarpCount > 0) {
			weapon.gui.firePropertyChange(GUI.WARPS_DISCOVERED, null, Arrays.copyOf(newWarps, newWarpCount));
			newWarpCount = 0;
		}
	}

	@Override
	public void avoidSet(CharSequence seq, int off, int len) {
		int id = ParserUtils.findInteger(seq, off, len);
		database.getSector(id).setAvoided(true);
		weapon.gui.firePropertyChange(GUI.SECTOR_UPDATED, null, id);
	}

	@Override
	public void avoidCleared(CharSequence seq, int off, int len) {
		int id = ParserUtils.findInteger(seq, off, len);
		database.getSector(id).setAvoided(false);
		weapon.gui.firePropertyChange(GUI.SECTOR_UPDATED, null, id);
	}

	@Override
	public void allAvoidsCleared(CharSequence seq, int off, int len) {
		for(Sector sector : database.getSectors()) {
			if(sector.isAvoided()) {
				sector.setAvoided(false);
				weapon.gui.firePropertyChange(GUI.SECTOR_UPDATED, null, sector.getNumber());
			}
		}
	}

	

	/* lexer event methods */
	
	@Override
	synchronized public void commandPrompt(CharSequence seq, int off, int len) {
		// hours, minutes, seconds, sector
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		int time = numbers.get(0) * 3600 + numbers.get(1) * 60 + numbers.get(2);
		Sector oldSector = you.getSector();
		Sector newSector = database.getSector(numbers.get(3));
		
		if(oldSector != newSector) {
			you.setSector(newSector);
			//weapon.scripts.fireEvent(ScriptEvent.SECTOR, newSector);
			weapon.gui.firePropertyChange(GUI.SHIP_SECTOR, oldSector == null ? null : oldSector.getNumber(), newSector.getNumber());
		}
		currentPrompt = ScriptEvent.COMMAND_PROMPT;
		weapon.scripts.fireEvent(ScriptEvent.COMMAND_PROMPT, time, newSector);
	}

	/* lexer event methods */
	
	@Override
	synchronized public void computerPrompt(CharSequence seq, int off, int len) {
		// hours, minutes, seconds, sector
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		int time = numbers.get(0) * 3600 + numbers.get(1) * 60 + numbers.get(2);
		Sector oldSector = you.getSector();
		Sector newSector = database.getSector(numbers.get(3));
		if(oldSector != newSector) {
			you.setSector(newSector);
			weapon.gui.firePropertyChange(GUI.SHIP_SECTOR, oldSector.getNumber(), newSector.getNumber());
		}
		if(currentPrompt == ScriptEvent.CITADEL_PROMPT || currentPrompt == ScriptEvent.CITADEL_COMPUTER_PROMPT) {
			currentPrompt = ScriptEvent.CITADEL_COMPUTER_PROMPT;
			weapon.scripts.fireEvent(ScriptEvent.CITADEL_COMPUTER_PROMPT, time, newSector);
		}
		else {
			currentPrompt = ScriptEvent.COMPUTER_PROMPT;
			weapon.scripts.fireEvent(ScriptEvent.COMPUTER_PROMPT, time, newSector);
		}
	}

	@Override
	synchronized public void planetPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PLANET_PROMPT);
		currentPrompt = ScriptEvent.PLANET_PROMPT;
		
	}

	@Override
	synchronized public void citadelPrompt(CharSequence seq, int off, int len) {
		currentPrompt = ScriptEvent.CITADEL_PROMPT;
		weapon.scripts.fireEvent(ScriptEvent.CITADEL_PROMPT);
		
	}

	@Override
	synchronized public void stardockPrompt(CharSequence seq, int off, int len) {
		currentPrompt = ScriptEvent.STARDOCK_PROMPT;
		weapon.scripts.fireEvent(ScriptEvent.STARDOCK_PROMPT);
		
	}

	@Override
	synchronized public void leavingMajorPrompt(CharSequence seq, int off, int len) {
		currentPrompt = null;		
	}
	
	synchronized public ScriptEvent getCurrentPrompt() {
		return currentPrompt;
	}

	@Override
	public void portCredits(CharSequence seq, int off, int len) {
		int credits = ParserUtils.findInteger(seq, off + 67, len - 67);
		you.getSector().getPort().setCredits(credits);
	}

	@Override
	public void showLogPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.SHOW_LOG_PROMPT);
	}

	@Override
	public void deathDelayLockout(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.DEATH_DELAY_LOCKOUT, ParserUtils.findInteger(seq, off + 31, len - 31));
	}

	@Override
	public void permanentLockout(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PERMANENT_LOCKOUT);			
	}

	@Override
	public void loginTurns(CharSequence seq, int off, int len) {
		int newValue = ParserUtils.findInteger(seq, off, len);
		int oldValue = you.getTurns();
		if(oldValue != newValue) {
			you.setTurns(newValue);
			weapon.gui.firePropertyChange(GUI.YOU_TURNS, oldValue, newValue);
		}
	}

	@Override
	public void accessModeLockout(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.ACCESS_MODE_LOCKOUT);		
	}

	@Override
	public void createTraderPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.CREATE_TRADER_PROMPT);
	}
	
	
	@Override
	public void planetTrading(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PLANET_TRADING);
		tradingState.planetTrading = true;
	}

	@Override
	public void shipTrading(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.SHIP_TRADING);
		tradingState.planetTrading = false;
	}

	@Override
	public void tradeInitPrompt(CharSequence seq, int off, int len) {
		tradingState.finalOffer = false;
		
		switch(seq.charAt(off + 31)) {
		case 'F':
			tradingState.product = Product.FUEL_ORE;
			break;
		case 'O':
			tradingState.product = Product.ORGANICS;
			break;
		case 'E':
			tradingState.product = Product.EQUIPMENT;
			break;
		default:
			log.error("failed to identify product at trading prompt");
			tradingState.product = UNKNOWN;
			break;
		}
		tradingState.buying = seq.charAt(off + len - 1) == 'y';
		weapon.scripts.fireEvent(ScriptEvent.TRADE_INIT_PROMPT, tradingState.product, tradingState.buying);		
	}	

	@Override
	public void tradeUnits(CharSequence seq, int off, int len) {
		tradingState.units = ParserUtils.findInteger(seq, off + 21, len - 21);
		weapon.scripts.fireEvent(ScriptEvent.TRADING_UNITS, tradingState.units);		
	}
	

	@Override
	public void finalOffer(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.FINAL_OFFER);
		tradingState.finalOffer = true;
	}
	
	@Override
	public void tradeOfferPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.TRADE_OFFER_PROMPT, ParserUtils.findInteger(seq, off + 25, len - 25));
		// TODO: record in trading state
	}

	@Override
	public void tradeAccepted(CharSequence seq, int off, int len) {
		// update planet or ship, etc.
		if(tradingState.planetTrading) {
			
		}
		else { // ship trading
			int oldCargo = you.getShip().getCargo(tradingState.product);
			int newCargo = (tradingState.buying) ? oldCargo + tradingState.units : oldCargo - tradingState.units;
			you.getShip().setCargo(tradingState.product, newCargo);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, tradingState.product, newCargo);
		}

		weapon.scripts.fireEvent(ScriptEvent.TRADE_ACCEPTED);
		
		// TODO: calculations in trading state
	}

	@Override
	public void tradeRejected(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.TRADE_REJECTED);
		// TODO: record in trading state
	}

	@Override
	public void credits(CharSequence seq, int off, int len) {
		List<Integer> numbers =  ParserUtils.findIntegers(seq, off + 22, len - 22);
		int newCredits = numbers.get(0);
		int oldCredits = you.getCredits();
		if(newCredits != oldCredits) {
			you.setCredits(newCredits);
			weapon.gui.firePropertyChange(GUI.YOU_CREDITS, oldCredits, newCredits);
		}
		// TODO: check trading state
		// always fire script credits during trades
		weapon.scripts.fireEvent(ScriptEvent.CREDITS, newCredits);
		// TODO: calculations in tradingState
	}

	@Override
	public void psyProbeReport(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PSYCHIC_PROBE_REPORT, Float.parseFloat(seq.subSequence(off + 44, off + len).toString()));
		// TODO: calculations in tradingState
	}

	@Override
	public void includeTimestampPrompt(CharSequence seq, int off, int len) {
		try {
			weapon.network.write("Y");
		} catch (IOException e) {
			// not this class's problem
		}	
	}

	@Override
	public void avoidsList(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		for(int s : numbers) {
			database.getSector(s).setAvoided(true);
		}
	}

	@Override
	public void useAliasPrompt(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.USE_ALIAS_PROMPT);
	}

	@Override
	public void densityScan(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		Sector sector = database.getSector(numbers.get(0));
		sector.setDensityData(numbers.get(1), numbers.get(2), numbers.get(3), seq.charAt(seq.length() - 1) == 'Y');
	}

	@Override
	public void probeEnteringSector(CharSequence seq, int off, int len) {
		parsingSector = database.getSector(ParserUtils.findInteger(seq, off + 39, len - 39));		
	}

	@Override
	public void probeSelfDestructs(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PROBE_SELF_DESTRUCTS, parsingSector);		
	}

	@Override
	public void probeDestroyed(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.PROBE_DESTROYED, parsingSector);
		
	}

	@Override
	public void sectorHeader(CharSequence seq, int off, int len) {
		int sector = ParserUtils.findInteger(seq, off + 28, len - 28);
		parsingSector = database.getSector(sector);
		parsingSector.setExplored(true);
		parsingSector.setHoloDate(new Date());
		
		// process movement
		if(moveMode != null) {
			if(moveTarget == sector) {
				// set your location
				int oldSector = you.getSectorNumber();
				you.setSector(parsingSector);
				weapon.gui.firePropertyChange(GUI.SHIP_SECTOR, oldSector, sector);
			
				// deduct turns & ore
				int oldTurns = you.getTurns();
				if(moveMode == MoveMode.BWARP) {
					int newTurns = oldTurns - 1;
					you.setTurns(newTurns);
					weapon.gui.firePropertyChange(GUI.YOU_TURNS, oldTurns, newTurns);
					
					// TODO: deduct ore from planet
					// TODO: clear "on planet" status... or do it in command prompt?
				}
				else {
					ShipType type = you.getShip().getType();
					if(type != null) {
					int tpw = you.getShip().getType().getTurnsPerWarp();
						if(tpw != UNKNOWN) {
							int newTurns = oldTurns - tpw;
							you.setTurns(newTurns);
							weapon.gui.firePropertyChange(GUI.YOU_TURNS, oldTurns, newTurns);
						}
					}
				}					
				
				if(moveMode == MoveMode.TWARP) {
					int oldOre = you.getShip().getCargo(Cargo.FUEL_ORE);
					int newOre = oldOre - moveDistance * 3;
					you.getShip().setCargo(Cargo.FUEL_ORE, newOre);
					weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.FUEL_ORE, newOre);
				}
	
			}
			else {
				// suggests that some form of movement cancellation isn't being parsed
				log.error("parsingSector == {}, moveTarget == {}", sector, moveTarget);
			}
			// always clear moveMode
			moveMode = null;
		}
		
		// TODO: also process movement on quasar blast?		
	}

	@Override
	public void nebulaName(CharSequence seq, int off, int len) {
		// -1 because match will always end with ' ' or '.'
		parsingSector.setNebula(seq.subSequence(off + 14, off + len - 1).toString());
	}

	@Override
	public void portStatusUnderConstruction(CharSequence seq, int off, int len) {
		parsingSector.getPort().setStatus(PortStatus.UNDER_CONSTRUCTION);		
	}

	@Override
	public void portStatusDestroyed(CharSequence seq, int off, int len) {
		Port port = parsingSector.getPort();
		if(port == null) {
			port = new Port(database.getSector(parsingSector.getNumber()));
			parsingSector.setPort(port);
		}
		port.setStatus(PortStatus.DESTROYED);
	}
	
	@Override
	public void sectorFightersYours(CharSequence seq, int off, int len) {
		int fighters = ParserUtils.findInteger(seq, off, len);
		FighterMode mode = FighterMode.getMode(seq.charAt(off + len - 1));
		parsingSector.setFighters(fighters, you, mode);		
	}

	@Override
	public void sectorFightersYourCorp(CharSequence seq, int off, int len) {
		int fighters = ParserUtils.findInteger(seq, off, len);
		FighterMode mode = FighterMode.getMode(seq.charAt(off + len - 1));
		parsingSector.setFighters(fighters, you.getCorp(), mode);	
	}

	@Override
	public void sectorFightersOther(CharSequence seq, int off, int len) {
		int fighters = ParserUtils.findInteger(seq, off, len);
		FighterMode mode = FighterMode.getMode(seq.charAt(off + len - 1));
		// TODO: identify correct owner
		parsingSector.setFighters(fighters, SpecialOwner.UNKNOWN, mode);
	}

	@Override
	public void sectorFightersCorp(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		int fighters = numbers.get(0);
		FighterMode mode = FighterMode.getMode(seq.charAt(off + len - 1));
		// TODO: identify correct owner
		parsingSector.setFighters(fighters, SpecialOwner.UNKNOWN, mode);	
	}

	@Override
	public void sectorFightersPirate(CharSequence seq, int off, int len) {
		int fighters = ParserUtils.findInteger(seq, off, len);
		FighterMode mode = FighterMode.getMode(seq.charAt(off + len - 1));
		parsingSector.setFighters(fighters, SpecialOwner.SPACE_PIRATES, mode);		
	}

	@Override
	public void sectorWarps(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		int[] warps = new int[numbers.size()];
		for(int i = 0; i < warps.length; ++i) {
			warps[i] = numbers.get(i);
			if(!parsingSector.hasWarpTo(warps[i])) {
				newWarps[newWarpCount][0] = parsingSector.getNumber();
				newWarps[newWarpCount][1] = warps[i];
				++newWarpCount;
			}
		}
		if(newWarpCount > 0) {
			parsingSector.setWarpsOut(warps);
			weapon.gui.firePropertyChange(GUI.WARPS_DISCOVERED, null, Arrays.copyOf(newWarps, newWarpCount));
			newWarpCount = 0;
		}
	}

	@Override
	public void sectorPort(CharSequence seq, int off, int len) {
		String name = ParserUtils.findPrintable(seq, off + 28, len - 28);
		int portClass = seq.charAt(off + len - 1) - 48;
		Port port = parsingSector.getPort();
		if(port == null) {
			if(portClass == 9) {
				port = new Stardock(database.getSector(parsingSector.getNumber()));
			}
			else {
				port = new Port(database.getSector(parsingSector.getNumber()));
				port.setTradingClass(portClass);
			}
			parsingSector.setPort(port);
		}
		if(portClass == 0) {
			database.addZeroSector(parsingSector.getNumber());
		}
		else if(portClass == 9) {
			database.setStardockSector(parsingSector.getNumber());
			if(port.getPortClass() != 9) {
				port = new Stardock(port);
				parsingSector.setPort(port);
			}
		}
		port.setName(name);
	}

	@Override
	public void inactivityWarning(CharSequence seq, int off, int len) {
		weapon.scripts.fireEvent(ScriptEvent.INACTIVITY_WARNING);		
	}

	@Override
	public void commCompact(CharSequence seq, int off, int len) {
		// "&#27;\[K&#27;\[(1A&#27;\[)?3[236]m[FRP] &#27;\[1(;36)?m{PRINTABLE}+&#27;\[33m{PRINTABLE}+"
		List<String> printables = ParserUtils.findPrintables(seq, off, len);
		// element 0: "[FRP] "
		// element 1: name
		// last element: message
		MessageType type = null;
		switch (printables.get(0).charAt(0)) {
		case 'F': type = MessageType.FED_COMM; break;
		case 'R': type = MessageType.SUBSPACE_RADIO; break;
		case 'P': type = MessageType.PRIVATE_HAIL; break;
		}
		Trader sender = database.getOrCreateTrader(printables.get(1).trim());
		String message = printables.get(printables.size() - 1);
		weapon.scripts.fireEvent(ScriptEvent.CHAT_MESSAGE, type, sender, message);
	}

	@Override
	public void commLong(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[32mIncoming transmission from &#27;\[1;36m{PRINTABLE}+&#27;\[0;32m( on (Federation comm-link|channel &#27;\[1;36m0&#27;\[0;32m))?:&#13;&#27;\[0m&#10;(&#7;&#7;)?&#27;\[1;33m{PRINTABLE}+"
		List<String> printables = ParserUtils.findPrintables(seq, off, len);
		// F (4): "Incoming transmission from ", sender, " on Federation comm-link:", message
		// R (6): "Incoming transmission from ", sender, " on channel ", channel, ":", message
		// P (4): "Incoming transmission from ", sender, ":", message
		MessageType type;
		if(printables.size() == 4) {
			if(":".equals(printables.get(2))) {
				type = MessageType.PRIVATE_HAIL;
			}
			else type = MessageType.FED_COMM;
		}
		else type = MessageType.SUBSPACE_RADIO;
		Trader sender = database.getOrCreateTrader(printables.get(1).trim());
		String message = printables.get(printables.size() - 1);
		weapon.scripts.fireEvent(ScriptEvent.CHAT_MESSAGE, type, sender, message);		
	}
	
	@Override
	public void hailHeader(CharSequence seq, int off, int len) {
		// "(&#27;\[K&#27;\[1A)?&#27;\[1;36m{PRINTABLE}+&#27;\[0;32m is hailing you!"
		// this will always contain the trader's full name, so do a lookup to save it
		String name = ParserUtils.findPrintable(seq, off, len).trim();
		database.getOrCreateTrader(name);
	}

	@Override
	public void portInArbitrarySector(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mWhat sector is the port in\? &#27;\[1;33m\[{SECTOR}\] [0-9\010 ]+"
		// if input resolves to an empty string, then port about to be parsed is in current sector
		String string = seq.subSequence(off, off + len).toString();
		int inputIdx = string.indexOf(']', 42) + 2;
		String input = ParserUtils.stripBackspaces(string.substring(inputIdx));
		if("".equals(input)) {
			parsingPort = you.getSector().getPort();
		}
		// if user creates a new db for an explored game, they might parse a
		// port report for a sector where port is not recorded.  so create one.
		else {
			Sector sector = database.getSector(Integer.parseInt(input)); 
			if (sector.hasPort()) parsingPort = sector.getPort();
			else {
				parsingPort = new Port(sector);
				sector.setPort(parsingPort);
			}			
		}
	}

	@Override
	public void portInCurrentSector(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[1;5;31mDocking\.\.\."
		parsingPort = you.getSector().getPort();
	}

	@Override
	public void portReportHeader(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[1;33mCommerce report for &#27;\[36m{PRINTABLE}+"
		parsingPort.setName(seq.subSequence(off + 33, off + len).toString());
		//System.err.println("port name: " + parsingPortName);
	}

	@Override
	public void portReport(CharSequence seq, int off, int len) {
		// columns always aligned like so:
		// "{LD}&#27;\[1;36mFuel Ore &#27;\[0;32m  Selling &#27;\[1;36m  2510&#27;\[0;32m    100"
		List<String> printables = ParserUtils.findPrintables(seq, off, len);
		int product = -1;
		switch(printables.get(0).charAt(0)) {
		case 'F': product = Product.FUEL_ORE; break;
		case 'O': product = Product.ORGANICS; break;
		case 'E': product = Product.EQUIPMENT; break;
		}
		boolean buying = (printables.get(1).charAt(2) == 'B');
		parsingPortIndicators[product] = buying ? 'B' : 'F';
		int level = Integer.parseInt(printables.get(2).trim());
		if(buying) level *= -1;
		int percent = Integer.parseInt(printables.get(3).trim());
		parsingPortLevels[product] = level;
		parsingPortPercents[product] = percent;
		// when we get the equipment line, update the port
		if(product == Product.EQUIPMENT) {
			// TODO: check for trading activity
			parsingPort.setReport(parsingPortLevels, parsingPortPercents);
			calculateCapacities(parsingPort);
			/* if(parsingPort.getTradingClass() == UNKNOWN) */ parsingPort.setTradingClass(parsingPortIndicators); // always do this?
			parsingPort.setStatus(PortStatus.AVAILABLE);
		}		
	}	
	
	void calculateCapacities(Port port) {
		int[] levels = port.getLevels();
		int[] percents = port.getPercents();
		int[] capacities = port.getCapacities();
		
		for(int i = 0; i < 3; ++i) {
			if(levels[i] == 0) continue; // can't calculate anything from a 0 level
			int cap;
			// if port is at 100%, capacity equals level
			if(percents[i] == 100) cap = levels[i];
			// else if current cap could produce displayed percent, roll with it
			else if(capacities[i] != 0 && levels[i] / capacities[i] == percents[i]) {
				continue;
			}
			// else calculate the smallest absolute value of the cap that could produce the displayed percent
			else {
				// I think this algorithm is right...
				cap = levels[i] * 100 / (percents[i] + 1);
				if(levels[i] > 0) cap += 10 - cap % 10;
				else cap -= 10 + cap % 10;
			}
			// calculated cap might have smaller absolute value than previously known cap;
			// this is likely when level is small.  cap can't actually decrease, so only
			// change it if it increased.
			int diff = Math.abs(cap) - Math.abs(capacities[i]);
			if(diff > 0) {
				port.setCapacity(i, cap);
				// if this isn't the first capacity recorded for this port, fire an upgrade event
				if(capacities[i] != 0) {
					weapon.scripts.fireEvent(ScriptEvent.PORT_UPGRADED, port, i, diff);	
				}
			}
		}
	}
	
	// info screen stuff
	
	@Override
	public void infoTraderName(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mTrader Name    &#27;\[1;33m: &#27;\[0;32m{PRINTABLE}+"
		String rankName = seq.subSequence(off + 37, off + len).toString();
		you.setName(database.resolveName(rankName));
		you.setRank(database.resolveRank(rankName));
		
		// fresh ship details incoming
		you.getShip().resetDetails();
	}

	@Override
	public void infoXpAlign(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mRank and Exp   &#27;\[1;33m: &#27;\[36m{THOUSANDS}&#27;\[0;32m points&#27;\[1;33m,&#27;\[0;32m Alignment&#27;\[1;33m=&#27;\[36m-?{THOUSANDS}"
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		
		int oldXP = you.getXp();
		int newXP = numbers.get(0);
		if(oldXP != newXP) {
			you.setXp(newXP);
			weapon.gui.firePropertyChange(GUI.YOU_XP, oldXP, newXP);
		}
		
		int oldAlign = you.getAlign();
		int newAlign = numbers.get(1);
		if(oldAlign != newAlign) {
			you.setAlign(newAlign);
			weapon.gui.firePropertyChange(GUI.YOU_ALIGN, oldAlign, newAlign);
		}
	}

	@Override
	public void infoTimesBlownUp(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mTimes Blown Up &#27;\[1;33m: &#27;\[0;32m{NN_INTEGER}"
		you.setTimesBlownUp(Integer.parseInt(seq.subSequence(off + 37, off + len).toString()));		
	}

	@Override
	public void infoCorp(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mCorp           &#27;\[1;33m\# &#27;\[0;32m{NN_INTEGER}&#27;\[1;33m, &#27;\[0;32m{PRINTABLE}+"
		List<String> printables = ParserUtils.findPrintables(seq, off + 37, len - 37);
		Corporation corp = database.getOrCreateCorp(Integer.parseInt(printables.get(0)));
		corp.setName(printables.get(2));
		you.setCorp(corp);
	}

	@Override
	public void infoShipName(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mShip Name      &#27;\[1;33m: &#27;\[0;32m{PRINTABLE}+"
		you.getShip().setName(seq.subSequence(off + 37, off + len).toString());	
	}

	@Override
	public void infoShipType(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mShip Info      &#27;\[1;33m: &#27;\[0;32m&#27;\[0m({PRINTABLE}|{ANSI})+ &#27;\[0m({PRINTABLE}|{ANSI})+"
		String string = seq.subSequence(off, off + len).toString();
		// hack off the part about ports and kills
		string = string.substring(0, string.lastIndexOf("\033[35mPorted"));
		// manufacturer and type are always separated by an ansi reset
		String typeName = string.substring(string.lastIndexOf("\033[0m") + 4);
		ShipType newType = database.getOrCreateShipType(ParserUtils.stripANSI(typeName).trim());
		ShipType oldType = you.getShip().getType();
		if(oldType == null) {
			you.getShip().setType(newType);
		}
		else if(oldType != newType) {
			Ship oldShip = you.getShip();
			Ship newShip = new Ship();
			newShip.setName(oldShip.getName());
			newShip.setType(newType);
			you.setShip(newShip);
			database.removeShip(oldShip.getNumber());
		}
	}

	@Override
	public void infoShipDate(CharSequence seq, int off, int len) {
		Date newDate = ParserUtils.parseLongDate(seq.subSequence(off + 37, off + len).toString());
		Date oldDate = you.getShip().getBuildDate();
		if(oldDate == null) {
			you.getShip().setBuildDate(newDate);
		}	
		else if(!newDate.equals(oldDate)) {
			Ship oldShip = you.getShip();
			Ship newShip = new Ship();
			newShip.setName(oldShip.getName());
			newShip.setType(oldShip.getType());
			newShip.setBuildDate(newDate);
			you.setShip(newShip);
			database.removeShip(oldShip.getNumber());			
		}
	}

	@Override
	public void infoTurns(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mTurns left     &#27;\[1;33m: &#27;\[(5;31|36)m{NN_INTEGER}"
		int oldTurns = you.getTurns();
		int newTurns = ParserUtils.findInteger(seq, off + 30, len - 30);
		if(oldTurns != newTurns) {
			you.setTurns(newTurns);
			weapon.gui.firePropertyChange(GUI.YOU_TURNS, oldTurns, newTurns);
		}
	}

	@Override
	public void infoPhotons(CharSequence seq, int off, int len) {
		you.getShip().setPhotons(ParserUtils.findInteger(seq, off, len));
	}
	

	@Override
	public void infoTW1Range(CharSequence seq, int off, int len) {
		you.getShip().setTransWarp(TransWarp.TYPE_1);
		// if a ship can have transwarp, it can have both types
		// hopefully this will become an option in the future
		//you.getShip().getType().setTransWarpLevel(TransWarp.TYPE_2);		
	}

	@Override
	public void infoTW2Range(CharSequence seq, int off, int len) {
		you.getShip().setTransWarp(TransWarp.TYPE_2);
		// will have been set when TW1 range was seen
		//you.getShip().getType().setTransWarpLevel(TransWarp.TYPE_2);
	}

	@Override
	public void infoLimpets(CharSequence seq, int off, int len) {
		// "&#27;\[0;35mLimpet Mines T2&#27;\[1;33m: &#27;\[36m{NN_INTEGER}"
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		you.getShip().setLimpets(numbers.get(1));	
	}

	@Override
	public void infoTPW(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mTurns to Warp  &#27;\[37m&#27;\[35m&#27;\[1;33m: &#27;\[0;32m{NN_INTEGER}"
		you.getShip().getType().setTurnsPerWarp(ParserUtils.findInteger(seq, off, len));
	}

	@Override
	public void infoAtomics(CharSequence seq, int off, int len) {
		you.getShip().setAtomics(ParserUtils.findInteger(seq, off, len));
	}

	@Override
	public void infoHolds(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mTotal Holds    &#27;\[1;33m: &#27;\[36m{NN_INTEGER}"
		int newHolds = Integer.parseInt(seq.subSequence(off + 35, off + len).toString());
		int oldHolds = you.getShip().getHolds();
		if(newHolds != oldHolds) {
			you.getShip().setHolds(newHolds);
			weapon.gui.firePropertyChange(GUI.SHIP_HOLDS, oldHolds, newHolds);
		}
	}

	@Override
	public void infoProbes(CharSequence seq, int off, int len) {
		// "&#27;\[0;35mEther Probes   &#27;\[37m&#27;\[35m&#27;\[1;33m: &#27;\[36m{NN_INTEGER}"
		you.getShip().setProbes(ParserUtils.findInteger(seq, off, len));
	}

	@Override
	public void infoDisruptors(CharSequence seq, int off, int len) {
		you.getShip().setDisruptors(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void infoCorbomite(CharSequence seq, int off, int len) {
		you.getShip().setCorbomite(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void infoShields(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getShields();
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setShields(newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_SHIELDS, oldValue, newValue);
		}
	}

	@Override
	public void infoFighters(CharSequence seq, int off, int len) {
		int newValue = ParserUtils.findInteger(seq, off, len);
		int oldValue = you.getShip().getFighters();
		if(newValue != oldValue) {
			you.getShip().setFighters(newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_FIGHTERS, oldValue, newValue);
		}
	}

	@Override
	public void infoPsyProbe(CharSequence seq, int off, int len) {
		// message only appears if you have one
		you.getShip().setPsyProbe(true);
	}

	@Override
	public void infoInvOre(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.FUEL_ORE);
		int newValue = Integer.parseInt(seq.subSequence(off + 21, off + len).toString());
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.FUEL_ORE, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.FUEL_ORE, newValue);
		}
	}

	@Override
	public void infoInvEqu(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.EQUIPMENT);
		int newValue = Integer.parseInt(seq.subSequence(off + 22, off + len).toString());
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.EQUIPMENT, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.EQUIPMENT, newValue);
		}
	}

	@Override
	public void infoPlanetScanner(CharSequence seq, int off, int len) {
		// message only appears if you have one
		you.getShip().setPlanetScan(true);
		
	}

	@Override
	public void infoLongRangeScanner(CharSequence seq, int off, int len) {
		// message only appears if you have one
		if(seq.charAt(off + len - 1) == 'H') {
			you.getShip().setLongRangeScan(Scanner.HOLOGRAPHIC);
		}
		else {
			you.getShip().setLongRangeScan(Scanner.DENSITY);
		}
	}

	@Override
	public void infoGenTorps(CharSequence seq, int off, int len) {
		you.getShip().setGenTorps(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void infoInvOrg(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.ORGANICS);
		int newValue = Integer.parseInt(seq.subSequence(off + 21, off + len).toString());
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.ORGANICS, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.ORGANICS, newValue);
		}
	}

	@Override
	public void infoCredits(CharSequence seq, int off, int len) {
		// "{LD}&#27;\[35mCredits        &#27;\[37m&#27;\[35m&#27;\[1;33m: &#27;\[36m{THOUSANDS}"
		int newCredits = ParserUtils.findInteger(seq, off + 37, len - 37);
		int oldCredits = you.getCredits();
		if(newCredits != oldCredits) {
			you.setCredits(newCredits);
			weapon.gui.firePropertyChange(GUI.YOU_CREDITS, oldCredits, newCredits);
		}
	}

	@Override
	public void infoBeacons(CharSequence seq, int off, int len) {
		you.getShip().setBeacons(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void infoCloaks(CharSequence seq, int off, int len) {
		you.getShip().setCloaks(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void infoInvColos(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.COLONISTS);
		int newValue = Integer.parseInt(seq.subSequence(off + 22, off + len).toString());
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.COLONISTS, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.COLONISTS, newValue);
		}
	}

	@Override
	public void infoTractor(CharSequence seq, int off, int len) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoArmids(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		you.getShip().setArmids(numbers.get(1));		
	}

	@Override
	public void infoInterdictOn(CharSequence seq, int off, int len) {
		if(seq.charAt(off + len - 1) == 'Y') {
			you.getShip().setInterdictOn(true);
		}
		else {
			you.getShip().setInterdictOn(false);
		}
		
	}

	@Override
	public void quickPsyProbe(CharSequence seq, int off, int len) {
		you.getShip().setPsyProbe(seq.charAt(off + len - 1) == 'Y');		
	}

	@Override
	public void quickAtomics(CharSequence seq, int off, int len) {
		you.getShip().setAtomics(ParserUtils.findInteger(seq, off, len));			
	}

	@Override
	public void quickPlanetScan(CharSequence seq, int off, int len) {
		you.getShip().setPlanetScan(seq.charAt(off + len - 1) == 'Y');		
		
	}

	@Override
	public void quickHolds(CharSequence seq, int off, int len) {
		int newHolds = ParserUtils.findInteger(seq, off, len);
		int oldHolds = you.getShip().getHolds();
		if(newHolds != oldHolds) {
			you.getShip().setHolds(newHolds);
			weapon.gui.firePropertyChange(GUI.SHIP_HOLDS, oldHolds, newHolds);
		}
	}

	@Override
	public void quickInvCol(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.COLONISTS);
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.COLONISTS, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.COLONISTS, newValue);
		}
	}

	@Override
	public void quickAlign(CharSequence seq, int off, int len) {
		int oldAlign = you.getAlign();
		int newAlign = ParserUtils.findInteger(seq, off, len);
		if(oldAlign != newAlign) {
			you.setAlign(newAlign);
			weapon.gui.firePropertyChange(GUI.YOU_ALIGN, oldAlign, newAlign);
		}
	}

	@Override
	public void quickBeacons(CharSequence seq, int off, int len) {
		you.getShip().setBeacons(ParserUtils.findInteger(seq, off, len));	
	}

	@Override
	public void quickArmids(CharSequence seq, int off, int len) {
		you.getShip().setArmids(ParserUtils.findInteger(seq, off, len));	
	}

	@Override
	public void quickShipNumber(CharSequence seq, int off, int len) {
		you.getShip().setNumber(ParserUtils.findInteger(seq, off, len));
	}

	@Override
	public void quickDisruptors(CharSequence seq, int off, int len) {
		you.getShip().setDisruptors(ParserUtils.findInteger(seq, off, len));			
	}

	@Override
	public void quickPhotons(CharSequence seq, int off, int len) {
		you.getShip().setPhotons(ParserUtils.findInteger(seq, off, len));			
	}

	@Override
	public void quickGenTorps(CharSequence seq, int off, int len) {
		you.getShip().setGenTorps(ParserUtils.findInteger(seq, off, len));			
	}

	@Override
	public void quickInvOrg(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.ORGANICS);
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.ORGANICS, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.ORGANICS, newValue);
		}
	}

	@Override
	public void quickInvOre(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.FUEL_ORE);
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.FUEL_ORE, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.FUEL_ORE, newValue);
		}
	}

	@Override
	public void quickInvEqu(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getCargo(Cargo.EQUIPMENT);
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setCargo(Cargo.EQUIPMENT, newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_CARGO, Cargo.EQUIPMENT, newValue);
		}	
	}

	@Override
	public void quickShields(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getShields();
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.getShip().setShields(newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_SHIELDS, oldValue, newValue);
		}		
	}

	@Override
	public void quickCorbomite(CharSequence seq, int off, int len) {
		you.getShip().setCorbomite(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void quickFighters(CharSequence seq, int off, int len) {
		int oldValue = you.getShip().getFighters();
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(newValue != oldValue) {
			you.getShip().setFighters(newValue);
			weapon.gui.firePropertyChange(GUI.SHIP_FIGHTERS, oldValue, newValue);
		}
	}

	@Override
	public void quickXP(CharSequence seq, int off, int len) {
		int oldValue = you.getXp();
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(newValue != oldValue) {
			you.setXp(newValue);
			weapon.gui.firePropertyChange(GUI.YOU_XP, oldValue, newValue);
		}
	}

	@Override
	public void quickTransWarp(CharSequence seq, int off, int len) {
		you.getShip().setTransWarp(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void quickLongRangeScan(CharSequence seq, int off, int len) {
		switch(seq.charAt(off + len - 1)) {
		case 'H':
			you.getShip().setLongRangeScan(Scanner.HOLOGRAPHIC);
			break;
		case 'D':
			you.getShip().setLongRangeScan(Scanner.DENSITY);
			break;
		case 'N':
			you.getShip().setLongRangeScan(Scanner.NONE);
			break;
		}
	}

	@Override
	public void quickCorp(CharSequence seq, int off, int len) {
		you.setCorp(database.getOrCreateCorp(ParserUtils.findInteger(seq, off, len)));
	}

	@Override
	public void quickProbes(CharSequence seq, int off, int len) {
		you.getShip().setProbes(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void quickLimpets(CharSequence seq, int off, int len) {
		you.getShip().setLimpets(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void quickCredits(CharSequence seq, int off, int len) {
		int newCredits = ParserUtils.findInteger(seq, off, len);
		int oldCredits = you.getCredits();
		if(newCredits != oldCredits) {
			you.setCredits(newCredits);
			weapon.gui.firePropertyChange(GUI.YOU_CREDITS, oldCredits, newCredits);
		}
	}

	@Override
	public void quickTurns(CharSequence seq, int off, int len) {
		int oldValue = you.getTurns();
		int newValue = ParserUtils.findInteger(seq, off, len);
		if(oldValue != newValue) {
			you.setTurns(newValue);
			weapon.gui.firePropertyChange(GUI.YOU_TURNS, oldValue, newValue);
		}
	}

	@Override
	public void quickSector(CharSequence seq, int off, int len) {
		you.getShip().setSector(database.getSector(ParserUtils.findInteger(seq, off, len)));
	}

	@Override
	public void quickCloaks(CharSequence seq, int off, int len) {
		you.getShip().setCloaks(ParserUtils.findInteger(seq, off, len));		
	}

	@Override
	public void warpTarget(CharSequence seq, int off, int len) {
		// "Warping to..." or "Auto warping to..."
		moveMode = MoveMode.WARP;
		moveTarget = ParserUtils.findInteger(seq, off, len);
		//log.debug("warpingTo = {}", warpingTo);
	}
	

	@Override
	public void mkeyWarpTarget(CharSequence seq, int off, int len) {
		String input = seq.subSequence(off + 24, off + len - 24).toString();
		input = ParserUtils.stripBackspaces(input);
		if(input.length() > 0) {
			moveMode = MoveMode.WARP;
			moveTarget = Integer.parseInt(input);
		}
	}

	@Override
	public void transWarpTarget(CharSequence seq, int off, int len) {
		// "Sector {nnn} is {m} hops away"
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		moveMode = MoveMode.BWARP; // changes to TWARP if transwarp engaged message is seen
		moveTarget = numbers.get(0);
		moveDistance = numbers.get(1);
	}
	
	@Override
	public void transWarpEngaged(CharSequence seq, int off, int len) {
		moveMode = MoveMode.TWARP;
	}

	@Override
	public void cancelWarp(CharSequence seq, int off, int len) {
		moveMode = null;		
	}	

	@Override
	public void cancelTransWarp(CharSequence seq, int off, int len) {
		moveMode = null;		
	}
	
	@Override
	public void interdicted(CharSequence seq, int off, int len) {
		moveMode = null;
		weapon.scripts.fireEvent(ScriptEvent.INTERDICTED);	
	}

	@Override
	public void noRoutePrompt(CharSequence seq, int off, int len) {
		List<Integer> numbers = ParserUtils.findIntegers(seq, off, len);
		weapon.scripts.fireEvent(ScriptEvent.NO_ROUTE_PROMPT, numbers.get(1), numbers.get(2));
	}

}
