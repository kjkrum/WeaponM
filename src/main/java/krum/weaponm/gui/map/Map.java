package krum.weaponm.gui.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;

import krum.weaponm.database.Sector;
import krum.weaponm.gui.GUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.controls.PanControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.collections.IntIterator;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 * An abstraction of the Prefuse <tt>Graph</tt> and other elements that make
 * up the map display. 
 */
public class Map implements PropertyChangeListener, HierarchyListener {
	private static final Logger log = LoggerFactory.getLogger(Map.class);
	
	private static final int DISPLAY_WIDTH = 400;
	private static final int DISPLAY_HEIGHT = 400;
	private static final String GRAPH_NAME = "graph";
	static final String SECTOR_COLUMN = "sector";
	private static final String REDRAW_ACTION = "redraw";
	static final int DEFAULT_RADIUS = 2;
	private static final int UNINITIALIZED = -1;

	private final Display display;
	private final GraphDistanceFilter filter;
	private final RadialTreeLayout layout;
	private final SectorRenderer sectorRenderer;
	private final WarpRenderer warpRenderer;
	private final Visualization vis;
	
	private Graph graph;
	private VisualGraph visGraph;
	/** Table row index, not sector number */
	private int root = UNINITIALIZED;
	private int radius = DEFAULT_RADIUS;
	private boolean showing = false;

	
	Map() {
		display = new Display();
		display.setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
		display.pan(DISPLAY_WIDTH/2, DISPLAY_HEIGHT/2);
		display.setHighQuality(true);
		Image background = new ImageIcon(getClass().getResource("/resources/weaponm/starnetblog_cloudy_starfield_texture4.jpg")).getImage();
		display.setBackgroundImage(background, true, true);
		display.addControlListener(new SubtreeDragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new WheelZoomControl(true, true));
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new ToolTipControl());
		display.addHierarchyListener(this);
		
		filter = new GraphDistanceFilter(GRAPH_NAME, radius);
		layout = new RadialTreeLayout(GRAPH_NAME);

		vis = new Visualization();
		ActionList redraw = new ActionList();
		redraw.add(filter);
		redraw.add(layout);
		redraw.add(new RepaintAction());
		//redraw.add(new ZoomToFitAction(vis));
		vis.putAction(REDRAW_ACTION, redraw);
		sectorRenderer = new SectorRenderer(25, 2);
		warpRenderer = new WarpRenderer(4);
		vis.setRendererFactory(new DefaultRendererFactory(sectorRenderer, warpRenderer));
		
		display.setVisualization(vis);
		filter.setVisualization(vis);
		layout.setVisualization(vis);
	}
	
	Display getDisplay() {
		return display;
	}
	
	void populate(Sector[] sectors) {
		graph = new Graph(true);
		graph.getNodeTable().addColumn(SECTOR_COLUMN, Sector.class);
		// add nodes
		for(int i = 0; i < sectors.length; ++i) {
			Node n = graph.addNode();
			n.set(SECTOR_COLUMN, sectors[i]);
		}
		// add edges
		for(int s = 0; s < sectors.length; ++s) {
			for(int t : sectors[s].getWarpsOut()) {
				graph.addEdge(s, t - 1);
			}
		}
		visGraph = vis.addGraph(GRAPH_NAME, graph);
		root = UNINITIALIZED;
		log.debug("map populated with {} sectors and {} warps", graph.getNodeCount(), graph.getEdgeCount());
	}
	
	void clear() {
		vis.reset();
		display.reset();
		filter.reset();
		layout.reset();
		graph = null;
		visGraph = null;
		display.repaint();
	}

	/**
	 * Adds a warp to the graph.  Does not redraw the display.
	 * 
	 * @param s from sector
	 * @param t to sector
	 */
	void addWarp(int s, int t) {
		graph.addEdge(s - 1, t - 1);
	}
	
	/**
	 * Invalidates the specified sector and repaints the visualization.  Has
	 * no effect if the sector is not visible.
	 */
	void updateSector(int sector) {
		if(visGraph != null) {
			NodeItem item = (NodeItem) visGraph.getNode(sector - 1);
			if(item.isVisible()) {
				log.debug("updating sector {}", sector);
				item.setValidated(false);
				// also redraw incident edges
				IntIterator iter = visGraph.edgeRows(item.getRow());
				while(iter.hasNext()) {
					EdgeItem edge = (EdgeItem) visGraph.getEdge(iter.nextInt());
					edge.setValidated(false);
				}
				vis.repaint();
			}
		}
	}
	
	/**
	 * Sets the layout root.  Will not redraw if the root is unchanged.
	 */
	void setRoot(int sector, boolean redraw) {
		if(sector < 1 || sector > graph.getNodeCount()) throw new IllegalArgumentException("Invalid sector number: " + sector);
		if(root != sector - 1) {
			root = sector - 1; // convert to row index
			NodeItem rootNode = (NodeItem) visGraph.getNode(root);
			vis.getGroup(Visualization.FOCUS_ITEMS).clear();
			vis.getGroup(Visualization.FOCUS_ITEMS).addTuple(rootNode);
			layout.setLayoutRoot(rootNode);
			log.debug("map root set to sector {}", sector);
			if(redraw) redraw();
		}
		
	}
	
	/**
	 * Sets the radius of the distance filter.  Will not redraw if the radius
	 * is unchanged.
	 */
	void setRadius(int radius, boolean redraw) {
		if(this.radius != radius) {
			this.radius = radius;
			filter.setDistance(radius);
			log.debug("map radius set to {}", radius);
			if(redraw) redraw();
		}
	}
	
	/**
	 * Determines if a sector is visible.
	 * 
	 * @param sector
	 * @return
	 */
	boolean isVisible(int sector) {
		if(visGraph == null) return false;
		return (visGraph.getNode(sector - 1).getBoolean(VisualItem.VISIBLE));
	}
	
	/**
	 * Redraws the display.
	 */
	void redraw() {
		if(showing && visGraph != null) {
			log.debug("running redraw action");
			vis.run(REDRAW_ACTION);
			/*
			try {
				throw new RuntimeException("check out my stack trace");
			}
			catch(RuntimeException e) {
				e.printStackTrace();
			}
			*/
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String property = e.getPropertyName();
		// TODO: always fire SECTOR_UPDATED along with STARDOCK_DISCOVERED?
		if(GUI.SECTOR_UPDATED.equals(property)) {
			updateSector((Integer) e.getNewValue());
		}
		else if(GUI.WARPS_DISCOVERED.equals(property)) {
			int[][] warps = (int[][]) e.getNewValue();
			boolean redraw = false;
			for(int i = 0; i < warps.length; ++i) {
				int s = warps[i][0];
				int t = warps[i][1];
				addWarp(s, t);
				if(isVisible(s) || isVisible(t)) redraw = true;
			}
			if(redraw) redraw();			
		}
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
			showing = display.isShowing();
			log.debug("map showing: {}", showing);
			if(showing) redraw();
		}		
	}	
}
