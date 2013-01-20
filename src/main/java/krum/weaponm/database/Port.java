package krum.weaponm.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Port implements Serializable, Constants {
	private static final long serialVersionUID = 6084226071622527367L;

	// maps trading class (less one) to selling bits;
	private static final int[] classToProduct = { 4,2,1,3,5,6,7,0 };
	// maps selling bits to trading class
	private static final int[] productToClass = { 8,3,2,4,1,5,6,7 };
	
	protected final Sector sector;
	private volatile String name;
	private PortStatus status;
	private Date statusDate;
	private final int[] mcic = new int[3];
	volatile int tradingClass = UNKNOWN;
	final int[] levels = new int[3];
	final int[] percents = new int[3];
	final int[] capacities = new int[3];
	Date reportDate;
	private int credits = UNKNOWN;
	private Date creditsDate;
	private volatile Date bustDate;
	
	public Port(Sector sector) {
		this.sector = sector;
	}

	/**
	 * Gets the sector where this port is located.
	 */
	public Sector getSector() {
		return sector;
	}
	
	/**
	 * Gets the name of this port.  Returns null if the name is unknown.
	 */
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		if(this.name == null) this.name = name;
	}
	
	/**
	 * Returns the last known status of this port.
	 */
	public PortStatus getStatus() {
		return status;
	}
	
	void setStatus(PortStatus status) {
		if(this.status != status) {
			statusDate = new Date();
			this.status = status;
		}
	}
	
	/**
	 * Returns the date when the status of this port last changed.
	 */
	public Date getStatusDate() {
		return statusDate;
	}
	
	/**
	 * Gets the product levels.  Negative values indicate that the port is
	 * buying the product.
	 */
	synchronized public int[] getLevels() {
		return Arrays.copyOf(levels, levels.length);
	}
	
	/**
	 * Gets the level for the specified product.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	synchronized public int getLevel(int product) {
		return levels[product];
	}
	
	/**
	 * Gets the product trading percents.
	 */
	synchronized public int[] getPercents() {
		return Arrays.copyOf(percents, percents.length);
	}
	
	/**
	 * Gets the trading percent for the specified product.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	synchronized public int getPercent(int product) {
		return percents[product];
	}
	
	/**
	 * Gets the product capacities.  Capacity is equal to level when the port
	 * is trading at 100%.  Because the game drops decimals from percents,
	 * this may not be accurate until the port is seen at 100%.  The parser
	 * will set capacities to the smallest absolute values that could  produce
	 * the observed percentages.
	 */
	synchronized public int[] getCapacities() {
		return Arrays.copyOf(capacities, capacities.length);
	}
	
	/**
	 * Gets the capacity for the specified product.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	synchronized public int getCapacity(int product) {
		return capacities[product];
	}
	
	synchronized void setCapacity(int product, int capacity) {
		capacities[product] = capacity;
	}
	
	synchronized public Date getReportDate() {
		return reportDate;
	}
	
	synchronized public int getCredits() {
		return credits;
	}
	
	synchronized public Date getCreditsDate() {
		return creditsDate;
	}
	
	synchronized void setCredits(int credits) {
		this.credits = credits;
		creditsDate = new Date();
	}
	
	// updates from list of values extracted from cim port report
	/*
	synchronized void setReport(List<Integer> numbers, int[] capacities) {
		for(int i = 0; i < 3; ++i) {
			levels[i] = numbers.get(i * 2 + 1);
			percents[i] = numbers.get(i * 2 + 2);
		}
		System.arraycopy(capacities, 0, this.capacities, 0, capacities.length);
		reportDate = new Date();
	}
	*/
	
	// for regular port reports
	synchronized void setReport(int[] levels, int[] percents) {
		System.arraycopy(levels, 0, this.levels, 0, levels.length);
		System.arraycopy(percents, 0, this.percents, 0, percents.length);
		reportDate = new Date();
	}
	
	/**
	 * Returns the trading class of this port.  For all ports except Stardock,
	 * this is identical to the port class.  For Stardock, this method returns
	 * <tt>UNKNOWN</tt> until you have discovered its actual trading class.
	 * Stardock's default trading class is 8, but it is often edited.
	 */
	public int getTradingClass() {
		return tradingClass;
	}
	
	void setTradingClass(int tradingClass) {
		if(this.tradingClass == UNKNOWN) this.tradingClass = tradingClass;
	}
	
	// sets trading class using an array of 'S' and 'B' or ' ' and '-'
	void setTradingClass(char[] indicators) {
		if(this.tradingClass == UNKNOWN) {
			int productCode = 0;
			for(int i = 0; i < 3; ++i) {
				if(indicators[i] == ' ' || indicators[i] == 'S') productCode += 1 << i;
			}
			tradingClass = productToClass[productCode];
		}
	}
	
	/**
	 * Returns a bit mask representing the products this port is trading.
	 * Bits that are on (1) represent products this port is selling.  Thus,
	 * a Class 7 (SSS) port has a product mask of 7 (111 binary) and a Class 2
	 * (BSB) port has a product mask of 2 (010 binary).  The least significant
	 * bit (the rightmost bit) represents Fuel Ore, so a Class 1 (BBS) port
	 * has a product mask of 4 (100 binary), not 1 (001 binary).
	 * <p>
	 * This method will throw an <tt>UnsupportedOperationException</tt> if it
	 * is called on a Class 0 port. 
	 */
	//public int getProductMask() {
	//	if(tradingClass == 0) throw new UnsupportedOperationException();
	//	return classToProduct[tradingClass - 1];
	//}
	
	/**
	 * Returns the nominal class of this port.  For all ports except Stardock,
	 * this is identical to the trading class.
	 * 
	 * @see #getTradingClass()
	 */
	public int getPortClass() {
		return tradingClass;
	}
	
	/**
	 * Returns true if the port sells the specified product.  For Stardock,
	 * this method returns false if the trading class is unknown.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	public boolean sells(int product) {
		if(product < 0 || product > 2) throw new IllegalArgumentException();
		if(tradingClass < 1) return false;
		return (classToProduct[tradingClass - 1] & 1 << product) != 0;
	}	
	
	/**
	 * Returns true if the port buys the specified product.  For Stardock,
	 * this method returns false if the trading class is unknown.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	public boolean buys(int product) {
		if(product < 0 || product > 2) throw new IllegalArgumentException();
		if(tradingClass < 1) return false;
		return (~classToProduct[tradingClass - 1] & 1 << product) != 0;
	}
	
	/**
	 * Returns the date you were busted at this port.  Returns null if you are
	 * not busted at this port.
	 * 
	 * @see #clearBust()
	 */
	public Date getBustDate() {
		return bustDate;
	}
	
	/**
	 * Gets the MCIC for the specified product.  MCICs are initialized to
	 * zero.
	 * 
	 * @param product one of the constants from the {@link Product} class
	 */
	public int getMCIC(int product) {
		if(product < 0 || product > 2) throw new IllegalArgumentException();
		return mcic[product];
	}
	
	// TODO: method to add mcic data points
	
	/**
	 * Returns a string like "BSB".  Returns "???" if the trading class is
	 * unknown.
	 */
	public String getProductString() {
		if(getTradingClass() == UNKNOWN) return "???";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 3; ++i) {
			sb.append(sells(i) ? 'S' : 'B');
		}
		return sb.toString();
	}
	
	/**
	 * Allows scripts to clear your bust at this port.  It is recommended that
	 * scripts use sector notes to store data about teammates' busts, since it
	 * is possible that a teammate could report a bust in a sector where your
	 * own database has not recorded the port.
	 * 
	 * @see Sector#setNote(String, String)
	 */
	public void clearBust() {
		bustDate = null;
	}

	/**
	 * Determines if this port can cross-trade the specified products with
	 * another port.
	 * 
	 * @param other the other port
	 * @param product1 one of the constants from {@link Product}
	 * @param product2 a different constant from {@link Product}
	 * @throws IllegalArgumentException if either product is invalid or product1 == product2
	 */
	public boolean canCrossTrade(Port other, int product1, int product2) {
		if(product1 < 0 || product2 < 0 || product1 > 2 || product2 > 2 || product1 == product2) {
			throw new IllegalArgumentException();
		}
		if(tradingClass < 1 || other.tradingClass < 1) return false;
		int mask = (1 << product1) + (1 << product2);
		int p1 = classToProduct[tradingClass - 1] & mask;
		int p2 = classToProduct[other.tradingClass - 1] & mask;
		return p1 != 0 && p2 != 0 && (p1 ^ p2) == mask;
	}
	
	/**
	 * Determines if this port can triple-trade with another port.  (Whether
	 * triple-trading is ever actually a good idea is left as an exercise for
	 * the script writer.)
	 * 
	 * @param other the other port
	 */
	public boolean canTripleTrade(Port other) {
		if(tradingClass < 1 || other.tradingClass < 1) return false;
		int p1 = classToProduct[tradingClass - 1];
		int p2 = classToProduct[other.tradingClass - 1];
		return p1 != 0 && p2 != 0 && (p1 ^ p2) == 7;
	}
	
	/************************************************************************/
	
	@Override
	public String toString() {
		if(name != null) return name + ", Class " + getPortClass();
		else return "Unidentified Class " + getPortClass();
	}
	
	synchronized private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	
	synchronized private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	/*
	public static void main(String[] args) {
		//for(int i = 1; i < 9; ++i) {
			//System.out.print("Class " + i + ": ");
			//for(int j = 0; j < 3; ++j) {
				// selling logic
				//System.out.print((classToProduct[i - 1] & 1 << j) != 0 ? 'S' : 'B');
				
				// buying logic
				//System.out.print((~classToProduct[i - 1] & 1 << j) != 0 ? 'B' : 'S');
			//}
			//System.out.println();
		//}
		
		// set trading class logic
		String port = "SSB";
		char[] indicators = port.toCharArray();
		int productCode = 0;
		for(int i = 0; i < 3; ++i) {
			if(indicators[i] == ' ' || indicators[i] == 'S') productCode += 1 << i;
		}
		System.out.println(port + ": Class " + productToClass[productCode]);
	}
	*/
}
