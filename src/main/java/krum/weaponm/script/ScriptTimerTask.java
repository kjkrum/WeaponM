package krum.weaponm.script;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A version of <tt>TimerTask</tt> that protects the timer thread from
 * uncaught throwables.  If something is thrown, it is logged and the timer
 * task is canceled.
 */
abstract public class ScriptTimerTask extends TimerTask {
	protected static final Logger log = LoggerFactory.getLogger(ScriptTimerTask.class);
	protected final Script script;
	
	public ScriptTimerTask(Script script) {
		this.script = script;
	}
	
	/**
	 * Does whatever this timer task does.  You must implement this method.
	 */
	abstract public void doTask();

	/**
	 * Calls <tt>doTask()</tt>.
	 */
	@Override
	public final void run() {
		try {
			doTask();
		} catch(Throwable t) {
			log.error("timer task error", t);
			cancel();
		}
	}
}
