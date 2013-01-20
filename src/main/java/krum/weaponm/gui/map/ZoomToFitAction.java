package krum.weaponm.gui.map;

import java.awt.geom.Rectangle2D;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;

public class ZoomToFitAction extends Action {

	public ZoomToFitAction(Visualization vis) {
		super();
		m_vis = vis;
	}

	public void run(double frac) {
		Rectangle2D bounds = m_vis.getBounds(Visualization.ALL_ITEMS);
		Display d = m_vis.getDisplay(0);
		GraphicsLib.expand(bounds, 50 + (int)(1/d.getScale()));
		DisplayLib.fitViewToBounds(d, bounds, 0);
	}
}

