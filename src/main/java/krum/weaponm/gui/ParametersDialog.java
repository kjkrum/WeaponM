package krum.weaponm.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import krum.weaponm.script.Parameter;

public class ParametersDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private boolean accepted;
	
	protected ParametersDialog(JFrame parent, String title, List<Parameter> parameters) {
		super(parent, title, true);
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		final List<ParameterPanel> paramPanels = new LinkedList<ParameterPanel>();
		for(Parameter param : parameters) {
			ParameterPanel panel = new ParameterPanel(param);
			paramPanels.add(panel);
			centerPanel.add(panel);
		}

		JButton okButton = new JButton("OK");
		okButton.setMnemonic('O');
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(ParameterPanel panel : paramPanels) {
					panel.copyInputToParameter();
				}
				accepted = true;
				dispose();
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic('C');
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();				
			}
		});
		
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(centerPanel);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(okButton);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
	}

	public static boolean showDialog(JFrame parent, String title, List<Parameter> parameters) {
		ParametersDialog dialog = new ParametersDialog(parent, title, parameters);
		dialog.setVisible(true);
		return dialog.accepted;
	}
	
	/*
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				List<Parameter> params = new LinkedList<Parameter>();
				params.add(new Parameter("Wretchedness", 92));
				params.add(new Parameter("Failure", true));
				new ParametersDialog(null, "Parameters", params).setVisible(true);
			}
		});
	}
	*/
}
