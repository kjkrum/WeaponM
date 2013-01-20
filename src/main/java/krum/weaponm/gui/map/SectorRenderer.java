package krum.weaponm.gui.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import krum.jtx.VGAColors;
import krum.weaponm.database.Port;
import krum.weaponm.database.Sector;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;

/**
 * Renders edges as circles.
 * Determines properties such as color and stroke by examining the
 * {@link krum.weaponm.database.Sector} contained within the node.
 */
public class SectorRenderer implements Renderer {
	//private static final Logger log = LoggerFactory.getLogger(SectorRenderer.class);
	
	private final int r; // radius
	private final int d; // diameter
	private final int er; // etch radius, for double stroke around dead ends
	private final int ed; // etch diameter
	private final Ellipse2D shape;
	private final Ellipse2D etchShape;
	private final BasicStroke normalStroke;
	private final BasicStroke thinStroke = new BasicStroke(1);
	private final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	private final Image sdImage = new ImageIcon(getClass().getResource("/resources/weaponm/Stardock.png")).getImage();
	private final Image youImage = new ImageIcon(getClass().getResource("/resources/weaponm/You.png")).getImage();

	public SectorRenderer(int radius, int strokeWidth) {
		if(strokeWidth <= 0 || radius < 0) throw new IllegalArgumentException();
		r = radius;
		d = 2 * r;
		er = r - 3;
		ed = 2 * er;
		normalStroke = new BasicStroke(strokeWidth);
		shape = new Ellipse2D.Double(0, 0, d, d);
		etchShape = new Ellipse2D.Double(0, 0, ed, ed);
	}
	
	@Override
	public void render(Graphics2D g, VisualItem item) {
		// draw bounding box for debugging purposes
		//g.setStroke(thinStroke);
		//g.setColor(Color.YELLOW);
		//g.draw(item.getBounds());		
		
		Sector sector = (Sector) item.get(Map.SECTOR_COLUMN);
		if(sector == null) return;
		
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Color fillColor;
		Color strokeColor;
		
		// TODO: better color scheme
		
		// FILL
		// avoided: dark red
		// fedspace: dark blue
		// msl (outside fedspace): bright blue 
		// unexplored: bright black
		// normal: dark black
		
		// STROKE
		// avoided: bright black (unexplored), dark white (explored)
		// fedspace: bright black (unexplored), bright white (explored)
		// msl (outside fedspace): same as fedspace
		// all others: dark white
		
		
		
		if(sector.isAvoided()) {
			fillColor = VGAColors.foreground(VGAColors.RED, false);
			if(sector.isExplored()) {
				strokeColor = VGAColors.foreground(VGAColors.WHITE, false);
			}
			else {
				strokeColor = VGAColors.foreground(VGAColors.BLACK, true);
			}
		}
		else if(sector.isFedSpace()) {
			fillColor = VGAColors.foreground(VGAColors.BLUE, false);
			if(sector.isExplored()) {
				strokeColor = VGAColors.foreground(VGAColors.WHITE, true);
			}
			else {
				strokeColor = VGAColors.foreground(VGAColors.WHITE, false);
			}
		}
		else { // normal sector
			if(sector.isExplored()) {
				fillColor = VGAColors.foreground(VGAColors.BLACK, false);
				strokeColor = VGAColors.foreground(VGAColors.WHITE, false);
			}
			else {
				fillColor = VGAColors.foreground(VGAColors.BLACK, true);
				strokeColor = VGAColors.foreground(VGAColors.WHITE, false);				
			}
		}
		
		double x = item.getX();
		double y = item.getY();
		//log.debug("sector {} x={} y={}", new Object[] { sector.getNumber(), x, y });
		
		shape.setFrame(x - r, y - r, d, d);
		g.setColor(fillColor);
		g.fill(shape);

		g.setColor(strokeColor);
		if(sector.getNumWarpsIn() == 1) {
			etchShape.setFrame(x - er, y - er, ed, ed);
			g.setStroke(thinStroke);
			g.draw(shape);
			g.draw(etchShape);
		}
		else {
			g.setStroke(normalStroke);
			g.draw(shape);
		}
		
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		
		if(sector.hasPort()) {
			int portClass = sector.getPort().getPortClass();
			if(portClass == 0 || portClass == 9) {
				g.drawImage(sdImage, (int) x - 10, (int) y - r - 15, 50, 50, null);
				if(portClass == 9) {
					String s = "SD";
					float w = (float) metrics.stringWidth(s);
					g.drawString(s, (float) x - w / 2, (float) (y - metrics.getHeight() * 0.5));
				}
			}
			else {
				float w = (float) metrics.stringWidth("BBB");
				for(int i = 0; i < 3; ++i) {
					if(sector.getPort().sells(i)) {
						g.setColor(VGAColors.foreground(VGAColors.CYAN, true));
						g.drawString("S", (float) x - w / 2 + i * w / 3, (float) (y - metrics.getHeight() * 0.5));
					}
					else {
						g.setColor(VGAColors.foreground(VGAColors.GREEN, false));
						g.drawString("B", (float) x - w / 2 + i * w / 3, (float) (y - metrics.getHeight() * 0.5));
					}
				}
				//g.drawString(prod, (float) x - w / 2, (float) (y - metrics.getHeight() * 0.5));
			}
		}
		
		if(sector.isYourLocation()) {
			g.drawImage(youImage, (int) x - 5, (int) y + 10, 10, 10, null);
		}
		
		String text = sector.toString();
		g.setColor(strokeColor);
		g.drawString(text, (float) x - metrics.stringWidth(text) / 2, (float) y + metrics.getHeight() / 3);
	}

	@Override
	public boolean locatePoint(Point2D p, VisualItem item) {
		return p.distance(item.getX(), item.getY()) <= r * item.getSize();
	}

	@Override
	public void setBounds(VisualItem item) {
		double size = item.getSize();
		double br = r * size; // bounding radius
		double bd = 2 * br; // bounding diameter
		// extra to add to top and right for port image
		double rightPad = 0;
		double topPad = 0;
		Sector sector = (Sector) item.get(Map.SECTOR_COLUMN);
		if(sector.hasPort()) {
			Port port = sector.getPort();
			if(port.getPortClass() == 0 || port.getPortClass() == 9) {
				// g.drawImage(sdImage, (int) x - 10, (int) y - r - 15, 50, 50, null);
				rightPad = (40 - r) * size;
				if (rightPad < 0) rightPad = 0;
				topPad = 15 * size;
			}
		}
		item.setBounds(item.getX() - br, item.getY() - br - topPad, bd + rightPad, bd + topPad);
	}

}
