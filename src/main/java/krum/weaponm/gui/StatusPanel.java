package krum.weaponm.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import krum.jtx.VGAColors;
import krum.weaponm.database.DataParser;
import krum.weaponm.database.Database;
import krum.weaponm.database.Sector;
import krum.weaponm.database.Ship;
import krum.weaponm.database.ShipType;
import krum.weaponm.database.You;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(DataParser.class);
	protected static final NumberFormat thousands = NumberFormat.getIntegerInstance();
	
	static {
		thousands.setGroupingUsed(true);
	}
	
	private final Field turns;
	private final Field credits;
	private final Field xp;
	private final Field align;
	
	private final Field sector;
	private final Field fighters;
	private final Field shields;
	private final Field holds;
	
	private final Field[] cargo;
	
	private int limpetRemovalCost = 0;
	private Database database;
	
	public StatusPanel(GUI gui) {
		turns = new Field("Turns", 5) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				if((Integer) value < 50) field.setForeground(VGAColors.foreground(VGAColors.RED, true));
				else field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
			}
		};
		gui.addPropertyChangeListener(GUI.YOU_TURNS, turns);
		
		credits = new Field("Credits", 8) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				if((Integer) value < limpetRemovalCost) field.setForeground(VGAColors.foreground(VGAColors.RED, true));
				else field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
			}
		};
		gui.addPropertyChangeListener(GUI.YOU_CREDITS, credits);
		
		xp = new Field("Exp", 8) {
			private static final long serialVersionUID = 1L;
			@Override
			void setValue(int value) {
				super.setValue(value);
				sector.computeColor();
			}
		};
		gui.addPropertyChangeListener(GUI.YOU_XP, xp);
		
		align = new Field("Align", 8) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				if((Integer) value < 0) field.setForeground(VGAColors.foreground(VGAColors.RED, true));
				else if((Integer) value < 1000) field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
				else field.setForeground(VGAColors.foreground(VGAColors.BLUE, true));
			}
			@Override
			void setValue(int value) {
				super.setValue(value);
				sector.computeColor();
			}
		};
		gui.addPropertyChangeListener(GUI.YOU_ALIGN, align);
		
		sector = new Field("Sector", 4) {
			private static final long serialVersionUID = 1L;
			// don't want sectors to be formatted with thousands separators
			@Override
			void setValue(int value) {
				this.value = value;  
				field.setText(Integer.toString(value));
				computeColor();
			}
			@Override
			void computeColor() {
				if(value < 1) return;
				Sector sector = database.getSector(value);
				if(sector.isFedSpace()) {
					if(database.getYou().isFedsafe()) field.setForeground(VGAColors.foreground(VGAColors.CYAN, true));
					else field.setForeground(VGAColors.foreground(VGAColors.RED, true));
				}
				else field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
			}
		};
		gui.addPropertyChangeListener(GUI.SHIP_SECTOR, sector);

		fighters = new Field("Figs", 5) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				ShipType shipType = database.getYou().getShip().getType();
				if(shipType == null || value >= shipType.getFighters() * 0.75) field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
				else if(value >= shipType.getFighters() * 0.5) field.setForeground(VGAColors.foreground(VGAColors.YELLOW, true));
				else field.setForeground(VGAColors.foreground(VGAColors.RED, true));
			}
		};
		gui.addPropertyChangeListener(GUI.SHIP_FIGHTERS, fighters);
		
		shields = new Field("Shields", 4) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				ShipType shipType = database.getYou().getShip().getType();
				if(shipType == null || value >= shipType.getShields() * 0.75) field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
				else if(value >= shipType.getShields() * 0.5) field.setForeground(VGAColors.foreground(VGAColors.YELLOW, true));
				else field.setForeground(VGAColors.foreground(VGAColors.RED, true));
			}
		};
		gui.addPropertyChangeListener(GUI.SHIP_SHIELDS, shields);

		holds = new Field("Holds", 4) {
			private static final long serialVersionUID = 1L;
			@Override
			void computeColor() {
				ShipType shipType = database.getYou().getShip().getType();
				if(shipType == null || value >= shipType.getMaxHolds()) field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
				else field.setForeground(VGAColors.foreground(VGAColors.YELLOW, true));
			}
		};
		gui.addPropertyChangeListener(GUI.SHIP_HOLDS, holds);
		
		cargo = new Field[4];
		cargo[0] = new Field("Ore", 3);
		cargo[1] = new Field("Org", 3);
		cargo[2] = new Field("Equ", 3);
		cargo[3] = new Field("Colos", 3);
		gui.addPropertyChangeListener(GUI.SHIP_CARGO, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				cargo[(Integer) e.getOldValue()].setValue((Integer)e.getNewValue());				
			}
		});
		
		gui.addPropertyChangeListener(GUI.DATABASE_INITIALIZED, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if((Boolean) e.getNewValue()) {
					database = (Database) e.getOldValue();
					limpetRemovalCost = database.getGameStats().limpetRemovalCost();
					You you = database.getYou();
					Ship ship = you.getShip();
					turns.setValue(you.getTurns());
					credits.setValue(you.getCredits());
					sector.setValue(you.getSectorNumber()); // before xp so it doesn't AIOOB on init
					xp.setValue(you.getXp());
					align.setValue(you.getAlign());
					fighters.setValue(ship.getFighters());
					shields.setValue(ship.getShields());
					holds.setValue(ship.getHolds());
					for(int i = 0; i < cargo.length; ++i) {
						cargo[i].setValue(ship.getCargo(i));
					}
				}
				else {
					database = null;
					turns.field.setText(null);
					credits.field.setText(null);
					xp.field.setText(null);
					align.field.setText(null);
					sector.field.setText(null);
					fighters.field.setText(null);
					shields.field.setText(null);
					holds.field.setText(null);
					for(int i = 0; i < cargo.length; ++i) {
						cargo[i].field.setText(null);
					}
				}
			}
		});
		
		// add fields to panel
		JPanel column = new JPanel();
		column.setBorder(BorderFactory.createTitledBorder("Trader"));
		column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
		column.add(xp);
		column.add(align);
		column.add(credits);
		column.add(turns);
		add(column);
		
		column = new JPanel();
		column.setBorder(BorderFactory.createTitledBorder("Ship"));
		column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
		column.add(sector);
		column.add(fighters);
		column.add(shields);
		column.add(holds);
		add(column);
		
		column = new JPanel();
		column.setBorder(BorderFactory.createTitledBorder("Cargo"));
		column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
		column.add(cargo[0]);
		column.add(cargo[1]);
		column.add(cargo[2]);
		column.add(cargo[3]);
		add(column);
		
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setMaximumSize(getPreferredSize());
	}

	static class Field extends JPanel implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		
		final JTextField field;
		int value;
		
		Field(String label, int fieldWidth) {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder());
			JLabel lbl = new JLabel(label);
			lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			lbl.setHorizontalAlignment(SwingConstants.LEFT);
			field = new JTextField(fieldWidth);
			field.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			field.setBackground(Color.BLACK);
			field.setForeground(VGAColors.foreground(VGAColors.GREEN, true));
			field.setEditable(false);
			field.setFocusable(false);
			field.setHorizontalAlignment(SwingConstants.RIGHT);
			add(lbl, BorderLayout.WEST);
			add(Box.createRigidArea(new Dimension(4, 0)));
			add(field, BorderLayout.EAST);
		}
		
		void setValue(int value) {
			this.value = value;  
			field.setText(thousands.format(value));
			computeColor();
		}
		
		void computeColor() {
			// default impl does nothing
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			setValue((Integer) e.getNewValue());
		}
	}
}
