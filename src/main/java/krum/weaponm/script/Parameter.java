package krum.weaponm.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A load-time parameter for a script.  Parameters are normally registered in
 * {@link Script#initScript()} and their values read in
 * {@link Script#startScript()}.
 * <p>
 * This class is not thread safe.  This is not normally a concern, since all
 * script initialization is performed in the Swing event thread.  But if a
 * <tt>Parameter</tt> is accessed in another thread, such as in
 * {@link Script#handleEvent(ScriptEvent, Object...)}, then all access should
 * be externally synchronized.  A better approach would be to copy the value
 * of the <tt>Parameter</tt> into a <tt>volatile</tt> field in
 * {@link Script#startScript()}.
 */
public class Parameter {
	/** The data type of this parameter. */
	private final Type type;
	/** A short description of what this parameter represents. */
	private final String label;
	private final List<Object> options;
	private Object value;
	private Number minValue;
	private Number maxValue;	

	/**
	 * Creates a new integer parameter.
	 * 
	 * @param label
	 * @param value
	 */
	public Parameter(String label, int value) {
		type = Type.INTEGER;
		this.label = label;
		options = null;
		this.value = value;
		minValue = Integer.MIN_VALUE;
		maxValue = Integer.MAX_VALUE;
	}
	
	/**
	 * Creates a new float parameter.
	 * 
	 * @param label
	 * @param value
	 */
	public Parameter(String label, float value) {
		type = Type.FLOAT;
		this.label = label;
		options = null;
		this.value = value;
		minValue = Float.MIN_VALUE;
		maxValue = Float.MAX_VALUE;
	}
	
	/**
	 * Creates a new boolean parameter.
	 * 
	 * @param label
	 * @param value
	 */
	public Parameter(String label, boolean value) {
		type = Type.BOOLEAN;
		this.label = label;
		options = null;
		this.value = value;		
	}
	
	/**
	 * Creates a new String parameter.
	 * 
	 * @param label
	 * @param value
	 */
	public Parameter(String label, String value) {
		type = Type.STRING;
		this.label = label;
		options = null;
		this.value = value;		
	}
	
	/**
	 * Creates a new option parameter.
	 * 
	 * @param label
	 * @param options
	 * @param selected
	 */
	public Parameter(String label, Object[] options, int selected) {
		type = Type.OPTION;
		this.label = label;
		this.options = Arrays.asList(options);
		value = options[selected];
	}
	
	/**
	 * Creates a new option parameter.
	 * 
	 * @param label
	 * @param options
	 */
	public Parameter(String label, Object[] options) {
		this(label, options, 0);
	}
	
	/**
	 * Creates a new option parameter.
	 * 
	 * @param label
	 * @param options
	 * @param selected
	 */
	public Parameter(String label, List<Object> options, Object selected) {
		if(!options.contains(selected)) throw new IllegalArgumentException("selected not in options");
		type = Type.OPTION;
		this.label = label;
		this.options = options;
		value = selected;
	}
	
	/**
	 * Creates a new option parameter.
	 * 
	 * @param label
	 * @param options
	 */
	public Parameter(String label, List<Object> options) {
		this(label, options, options.get(0));
	}
	
	/**
	 * Sets the minimum integer value of this parameter.  If the current value
	 * is less than <tt>minValue</tt>, it will be set to <tt>minValue</tt>.
	 * Additionally, if <tt>maxValue</tt> is less than </tt>minValue</tt>,
	 * both <tt>maxValue</tt> and the current value will be set to
	 * </tt>minValue</tt>. 
	 * 
	 * @param minValue
	 * @throws UnsupportedOperationException if this is not an integer parameter
	 * @throws IllegalArgumentException if <tt>maxValue < minValue</tt>
	 */
	public void setMinValue(int minValue) {
		if(type != Type.INTEGER) throw new UnsupportedOperationException("parameter type " + type.name());
		if(maxValue.intValue() < minValue) {
			maxValue = minValue;
			value = minValue;
		}
		this.minValue = minValue;
		if(((Integer)value).intValue() < minValue) value = minValue;
	}
	
	/**
	 * Sets the maximum integer value of this parameter.  If the current value
	 * is greater than <tt>maxValue</tt>, it will be set to <tt>maxValue</tt>.
	 * Additionally, if <tt>minValue</tt> is greater than </tt>maxValue</tt>,
	 * both <tt>minValue</tt> and the current value will be set to
	 * </tt>maxValue</tt>.
	 * 
	 * @param maxValue
	 * @throws UnsupportedOperationException if this is not an integer parameter
	 */
	public void setMaxValue(int maxValue) {
		if(type != Type.INTEGER) throw new UnsupportedOperationException("parameter type " + type.name());
		if(minValue.intValue() > maxValue) {
			minValue = maxValue;
			value = maxValue;
		}
		this.maxValue = maxValue;
		if(((Integer)value).intValue() > maxValue) value = maxValue;
	}
	
	/**
	 * Sets the minimum float value of this parameter.  If the current value
	 * is less than <tt>minValue</tt>, it will be set to <tt>minValue</tt>.
	 * Additionally, if <tt>maxValue</tt> is less than </tt>minValue</tt>,
	 * both <tt>maxValue</tt> and the current value will be set to
	 * </tt>minValue</tt>. 
	 * 
	 * @param minValue
	 * @throws UnsupportedOperationException if this is not a float parameter
	 */
	public void setMinValue(float minValue) {
		if(type != Type.FLOAT) throw new UnsupportedOperationException("parameter type " + type.name());
		if(maxValue.floatValue() < minValue) {
			maxValue = minValue;
			value = minValue;
		}
		this.minValue = minValue;
		if(((Float)value).floatValue() < minValue) value = minValue;
	}
	
	/**
	 * Sets the maximum float value of this parameter.  If the current value
	 * is greater than <tt>maxValue</tt>, it will be set to <tt>maxValue</tt>.
	 * Additionally, if <tt>minValue</tt> is greater than </tt>maxValue</tt>,
	 * both <tt>minValue</tt> and the current value will be set to
	 * </tt>maxValue</tt>.
	 * 
	 * @param maxValue
	 * @throws UnsupportedOperationException if this is not a float parameter
	 */
	public void setMaxValue(float maxValue) {
		if(type != Type.FLOAT) throw new UnsupportedOperationException("parameter type " + type.name());
		if(minValue.floatValue() > maxValue) {
			minValue = maxValue;
			value = maxValue;
		}
		this.maxValue = maxValue;
		if(((Float)value).floatValue() > maxValue) value = maxValue;
	}
	
	/**
	 * Gets the minimum value of this parameter.
	 */
	public Number getMinValue() {
		return minValue;
	}
	
	/**
	 * Gets the maximum value of this parameter.
	 */
	public Number getMaxValue() {
		return maxValue;
	}
	
	/**
	 * Gets the type of this parameter.
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Gets the label of this parameter.
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Gets the integer value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not an integer parameter
	 */
	public int getInteger() {
		if(type != Type.INTEGER) throw new UnsupportedOperationException("parameter type " + type.name());
		return (Integer) value;
	}
	
	/**
	 * Sets the integer value of this parameter.  If the value is less than
	 * <tt>minValue</tt>, it will be set to <tt>minValue</tt>.  Likewise, if
	 * the value is larger than <tt>maxValue</tt>, it will be set to
	 * <tt>maxValue</tt>.
	 * 
	 * @throws UnsupportedOperationException if this is not an integer parameter
	 */
	public void setInteger(int value) {
		if(type != Type.INTEGER) throw new UnsupportedOperationException("parameter type " + type.name());
		if(value > ((Integer)maxValue).intValue()) {
			this.value = maxValue;
		}
		else if(value < ((Integer)minValue).intValue()) {
			this.value = minValue;
		}
		else this.value = value;
	}
	
	/**
	 * Gets the float value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not a float parameter
	 */
	public float getFloat() {
		if(type != Type.FLOAT) throw new UnsupportedOperationException("parameter type " + type.name());
		return (Float) value;
	}
	
	/**
	 * Sets the float value of this parameter.  If the value is less than
	 * <tt>minValue</tt>, it will be set to <tt>minValue</tt>.  Likewise, if
	 * the value is larger than <tt>maxValue</tt>, it will be set to
	 * <tt>maxValue</tt>.
	 * 
	 * @throws UnsupportedOperationException if this is not a float parameter
	 */
	public void setFloat(float value) {
		if(type != Type.FLOAT) throw new UnsupportedOperationException("parameter type " + type.name());
		if(value > ((Float)maxValue).floatValue()) {
			this.value = maxValue;
		}
		else if(value < ((Float)minValue).floatValue()) {
			this.value = minValue;
		}
		else this.value = value;
	}
	
	/**
	 * Gets the boolean value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not a boolean parameter
	 */
	public boolean getBoolean() {
		if(type != Type.BOOLEAN) throw new UnsupportedOperationException("parameter type " + type.name());
		return (Boolean) value;
	}
	
	/**
	 * Sets the boolean value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not a boolean parameter
	 */
	public void setBoolean(boolean value) {
		if(type != Type.BOOLEAN) throw new UnsupportedOperationException("parameter type " + type.name());
		this.value = value;
	}
	
	/**
	 * Gets the String value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not a String parameter
	 */
	public String getString() {
		if(type != Type.STRING) throw new UnsupportedOperationException("parameter type " + type.name());
		return (String) value;
	}
	
	/**
	 * Sets the String value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not a String parameter
	 */
	public void setString(String value) {
		if(type != Type.STRING) throw new UnsupportedOperationException("parameter type " + type.name());
		this.value = value;
	}
	
	/**
	 * Gets the option value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not an option parameter
	 */
	public Object getOption() {
		if(type != Type.OPTION) throw new UnsupportedOperationException("parameter type " + type.name());
		return value;
	}
	
	/**
	 * Gets the index of the selected option.
	 * 
	 * @throws UnsupportedOperationException if this is not an option parameter
	 */
	public int getOptionIndex() {
		return options.indexOf(value);
	}
	
	/**
	 * Gets the possible option values for this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not an option parameter
	 */
	public List<Object> getOptions() {
		if(type != Type.OPTION) throw new UnsupportedOperationException("parameter type " + type.name());
		return new ArrayList<Object>(options);
	}
	
	/**
	 * Sets the option value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not an option parameter
	 */
	public void setOption(int selected) {
		if(type != Type.OPTION) throw new UnsupportedOperationException("parameter type " + type.name());
		value = options.get(selected);
	}
	
	/**
	 * Sets the option value of this parameter.
	 * 
	 * @throws UnsupportedOperationException if this is not an option parameter
	 * @throws IllegalArgumentException if the specified object is not one of the options
	 */
	public void setOption(Object selected) {
		if(type != Type.OPTION) throw new UnsupportedOperationException("parameter type " + type.name());
		if(!options.contains(selected)) throw new IllegalArgumentException("selected not in options");
		value = selected;
	}
	
	/**
	 * Adds an option to this <tt>Parameter</tt>.
	 * 
	 * @param option the option to add
	 * @throws UnsupportedOperationException if this is not an option parameter
	 */
	public void addOption(Object option) {
		if(type != Type.OPTION) throw new UnsupportedOperationException("parameter type " + type.name());
		if(!options.contains(option)) options.add(option);
	}
	
	/**
	 * Identifies the data type of a <tt>Parameter</tt>. 
	 */
	public static enum Type {
		INTEGER,
		FLOAT,
		BOOLEAN,
		STRING,
		OPTION
	}
}
