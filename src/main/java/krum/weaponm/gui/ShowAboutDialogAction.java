package krum.weaponm.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ShowAboutDialogAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	protected final Frame frame;
	
	public ShowAboutDialogAction(Frame frame) {
		this.frame = frame;
		putValue(NAME, "About");
		putValue(MNEMONIC_KEY, KeyEvent.VK_A);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		AboutDialog.showDialog(frame);
	}
}
