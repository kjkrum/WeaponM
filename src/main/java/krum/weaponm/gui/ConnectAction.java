package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(ConnectAction.class);
	protected final GUI gui;
	
	public ConnectAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Connect");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			gui.weapon.network.connect();
		} catch (IOException ex) {
			log.error("network error", ex);
		}
	}
}
