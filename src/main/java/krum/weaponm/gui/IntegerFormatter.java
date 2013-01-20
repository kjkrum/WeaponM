package krum.weaponm.gui;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

class IntegerFormatter extends JFormattedTextField.AbstractFormatter {
	private static final long serialVersionUID = 1L;
	
	private final int min;
	private final int max;
	
	IntegerFormatter(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public Object stringToValue(String string) throws ParseException {
		try {
			int value = Integer.parseInt(string);
			if(value < min) value = min;
			if(value > max) value = max;
			return value;
		} catch(NumberFormatException e) {
			return this.getFormattedTextField().getValue();
		}

	}

	@Override
	public String valueToString(Object object) throws ParseException {
		if(object == null) return "0";
		return object.toString();
	}
	
}
