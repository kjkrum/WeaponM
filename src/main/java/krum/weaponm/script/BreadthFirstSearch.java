package krum.weaponm.script;

import java.util.Arrays;
import java.util.BitSet;

import krum.weaponm.database.Sector;

/**
 * A class for performing a
 * <a href="http://en.wikipedia.org/wiki/Breadth-first_search">breadth-first
 * search</a> of sectors.  This is a powerful and flexible way to search for
 * sectors matching any criteria you design.
 */
public class BreadthFirstSearch {
	private final Node[] included;
	private final Node[] ordered;
	private int size;
	private Node targetNode;

	/**
	 * Performs a breadth-first search around the origin sector, including
	 * sectors that match <tt>include</tt> and stopping when a sector is found
	 * that matches <tt>target</tt>.  If <tt>includeTarget</tt> is true, the
	 * sector that matched <tt>target</tt> will be included in the search
	 * result.
	 * <p>
	 * If the target matcher matches the origin sector, the search will
	 * terminate immediately.  If <tt>includeTarget</tt> is true, the search
	 * result will contain the origin node; if not, it will be empty.
	 * Otherwise, if the include matcher does not match the origin sector, the
	 * search will terminate immediately and the search result will be empty.
	 * 
	 * @param sectors all sectors as returned by <tt>Database.getSectors()</tt>
	 * @param origin the sector number from which to begin the search
	 * @param limit the maximum number of nodes to include in the search result; 0 = no limit
	 * @param include the matcher for nodes to include in the search result
	 * @param target the matcher for the target of the search
	 * @param includeTarget whether to include the node matching <tt>target</tt> in the search result
	 */
	public BreadthFirstSearch(Sector[] sectors, int origin, int limit, NodeMatcher include, NodeMatcher target, boolean includeTarget) {
		if(limit < 0 || origin < 1 || origin > sectors.length) throw new IllegalArgumentException();
		if(limit == 0) limit = Integer.MAX_VALUE;
		included = new Node[sectors.length];
		ordered = new Node[Math.min(limit, sectors.length)];
		BitSet considered = new BitSet(sectors.length);
		
		// test for weird conditions
		Node originNode = new Node(sectors[origin - 1], null);
		if(target.matches(originNode)) {
			if(includeTarget) {
				included[origin - 1] = originNode;
				ordered[0] = originNode;
				size = 1;
				targetNode = originNode;
			}
			return;
		}
		else if(!include.matches(originNode)) return;
		
		// prime it with the origin node
		included[origin - 1] = originNode;
		ordered[0] = originNode;
		size = 1;
		considered.set(origin - 1, true);
		
		// go!
		for(int i = 0; i < size; ++i) {
			Node parent = ordered[i];
			int[] warps = originNode.sector.getWarpsOut();
			for(int w : warps) {
				if(considered.get(w - 1)) continue;
				considered.set(w - 1, true);
				Node child = new Node(sectors[w - 1], parent);
				if(target.matches(child)) {
					if(includeTarget) {
						included[w - 1] = child;
						ordered[size] = child;
						++size;
						targetNode = child;
					}
					return;
				}
				else if(include.matches(child)) {
					included[w - 1] = child;
					ordered[size] = child;
					++size;
					if(size == ordered.length) return;
				}
			}
		}
	}
	
	/**
	 * @see #BreadthFirstSearch(Sector[], int, int, NodeMatcher, NodeMatcher, boolean)
	 */
	public BreadthFirstSearch(Sector[] sectors, Sector origin, int limit, NodeMatcher include, NodeMatcher target, boolean includeTarget) {
		this(sectors, origin.getNumber(), limit, include, target, includeTarget);
	}
	
	/**
	 * Gets the number of sectors included in the search result.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Gets all nodes included in the search result, ordered by distance.
	 */
	public Node[] getNodes() {
		return Arrays.copyOf(ordered, size);
	}
	
	/**
	 * Returns true if the search result includes the specified sector.
	 */
	public boolean includesSector(int sector) {
		return included[sector - 1] != null;
	}
	
	/**
	 * Gets the node corresponding to the specified sector, or null if the
	 * sector was not included.
	 * 
	 * @see #includesSector(int)
	 */
	public Node getNode(int sector) {
		return included[sector - 1];
	}
	
	/**
	 * Returns true if this search found a node matching the target matcher.
	 */
	public boolean targetFound() {
		return targetNode != null;
	}
	
	/**
	 * Gets the node that matched the target matcher, or null if none did.
	 * 
	 * @see #targetFound()
	 */
	public Node getTargetNode() {
		return targetNode;
	}
	
	/**
	 * Plots a course from the origin sector to the specified sector.
	 * 
	 * @param dest the destination sector
	 * @return the calculated course, or null if the destination was not
	 * included in the search tree
	 */
	public Sector[] plotCourse(Sector dest) {
		if(includesSector(dest.getNumber())) {
			Node node = getNode(dest.getNumber());
			Sector[] course = new Sector[node.distance + 1];
			while(node.distance >= 0) {
				course[node.distance] = node.sector;
				node = node.parent;
			}
			return course;
		}
		else return null;
	}
	
	/**
	 * Plots a course from the origin sector to the specified sector.
	 * 
	 * @param dest the destination sector
	 * @return the calculated course, or null if the destination was not
	 * included in the search tree
	 */
	public int[] plotCourse(int dest) {
		if(includesSector(dest)) {
			Node node = getNode(dest);
			int[] course = new int[node.distance + 1];
			while(node.distance >= 0) {
				course[node.distance] = node.sector.getNumber();
				node = node.parent;
			}
			return course;
		}
		else return null;
	}
	
	/**
	 * A node in a {@link BreadthFirstSearch}.
	 */
	public static class Node {
		/** The sector corresponding to this node. */
		public final Sector sector;
		/** The distance from the origin to this node. */
		public final int distance;
		/**
		 * The node via which this node was added to the search result, or
		 * null if this is the origin node.
		 */
		public final Node parent;
		
		Node(Sector sector, Node parent) {
			this.sector = sector;
			this.parent = parent;
			if(parent == null) distance = 0;
			else distance = parent.distance + 1;
		}
	}
}
