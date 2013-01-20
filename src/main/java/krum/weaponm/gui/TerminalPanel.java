package krum.weaponm.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import krum.jtx.Buffer;
import krum.jtx.Display;
import krum.jtx.SoftFont;
import krum.jtx.VGASoftFont;
import krum.swing.StickyScrollPane;
import krum.weaponm.AppSettings;
import krum.weaponm.network.NetworkManager;

public class TerminalPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected final Display display;

	/**
	 * 
	 * @throws IOException if there was an error loading the font resource
	 * @throws ClassNotFoundException 
	 */
	protected TerminalPanel(Buffer buffer, final NetworkManager networkManager) throws IOException {
		super(new BorderLayout());
		SoftFont font;
		if(AppSettings.getGiantFont()) font = new VGASoftFont("/resources/weaponm/custom18x32.png");
		else font = new VGASoftFont();
		display = new Display(buffer, font, 80, 25);
		display.addKeyListener(new KeyListener() {
			ByteBuffer buffer = ByteBuffer.allocate(4);
			
			@Override
			synchronized public void keyTyped(KeyEvent e) {
				if(networkManager.isConnected()) {
					char c = e.getKeyChar();
					if(c == KeyEvent.VK_ENTER) {
						buffer.put((byte) 13);
						buffer.put((byte) 10);
					}
					else if(c == KeyEvent.VK_TAB) {
						buffer.put((byte) 9);
					}
					else {
						buffer.put((byte) c);
					}
					buffer.flip();
					try {
						networkManager.write(buffer);
					}
					catch(IOException ex) {
						// not this class's problem
					}
					finally {
						buffer.clear();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// don't care		
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// don't care
			}
		});
		
		JScrollPane scrollPane = new StickyScrollPane(display);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().setBackground(Color.BLACK);
		scrollPane.getViewport().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				display.requestFocusInWindow();
			}			
		});
		add(scrollPane, BorderLayout.CENTER);

	}
}
