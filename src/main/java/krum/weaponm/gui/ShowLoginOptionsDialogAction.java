package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ShowLoginOptionsDialogAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	protected final GUI gui;
	
	public ShowLoginOptionsDialogAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Login Options");
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		LoginOptionsDialog.showDialog(gui.mainWindow, gui.weapon.dbm.getDatabase().getLoginOptions());
	}
}
