package krum.weaponm.database;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

class RankNameResolver implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Set<String> ranks = new HashSet<String>();
	private Automaton automaton;
	private RunAutomaton run;
	
	RankNameResolver() {
		ranks.add("Private");
		ranks.add("Private 1st Class");
		ranks.add("Lance Corporal");
		ranks.add("Corporal");
		ranks.add("Sergeant");
		ranks.add("Staff Sergeant");
		ranks.add("Gunnery Sergeant");
		ranks.add("1st Sergeant");
		ranks.add("Sergeant Major");
		ranks.add("Warrant Officer");
		ranks.add("Chief Warrant Officer");
		ranks.add("Ensign");
		ranks.add("Lieutenant J.G.");
		ranks.add("Lieutenant");
		ranks.add("Lieutenant Commander");
		ranks.add("Commander");
		ranks.add("Captain");
		ranks.add("Commodore");
		ranks.add("Rear Admiral");
		ranks.add("Vice Admiral");
		ranks.add("Admiral");
		ranks.add("Fleet Admiral");
		ranks.add("Nuisance 3rd Class");
		ranks.add("Nuisance 2nd Class");
		ranks.add("Nuisance 1st Class");
		ranks.add("Menace 3rd Class");
		ranks.add("Menace 2nd Class");
		ranks.add("Menace 1st Class");
		ranks.add("Smuggler 3rd Class");
		ranks.add("Smuggler 2nd Class");
		ranks.add("Smuggler 1st Class");
		ranks.add("Smuggler Savant");
		ranks.add("Robber");
		ranks.add("Terrorist");
		ranks.add("Pirate");
		ranks.add("Infamous Pirate");
		ranks.add("Notorious Pirate");
		ranks.add("Dread Pirate");
		ranks.add("Galactic Scourge");
		ranks.add("Enemy of the State");
		ranks.add("Enemy of the People");
		ranks.add("Enemy of Humankind");
		ranks.add("Heinous Overlord");
		ranks.add("Prime Evil");
		ranks.add("Servant");
		ranks.add("Capitalist");
		ranks.add("Trader");
		ranks.add("Entrepreneur");
		ranks.add("Merchant Apprentice");
		ranks.add("Merchant");
		ranks.add("Executive Merchant");
		ranks.add("Merchant Baron");
		ranks.add("Merchant Prince");
		ranks.add("Grand Merchant");
		ranks.add("Financier");
		ranks.add("Tycoon");
		ranks.add("Mogul");
		ranks.add("Supreme Mogul");
		
		List<Automaton> automatons = new LinkedList<Automaton>();
		for(String rankName : ranks) {
			automatons.add(new RegExp(rankName).toAutomaton());
		}
		automaton = Automaton.union(automatons);
		run = new RunAutomaton(automaton);		
	}
	
	/**
	 * Attempts to resolve a trader or alien's name from a string containing
	 * their rank and name.  If the beginning of the parameter matches a known
	 * rank, the name portion will be returned.  If not, the whole parameter
	 * will be returned.
	 *  
	 * @param rankName
	 * @return
	 */
	String resolveName(String rankName) {
		AutomatonMatcher matcher = run.newMatcher(rankName);
		if(matcher.find()) {
			return rankName.substring(matcher.end() + 1);
		}
		else return rankName;
	}
	
	/**
	 * Attempts to resolve a trader or alien's rank from a string containing
	 * their rank and name.  If the beginning of the parameter matches a known
	 * rank, the rank portion will be returned.  If not, the whole parameter
	 * will be returned.
	 *  
	 * @param rankName
	 * @return
	 */
	String resolveRank(String rankName) {
		AutomatonMatcher matcher = run.newMatcher(rankName);
		if(matcher.find()) {
			return matcher.group();
		}
		else return rankName;
	}
	
	/**
	 * Adds a string to the set of known ranks.
	 * 
	 * @param rank
	 */
	void addRank(String rank) {
		if(!ranks.contains(rank)) {
			ranks.add(rank);
			automaton = automaton.union(new RegExp(rank).toAutomaton());
			run = new RunAutomaton(automaton);
		}
	}
	
	/*
	public static void main(String[] args) {
		RankNameResolver resolver = new RankNameResolver();
		String rankName = "Lieutenant J.G. Derpy McStupid";
		System.out.printf("Name: \"%s\"\n", resolver.resolveName(rankName));
		System.out.printf("Rank: \"%s\"\n", resolver.resolveRank(rankName));
	}
	*/
}
