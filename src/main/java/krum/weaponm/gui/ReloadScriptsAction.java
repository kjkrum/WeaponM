package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ReloadScriptsAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final ActionManager manager;

	public ReloadScriptsAction(ActionManager manager) {
		this.manager = manager;
		putValue(AbstractAction.NAME, "Reload Scripts");
		putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_R);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		manager.reloadScripts();
	}
	
}
