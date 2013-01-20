package krum.weaponm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import krum.weaponm.database.Database;
import krum.weaponm.database.LoginOptions;


public class NewDatabaseAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(NewDatabaseAction.class);
	protected final GUI gui;
	
	public NewDatabaseAction(GUI gui) {
		this.gui = gui;
		putValue(NAME, "New");
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
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
		// create a standalone LoginOptions because we don't have a db yet
		LoginOptions dialogOptions = new LoginOptions();
		if(LoginOptionsDialog.showDialog(gui.mainWindow, dialogOptions) == LoginOptionsDialog.OK_ACTION) {
			if(gui.databaseFileChooser.showSaveDialog(gui.mainWindow) == JFileChooser.APPROVE_OPTION) {
				File file = gui.databaseFileChooser.getSelectedFile();
				String filename = file.getPath();
				if(!filename.endsWith(".wmd")) {
					file = new File(filename + ".wmd");
				}
				if(gui.confirmOverwrite(file)) {
					try {
						Database database = gui.weapon.dbm.create(file);
						// copy new login options to database
						LoginOptions dbOptions = database.getLoginOptions();
						dbOptions.setHost(dialogOptions.getHost());
						dbOptions.setPort(dialogOptions.getPort());
						dbOptions.setGame(dialogOptions.getGame());
						dbOptions.setName(dialogOptions.getName());
						dbOptions.setPassword(dialogOptions.getPassword());
						dbOptions.setAutoLogin(dialogOptions.isAutoLogin());
						gui.weapon.dbm.save();
						gui.weapon.autoLoadScripts();
					} catch (Throwable t) {
						log.error("error creating database", t);
						gui.threadSafeMessageDialog(t.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
}
