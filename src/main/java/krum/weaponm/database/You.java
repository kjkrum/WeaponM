package krum.weaponm.database;

public class You extends Trader {
	private static final long serialVersionUID = 645447870800429471L;
	
	private volatile int turns;
	private volatile int credits;
	private volatile int bankBalance;
	private volatile Sector lastSector;
	private volatile int timesBlownUp;

	public int getTurns() {
		return turns;
	}
	
	void setTurns(int turns) {
		this.turns = turns;
	}

	public int getCredits() {
		return credits;
	}
	
	void setCredits(int credits) {
		this.credits = credits;
	}

	public int getBankBalance() {
		return bankBalance;
	}

	void setBankBalance(int bankBalance) {
		this.bankBalance = bankBalance;
	}

	public Sector getLastSector() {
		return lastSector;
	}
	
	void setLastSector(Sector lastSector) {
		this.lastSector = lastSector;
	}
	
	public int getTimesBlownUp() {
		return timesBlownUp;
	}
	
	void setTimesBlownUp(int timesBlownUp) {
		this.timesBlownUp = timesBlownUp;
	}

	/** won't NPE if database is not initialized */
	public int getSectorNumber() {
		Sector s = getSector();
		return s == null ? 0 : s.getNumber();
	}
}
