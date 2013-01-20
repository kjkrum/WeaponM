package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import krum.weaponm.WeaponM;
import krum.weaponm.database.Database;
import krum.weaponm.database.Sector;
import krum.weaponm.database.You;
import krum.weaponm.script.Script;

/**
 * Creates and maintains GUI menus and actions.  Enables and disables items in
 * response to property change events.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
public class ActionManager {
	private static final Logger log = LoggerFactory.getLogger(ReloadScriptsAction.class);
	
	private final GUI gui;
	
	// collections of actions to enable/disable in response to property changes
	private final Set<AbstractAction> enableOnLoad = new HashSet<AbstractAction>(); // disable on unload
	private final Set<JMenu> enableOnLoadMenus = new HashSet<JMenu>(); // because AbstractAction and JMenu have nothing in common that includes enable/disable
	private final Set<AbstractAction> enableOnConnect = new HashSet<AbstractAction>(); // disable on disconnect
	private final Set<AbstractAction> disableOnConnect = new HashSet<AbstractAction>(); // enable on disconnect
	
	// actions
	private final AbstractAction newDatabase;
	private final AbstractAction openDatabase;
	private final AbstractAction saveDatabase;
	private final AbstractAction closeDatabase;

	private final AbstractAction connect;
	private final AbstractAction disconnect;
	
	private final AbstractAction showAboutDialog;
	private final AbstractAction showCreditsWindow;
	private final AbstractAction showLoginOptionsDialog;
	private final AbstractAction website;
	
	// script stuff
	private final JMenu scriptsMenu;
	protected final Set<ScriptMenuItem> scriptMenuItems = new HashSet<ScriptMenuItem>();
	private final Map<String, JMenu> menuPathMap = new HashMap<String, JMenu>();

	public ActionManager(GUI gui) {
		this.gui = gui;
		
		/* scripts menu */
		scriptsMenu = new JMenu("Scripts");
		scriptsMenu.setMnemonic('S');
		scriptsMenu.setEnabled(false);
		enableOnLoadMenus.add(scriptsMenu);
		
		/* db actions */
		
		newDatabase = new NewDatabaseAction(gui);
		disableOnConnect.add(newDatabase);
		
		openDatabase = new OpenDatabaseAction(gui);
		disableOnConnect.add(openDatabase);
		
		saveDatabase = new SaveDatabaseAction(gui);
		saveDatabase.setEnabled(false);
		enableOnLoad.add(saveDatabase);
		
		closeDatabase = new CloseDatabaseAction(gui);
		closeDatabase.setEnabled(false);
		enableOnLoad.add(closeDatabase);
		
		/* network actions */
		
		connect = new ConnectAction(gui);
		connect.setEnabled(false);
		enableOnLoad.add(connect);
		disableOnConnect.add(connect);
		
		disconnect = new DisconnectAction(gui);
		disconnect.setEnabled(false);
		enableOnConnect.add(disconnect);
		
		showLoginOptionsDialog = new ShowLoginOptionsDialogAction(gui);
		showLoginOptionsDialog.setEnabled(false);
		enableOnLoad.add(showLoginOptionsDialog);
		disableOnConnect.add(showLoginOptionsDialog);
		
		/* weapon actions */
		
		showAboutDialog = new ShowAboutDialogAction(gui.mainWindow);
		showCreditsWindow = new ShowCreditsWindowAction(gui);
		website = new WebsiteAction();
		
		gui.addPropertyChangeListener(GUI.DATABASE_LOADED, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				boolean loaded = (Boolean) e.getNewValue();
				if(loaded) populateScriptsMenu();
				else clearScriptsMenu();
				for(AbstractAction action : enableOnLoad) {
					action.setEnabled(loaded);
				}
				for(JMenu menu : enableOnLoadMenus) {
					menu.setEnabled(loaded);
				}
			}
		});
		
		gui.addPropertyChangeListener(GUI.NETWORK_ACTIVE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				for(AbstractAction action : enableOnConnect) {
					action.setEnabled((Boolean) e.getNewValue());
				}
				for(AbstractAction action : disableOnConnect) {
					action.setEnabled(!(Boolean) e.getNewValue());
				}
			}
		});
	}
		
	void reloadScripts() {
		log.info("Reloading scripts");
		clearScriptsMenu();
		gui.weapon.scripts.reset(); // makes the script manager search for classes
		populateScriptsMenu();
		gui.weapon.autoLoadScripts();
	}
	
	void populateScriptsMenu() {
		Collection<Class<? extends Script>> classes = gui.weapon.scripts.getScripts();
		for(Class<? extends Script> clazz : classes) {
			try {
				Script script = clazz.newInstance();
				ScriptAction action = new ScriptAction(clazz, script.getScriptName(), gui);
				action.setEnabled(false);
				ScriptMenuItem menuItem = new ScriptMenuItem(action);
				gui.addPropertyChangeListener(GUI.SCRIPT_LOADED, menuItem);
				gui.addPropertyChangeListener(GUI.DATABASE_LOADED, menuItem);
				resolveMenuPath(script.getMenuPath()).add(menuItem, 0);
				scriptMenuItems.add(menuItem);
			} catch (Throwable t) {
				log.error("Error populating scripts menu", t);
			}
		}
		scriptsMenu.addSeparator();
		scriptsMenu.add(new ReloadScriptsAction(this));
	}
	
	JMenu resolveMenuPath(String menuPath) {
		if(menuPath == null) return scriptsMenu;
		String[] split = menuPath.split("\\|");
		for(int i = 0; i < split.length; ++i) {
			split[i] = split[i].trim();
		}
		String workingPath = "";
		JMenu workingMenu = scriptsMenu;
		for(String pathElement : split) {
			workingPath += pathElement;
			if(menuPathMap.containsKey(workingPath)) {
				workingMenu = menuPathMap.get(workingPath);
			}
			else {
				JMenu newMenu = new JMenu(pathElement);
				menuPathMap.put(workingPath, newMenu);
				workingMenu.add(newMenu);
				workingMenu = newMenu;
			}
			workingPath += "|";
		}
		return workingMenu;
	}
	
	void clearScriptsMenu() {
		scriptsMenu.removeAll();
		for(ScriptMenuItem menuItem : scriptMenuItems) {
			gui.removePropertyChangeListener(menuItem);
		}
		scriptMenuItems.clear();
		menuPathMap.clear();
	}
	
	JMenuBar createMainMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu dbMenu = new JMenu("Database");
		dbMenu.setMnemonic('D');
		dbMenu.add(new JMenuItem(newDatabase));
		dbMenu.add(new JMenuItem(openDatabase));
		dbMenu.add(new JMenuItem(saveDatabase));
		dbMenu.add(new JMenuItem(closeDatabase));
		menuBar.add(dbMenu);
		
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		viewMenu.add(new JMenuItem(new ShowMapAction(gui)));
		viewMenu.setEnabled(false);
		enableOnLoadMenus.add(viewMenu);
		menuBar.add(viewMenu);
		
		JMenu networkMenu = new JMenu("Network");
		networkMenu.setMnemonic('N');
		networkMenu.add(new JMenuItem(connect));
		networkMenu.add(new JMenuItem(disconnect));
		networkMenu.addSeparator();
		networkMenu.add(new JMenuItem(showLoginOptionsDialog));
		networkMenu.setEnabled(false);
		enableOnLoadMenus.add(networkMenu);
		menuBar.add(networkMenu);
		
		menuBar.add(scriptsMenu);
				
		JMenu weaponMenu = new JMenu("Weapon");
		weaponMenu.setMnemonic('W');
		weaponMenu.add(new JMenuItem(showAboutDialog));
		weaponMenu.add(new JMenuItem(showCreditsWindow));
		weaponMenu.add(new JMenuItem(website));
		menuBar.add(weaponMenu);
		
		// debugging stuff
		
		JMenu debugMenu = new JMenu("Debug");
		debugMenu.setMnemonic('D');
		
		AbstractAction ansiAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				WeaponM.DEBUG_ANSI = !WeaponM.DEBUG_ANSI;				
			}
		};
		ansiAction.putValue(AbstractAction.NAME, "ANSI");
		ansiAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_A);
		debugMenu.add(new JCheckBoxMenuItem(ansiAction));
		
		AbstractAction scriptsAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				WeaponM.DEBUG_SCRIPTS = !WeaponM.DEBUG_SCRIPTS;				
			}
		};
		scriptsAction.putValue(AbstractAction.NAME, "Scripts");
		scriptsAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S);
		debugMenu.add(new JCheckBoxMenuItem(scriptsAction));
		
		AbstractAction lexerAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WeaponM.DEBUG_LEXER = !WeaponM.DEBUG_LEXER;
				gui.weapon.dbm.getDataParser().enableDebugLogging(WeaponM.DEBUG_LEXER);
			}
		};
		lexerAction.putValue(AbstractAction.NAME, "Lexer");
		lexerAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_L);
		debugMenu.add(new JCheckBoxMenuItem(lexerAction));
		lexerAction.setEnabled(false);
		enableOnLoad.add(lexerAction);
		
		AbstractAction dbAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Database database = gui.weapon.dbm.getDatabase();
				org.pf.joi.Inspector.inspect(database);
			}
		};
		dbAction.putValue(AbstractAction.NAME, "Database");
		dbAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_D);
		debugMenu.add(new JMenuItem(dbAction));
		dbAction.setEnabled(false);
		enableOnLoad.add(dbAction);
		
		AbstractAction dbSectorAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Database database = gui.weapon.dbm.getDatabase();
				Sector sector = database.getYou().getSector();
				if(sector == null)	org.pf.joi.Inspector.inspect(database);
				else org.pf.joi.Inspector.inspect(sector);
			}
		};
		dbSectorAction.putValue(AbstractAction.NAME, "Database (Sector)");
		dbSectorAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_C);
		debugMenu.add(new JMenuItem(dbSectorAction));
		dbSectorAction.setEnabled(false);
		enableOnLoad.add(dbSectorAction);
		
		AbstractAction dbYouAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Database database = gui.weapon.dbm.getDatabase();
				You you = database.getYou();
				org.pf.joi.Inspector.inspect(you);
			}
		};
		dbYouAction.putValue(AbstractAction.NAME, "Database (You)");
		dbYouAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_Y);
		debugMenu.add(new JMenuItem(dbYouAction));
		dbYouAction.setEnabled(false);
		enableOnLoad.add(dbYouAction);
		
		menuBar.add(debugMenu);
		
		return menuBar;
	}	
}
