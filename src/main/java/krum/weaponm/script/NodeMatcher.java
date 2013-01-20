package krum.weaponm.script;

import krum.weaponm.database.Sector;
import krum.weaponm.script.BreadthFirstSearch.Node;


/**
 * Defines the include or target criteria of a {@link BreadthFirstSearch}.
 * You can construct powerful searches by writing classes that extend this
 * class.  This class also provides static instances and methods to create
 * various common matchers, and methods to create composite matchers.
 */
abstract public class NodeMatcher {
	/** Returns true if the specified node matches the criteria of this matcher. */
	abstract public boolean matches(Node node);
	
	/**
	 * Matches all nodes.  Useful as an include matcher.
	 */
	public static final NodeMatcher ALL = new NodeMatcher() {
		@Override
		public boolean matches(Node node) {
			return true;
		}		
	};
	
	/**
	 * Matches no nodes.  Useful as a target matcher.
	 */
	public static final NodeMatcher NONE = complement(ALL);
	
	/**
	 * Matches nodes where the sector is not avoided.  Useful as an include
	 * matcher.
	 */
	public static final NodeMatcher NOT_AVOIDED = new NodeMatcher() {
		@Override
		public boolean matches(Node node) {
			return !node.sector.isAvoided();
		}	
	};
	
	/**
	 * Matches nodes where the sector is unexplored.  Useful as a target
	 * matcher.
	 */
	public static final NodeMatcher UNEXPLORED = new NodeMatcher() {
		@Override
		public boolean matches(Node node) {
			return !node.sector.isExplored();
		}
	};
	
	public static NodeMatcher sector(final int sector) {
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return node.sector.getNumber() == sector;
			}
		};
	}
	
	public static NodeMatcher sector(final Sector sector) {
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return node.sector.equals(sector);
			}
		};
	}
	
	/**
	 * Creates a matcher that matches nodes where the distance equals the
	 * specified distance.  Useful as a target matcher.
	 */
	public static NodeMatcher atDistance(final int distance) {
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return node.distance == distance;
			}
		};
	}
	
	/**
	 * Creates a matcher that matches the intersection of two matchers.
	 */
	public static NodeMatcher intersection(final NodeMatcher m0, final NodeMatcher m1) {
		if(m0 == null || m1 == null) throw new NullPointerException();
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return m0.matches(node) && m1.matches(node);
			}
		};
	}
	
	/**
	 * Creates a matcher that matches the union of two matchers.
	 */
	public static NodeMatcher union(final NodeMatcher m0, final NodeMatcher m1) {
		if(m0 == null || m1 == null) throw new NullPointerException();
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return m0.matches(node) || m1.matches(node);
			}
		};
	}
	
	/**
	 * Creates a matcher that matches the complement of the specified matcher.
	 */
	public static NodeMatcher complement(final NodeMatcher m) {
		if(m == null) throw new NullPointerException();
		return new NodeMatcher() {
			@Override
			public boolean matches(Node node) {
				return !m.matches(node);
			}
		};
	}
}
