package krum.weaponm.database;

class Stardock extends Port {
	private static final long serialVersionUID = -4422169779022692748L;

	// normal discovery
	Stardock(Sector sector) {
		super(sector);
	}
	
	// for replacing a cim port later discovered to be stardock
	Stardock(Port port) {
		super(port.sector);
		synchronized(port) {
			tradingClass = port.tradingClass;
			System.arraycopy(port.levels, 0, levels, 0, levels.length);
			System.arraycopy(port.percents, 0, percents, 0, percents.length);
			System.arraycopy(port.capacities, 0, capacities, 0, capacities.length);
			reportDate = port.reportDate;
		}
	}

	@Override
	public int getPortClass() {
		return 9;
	}	
	
}
