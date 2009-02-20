package coms6111.proj1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunnerGUI extends JFrame {
	
	protected static final Log log = LogFactory.getLog(RunnerGUI.class);
	private static RunnerGUI frame;
	
	private JLabel queryLabel;
	private JTextField queryTextField;
	private JButton queryButton;
	private JButton aboutButton;
	private JButton exitButton;
	
	private JPanel resultsPanel;
	private Resultset visibleResultset;
	
	public RunnerGUI(String title) {
		super(title);
	}
	
	public void setResultset(Resultset rs) {
		Iterator<Result> it = rs.getIterator();
		int i = 0;
		
		visibleResultset = rs;
		
		// Add the Resultset to the GUI
		resultsPanel.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		while (it.hasNext()) {
			Result r = (Result) it.next();
			
			// TODO associate the checkbox with the Result somehow
			JCheckBox cb = new JCheckBox();
			cb.setSelected(true);
			c.gridx = 0;
			c.gridy = i;
			resultsPanel.add(cb, c);
			
			JLabel titleLabel = new JLabel(r.title);
			c.gridx = 1;
			c.gridy = i;
			resultsPanel.add(titleLabel, c);
			
			JTextArea summaryTextArea = new JTextArea(r.summary);
			summaryTextArea.setEditable(false);
			c.gridx = 2;
			c.gridy = i;
			resultsPanel.add(summaryTextArea, c);
			
			// TODO make this clickable
			JLabel urlLabel = new JLabel(r.url.toExternalForm());
			c.gridx = 3;
			c.gridy = i;
			resultsPanel.add(urlLabel, c);
			
			i++;
		}
	}
	
	/**
	 * Setup the UI layout
	 */
	public void init() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// The panel where user types query
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		queryLabel = new JLabel("Your query: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(queryLabel, c);
		
		// TODO add ActionListener
		queryTextField = new JTextField(50);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		queryLabel.setLabelFor(queryTextField);
		inputPanel.add(queryTextField, c);
		
		queryButton = new JButton("Submit");
		queryButton.addActionListener(new QueryActionListener());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		inputPanel.add(queryButton, c);
		
		// TODO add ActionListeners
		JButton aboutButton = new JButton("About");
		JButton exitButton = new JButton("Exit");
		
		resultsPanel = new JPanel(new GridBagLayout());
		
		// Add stuff to container
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(inputPanel, BorderLayout.NORTH);
		content.add(resultsPanel, BorderLayout.CENTER);

		this.pack();
		this.setVisible(true);
	}
	
	private class QueryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Query q = new Query(queryTextField.getText());
			Resultset rs = q.execute();
			setResultset(rs);
			frame.pack();
		}
	}

	public static void main(String[] args) {
		frame = new RunnerGUI("Super Relevant Info Retrieval");
		frame.init();
		
	}
}
