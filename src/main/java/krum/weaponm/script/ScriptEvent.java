package krum.weaponm.script;

/**
 * Events received by scripts.  Events have a variable number of parameters.
 * The types and meanings of each event's parameters are documented here.
 * <p>
 * Events with names ending in "_PROMPT" indicate that the server is waiting
 * for user input.
 */
public enum ScriptEvent {
	// major prompts
	/** The command prompt.<p>Parameters: {@link Integer} seconds, {@link krum.weaponm.database.Sector} sector */
	COMMAND_PROMPT,
	/** The computer prompt on a ship.<p>Parameters: {@link Integer} seconds, {@link krum.weaponm.database.Sector} sector */
	COMPUTER_PROMPT,
	/** The prompt when you have landed on a planet. */
	PLANET_PROMPT,
	/** The prompt when you have entered a citadel. */
	CITADEL_PROMPT,
	/** The computer prompt inside a citadel.<p>Parameters: {@link Integer} seconds, {@link krum.weaponm.database.Sector} sector */
	CITADEL_COMPUTER_PROMPT,
	/** The prompt when you are docked at Stardock. */
	STARDOCK_PROMPT,
	
	// minor prompts
	/** The ubiquitous "[Pause]". */
	PAUSE_PROMPT,
	/** The "Enter your choice:" prompt outside the game.  Sometimes called the "T-menu". */
	GAME_PROMPT,
	/** Either of the prompts requesting your name during login. */
	NAME_PROMPT,
	/** The password prompt. */
	PASSWORD_PROMPT,
	/** The "Show today's log?" prompt during login. */
	SHOW_LOG_PROMPT,
	/** The "Do you wish to clear some avoids?" prompt.  Seen during login and within the game. */
	CLEAR_AVOIDS_PROMPT,
	/** The CIM's ":" prompt. */
	CIM_PROMPT,
	/** The "Would you like to start a new character in this game?" prompt during login. */
	CREATE_TRADER_PROMPT,
	/** The "Use (N)ew name or (B)BS name?" prompt during login. */
	USE_ALIAS_PROMPT,
	
	// data change events
	/** Your current sector.<p>Parameters: {@link krum.weaponm.database.Sector} sector */
	//SECTOR,
	/** How many credits you have on hand.<p>Parameters: {@link Integer} credits */
	//CREDITS_CHANGED,
	
	
	
	// messages
	/** Fired when you attempt to log in with the wrong password. */
	INVALID_PASSWORD,
	/** Fired when you can't log in because of the access mode in effect. */
	ACCESS_MODE_LOCKOUT,
	/** Fired when you can't log in due to death delay.<p>Parameters: {@link Integer} days */
	DEATH_DELAY_LOCKOUT,
	/** Fired when you try to join a closed tournament or when you've been blown up too many times. */
	PERMANENT_LOCKOUT,
	/** Fired when a probe self-destructs.<p>Parameters: {@link krum.weaponm.database.Sector} sector */
	PROBE_SELF_DESTRUCTS,
	/** Fired when a probe is destroyed by enemy fighters.<p>Parameters: {@link krum.weaponm.database.Sector} sector */
	PROBE_DESTROYED,
	
	
	/** Fired before a planet trade transaction. */
	PLANET_TRADING,
	/** Fired before a ship trade transaction. */
	SHIP_TRADING,
	/**
	 * Fired when a port asks, "How many holds of (product) do you want to
	 * (buy|sell)?"  The buying parameter indicates whether <em>you</em> are
	 * buying the product.
	 * <p>Parameters: {@link Integer} product, {@link Boolean} buying
	 */
	TRADE_INIT_PROMPT,
	/** Fired when a port says, "Agreed, <i>n</i> units."<p>Parameters: {@link Integer} units */
	TRADING_UNITS,
	/** A port's offer and request for a counter-offer.<p>Parameters: {@link Integer} credits */
	TRADE_OFFER_PROMPT,
	/**
	 * Indicates that subsequent {@link #TRADE_OFFER_PROMPT}s in this trade are
	 * final.  (The "final" offer may be repeated if the port takes your
	 * counter-offer as a joke.)
	 */
	FINAL_OFFER,
	/** Indicates that the current trade completed successfully. */
	TRADE_ACCEPTED,
	/** Indicates that the trade was canceled. */
	TRADE_REJECTED,
	/**
	 * The credits update before and after each trade.  The same message
	 * appears in several other places in the game.
	 * <p>Parameters: {@link Integer} credits
	 */
	CREDITS,
	/**
	 * The report from your psychic probe.  This will appear before the
	 * associated {@link #CREDITS}.
	 * <p>Parameters: {@link Float} percent */
	PSYCHIC_PROBE_REPORT,
	
	
	/** Parameters: {@link krum.weaponm.database.MessageType} type, {@link krum.weaponm.database.Trader} sender, String message. */ 
	CHAT_MESSAGE,
	
	/** 
	 * Fired when an upgrade is detected in a port report.  If a port is
	 * trading at a very low percentage when you discover it, it may generate
	 * bogus upgrade events as it regenerates.
	 * <p>Parameters: {@link krum.weaponm.database.Port} port, {@link Integer} product, {@link Integer} amount
	 */
	PORT_UPGRADED,

	/**
	 * Fired when the game sends an inactivity warning.
	 */
	INACTIVITY_WARNING,
	
	/**
	 * Fired when a new network thread is started.
	 */
	CONNECTING,
	/**
	 * Fired when the network thread is interrupted.
	 */
	DISCONNECTING,
	
	
	
	// TODO: sector explored, port discovered, warp discovered
	
	
	/**
	 * Fired when a course is plotted in the CIM.  (Will also be fired for
	 * autopilot and computer course plots.)
	 * 
	 * <p>Parameters: int[] course
	 */
	COURSE_PLOT,
	
	/**
	 * Fired when the game cannot plot a course, normally because of avoids.
	 * Responding 'Y' to this prompt will clear all avoids.
	 * <p>Parameters: {@link Integer} from, {@link Integer} to
	 */
	NO_ROUTE_PROMPT,
	
	/**
	 * Fired when you try to move and are stopped by an interdictor field.
	 */
	INTERDICTED;
	
	
	
	
	
	/**
	 * The major prompts.  These are the prompts that may be returned by
	 * {@link Script#getCurrentPrompt()}.
	 */
	public static final ScriptEvent[] MAJOR_PROMPTS = {
		COMMAND_PROMPT,
		COMPUTER_PROMPT,
		PLANET_PROMPT,
		CITADEL_PROMPT,
		CITADEL_COMPUTER_PROMPT,
		STARDOCK_PROMPT
	};
}
