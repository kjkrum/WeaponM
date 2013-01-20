package krum.weaponm.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import krum.weaponm.WeaponM;
import krum.weaponm.database.lexer.DataLexer;
import krum.weaponm.gui.GUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The database manager.  Although this class is public, scripts cannot
 * normally obtain a reference to the Weapon's instance of it.
 */
public class DatabaseManager {
	protected static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
	protected final WeaponM weapon;
	protected final DataLexer lexer; // reusable
	private File file;
	private Database database;
	private DataParser parser;
	//private boolean dirty;
	
	public DatabaseManager(WeaponM weapon) throws IOException, ClassNotFoundException {
		this.weapon = weapon;
		lexer = new DataLexer();
	}

	synchronized public boolean isDatabaseOpen() {
		return database != null;
	}
	
	synchronized public Database getDatabase() {
		return database;
	}
	
	synchronized public DataParser getDataParser() {
		return parser;
	}
	
	synchronized public Database open(File file) throws IOException {
		// the use of createNewFile for locking is not recommended, but we can live with the limitations
		File lockFile = new File(file.getPath() + ".lock");
		if(!lockFile.createNewFile()) {
			throw new IOException("Lock file " + lockFile.getPath() + " exists.");
		}		
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			try {
				this.database = (Database) in.readObject();
			}
			finally { in.close(); }
			this.file = file;
			parser = new DataParser(weapon, lexer, database);
			weapon.gui.firePropertyChange(GUI.DATABASE_LOADED, database, true);
			if(database.isInitialized()) {
				weapon.gui.firePropertyChange(GUI.DATABASE_INITIALIZED, database, true);
			}
			if(database.getStardockSector() != null) {
				weapon.gui.firePropertyChange(GUI.STARDOCK_DISCOVERED, null, database.getStardockSector().getNumber());
			}
			if(database.getYou().getSector() != null) {
				weapon.gui.firePropertyChange(GUI.SHIP_SECTOR, null, database.getYou().getSector().getNumber());
			}
			log.info("database loaded from {}", file.getPath());
			return database;
		} catch(Exception e) {
			lockFile.delete();
			if(e instanceof IOException) throw (IOException) e;
			else { // ClassNotFoundException, ClassCastException
				throw new IOException("The file \"" + file.getPath() + "\" is not a compatible Weapon M database.", e); 
			}
		}
	}

	synchronized public Database create(File file) throws IOException, ClassNotFoundException {
		File lockFile = new File(file.getPath() + ".lock");
		if(!lockFile.createNewFile()) {
			throw new IOException("Lock file " + lockFile.getPath() + " exists.");
		}		
		Database database = new Database();
		save(file, database);
		close();
		this.file = file;
		this.database = database;
		parser = new DataParser(weapon, lexer, database);
		weapon.gui.firePropertyChange(GUI.DATABASE_LOADED, database, true);
		log.info("database created in {}", file.getPath());
		return database;
	}
	
	synchronized public void save() throws IOException {
		save(file, database);
		log.info("database saved");
	}
	
	synchronized public void saveAs(File newFile) throws IOException {
		// create new lock file
		File lockFile = new File(newFile.getPath() + ".lock");
		if(!lockFile.createNewFile()) {
			throw new IOException("Lock file " + lockFile.getPath() + " exists.");
		}
		save(newFile, database);
		// delete old lock file
		new File(file.getPath() + ".lock").delete();
		file = newFile;
		log.info("database saved as '{}'", file.getPath());
	}
	
	synchronized public void saveCopy(File copyFile) throws IOException {
		File lockFile = new File(file.getPath() + ".lock");
		if(!lockFile.createNewFile()) {
			throw new IOException("Lock file " + lockFile.getPath() + " exists.");
		}
		save(copyFile, database);
		lockFile.delete();
		log.info("database copied to {}", copyFile.getPath());
	}
	
	protected void save(File file, Database database) throws IOException {
		if(file.isDirectory()) throw new IOException("Target file is a directory.");
		File tmpFile = new File(file.getPath() + ".tmp");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmpFile));
		//FileOutputStream out = new FileOutputStream(tmpFile);
		//XStream xstream = new XStream(new DomDriver());
		try {
			out.writeObject(database);
			//xstream.toXML(database, out);
		} finally {
			out.close();
		}
		if(file.exists() && !file.delete()) {
			throw new IOException("Save incomplete: could not delete old file.");
		}
		if(!tmpFile.renameTo(file)) {
			throw new IOException("Save incomplete: could not rename temp file.");
		}
	}
	
	/**
	 * Nulls database references and shuts down network and scripts.
	 */
	public void close() {
		if(file != null) {
			weapon.scripts.unloadAll();
			weapon.network.disconnect();
			new File(file.getPath() + ".lock").delete();
			file = null;
			database = null;
			lexer.removeEventListener(parser);
			parser = null;
			weapon.gui.firePropertyChange(GUI.DATABASE_INITIALIZED, null, false);
			weapon.gui.firePropertyChange(GUI.DATABASE_LOADED, null, false);
			log.info("database closed");
		}
		
	}
}
