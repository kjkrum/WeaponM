package krum.weaponm.gui;

import java.awt.BorderLayout;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import krum.weaponm.script.Parameter;

class ParameterPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int COLUMNS = 6;
	private final Parameter parameter;
	private  JComponent inputComponent;

	ParameterPanel(Parameter parameter) {
		super(new BorderLayout());
		this.parameter = parameter;
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		add(new JLabel(parameter.getLabel()), BorderLayout.WEST);
		switch(parameter.getType()) {
		case INTEGER:
			inputComponent = new JFormattedTextField(new IntegerFormatter(parameter.getMinValue().intValue(), parameter.getMaxValue().intValue()));
			((JFormattedTextField) inputComponent).setColumns(COLUMNS);
			((JFormattedTextField) inputComponent).setValue(parameter.getInteger());
			((JTextField) inputComponent).addFocusListener(SelectOnFocus.getInstance());
			break;
		case FLOAT:
			inputComponent = new JFormattedTextField(new FloatFormatter(parameter.getMinValue().floatValue(), parameter.getMaxValue().floatValue()));
			((JFormattedTextField) inputComponent).setColumns(COLUMNS);
			((JFormattedTextField) inputComponent).setValue(parameter.getFloat());
			((JTextField) inputComponent).addFocusListener(SelectOnFocus.getInstance());
			break;
		case BOOLEAN:
			inputComponent = new JCheckBox();
			((JCheckBox) inputComponent).setSelected(parameter.getBoolean());
			break;
		case STRING:
			inputComponent = new JTextField(parameter.getString(), COLUMNS);
			((JTextField) inputComponent).addFocusListener(SelectOnFocus.getInstance());
			break;
		case OPTION:
			inputComponent = new JComboBox(parameter.getOptions().toArray());
			((JComboBox) inputComponent).setSelectedItem(parameter.getOption());
			((JComboBox) inputComponent).setEditable(false);
			break;
		default:
			break;
		}
		add(Box.createHorizontalStrut(20));
		add(inputComponent, BorderLayout.EAST);
	}
	
	void copyInputToParameter() {
		switch(parameter.getType()) {
		case INTEGER:
			try {
				((JFormattedTextField) inputComponent).commitEdit();
			} catch (ParseException e) {
				// can't happen with IntegerParameterFormatter
			}
			parameter.setInteger(((Number)((JFormattedTextField) inputComponent).getValue()).intValue());
			break;
		case FLOAT:
			try {
				((JFormattedTextField) inputComponent).commitEdit();
			} catch (ParseException e) {
				// can't happen with FloatParameterFormatter
			}
			parameter.setFloat(((Number)((JFormattedTextField) inputComponent).getValue()).floatValue());		
			break;
		case BOOLEAN:
			parameter.setBoolean(((JCheckBox) inputComponent).isSelected());
			break;
		case STRING:
			parameter.setString(((JTextField) inputComponent).getText());
			break;
		case OPTION:
			parameter.setOption(((JComboBox) inputComponent).getSelectedItem());
			break;
		default:
			break;
		}
	}
	
	/*
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPanel panel = new JPanel();
				panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
				
				Parameter param = new Parameter("Integer", 42);
				param.setMaxValue(99);
				ParameterPanel paramPanel = new ParameterPanel(param);
				panel.add(paramPanel);
				
				param = new Parameter("Float", 3.14f);
				paramPanel = new ParameterPanel(param);
				panel.add(paramPanel);		
				
				param = new Parameter("String", "blue");
				paramPanel = new ParameterPanel(param);
				panel.add(paramPanel);
				
				param = new Parameter("Boolean", true);
				paramPanel = new ParameterPanel(param);
				panel.add(paramPanel);
				
				param = new Parameter("Option", new Object[] { "Option A", "Option B", "Option C"}, 0);
				paramPanel = new ParameterPanel(param);
				panel.add(paramPanel);	
				
				javax.swing.JFrame frame = new javax.swing.JFrame("ParameterPanel Test");
				frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
				frame.add(panel);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	*/
}
