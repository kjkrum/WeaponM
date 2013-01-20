package krum.weaponm.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import krum.weaponm.script.Script;
import krum.weaponm.script.ScriptManager;

public class ScriptAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(ScriptAction.class);	
	protected final Class<? extends Script> scriptClass;
	protected final ScriptManager manager;
	protected final GUI gui;

	public ScriptAction(Class<? extends Script> scriptClass, String scriptName, GUI gui) {
		this.scriptClass = scriptClass;
		putValue(NAME, scriptName);
		this.gui = gui;
		this.manager = gui.weapon.scripts;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(manager.isLoaded(scriptClass)) {
			manager.unloadScript(scriptClass, true);
		}
		else {
			try {
				manager.loadScript(scriptClass, null);
			} catch (Throwable t) {
				log.error("script error", t);
				String message = t.getMessage();
				if(message == null) message = t.getClass().getName(); 
				gui.threadSafeMessageDialog(message, "Script Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
