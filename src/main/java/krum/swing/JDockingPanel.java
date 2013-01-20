package krum.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class JDockingPanel extends JLayeredPane {
	private static final long serialVersionUID = 1L;
	
	protected final JButton button;
	protected final JPanel buttonPanel;
	protected final JFrame frame;
	protected Container contentPane;
	
	// this is the preferred size of the entire JDockingPanel when
	// the content pane is docked.  since there is no layout manager,
	// the size of the content pane should be set to this size.
	protected final Dimension dockedSize;
	// this is the preferred size of the content pane when it is in the frame.
	// the frame's layout manager should set its size accordingly.
	protected final Dimension floatingSize;
	
	public JDockingPanel(JButton button, String title, Image icon, Container contentPane) {
		this.button = button;
		button.setSize(button.getPreferredSize());
		button.setVisible(false);
		add(button, Integer.valueOf(2));
		
		// hides the button when the mouse moves out of it
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				JDockingPanel.this.button.setVisible(false);
			}
		});
		
		// floats the content pane when the button is clicked
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeContentPaneFromPanel();
				JDockingPanel.this.button.setVisible(false);
				addContentPaneToFrame();
				frame.setVisible(true);
			}
		});
		
		buttonPanel = new JPanel();
		buttonPanel.setSize(button.getSize());
		buttonPanel.setOpaque(false);
		add(buttonPanel, Integer.valueOf(1));
		
		// shows the button when the mouse enters its bounds
		buttonPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if(getComponentCountInLayer(0) > 0) {
					JDockingPanel.this.button.setVisible(true);
				}
			}
		});
		
		frame = new JFrame(title);
		frame.setIconImage(icon);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// adds the content pane to the dock when the frame is closed
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false); // FIXME: dispose???
				removeContentPaneFromFrame();
				addContentPaneToPanel();
			}			
		});
		
		/* clever, but overkill
		// shows the button when the mouse enters its bounds
		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent e) {
				// if the content pane is docked...
				if(JDockingPanel.this.getComponentCountInLayer(0) > 0) {
					// and the event is inside the content pane...
					MouseEvent mouseEvent = SwingUtilities.convertMouseEvent((Component)e.getSource(), (MouseEvent)e, JDockingPanel.this.contentPane);
					if(JDockingPanel.this.button.getBounds().contains(mouseEvent.getPoint())) {
						JDockingPanel.this.button.setVisible(true);
					}
					else JDockingPanel.this.button.setVisible(false);
				}
			}
		}, eventMask);
		*/
		
		// resizes the content pane when this JDockingPanel is resized by its parent
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// if content pane is docked...
				if(JDockingPanel.this.getComponentCountInLayer(0) > 0) {
					JDockingPanel.this.contentPane.setSize(getSize());
					revalidate();
					setButtonLocation();
				}
			}
		});
		
		dockedSize = new Dimension(contentPane.getPreferredSize());
		floatingSize = new Dimension(contentPane.getPreferredSize());
		
		setContentPane(contentPane);
	}

	public Container getContentPane() {
		return contentPane;
	}
	
	public void setContentPane(Container contentPane) {
		if(frame.isVisible()) {
			removeContentPaneFromFrame();
			this.contentPane = contentPane;
			addContentPaneToFrame();
		}
		else {		
			if(this.contentPane != null) removeContentPaneFromPanel();
			this.contentPane = contentPane;
			addContentPaneToPanel();
		}
	}
	
	protected void setButtonLocation() {
		if(contentPane instanceof DockingAwareContainer) {
			Point location = ((DockingAwareContainer) contentPane).getPreferredButtonPosition(button.getSize());
			if(location != null) {
				// TODO: could check that it's inside the content pane...
				button.setLocation(location);
				buttonPanel.setLocation(button.getLocation());
				return;
			}
		}
		// fall back to default location
		int width = contentPane.getSize().width;
		int buttonWidth = button.getPreferredSize().width;
		button.setLocation(width - buttonWidth - 10, 10);
		buttonPanel.setLocation(button.getLocation());
	}
	
	protected void removeContentPaneFromPanel() {
		dockedSize.setSize(getSize());
		remove(contentPane);
		button.setVisible(false);
		setPreferredSize(new Dimension(0, 0));
	}
	
	protected void addContentPaneToPanel() {
		if(contentPane instanceof DockingAwareContainer) {
			((DockingAwareContainer) contentPane).docking();
		}
		setPreferredSize(dockedSize);
		//contentPane.setSize(dockedSize);
		setButtonLocation(); // TODO: will size be right for this?
		add(contentPane, Integer.valueOf(0));
	}

	protected void removeContentPaneFromFrame() {
		floatingSize.setSize(contentPane.getSize());
		frame.remove(contentPane);
	}
	
	protected void addContentPaneToFrame() {
		if(contentPane instanceof DockingAwareContainer) {
			((DockingAwareContainer) contentPane).floating();
		}
		contentPane.setPreferredSize(floatingSize);
		frame.getContentPane().add(contentPane);
		frame.pack();
	}
	
}