package krum.weaponm.script;


/**
 * A task that calls a script's initialization sequence.  This will be invoked
 * in the Swing event dispatch thread.
 */
class ScriptInitializer implements Runnable {
	
	private final ScriptManager manager;
	private final Script instance;
	private volatile Throwable error;
	
	ScriptInitializer(ScriptManager manager, Script instance) {
		this.manager = manager;
		this.instance = instance;
	}
	
	boolean hasError() {
		return error != null;
	}
	
	Throwable getError() {
		return error;
	}

	@Override
	public void run() {
		// manager will unload if error is set
		if(!manager.isLoaded(instance)) {
			error = new ScriptException("Script was unloaded before initialization.");
			return;
		}
		try {
			instance.initScript();
			if(!manager.isLoaded(instance)) {
				error = new ScriptException("Script was unloaded during initScript().");
				return;
			}
			instance.displayParametersDialog();
			if(!manager.isLoaded(instance)) {
				error = new ScriptException("Script was unloaded during displayParametersDialog().");
				return;
			}
			instance.startScript();
			if(!manager.isLoaded(instance)) {
				error = new ScriptException("Script was unloaded during startScript().");
				return;
			}
			instance.setInitialized();
			manager.activateEvents(instance);
		}
		catch(Throwable t) {
			error = t;
		}
	}
}
