package krum.weaponm.gui;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

public class FloatFormatter extends JFormattedTextField.AbstractFormatter {
	private static final long serialVersionUID = 1L;
	
	private final float min;
	private final float max;
	
	FloatFormatter(float min, float max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public Object stringToValue(String string) throws ParseException {
		try {
			float value = Float.parseFloat(string);
			if(value < min) value = min;
			if(value > max) value = max;
			return value;
		} catch(NumberFormatException e) {
			return this.getFormattedTextField().getValue();
		}
	}

	@Override
	public String valueToString(Object object) throws ParseException {
		if(object == null) return "0.0";
		return object.toString();
	}
	
}
