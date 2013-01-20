package krum.weaponm.gui;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.SwingPropertyChangeSupport;

import krum.swing.ExtensionFileFilter;
import krum.weaponm.AppSettings;
import krum.weaponm.WeaponM;
import krum.weaponm.gui.map.MapWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction of the GUI.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
public class GUI {
	protected static final Logger log = LoggerFactory.getLogger(GUI.class);
	// keys for property change listeners
	/**
	 * oldValue = Database | null, newValue = Boolean.  Fired when a database
	 * is created, loaded, or closed.  The old value is a reference to the
	 * database if it was loaded or created and null if it was closed.  The
	 * new value is true if the database is loaded and false otherwise.
	 */
	public static final String DATABASE_LOADED = "DATABASE_LOADED";
	/**
	 * oldValue = Database | null, newValue = Boolean.  Fired when the
	 * database is initialized.  Also fired when a database is created,
	 * loaded, or unloaded.  The old value is a reference to the database if
	 * it is loaded or null if it was unloaded.  The new value is true if the
	 * database is loaded and initialized and false otherwise.
	 */
	public static final String DATABASE_INITIALIZED = "DATABASE_INITIALIZED";
	/**
	 * oldValue = Boolean, newValue = Boolean.  Fired when the network thread
	 * is started or interrupted.  The new value is true if it was started and
	 * false if it was interrupted.  The old value is the inverse of the new
	 * value.
	 */
	public static final String NETWORK_ACTIVE = "NETWORK_ACTIVE";
	/**
	 * oldValue = Class&lt;? extends Script&gt;, newValue = Boolean.  Fired
	 * when a script is loaded or unloaded.
	 */
	public static final String SCRIPT_LOADED = "SCRIPT_LOADED";
	/** oldValue = Class&lt;? extends Script&gt;, newValue = String */
	public static final String SCRIPT_STATUS = "SCRIPT_STATUS";
	/** oldValue = Class&lt;? extends Script&gt;, newValue = Integer */
	public static final String SCRIPT_PROGRESS = "SCRIPT_PROGRESS";
	
	// properties for color-coded stats get both old & new values;
	// others just get null for old value
	
	/** oldValue = Integer xp, newValue = Integer xp */
	public static final String YOU_XP = "YOU_XP";
	/** oldValue = Integer align, newValue = Integer align */
	public static final String YOU_ALIGN = "YOU_ALIGN";
	/** oldValue = Integer credits, newValue = Integer credits */
	public static final String YOU_CREDITS = "YOU_CREDITS";
	/** oldValue = Integer turns, newValue = Integer turns */
	public static final String YOU_TURNS = "YOU_TURNS";
	
	/** oldValue = Integer sector, newValue = Integer sector */
	public static final String SHIP_SECTOR = "SHIP_SECTOR";
	/** oldValue = Integer fighters, newValue = Integer fighters  */
	public static final String SHIP_FIGHTERS = "SHIP_FIGHTERS";
	/** oldValue = Integer shields, newValue = Integer shields */
	public static final String SHIP_SHIELDS = "SHIP_SHIELDS";
	/** oldValue = Integer holds, newValue = Integer holds */
	public static final String SHIP_HOLDS = "SHIP_HOLDS";

	/** oldValue = Integer cargo, newValue = Integer quantity */
	public static final String SHIP_CARGO = "SHIP_CARGO";
		
	/** oldValue = null, newValue = Integer sector */
	public static final String MAP_ROOT = "MAP_ROOT";
	/** oldValue = null, newValue = Integer sector */
	public static final String SECTOR_UPDATED = "SECTOR_UPDATED";
	/** oldValue = null, newValue = int[][] warps */
	public static final String WARPS_DISCOVERED = "WARPS_DISCOVERED";
	/** oldValue = null, newValue = Integer sector */
	public static final String STARDOCK_DISCOVERED = "STARDOCK_DISCOVERED";
		
	private final SwingPropertyChangeSupport swingProps = new SwingPropertyChangeSupport(this, true);
	protected final WeaponM weapon;
	private final ImageIcon icon;
	protected final JFileChooser databaseFileChooser;
	protected final JFileChooser exportFileChooser;
	
	protected final ActionManager actionManager;
	protected final MainWindow mainWindow;
	protected final MapWindow mapWindow;
	protected final CreditsWindow creditsWindow = new CreditsWindow();
	
	public GUI(WeaponM weapon) throws IOException {
		this.weapon = weapon;
		
		// establish l&f
		String laf = AppSettings.getLookAndFeel();
		if("Nimbus".equals(laf)) {
			try {
			    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			        if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
			} catch (Throwable t) {
				log.error("falling back to Metal look & feel", t);
			}	
		}
		else if("System".equals(laf)) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Throwable t) {
				log.error("falling back to Metal look & feel", t);
			} 
		}
				
		icon = new ImageIcon(getClass().getResource("/resources/weaponm/WeaponM.png"));
		
		databaseFileChooser = new JFileChooser();
		databaseFileChooser.setFileFilter(new ExtensionFileFilter(".wmd", "Weapon M databases"));
		databaseFileChooser.setAcceptAllFileFilterUsed(false);
		databaseFileChooser.setMultiSelectionEnabled(false);
		
		exportFileChooser = new JFileChooser();
		exportFileChooser.addChoosableFileFilter(new ExtensionFileFilter(".twx", "Trade Wars Export v2"));
		exportFileChooser.setFileFilter(new ExtensionFileFilter(".wmx", "Weapon M export"));
		exportFileChooser.setAcceptAllFileFilterUsed(false);
		exportFileChooser.setMultiSelectionEnabled(false);
		
		mapWindow = new MapWindow(this);
		mapWindow.pack();
		
		mainWindow = new MainWindow(this);
		actionManager = new ActionManager(this);
		mainWindow.setJMenuBar(actionManager.createMainMenu());
		//mainWindow.setPreferredSize(new Dimension(1024, 600));
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	public JFrame getMainWindow() {
		return mainWindow;
	}
	
	/** thread safe */
	//public void setStatusField(int field, int value) {
	//	mainWindow.statusPanel.setField(field, value);
	//}
	
	/**
	 * This method blocks until the user's response is registered.
	 * 
	 * @param message
	 * @param title
	 * @param optionType
	 * @return
	 * @throws InterruptedException 
	 */
	public int threadSafeConfirmDialog(final String message, final String title, final int optionType) throws InterruptedException {
		if(SwingUtilities.isEventDispatchThread()) {
			return JOptionPane.showConfirmDialog(mainWindow, message, title, optionType);
		}
		else {
			final MutableInteger ret = new MutableInteger();
			synchronized(ret) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						synchronized(ret) {
							ret.value = JOptionPane.showConfirmDialog(mainWindow, message, title, optionType);
							ret.notify();
						}
					}
				});
				ret.wait();
				return ret.value;
			}
		}
	}
	
	/**
	 * This method does not block.
	 *
	 * @param message
	 * @param title
	 * @param messageType
	 */
	public void threadSafeMessageDialog(final String message, final String title, final int messageType) {
		if(SwingUtilities.isEventDispatchThread()) {
			JOptionPane.showMessageDialog(mainWindow, message, title, messageType);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(mainWindow, message, title, messageType);
				}
			});
		}
	}
	
	/**
	 * Utility method to check for the existence of a file and interactively
	 * confirm overwriting it.  This method should only be called in the EDT.
	 * 
	 * @param file the file to check
	 * @return true if the file does not exist or overwrite is confirmed
	 */
	public boolean confirmOverwrite(File file) {
		return (!file.exists() || JOptionPane.showConfirmDialog(
				mainWindow,
				"File exists:\n" + file.getPath() + "\nOverwrite?",
				"Confirm overwrite",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION);
	}

	// delegate methods for Script and DataParser
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		swingProps.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		swingProps.addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		swingProps.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		swingProps.removePropertyChangeListener(property, listener);
	}

	public void firePropertyChange(String property, Object oldValue, Object newValue) {
		//log.debug("firing property change: {} {} {}", new Object[] { property, oldValue, newValue });
		swingProps.firePropertyChange(property, oldValue, newValue);
	}

	public ImageIcon getIcon() {
		return icon;
	}
}
