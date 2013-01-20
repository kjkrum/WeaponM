package krum.weaponm.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	protected final GUI gui;
	protected final TerminalPanel terminalPanel;
	protected final StatusPanel statusPanel;
	
	public MainWindow(GUI gui) throws IOException {
		this.gui = gui;
		setIconImage(gui.getIcon().getImage());
		setTitle("Weapon M");
		terminalPanel = new TerminalPanel(gui.weapon.buffer, gui.weapon.network);
		add(terminalPanel);
		
		statusPanel = new StatusPanel(gui);
		add(statusPanel, BorderLayout.SOUTH);
		//JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		//bottomPanel.add(statusPanel);
		//add(bottomPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//String[] options = { "Save and exit", "Discard changes and exit", "Do not exit" };
				String[] options = { "Save and exit", "Exit without saving", "Do not exit" };
				if(MainWindow.this.gui.weapon.dbm.isDatabaseOpen()) {
					//option = JOptionPane.showConfirmDialog(MainWindow.this, "Save database?", "Confirm exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					int option = JOptionPane.showOptionDialog(
							MainWindow.this,
							"Save database and exit?",
							"Confirm exit",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);
					if(option == JOptionPane.YES_OPTION) {
						try {
							MainWindow.this.gui.weapon.dbm.save();
						} catch (IOException ex) {
							log.error("error saving database", ex);
						}
					}
					if(option == JOptionPane.YES_OPTION || option == JOptionPane.NO_OPTION) {
						MainWindow.this.gui.weapon.shutdown();
					}
				}
				else {
					int option = JOptionPane.showConfirmDialog(
							MainWindow.this,
							"Exit Weapon M?",
							"Confirm exit",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if(option == JOptionPane.OK_OPTION) {
						MainWindow.this.gui.weapon.shutdown();
					}					
				}				
			}
		});
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		terminalPanel.display.requestFocusInWindow();
	}
	
	
}
