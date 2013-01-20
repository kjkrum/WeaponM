package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveDatabaseAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(OpenDatabaseAction.class);
	protected final GUI gui;
	
	public SaveDatabaseAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Save");
		putValue(MNEMONIC_KEY, KeyEvent.VK_S);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(gui.weapon.dbm.isDatabaseOpen()) {
			try {
				gui.weapon.dbm.save();
			} catch (IOException ex) {
				log.error("error saving database", ex);
				gui.threadSafeMessageDialog(ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
			}
		}		
	}

}
