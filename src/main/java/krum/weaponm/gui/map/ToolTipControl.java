package krum.weaponm.gui.map;

import java.awt.event.MouseEvent;

import javax.swing.ToolTipManager;

import krum.weaponm.database.Port;
import krum.weaponm.database.Product;
import krum.weaponm.database.Sector;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 * Displays tool tips for sectors and warps.
 */
public class ToolTipControl extends ControlAdapter {

    private StringBuilder sb = new StringBuilder();
    
    static {
    	// make tool tips stay up forever
    	ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }
    
    public void itemEntered(VisualItem item, MouseEvent e) {
		Display d = (Display) e.getSource();
    	if(item instanceof NodeItem) {
    		Sector s = (Sector) item.get(Map.SECTOR_COLUMN);
    		if(s == null) return;
    		sb.append("<html>Sector ");
    		sb.append(s.getNumber());
    		if(s.getNebula() != null) {
    			sb.append(" in ");
    			sb.append(s.getNebula());
    		}
    		if(s.isAvoided()) sb.append("<br/>Avoided");
    		if(!s.isExplored()) sb.append("<br/>Unexplored");
    		if(s.hasPort()) {
    			Port p = s.getPort();
    			sb.append("<br/>");
				sb.append(p.toString());
    			if(p.getTradingClass() > 0) {
    				for(int i = 0; i < 3; ++i) {
    	    			sb.append("<br/>");
    					sb.append(p.getLevel(i));
    					sb.append(" (");
    					sb.append(p.getPercent(i));
    					sb.append("%)");
    				}
    			}
    		}
    		sb.append("</html>");      
    		d.setToolTipText(sb.toString());
    		sb.setLength(0);
    	}
    	else { // EdgeItem
    		EdgeItem edge = (EdgeItem) item;
    		Sector s = (Sector) edge.getSourceItem().get(Map.SECTOR_COLUMN);
			Sector t = (Sector) edge.getTargetItem().get(Map.SECTOR_COLUMN);
			
			if(s.hasPort() && t.hasPort()) {
				if(s.getPort().buys(Product.EQUIPMENT) && t.getPort().buys(Product.EQUIPMENT)) {
					// ssm pairs
					//sb.append("Limit: ");
					int percent = Math.min(s.getPort().getPercent(Product.EQUIPMENT), t.getPort().getPercent(Product.EQUIPMENT));
					sb.append(percent);
					sb.append('%');
		    		// TODO: report mcic
		    		d.setToolTipText(sb.toString());
		    		sb.setLength(0);
				}
				else if(s.getPort().canCrossTrade(t.getPort(), Product.ORGANICS, Product.EQUIPMENT)) {
					// ppt pairs
					//sb.append("Limit: ");
					int holds = Math.abs(s.getPort().getLevel(Product.ORGANICS));
					holds = Math.min(holds, Math.abs(s.getPort().getLevel(Product.EQUIPMENT)));
					holds = Math.min(holds, Math.abs(t.getPort().getLevel(Product.ORGANICS)));
					holds = Math.min(holds, Math.abs(t.getPort().getLevel(Product.EQUIPMENT)));
					sb.append(holds);
					sb.append(" holds, ");
					int percent = s.getPort().getPercent(Product.ORGANICS);
					percent = Math.min(percent, s.getPort().getPercent(Product.EQUIPMENT));
					percent = Math.min(percent, t.getPort().getPercent(Product.ORGANICS));
					percent = Math.min(percent, t.getPort().getPercent(Product.EQUIPMENT));
					sb.append(percent);
					sb.append('%');
		    		d.setToolTipText(sb.toString());
		    		sb.setLength(0);
				}
			}
    	}
    }
    
    public void itemExited(VisualItem item, MouseEvent e) {
        Display d = (Display) e.getSource();
        d.setToolTipText(null);
    }
    
}