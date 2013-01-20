package krum.weaponm.script;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;

import krum.jplex.UnderflowException;
import krum.weaponm.database.Database;
import krum.weaponm.database.Sector;
import krum.weaponm.gui.ParametersDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for user-defined scripts.
 * <p>
 * <b>Script Life Cycle</b>
 * <p>
 * When a script is loaded, a new instance is created.  The instance is
 * registered in the <tt>ScriptManager</tt> and its initialization methods are
 * called in the Swing event dispatch thread.  The initialization methods are
 * {@link #initScript()}, {@link #displayParametersDialog()}, and
 * {@link #startScript()}.  If any of these methods fail, the initialization
 * sequence will halt and the script will be unloaded.
 * <p>
 * Typically, a script will register for one or more script events.  While the
 * script remains loaded, its {@link #handleEvent(ScriptEvent, Object...)}
 * method will be called in the network thread each time a registered event is
 * received.  However, scripts can also do other things unrelated to script
 * events.  For example, a script might open a window that presents a custom
 * data display.  Such a script would probably set up a
 * {@link java.awt.event.WindowListener} to call {@link #unloadSelf()} when
 * its window is closed.
 * <p>
 * After a script instance is unloaded, {@link #endScript()} is called on it.
 * A script that has been unloaded will not receive events.  It will also be
 * unable to register for events, schedule tasks, load other scripts, or lock
 * or write to the network; any attempt to do so will fail silently.
 * <p>
 * <b>Using Loggers</b>
 * <p>
 * Weapon M uses the slf4j logging API, and scripts can create their own
 * <tt>Logger</tt> instance to write to the application log.  To do this,
 * you'll need to add the slf4j API jar to your classpath when compiling your
 * scripts.  To ensure compatibility, use the version distributed with Weapon
 * M, located in the WeaponM_lib directory.  For more info, see the
 * <a href="http://www.slf4j.org/docs.html">slf4j documentation</a>.
 * <p>
 * <b>Using System Resources</b>
 * <p>
 * Scripts can freely use system resources like files, sockets, and threads.
 * If a script uses any system resources, it should override
 * {@link #endScript()} to ensure that the resources are closed when the
 * script is unloaded.
 */
public abstract class Script {
	/** A network newline. */
	public static final String RETURN = "\r\n"; // suggested by Tweety :P
	
	private static final Logger log = LoggerFactory.getLogger(Script.class);
	private final ScriptManager manager;
	private final List<Parameter> parameters = new LinkedList<Parameter>();
	private final StringBuilder burst = new StringBuilder();
	private volatile boolean initialized = false;

	public Script() {
		manager = ScriptManager.getManagerForScript(this);
	}
	
	/**
	 * Returns the name of this script.  You should override this method to
	 * return a short, descriptive name.  The name will appear in the Scripts
	 * menu and elsewhere.
	 * <p>
	 * The default implementation returns the class name without the package
	 * name.
	 */
	public String getScriptName() {
		String name = getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
	
	/**
	 * Returns the menu path of this script.  The GUI uses menu paths to
	 * organize the Scripts menu.  You are encouraged to override this method.
	 * <p>
	 * Returning <tt>null</tt> or an empty string causes the script to be
	 * placed directly into the Scripts menu.  Returning <tt>"My Scripts"</tt>
	 * causes the script to be placed in a submenu called My Scripts.  To
	 * define nested submenus, use the pipe character (<tt>|</tt>) to separate
	 * elements of the menu path, e.g., <tt>"My Scripts|Red Cashing"</tt>.
	 * <p>
	 * The default implementation returns the package name converted to a menu
	 * path.
	 */
	public String getMenuPath() {
		String name = getClass().getName();
		return name.substring(0, name.lastIndexOf('.')).replaceAll("\\.", "|");
	}
	
	/**
	 * The first method called during initialization.  It has two purposes: to
	 * check that conditions are right for loading the script, and to register
	 * the script's parameters.  If your script does not have any loading
	 * conditions and does not require parameters, you do not need to override
	 * this method.
	 * <p>
	 * If conditions are not right for loading the script, this method should
	 * throw a <tt>ScriptException</tt>.
	 * 
	 * @see #displayParametersDialog()
	 * @see #startScript()
	 * @throws ScriptException
	 */
	public void initScript() throws ScriptException {	}
	
	/**
	 * The second method called during initialization.  The default
	 * implementation retrieves any parameters registered by
	 * <tt>initScript()</tt> and displays them in a simple dialog.  Advanced
	 * script authors may override this method to display a custom Swing
	 * dialog.  The method should throw a <tt>ScriptException</tt> if the
	 * dialog is closed or canceled or script initialization should not
	 * proceed for some other reason.  This method will be called in the Swing
	 * event dispatch thread.
	 * 
	 * @see #initScript()
	 * @see #startScript()
	 * @throws ScriptException
	 */
	public void displayParametersDialog() throws ScriptException {
		List<Parameter> params = getParameters();
		if(params.isEmpty()) return; // nothing to do
		boolean accepted = ParametersDialog.showDialog(manager.weapon.gui.getMainWindow(), getScriptName(), params);
		if(!accepted) throw new ScriptException("Parameters dialog canceled.");
	}
	
	/**
	 * Registers a script parameter.
	 */
	public void registerParameter(Parameter parameter) {
		if(parameter == null) throw new NullPointerException();
		if(!parameters.contains(parameter)) parameters.add(parameter);
	}

	/**
	 * Gets this script's parameters in the order they were registered.
	 */
	public List<Parameter> getParameters() {
		return new LinkedList<Parameter>(parameters);
	}
	
	/**
	 * The third method called during initialization.  Its purpose is to
	 * retrieve values from the script's parameters and register for script
	 * events.  It may also send text to the game to begin the sequence of
	 * events the script will receive.  All scripts must implement this
	 * method.
	 * 
	 * @see #initScript()
	 * @see #displayParametersDialog()
	 * @throws ScriptException
	 */
	abstract public void startScript() throws ScriptException;
	
	/**
	 * Called after a script has been unloaded.  You do not normally need to
	 * override this method, as the script manager will take care of unlocking
	 * the network, unregistering events, and canceling any timer tasks the
	 * script has scheduled.  You only need to override this method if your
	 * script uses a system resource that needs to be cleaned up, such as a
	 * file, socket, or thread.  Like the initialization methods, this method
	 * will always be called in the Swing event dispatch thread.
	 */
	public void endScript() { }
	
	/**
	 * Registers this script to receive one or more events.  Registering for
	 * the same event more than once has no effect.
	 */
	public void registerEvents(ScriptEvent... events) {
		manager.addEventListener(this, events);
	}
	
	/**
	 * Unregisters this script for one or more events.  Unregistering an event
	 * the script was not registered for has no effect.
	 */
	public void unregisterEvents(ScriptEvent... events) {
		manager.removeEventListener(this, events);
	}
	
	/**
	 * Unregisters this script for all events.
	 */
	public void unregisterEvents() {
		manager.removeEventListener(this);
	}
	
	/**
	 * Called when a registered event is received.  You should override this
	 * method to process the events your script has registered to receive.
	 * Parameters associated with each event are documented in the
	 * <tt>ScriptEvent</tt> enum.
	 * 
	 * @see ScriptEvent
	 */
	public void handleEvent(ScriptEvent event, Object... params) { }

	/**
	 * Gets the current database.
	 */
	public Database getDatabase() {
		return manager.weapon.dbm.getDatabase();
	}
	
	/**
	 * Writes a character sequence to the network.  (A <tt>String</tt> is a
	 * type of character sequence.)  Throws a <tt>NetworkLockedException</tt>
	 * if some other script has locked the network.
	 * 
	 * @param seq the sequence to send
	 * @throws IOException if an I/O error occurred
	 */
	public void sendText(CharSequence seq) throws NetworkLockedException {
		try {
			manager.writeToNetwork(seq, this); // manager does loaded check
		} catch (NetworkLockedException e) {
			throw e;
		} catch (IOException e) {
			log.error("error writing to network", e);
		}
	}
	
	/**
	 * Prints a character sequence to the terminal.  (A <tt>String</tt> is a
	 * type of character sequence.)
	 */
	public void printText(CharSequence seq) {
		//if(!manager.isLoaded(this)) return;
		try {
			manager.weapon.emulation.write(seq, 0, seq.length(), true);
		} catch (UnderflowException e) {
			// can't underflow if endOfInput == true
		}
	}
	
	/**
	 * Locks the network so that only this script may write to it.  Use this
	 * when you are about to send a burst that will trigger multiple events,
	 * so that other scripts cannot respond to those events and interfere with
	 * whatever you're doing.  Other scripts will still receive events, but
	 * they will get a <tt>NetworkLockedException</tt> if they try to write to
	 * the network.
	 * <p>
	 * When a script has exclusive network access, it will always be the first
	 * listener to receive script events.  If you unlock the network upon
	 * receiving the last expected event, other scripts will have a chance to
	 * respond to it.
	 * 
	 * @throws NetworkLockedException if the network is locked by another script
	 */
	public void lockNetwork() throws NetworkLockedException {
		manager.lockNetwork(this);
	}
	
	/**
	 * Releases this script's exclusive lock on the network.
	 * 
	 * @throws NetworkLockedException if the network is locked by another script
	 */
	public void unlockNetwork() throws NetworkLockedException {
		manager.unlockNetwork(this);
	}
	
	/**
	 * Requires that another script be loaded.  If the script is not already
	 * loaded, a new instance will be created and the usual sequence of
	 * initialization methods will be called on it.  This method will block if
	 * the required script displays a parameters dialog, so it should not be
	 * called from {@link #handleEvent(ScriptEvent, Object...)}.
	 * <p>
	 * If the required script throws an exception during its initialization,
	 * this method will throw an exception.  Once the required script is fully
	 * initialized, if it is unloaded for any reason while this script still
	 * requires it, then this script will also be unloaded.  If this script is
	 * unloaded and it is the only script that requires another script, then
	 * the other script will also be unloaded.
	 * 
	 * @param scriptClass the script class to load
	 * @throws ScriptException if the script instance cannot be created or
	 * the initialization sequence fails
	 */
	public void requireScript(Class<? extends Script> scriptClass) throws ScriptException {
		try {
			manager.loadScript(scriptClass, this);
		} catch (InstantiationException e) {
			throw new ScriptException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptException(e);
		}
	}
	
	/**
	 * Requires that another script be loaded.  See
	 * {@link #requireScript(Class)} for details.
	 * 
	 * @param className the name of the script class to load
	 * @throws ScriptException if the script instance cannot be created or
	 * the initialization sequence fails
	 */
	public void requireScript(String className) throws ScriptException {
		try {
			manager.loadScript(className, this);
		} catch (InstantiationException e) {
			throw new ScriptException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptException(e);
		}
	}
	
	/**
	 * Cancels this script's requirement that another script be loaded.  If
	 * the other script was only required by this script, it will be unloaded.
	 * If the specified script class was not required by this script, this
	 * method has no effect.
	 */
	public void cancelRequirement(Class<? extends Script> scriptClass) {
		manager.cancelRequirement(scriptClass, this);
	}
	
	/**
	 * Cancels this script's requirement that another script be loaded.  If
	 * the other script was only required by this script, it will be unloaded.
	 * If the specified script class was not required by this script, this
	 * method has no effect.
	 */
	public void cancelRequirement(String className) {
		manager.cancelRequirement(className, this);
	}
	
	/**
	 * Immediately unloads this script, all scripts that require this script,
	 * and all scripts that only this script requires.  After the method that
	 * calls <tt>unloadSelf()</tt> returns, the next method called on the
	 * script instance will be {@link #endScript()}.
	 */
	public void unloadSelf() {
		manager.unloadScript(this.getClass(), true);
	}
	
	/**
	 * Displays a dialog modal to the main window.  This method is thread safe
	 * and does not block, so it can be used to display messages in
	 * <tt>handleEvent()</tt>.
	 * 
	 * @param message the dialog message
	 * @param title the dialog title
	 * @param messageType a message type constant from <tt>JOptionPane</tt>
	 */
	public void showMessageDialog(String message, String title, int messageType) {
		manager.weapon.gui.threadSafeMessageDialog(message, title, messageType);
	}

	/**
	 * Schedules a task to run once after the specified delay.
	 * 
	 * @param delay the delay in milliseconds
	 */
	synchronized public void scheduleTask(ScriptTimerTask task, long delay) {
		manager.scheduleTask(task, delay);
	}
	
	/**
	 * Schedules a task to run once at the specified date.
	 */
	synchronized public void scheduleTask(ScriptTimerTask task, Date date) {
		manager.scheduleTask(task, date);
	}
	
	/**
	 * Schedules a task to run repeatedly, starting after the specified delay.
	 * 
	 * @param delay the delay in milliseconds
	 * @param interval the interval in milliseconds
	 */
	synchronized public void scheduleTask(ScriptTimerTask task, long delay, long interval) {
		manager.scheduleTask(task, delay, interval);
	}
	
	/**
	 * Schedules a task to run repeatedly, starting at the specified date.
	 * 
	 * @param date the date to begin running the task
	 * @param interval the interval in milliseconds
	 */
	synchronized public void scheduleTask(ScriptTimerTask task, Date date, long interval) {
		manager.scheduleTask(task, date, interval);
	}
	
	/*
	 * Gets the main window of the application so you can use it to display
	 * custom dialogs.  Disposing of the window or making it invisible would
	 * be bad, so don't do that.
	 */
	/*
	public Window getMainWindow() {
		return manager.weapon.gui.getWindow();
	}
	*/
	
	/**
	 * Gets the current major prompt, or null if the lexer is not at a major
	 * prompt.
	 */
	public ScriptEvent getCurrentPrompt() {
		return manager.weapon.dbm.getDataParser().getCurrentPrompt();
	}
	
	/**
	 * Connects to the game server.  Returns immediately if the network
	 * manager already has a live network thread.  Otherwise, starts a new
	 * network thread and blocks until the connection is established.
	 * 
	 * @throws IOException if a network error occurs during connection
	 * @throws InterruptedException if this thread is interrupted before the connection is established
	 */
	public void connect() throws IOException, InterruptedException {
		manager.weapon.network.blockingConnect();
	}
	
	/**
	 * Disconnects from the game server.  This method returns immediately.
	 * The network thread will be interrupted, but it may remain alive for a
	 * short time.
	 */
	public void disconnect() {
		manager.weapon.network.disconnect();
	}
	
	/**
	 * Returns true if the Weapon is connected to the game server.  More
	 * precisely, returns true if the network manager has a live network
	 * thread that has not been interrupted. 
	 */
	public boolean isConnected() {
		return manager.weapon.network.isConnected();
	}
	
	/**
	 * A convenient way to create a {@link Parameter} representing a sector
	 * number.  Initializes an {@link Parameter.Type#INTEGER} parameter with a
	 * minimum value of 1 and a maximum value equal to the number of sectors
	 * in the universe.
	 * 
	 * @throws ScriptException if the database has not been initialized
	 */
	public Parameter createSectorParameter(String title, int sector) throws ScriptException {
		if(!getDatabase().isInitialized()) throw new ScriptException("Database has not been initialized.");
		Parameter p = new Parameter(title, sector);
		p.setMinValue(1);
		p.setMaxValue(getDatabase().getNumSectors());
		return p;
	}
	
	/**
	 * Shortcut for <tt>getDatabase().getNumSectors()</tt>.
	 */
	public int sectors() {
		return getDatabase().getNumSectors();
	}

	/**
	 * Shortcut for <tt>getDatabase().getSector(number)</tt>.
	 */
	public Sector sector(int number) {
		return getDatabase().getSector(number);
	}
	
	/**
	 * Shortcut for <tt>getDatabase().getStardockSector()</tt>.
	 */
	public Sector stardock() {
		return getDatabase().getStardockSector();
	}
	
	/**
	 * Appends the string representation of an object to the burst buffer.
	 * Each script instance has its own independent burst buffer.
	 */
	public void appendBurst(Object obj) {
		burst.append(obj.toString());
	}
	
	/**
	 * Clears the burst buffer without sending it.
	 */
	public void clearBurst() {
		burst.setLength(0);
	}
	
	/**
	 * Sends the burst buffer and clears it.
	 * 
	 * @throws NetworkLockedException if the network is locked by another script
	 */
	public void sendBurst() throws NetworkLockedException {
		sendText(burst.toString());
		clearBurst();
	}
	
	/**
	 * Creates a new, empty, invisible <tt>JDialog</tt> that may be modal to
	 * the main window.  This method should only be called in the Swing event
	 * dispatch thread.  Its purpose is to give advanced script authors a way
	 * to replace the standard script parameters dialog. 
	 * 
	 * @param modal true if the dialog should be modal to the main window
	 */
	public JDialog createDialog(boolean modal) {
		return new JDialog(manager.weapon.gui.getMainWindow(), modal) {
			private static final long serialVersionUID = 1L;
			@Override
			public Window getOwner() { return null; }
			@Override
			public Container getParent() { return null; }
		};
	}
	
	/**
	 * Centers a dialog on the main window.  Call this after adding components
	 * and calling <tt>pack()</tt> on your custom dialog.
	 */
	public void centerDialog(JDialog dialog) {
		dialog.setLocationRelativeTo(manager.weapon.gui.getMainWindow());
	}

	boolean isInitialized() {
		return initialized;
	}

	void setInitialized() {
		this.initialized = true;
	}
}
