package krum.weaponm.gui.map;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import krum.weaponm.database.Database;
import krum.weaponm.gui.GUI;
import krum.weaponm.gui.SelectOnFocus;
import uk.co.timwise.wraplayout.WrapLayout;

public class ControlPanel extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	private static final int UNINITIALIZED = 0;
	//private static final Logger log = LoggerFactory.getLogger(ControlPanel.class);
	
	private final Map map;
	private int sdSector = UNINITIALIZED;
	private int shipSector = UNINITIALIZED;
	
	private final JRadioButton currentButton;
	private final JRadioButton arbitraryButton;
	private final JFormattedTextField arbitraryField;
	private final NumberFormatter arbitraryFormatter;
	private final JButton sdButton;
	private final JSpinner radiusSpinner;
	private final JButton redrawButton;

	public ControlPanel(Map map) {
		super(new WrapLayout());
		this.map = map;
		
		// used to group components that should be wrapped together
		FlowLayout group = new FlowLayout();
		group.setHgap(0);
		group.setVgap(0);
		
		JLabel rootLabel = new JLabel("Root:");
		add(rootLabel);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		
		currentButton = new JRadioButton("Current");
		currentButton.setEnabled(false);
		currentButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// shouldn't be enabled until shipSector is initialized
				if(e.getStateChange() == ItemEvent.SELECTED) {
					ControlPanel.this.map.setRoot(shipSector, true);
				}
			}
		});
		buttonGroup.add(currentButton);
		add(currentButton);		
		
		JPanel arbitraryPanel = new JPanel(group);
		arbitraryButton = new JRadioButton();
		arbitraryButton.setEnabled(false);
		arbitraryButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// shouldn't be enabled until database is initialized and map is populated
				if(e.getStateChange() == ItemEvent.SELECTED) {
					ControlPanel.this.map.setRoot((Integer) arbitraryField.getValue(), true);
				}
			}
		});
		buttonGroup.add(arbitraryButton);
		arbitraryPanel.add(arbitraryButton);
		
		arbitraryFormatter = new NumberFormatter(new DecimalFormat("#####"));
		arbitraryFormatter.setMinimum(1);
		arbitraryFormatter.setMaximum(1);
		arbitraryFormatter.setValueClass(Integer.class);
		arbitraryField = new JFormattedTextField(arbitraryFormatter);
		arbitraryField.setColumns(4);
		arbitraryField.setValue(1);
		arbitraryField.setEnabled(false);
		arbitraryField.addFocusListener(SelectOnFocus.getInstance());
		arbitraryField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				arbitraryButton.setSelected(true);
			}
			@Override
			public void focusLost(FocusEvent e) {
				try {
					arbitraryField.commitEdit();
					ControlPanel.this.map.setRoot((Integer) arbitraryField.getValue(), true);
				} catch (ParseException ex) {
					// ignore
				}
			}
		});
		arbitraryField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "commit");
		arbitraryField.getActionMap().put("commit", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					arbitraryField.commitEdit();
					ControlPanel.this.map.setRoot((Integer) arbitraryField.getValue(), true);
					arbitraryButton.requestFocus();
				} catch (ParseException ex) {
					// ignore
				}				
			}
		});
		arbitraryPanel.add(arbitraryField);
		
		sdButton = new JButton("SD");
		sdButton.setEnabled(false);
		sdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				arbitraryField.setValue(sdSector);
				if(arbitraryButton.isSelected()) {
					ControlPanel.this.map.setRoot(sdSector, true);
				}
				else {
					arbitraryButton.setSelected(true);
				}
				
			}
		});
		arbitraryPanel.add(sdButton);
		add(arbitraryPanel);
		
		JPanel radiusPanel = new JPanel(group);
		radiusPanel.add(new JLabel("Radius:"));
		radiusSpinner = new JSpinner(new SpinnerNumberModel(Map.DEFAULT_RADIUS, 1, 99, 1));
		radiusSpinner.setEnabled(false);
		radiusSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ControlPanel.this.map.setRadius((Integer) radiusSpinner.getValue(), true);				
			}
		});
		radiusPanel.add(radiusSpinner);
		add(radiusPanel);
		
		redrawButton = new JButton("Redraw");
		redrawButton.setEnabled(false);
		redrawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ControlPanel.this.map.redraw();					
			}
		});
		add(redrawButton);
		
		/*
		arbitraryButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					arbitraryField.requestFocus();
				}
			}
		});


		arbitraryField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				arbitraryButton.setSelected(true);
				arbitraryField.requestFocus();
			}
		});
		*/
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String property = e.getPropertyName();
		if(GUI.SHIP_SECTOR.equals(property)) {
			int oldSector = e.getOldValue() == null ? UNINITIALIZED : (Integer) e.getOldValue();
			shipSector = e.getNewValue() == null ? UNINITIALIZED : (Integer) e.getNewValue();
			
			// if oldSector == 0, your location is being initialized;
			// enable and select current button, redraw
			// if shipSector == 0, you are #SD#; disable current button,
			// select arb. button or update oldSector
			// else action depends on which button is selected;
			//   if current, redraw
			//   if arb., update oldSector & shipSector
			
			if(oldSector == UNINITIALIZED) {
				currentButton.setEnabled(true);
				currentButton.setSelected(true);
				// item listener triggers redraw
			}
			else if(shipSector == UNINITIALIZED) {
				currentButton.setEnabled(false);
				// if arb button is already selected,
				// selecting it won't trigger redraw
				if(arbitraryButton.isSelected()) {
					map.updateSector(oldSector);
				}
				else {
					arbitraryField.setValue(oldSector);
					arbitraryButton.setSelected(true);
					// item listener triggers redraw
				}
			}
			else if(currentButton.isSelected()) {
				map.setRoot(shipSector, true);
			}
			else {
				map.updateSector(oldSector);
				map.updateSector(shipSector);
			}
		}
		else if(GUI.MAP_ROOT.equals(property)) {
			int value = (Integer) e.getNewValue();
			arbitraryField.setValue(value);
			if(arbitraryButton.isSelected()) {
				map.setRoot(value, true);
			}
			else arbitraryButton.setSelected(true); // triggers redraw
		}
		else if(GUI.DATABASE_INITIALIZED.equals(property)) {
			if((Boolean)e.getNewValue()) {
				Database db = (Database) e.getOldValue();
				arbitraryFormatter.setMaximum(db.getNumSectors());
				map.populate(db.getSectors());
				arbitraryField.setValue(1);
				arbitraryField.setEnabled(true);
				arbitraryButton.setEnabled(true);
				arbitraryButton.setSelected(true); // triggers redraw
				radiusSpinner.setEnabled(true);
				redrawButton.setEnabled(true);
				// SHIP_SECTOR may select current button
			}
			else {
				currentButton.setEnabled(false);
				currentButton.setSelected(false);
				arbitraryButton.setEnabled(false);
				arbitraryButton.setSelected(false);
				arbitraryField.setValue(null);
				arbitraryField.setEnabled(false);
				sdButton.setEnabled(false);
				radiusSpinner.setEnabled(false);
				redrawButton.setEnabled(false);
				map.clear();
				shipSector = UNINITIALIZED;
				sdSector = UNINITIALIZED;
			}
		}
		else if(GUI.STARDOCK_DISCOVERED.equals(property)) {
			sdSector = (Integer) e.getNewValue();
			sdButton.setEnabled(true);
		}
	}	
}