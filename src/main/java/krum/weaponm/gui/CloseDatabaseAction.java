package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseDatabaseAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(CloseDatabaseAction.class);
	protected final GUI gui;
	
	public CloseDatabaseAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "Close");
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(gui.weapon.dbm.isDatabaseOpen()) {
			String[] options = { "Save and close", "Close without saving", "Do not close" };
			int option = JOptionPane.showOptionDialog(
					gui.mainWindow,
					"Close current database?",
					"Confirm close",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);
			if(option == JOptionPane.YES_OPTION) {
				try {
					gui.weapon.dbm.save();
					gui.weapon.dbm.close();
				} catch (IOException ex) {
					log.error("error saving database", ex);
					gui.threadSafeMessageDialog(ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else if(option == JOptionPane.NO_OPTION) {
				gui.weapon.dbm.close();
			}
			else return;
		}
	}
}
