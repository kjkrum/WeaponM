package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ShowCreditsWindowAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	protected final GUI gui;
	
	public ShowCreditsWindowAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Credits");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		gui.creditsWindow.setVisible(true);
		gui.creditsWindow.toFront();
	}
}
