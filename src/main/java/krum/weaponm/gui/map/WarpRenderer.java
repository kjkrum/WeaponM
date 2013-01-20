package krum.weaponm.gui.map;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import krum.jtx.VGAColors;
import krum.weaponm.database.Product;
import krum.weaponm.database.Sector;
import prefuse.render.Renderer;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class WarpRenderer implements Renderer {
	
	private final float strokeWidth;	
	private final BasicStroke dashStroke;
	private final BasicStroke solidStroke;

	public WarpRenderer(float strokeWidth) {
		if(strokeWidth <= 0) throw new IllegalArgumentException();
		this.strokeWidth = strokeWidth;
		float[] dashPattern = new float[] { strokeWidth * 2, strokeWidth * 2};
		dashStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0);
		solidStroke = new BasicStroke(strokeWidth);
	}
	
	@Override
	public void render(Graphics2D g, VisualItem item) {
		EdgeItem edgeItem = (EdgeItem) item;
		NodeItem s = edgeItem.getSourceItem();
		NodeItem t = edgeItem.getTargetItem();
		
	    // check for existence of return edge
		// if return edge exists
		//    if s < t, draw this edge as undirected
		//    else draw nothing
		// else draw this edge as directed
		
		if(t.getGraph().getEdge(t, s) != null) { // two-ways
			if(s.getRow() < t.getRow()) { // only draw ascending edges
				Sector sSect = (Sector) s.get(Map.SECTOR_COLUMN);
				Sector tSect = (Sector) t.get(Map.SECTOR_COLUMN);
				if(sSect.hasPort() && tSect.hasPort()) {
					if(sSect.getPort().canCrossTrade(tSect.getPort(), Product.ORGANICS, Product.EQUIPMENT)) {
						g.setColor(VGAColors.foreground(VGAColors.GREEN, false)); // ppt pairs
					}
					else if(sSect.getPort().buys(Product.EQUIPMENT) && tSect.getPort().buys(Product.EQUIPMENT)) {
						g.setColor(VGAColors.foreground(VGAColors.MAGENTA, false)); // ssm pairs
					}
					else {
						g.setColor(VGAColors.foreground(VGAColors.WHITE, false)); // default color
					}
				}
				else {
					g.setColor(VGAColors.foreground(VGAColors.WHITE, false)); // default color
				}
				g.setStroke(solidStroke);
				g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(), (int) t.getY());
			}
		}
		else { // one-ways
			g.setColor(VGAColors.foreground(VGAColors.WHITE, false));
			g.setStroke(solidStroke);
			Point2D mid = new Point2D.Double((s.getX() + t.getX()) / 2, (s.getY() + t.getY()) / 2);
			g.drawLine((int) s.getX(), (int) s.getY(), (int) mid.getX(), (int) mid.getY());
			g.setColor(VGAColors.foreground(VGAColors.RED, false));
			g.setStroke(dashStroke);
			g.drawLine((int) mid.getX(), (int) mid.getY(), (int) t.getX(), (int) t.getY());			
		}
		
	}

	@Override
	public boolean locatePoint(Point2D p, VisualItem item) {
		EdgeItem edgeItem = (EdgeItem) item;
		NodeItem s = edgeItem.getSourceItem();
		NodeItem t = edgeItem.getTargetItem();
		// use a generous width for easy tool tip activation
		return Line2D.ptSegDist(s.getX(), s.getY(), t.getX(), t.getY(), p.getX(), p.getY()) <= strokeWidth * 3;
	}

	@Override
	public void setBounds(VisualItem item) {
		EdgeItem edgeItem = (EdgeItem) item;
		NodeItem s = edgeItem.getSourceItem();
		NodeItem t = edgeItem.getTargetItem();
		double x = s.getX(); 
		double y = s.getY();
		double w = t.getX() - x;
		double h = t.getY() - y;
		if(w < 0) {
			x += w;
			w *= -1;
		}
		if(h < 0) {
			y += h;
			h *= -1;
		}
		item.setBounds(x, y, w, h);
	}

}
