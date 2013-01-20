package krum.weaponm.script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import krum.weaponm.AppSettings;
import krum.weaponm.WeaponM;
import krum.weaponm.gui.GUI;

import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.ClassLoaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The script manager.  Although this class is public, scripts cannot normally
 * obtain a reference to the Weapon's instance of it.
 */
public class ScriptManager {
	private static final Logger log = LoggerFactory.getLogger(ScriptManager.class);
	// experimental 
	//private static final Map<Class<? extends Script>, ScriptManager> managerMap = new HashMap<Class<? extends Script>, ScriptManager>();
	private static final Map<ClassLoader, ScriptManager> managerMap = new HashMap<ClassLoader, ScriptManager>();
	// package - accessed by script
	final WeaponM weapon;
	private final Timer timer = new Timer("ScriptTimer");
	// found classes
	//private Set<Class<? extends Script>> classes = new HashSet<Class<? extends Script>>();
	private Map<String, Class<? extends Script>> classMap = new HashMap<String, Class<? extends Script>>();
	
	// maps script classes to loaded instances; a class with a key in this map is considered loaded
	private final Map<Class<? extends Script>, Script> instances =
			new HashMap<Class<? extends Script>, Script>();
	// maps loaded script classes to other loaded script classes that require them
	private final Map<Class<? extends Script>, Set<Class<? extends Script>>> deps =
			new HashMap<Class<? extends Script>, Set<Class<? extends Script>>>();
	// maps script instances to timer tasks they have scheduled
	private final Map<Script, Set<TimerTask>> timerTasks = new HashMap<Script, Set<TimerTask>>();
	// maps uninitialized script instances to events they have registered for
	private final Map<Script, Set<ScriptEvent>> pendingEventRegs = new HashMap<Script, Set<ScriptEvent>>();
	// maps script events to script instances that are registered for them
	private final Map<ScriptEvent, Set<Script>> eventListeners = new HashMap<ScriptEvent, Set<Script>>();
	// the script instance with exclusive network write access
	private volatile Script exclusiveScript;
	// true if network is locked because some listener wrote in response to the event currently being dispatched
	private volatile boolean networkLocked;
	
	public ScriptManager(WeaponM weapon) {
		this.weapon = weapon;
		//Script.setManager(this);
		findScriptClasses();
	}
	
	synchronized public void addEventListener(Script listener, ScriptEvent... events) {
		if(!isLoaded(listener)) return;
		if(listener.isInitialized()) {
			for(ScriptEvent event : events) {
				if(!eventListeners.containsKey(event)) eventListeners.put(event, new HashSet<Script>());
				eventListeners.get(event).add(listener);
			}
		}
		else {
			if(!pendingEventRegs.containsKey(listener)) pendingEventRegs.put(listener, new HashSet<ScriptEvent>());
			Set<ScriptEvent> set = pendingEventRegs.get(listener);
			for(ScriptEvent event : events) {
				set.add(event);
			}
		}
	}
	
	synchronized public void removeEventListener(Script listener, ScriptEvent... events) {
		if(listener.isInitialized()) {
			for(ScriptEvent event : events) {
				if(eventListeners.containsKey(event)) {
					Set<Script> listeners = eventListeners.get(event);
					listeners.remove(listener);
					if(listeners.isEmpty()) eventListeners.remove(event);
				}
			}
		}
		else if(pendingEventRegs.containsKey(listener)) {
			Set<ScriptEvent> set = pendingEventRegs.get(listener);
			for(ScriptEvent event : events) {
				set.remove(event);
			}
		}
	}
	
	synchronized public void removeEventListener(Script listener) {
		if(listener.isInitialized()) {
			// copy the key set to avoid a ConcurrentModificationException
			for(ScriptEvent event : new HashSet<ScriptEvent>(eventListeners.keySet())) {
				Set<Script> listeners = eventListeners.get(event);
				listeners.remove(listener);
				if(listeners.isEmpty()) eventListeners.remove(event);
			}
		}
		else pendingEventRegs.remove(listener);
	}
	
	synchronized public void activateEvents(Script listener) {
		if(!listener.isInitialized()) return;
		if(pendingEventRegs.containsKey(listener)) {
			Set<ScriptEvent> set = pendingEventRegs.get(listener);			
			ScriptEvent[] events = new ScriptEvent[set.size()]; 
			set.toArray(events);
			addEventListener(listener, events);
			pendingEventRegs.remove(listener);
		}
	}
	
	synchronized public void fireEvent(ScriptEvent event, Object... params) {
		if(WeaponM.DEBUG_SCRIPTS) {
			StringBuilder sb = new StringBuilder();
			sb.append("firing ");
			sb.append(event);
			for(Object param : params) {
				sb.append(' ');
				sb.append(param);
			}
			log.debug(sb.toString());
		}
		if(!eventListeners.containsKey(event)) return;
		Set<Script> listenerSet = eventListeners.get(event);
		int bytesWritten = weapon.network.getTotalBytesWritten();
		// exclusive script always gets events first.  this way, it can unlock
		// the network on the last expected event and other scripts can respond
		// to it.  cache the exclusive script in a local var in case the field
		// is cleared during the method invocation.
		Script exclusiveScript = this.exclusiveScript;
		if(listenerSet.contains(exclusiveScript)) {
			try {
				exclusiveScript.handleEvent(event, params);
			} catch(Throwable t) {
				log.error("script error", t);
				unloadScript(exclusiveScript.getClass(), true);
			}
			if(weapon.network.getTotalBytesWritten() != bytesWritten) networkLocked = true;
		}
		// now for the others
		// randomize the order to make it more fair
		// could even get fancy and sort by last successful network write... but no
		// copying the collection also prevents weirdness from scripts unloading
		List<Script> listenerList = new ArrayList<Script>(listenerSet);
		Collections.shuffle(listenerList);
		for(Script listener : listenerList) {
			if(listener == exclusiveScript) continue;
			try {
				if(isLoaded(listener)) listener.handleEvent(event, params);
			} catch(Throwable t) {
				log.error("script error", t);
				unloadScript(listener.getClass(), true);
			}
			if(weapon.network.getTotalBytesWritten() != bytesWritten) networkLocked = true;
		}
		networkLocked = false;
	}
	
	/**
	 * 
	 * @param seq
	 * @param sender
	 * @throws NetworkLockedException if the network is locked
	 * @throws IOException if some other I/O error occurred
	 */
	synchronized public void writeToNetwork(CharSequence seq, Script sender) throws IOException {
		if(!isLoaded(sender)) return;
		if(networkLocked || (exclusiveScript != null && exclusiveScript != sender)) {
			throw new NetworkLockedException();
		}
		weapon.network.write(seq);
	}
	
	public void lockNetwork(Script script) throws NetworkLockedException {
		if(!isLoaded(script)) return;
		if(exclusiveScript != null && exclusiveScript != script) {
			throw new NetworkLockedException("network is locked by " + exclusiveScript.getClass().getName());
		}
		exclusiveScript = script;
	}
	
	public void unlockNetwork(Script script) throws NetworkLockedException {
		if(exclusiveScript != null && exclusiveScript != script) {
			throw new NetworkLockedException("network is locked by " + exclusiveScript.getClass().getName());
		}
		exclusiveScript = null;
	}
	
	synchronized public void loadScript(String className, final Script caller)
			throws ScriptException, InstantiationException, IllegalAccessException {
		if(!classMap.containsKey(className)) {
			throw new ScriptException("Class not found: " + className);
		}
		loadScript(classMap.get(className), caller);
	}
	
	/**
	 * 
	 * @param classToLoad the script class to load
	 * @param caller the script instance calling this method, or null if loaded by the GUI
	 * @throws ScriptException if initScript fails
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	synchronized public void loadScript(final Class<? extends Script> classToLoad, final Script caller)
			throws ScriptException, InstantiationException, IllegalAccessException {
		// ignore if caller isn't the gui and isn't loaded
		if(caller != null && !isLoaded(caller)) return;
		
		if(!classMap.containsValue(classToLoad)) {
			throw new ScriptException("Unrecognized class " + classToLoad.getName());
		}
		
		if(instances.containsKey(classToLoad)) {
			// required script is already loaded; just record the dependency
			//Script instance = instances.get(classToLoad);
			if(!deps.containsKey(classToLoad)) deps.put(classToLoad, new HashSet<Class<? extends Script>>());
			deps.get(classToLoad).add(caller == null ? null : caller.getClass());
		}
		else {
			// create a new instance
			final Script instance = classToLoad.newInstance();	
			//instance.setManager(this);
			instances.put(classToLoad, instance);
			
			// add loader to loader map
			if(!deps.containsKey(classToLoad)) deps.put(classToLoad, new HashSet<Class<? extends Script>>());
			deps.get(classToLoad).add(caller == null ? null : caller.getClass());
			
			// script is now considered loaded; it should be unloaded if any error occurs
			log.info("loaded {}", classToLoad.getName());
			weapon.gui.firePropertyChange(GUI.SCRIPT_LOADED, classToLoad, true);
			
			// initialize the instance
			ScriptInitializer initializer = new ScriptInitializer(this, instance);
			if(SwingUtilities.isEventDispatchThread()) {
				initializer.run();
			}
			else {
				try {
					SwingUtilities.invokeAndWait(initializer);
				} catch (InterruptedException e) {
					unloadScript(classToLoad, false);
					throw new ScriptException(e);
				} catch (InvocationTargetException e) {
					unloadScript(classToLoad, false);
					throw new ScriptException(e);
				}
			}
			if(initializer.hasError()) {
				unloadScript(classToLoad, false);
				Throwable t = initializer.getError();
				if(t instanceof ScriptException) throw (ScriptException) t;
				else throw new ScriptException(t);
			}
		}
	}
	
	synchronized public boolean isLoaded(Class<? extends Script> scriptClass) {
		return instances.containsKey(scriptClass);
	}
	
	synchronized public boolean isLoaded(Script instance) {
		Class<? extends Script> scriptClass = instance.getClass();
		return instances.get(scriptClass) == instance;
	}
	
	synchronized public void unloadScript(Class<? extends Script> classToUnload, boolean cascade) {
		if(!instances.containsKey(classToUnload)) return;
		
		// cancel timer tasks
		if(timerTasks.containsKey(classToUnload)) {
			for(TimerTask task : timerTasks.get(classToUnload)) {
				task.cancel();
			}
			timerTasks.remove(classToUnload);
			timer.purge();
		}
		
		// terminate instance
		final Script instance = instances.remove(classToUnload);
		removeEventListener(instance);
		if(exclusiveScript == instance) exclusiveScript = null;
		try {
			if(SwingUtilities.isEventDispatchThread()) {
				instance.endScript();	
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						instance.endScript();
					}
				});
			}
		} catch(Throwable t) {
			// recursion must succeed!
			log.error("error unloading script", t);
		}
		
		if(cascade) { // unload scripts that require instance
			for(Class<? extends Script> loaderClass : deps.get(instance.getClass())) {
				if(loaderClass != null) unloadScript(loaderClass, true);
			}
			deps.remove(classToUnload);
		}
		
		// unload scripts required only by scriptClass
		for(Class<? extends Script> loadedClass : deps.keySet()) {
			if(deps.get(loadedClass).contains(classToUnload)) {
				deps.get(loadedClass).remove(classToUnload);
				if(deps.get(loadedClass).isEmpty()) {
					unloadScript(loadedClass, true);
				}
			}
		}
		log.info("unloaded {}", classToUnload.getName());
		weapon.gui.firePropertyChange(GUI.SCRIPT_LOADED, classToUnload, false);
	}
	
	/**
	 * Cancels one script's requirement of another.  When no other scripts
	 * require a script, it will be unloaded.
	 */
	synchronized public void cancelRequirement(Class<? extends Script> requiredClass, Script caller) {
		if(deps.containsKey(requiredClass)) {
			deps.get(requiredClass).remove(caller.getClass());
			if(deps.get(requiredClass).isEmpty()) {
				unloadScript(requiredClass, true);
			}
		}
	}
	
	synchronized public void cancelRequirement(String className, Script caller) {
		if(classMap.containsKey(className)) { 
			cancelRequirement(classMap.get(className), caller);
		}
	}
	
	/**
	 * Unloads all scripts. 
	 */
	synchronized public void unloadAll() {
		if(SwingUtilities.isEventDispatchThread()) {
			Set<Class<? extends Script>> loaded = new HashSet<Class<? extends Script>>(instances.keySet());
			for(Class<? extends Script> klass : loaded) {
				if(isLoaded(klass)) unloadScript(klass, true);
			}
		}
		else try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					unloadAll();
				}
			});
		} catch (Exception e) {
			log.error("error unloading scripts", e);
		}
			
		/*
		// kill timer tasks
		for(Script script : timerTasks.keySet()) {
			for(TimerTask task : timerTasks.get(script)) {
				task.cancel();
			}
		}
		timerTasks.clear();
		
		// end all scripts
		for(Script instance : instances.values()) {
			try {
				instance.endScript();
				log.info("unloaded {}", instance.getClass().getName());
				weapon.gui.firePropertyChange(GUI.SCRIPT_LOADED, instance.getClass(), false);
			} catch(Throwable t) {
				log.error("script error", t);
			}
		}
		exclusiveScript = null;
		instances.clear();
		deps.clear();
		eventListeners.clear();
		*/
	}
	
	/**
	 * Unloads all scripts and reinitializes the class loader.
	 */
	synchronized public void reset() {
		synchronized(managerMap) {
			managerMap.clear(); // FIXME: should actually find keys with value 'this' and remove them
		}
		unloadAll();
		findScriptClasses();
	}
	
	synchronized public void scheduleTask(ScriptTimerTask task, long delay) {
		if(!isLoaded(task.script)) return;
		timer.schedule(task, delay);
		if(!timerTasks.containsKey(task.script)) {
			timerTasks.put(task.script, new HashSet<TimerTask>());
		}
		timerTasks.get(task.script).add(task);
	}
	
	synchronized public void scheduleTask(ScriptTimerTask task, Date date) {
		if(!isLoaded(task.script)) return;
		timer.schedule(task, date);
		if(!timerTasks.containsKey(task.script)) {
			timerTasks.put(task.script, new HashSet<TimerTask>());
		}
		timerTasks.get(task.script).add(task);
	}
	
	synchronized public void scheduleTask(ScriptTimerTask task, long delay, long interval) {
		if(!isLoaded(task.script)) return;
		timer.schedule(task, delay, interval);
		if(!timerTasks.containsKey(task.script)) {
			timerTasks.put(task.script, new HashSet<TimerTask>());
		}
		timerTasks.get(task.script).add(task);
	}
	
	synchronized public void scheduleTask(ScriptTimerTask task, Date date, long interval) {
		if(!isLoaded(task.script)) return;
		timer.schedule(task, date, interval);
		if(!timerTasks.containsKey(task.script)) {
			timerTasks.put(task.script, new HashSet<TimerTask>());
		}
		timerTasks.get(task.script).add(task);
	}
	
	private void findScriptClasses() {
		String classpath = AppSettings.getScriptClasspath();
		log.info("searching for scripts in {}", classpath);
		String[] locations = classpath.split(System.getProperty("path.separator"));
		ClassFinder finder = new ClassFinder();
		ClassLoaderBuilder builder = new ClassLoaderBuilder();
		for(String location : locations) {
			File file = new File(location);
			finder.add(file);
			builder.add(file);
		}
		Set<ClassInfo> classInfo = new HashSet<ClassInfo>();
		finder.findClasses(classInfo);
		ClassLoader loader = builder.createClassLoader();
		synchronized(managerMap) {
			managerMap.put(loader, this);
		}
		classMap.clear();
		for(ClassInfo info : classInfo) {
			Class<?> clazz;
			try {
				clazz = loader.loadClass(info.getClassName());
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			if(Script.class.isAssignableFrom(clazz)) {
				int mods = clazz.getModifiers();
				if(Modifier.isPublic(mods) && !Modifier.isAbstract(mods)) { 
					classMap.put(clazz.getName(), clazz.asSubclass(Script.class));
				}
			}
		}
		log.info("found {} scripts", classMap.size());
	}
	
	/**
	 * Returns the script classes that were found in the script classpath.
	 */
	synchronized public Collection<Class<? extends Script>> getScripts() {
		return classMap.values();
	}
	
	
	static ScriptManager getManagerForScript(Script script) {
		synchronized(managerMap) {
			return managerMap.get(script.getClass().getClassLoader());
		}
	}

}
