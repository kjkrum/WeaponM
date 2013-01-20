package krum.weaponm;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import krum.jtx.ScrollbackBuffer;
import krum.weaponm.database.DatabaseManager;
import krum.weaponm.emulation.Emulation;
import krum.weaponm.gui.GUI;
import krum.weaponm.network.NetworkManager;
import krum.weaponm.script.ScriptManager;

// threads to think about: edt, timer thread, network thread
// network thread lifecycle is completely bounded by database reference lifecycle

public class WeaponM {
	public static final String VERSION = "20121231";
	//public static volatile boolean DEBUG = false;
	public static volatile boolean DEBUG_LEXER = false;
	public static volatile boolean DEBUG_ANSI = false;
	public static volatile boolean DEBUG_SCRIPTS = false;
	protected static final Logger log = LoggerFactory.getLogger(WeaponM.class);
	public final ScrollbackBuffer buffer;
	public final Emulation emulation;
	public final NetworkManager network;
	public final ScriptManager scripts;
	public final DatabaseManager dbm;
	public final GUI gui;
	
	protected WeaponM() throws IOException, ClassNotFoundException {
		log.info("Weapon M started {}", new Date());
		buffer = new ScrollbackBuffer(80, AppSettings.getBufferLines());
		emulation = new Emulation(this);
		network = new NetworkManager(this);
		scripts = new ScriptManager(this);
		dbm = new DatabaseManager(this);
		gui = new GUI(this);
	}

	public void shutdown() {
		dbm.close(); // kills network and scripts
		log.info("Weapon M exiting");
		System.exit(0);
	}
	
	// called when a db is loaded or created, and when scripts are reloaded
	public void autoLoadScripts() {
		String[] names = AppSettings.getAutoLoadScripts().split(","); 
		for(String name : names) {
			if("".equals(name)) continue;
			try {
				scripts.loadScript(name, null);
			} catch (Throwable t) {
				log.error(t.getMessage());
				gui.threadSafeMessageDialog(t.getMessage(), "Script Auto-Loader", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					WeaponM weapon = new WeaponM();
					if(args.length > 0) {
						try {
							weapon.dbm.open(new File(args[0]));
							weapon.autoLoadScripts();
						}
						catch(Throwable t) {
							log.error("unspecified error", t);
							String msg = t.getMessage();
							if(msg == null) msg = t.getClass().getName();
							weapon.gui.threadSafeMessageDialog(msg, "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}
}
