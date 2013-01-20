package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class DisconnectAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	protected final GUI gui;
	
	public DisconnectAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Disconnect");
		putValue(MNEMONIC_KEY, KeyEvent.VK_D);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.weapon.network.disconnect();	
	}

}
