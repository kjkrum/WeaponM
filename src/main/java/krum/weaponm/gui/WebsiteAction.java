package krum.weaponm.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsiteAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(WebsiteAction.class);
	
	public WebsiteAction() {
		putValue(NAME, "Website");
		putValue(MNEMONIC_KEY, KeyEvent.VK_W);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Desktop.getDesktop().browse(new URL("https://sourceforge.net/projects/weapon-m/").toURI());
		} catch (Exception ex) {
			log.error("error opening system browser", ex);
		}
	}

}
