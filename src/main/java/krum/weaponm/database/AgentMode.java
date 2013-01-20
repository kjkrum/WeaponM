package krum.weaponm.database;

/**
 * I refer to ships piloted by bosses, traders, and aliens as <em>agents</em>.
 * Agents are grouped by type in the sector display, and all agents use the
 * same basic text pattern.  The parser uses an <tt>AgentMode</tt> field to
 * track which agent type it is currently parsing. 
 */
enum AgentMode {
	FEDERALS,
	TRADERS,
	ALIENS
}
