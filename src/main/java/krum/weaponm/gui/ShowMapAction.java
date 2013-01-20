package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ShowMapAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private final GUI gui;
	
	public ShowMapAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Map");
		putValue(MNEMONIC_KEY, KeyEvent.VK_M);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		gui.mapWindow.setVisible(true);
		gui.mapWindow.toFront();
	}
}
