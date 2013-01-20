package krum.weaponm.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import krum.weaponm.database.LoginOptions;

// TODO: mnemonics everywhere

public class LoginOptionsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	public static final int CANCEL_ACTION = 0;
	public static final int OK_ACTION = 1;
	
	// acceptable names are 1-41 chars, but supplying it is optional
	protected static final Pattern namePattern = Pattern.compile("[ -\\}]{0,41}");
	// password can actually be blank
	protected static final Pattern passwordPattern = Pattern.compile("[ -\\}]{0,8}");

	protected LoginOptions options;
	protected int action = CANCEL_ACTION;
	
	protected JTextField hostField;
	protected JTextField portField; // TODO: make formatted text field?
	protected JComboBox gameComboBox;

	protected JTextField nameField;
	protected JPasswordField passwordField;
	protected JCheckBox autoLoginCheckBox;

	protected JButton okButton;
	protected JButton cancelButton;
	
	public static int showDialog(Frame parent, LoginOptions options) {
		LoginOptionsDialog dialog = new LoginOptionsDialog(parent, options);
		dialog.setVisible(true);
		// thread resumes when dialog is hidden
		return dialog.action;
	}
	
	protected LoginOptionsDialog(Frame parent, LoginOptions options) {
		super(parent, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.options = options;
		initComponents();
		action = CANCEL_ACTION;
		hostField.setText(options.getHost());
		portField.setText(Integer.toString(options.getPort()));
		gameComboBox.setSelectedItem(new Character(options.getGame()));
		nameField.setText(options.getName());
		passwordField.setText(options.getPassword());
		autoLoginCheckBox.setSelected(options.isAutoLogin());
		setLocationRelativeTo(parent);
	}
	
	protected void initComponents() {
		
		/*
		FocusListener selectOnFocus = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				((JTextField) e.getComponent()).selectAll();
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				((JTextField) e.getComponent()).select(0, 0);
			}
		};
		*/

		hostField = new JTextField();
		hostField.addFocusListener(SelectOnFocus.getInstance());
		portField = new JTextField();
		portField.addFocusListener(SelectOnFocus.getInstance());
		
		Character[] letters = new Character[25];
		int i = 0;
		for (char c = 'A'; c <= 'Z'; ++c) {
			if (c != 'Q') {
				letters[i] = c;
				++i;
			}
		}
		gameComboBox = new JComboBox(letters);

		nameField = new JTextField();
		nameField.addFocusListener(SelectOnFocus.getInstance());
		passwordField = new JPasswordField();
		passwordField.addFocusListener(SelectOnFocus.getInstance());
		autoLoginCheckBox = new JCheckBox();

		okButton = new JButton("OK");
		okButton.setMnemonic('O');
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveFields();
					action = OK_ACTION;
					dispose();
				}
				catch(InputValidationException ex) {
					JOptionPane.showMessageDialog(
							LoginOptionsDialog.this,
							ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic('C');
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action = CANCEL_ACTION;
				dispose();				
			}
		});

		setTitle("Login Options");
		JLabel jLabel1 = new JLabel("Host");
		JLabel jLabel2 = new JLabel("Port");
		JLabel jLabel3 = new JLabel("Game");
		JLabel jLabel4 = new JLabel("Name");
		JLabel jLabel5 = new JLabel("Password");
		JLabel jLabel6 = new JLabel("Auto-login");
		JPanel jPanel1 = new JPanel();
		jPanel1.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Server"));
		JPanel jPanel2 = new JPanel();
		jPanel2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Player"));

		// layout shizzle
		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabel1,
																GroupLayout.Alignment.TRAILING)
														.addComponent(
																jLabel2,
																GroupLayout.Alignment.TRAILING)
														.addComponent(
																jLabel3,
																GroupLayout.Alignment.TRAILING))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																hostField,
																GroupLayout.DEFAULT_SIZE,
																199,
																Short.MAX_VALUE)
														.addComponent(
																gameComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																portField,
																GroupLayout.PREFERRED_SIZE,
																51,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(
																hostField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(
																portField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel3)
														.addComponent(
																gameComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabel4,
																GroupLayout.Alignment.TRAILING)
														.addComponent(
																jLabel5,
																GroupLayout.Alignment.TRAILING)
														.addComponent(
																jLabel6,
																GroupLayout.Alignment.TRAILING))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																nameField,
																GroupLayout.DEFAULT_SIZE,
																166,
																Short.MAX_VALUE)
														.addComponent(
																passwordField,
																GroupLayout.DEFAULT_SIZE,
																166,
																Short.MAX_VALUE)
														.addComponent(
																autoLoginCheckBox))
										.addContainerGap()));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel4)
														.addComponent(
																nameField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel5)
														.addComponent(
																passwordField,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel6)
														.addComponent(
																autoLoginCheckBox))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(
														jPanel2,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(
														jPanel1,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addGroup(
														GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup()
																.addComponent(
																		okButton)
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		cancelButton)))
								.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jPanel1,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel2,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
												.addComponent(cancelButton)
												.addComponent(okButton))
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		getRootPane().setDefaultButton(okButton);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setResizable(false);
	}

	protected void saveFields() throws InputValidationException {
		// host
		String host = hostField.getText();
		if("".equals(host)) {
			hostField.requestFocusInWindow();
			throw new InputValidationException("host required");
		}
		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			hostField.requestFocusInWindow();
			throw new InputValidationException("unknown host: " + host);
		}
		
		// port
		int port;
		try {
			port = Integer.parseInt(portField.getText());
			if(port < 1 || port > 65535) {
				portField.requestFocusInWindow();
				throw new InputValidationException("invalid port: " + port);
			}
		}
		catch(NumberFormatException e) {
			portField.requestFocusInWindow();
			throw new InputValidationException("invalid port: " + portField.getText());
		}
		
		// game
		char game = (Character) gameComboBox.getSelectedItem();
		if(game < 'A' || game > 'Z' || game == 'Q') {
			gameComboBox.requestFocusInWindow();
			throw new InputValidationException("invalid game letter: " + game);
		}
		
		// name
		String name = nameField.getText();
		if(!namePattern.matcher(name).matches()) {
			nameField.requestFocusInWindow();
			throw new InputValidationException("invalid name: " + name);
		}
		
		// password
		@SuppressWarnings("deprecation")
		String password = passwordField.getText();
		if(!passwordPattern.matcher(password).matches()) {
			passwordField.requestFocusInWindow();
			throw new InputValidationException("invalid password: " + password);
		}
		
		// auto-login
		boolean autoLogin = autoLoginCheckBox.isSelected();
		if(autoLogin && "".equals(name)) {
			nameField.requestFocusInWindow();
			throw new InputValidationException("name required for auto-login");
		}
		
		// golden!
		options.setHost(host);
		options.setPort(port);
		options.setGame(game);
		options.setName(name);
		options.setPassword(password);
		options.setAutoLogin(autoLogin);
	}
}
