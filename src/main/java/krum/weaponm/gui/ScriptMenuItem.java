package krum.weaponm.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;

import krum.weaponm.script.Script;

/**
 * Menu item for script activation.  This is a <tt>JCheckBoxMenuItem</tt> with
 * its button model replaced by a <tt>DefaultButtonModel</tt>.  This is so it
 * doesn't automatically become selected when clicked.  It sets its selected
 * state based on property change events from the script manager.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
public class ScriptMenuItem extends JCheckBoxMenuItem implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	final Class<? extends Script> scriptClass;

	ScriptMenuItem(ScriptAction action) {
		super(action);
		setModel(new DefaultButtonModel());
		scriptClass = action.scriptClass;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if(e.getPropertyName().equals(GUI.DATABASE_LOADED)) {
			setEnabled((Boolean)e.getNewValue());
		}
		else if(e.getPropertyName().equals(GUI.SCRIPT_LOADED)) {
			if(e.getOldValue().equals(scriptClass)) setSelected((Boolean)e.getNewValue());
		}
	}	
}
