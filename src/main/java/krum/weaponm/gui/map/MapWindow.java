package krum.weaponm.gui.map;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import krum.weaponm.database.Database;
import krum.weaponm.gui.GUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(MapWindow.class);
	
	protected final Map map;
	protected Database database;
	
	public MapWindow(GUI gui) {
		setIconImage(gui.getIcon().getImage());
		setTitle("Map");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		map = new Map();
		gui.addPropertyChangeListener(GUI.SECTOR_UPDATED, map);
		gui.addPropertyChangeListener(GUI.WARPS_DISCOVERED, map);
		add(map.getDisplay());
		
		ControlPanel controlPanel = new ControlPanel(map);
		gui.addPropertyChangeListener(GUI.DATABASE_INITIALIZED, controlPanel);
		gui.addPropertyChangeListener(GUI.SHIP_SECTOR, controlPanel);
		gui.addPropertyChangeListener(GUI.MAP_ROOT, controlPanel);
		gui.addPropertyChangeListener(GUI.STARDOCK_DISCOVERED, controlPanel);
		add(controlPanel, BorderLayout.SOUTH);
	}
}
