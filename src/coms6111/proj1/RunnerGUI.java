package coms6111.proj1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunnerGUI extends JFrame {
	static final long serialVersionUID = 8; // SVN rev
	
	protected static final Log log = LogFactory.getLog(RunnerGUI.class);
	private static RunnerGUI frame;
	
	private JTextField queryTextField;
	private JSpinner targetPrecisionSpinner;
	private JButton submitButton;
	
	private JPanel resultsPanel;
	private Query currentQuery;
	private Resultset visibleResultset;
	private List<JCheckBox> cbList; // Use this to get at the relevant Results
	
	private QueryExpander qe;
	
	public RunnerGUI(String title) {
		super(title);
	}
	
	public void setResultset(Resultset rs) {
		Iterator<Result> it = rs.getIterator();
		int i = 0;
		
		visibleResultset = rs;
		cbList = new ArrayList<JCheckBox>();
		
		// Add the Resultset to the GUI
		resultsPanel.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		while (it.hasNext()) {
			Result r = (Result) it.next();
			Color bgcolor;
			if ((i & 0x1) == 0)
				bgcolor = Color.decode("0xe7e7e7");
			else
				bgcolor = Color.WHITE;
			
			JCheckBox cb = new JCheckBox();
			cbList.add(cb);
			cb.setText(""+(i+1));
			cb.setBackground(bgcolor);
			cb.setSelected(true);
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = i;
			resultsPanel.add(cb, c);
			
			JLabel titleLabel = 
				new JLabel(StringEscapeUtils.unescapeHtml(r.title));
			titleLabel.setBackground(bgcolor);
			titleLabel.setOpaque(true);
			c.gridx = 1;
			c.gridy = i;
			resultsPanel.add(titleLabel, c);
			
			JTextArea summaryTextArea =
				new JTextArea(StringEscapeUtils.unescapeHtml(r.summary));
			summaryTextArea.setBackground(bgcolor);
			summaryTextArea.setEditable(false);
			summaryTextArea.setWrapStyleWord(true);
			summaryTextArea.setLineWrap(true);
			summaryTextArea.setColumns(40);
			c.gridx = 2;
			c.gridy = i;
			resultsPanel.add(summaryTextArea, c);
			
			// TODO make this clickable
			JLabel urlLabel = new JLabel(r.url.toExternalForm());
			urlLabel.setBackground(bgcolor);
			urlLabel.setOpaque(true);
			c.gridx = 3;
			c.gridy = i;
			resultsPanel.add(urlLabel, c);
			
			i++;
		}
		JButton expandButton = new JButton("Expand query & Refine results");
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<Result> relevantResults = new ArrayList<Result>();
				Iterator<JCheckBox> itCb = cbList.iterator();
				Iterator<Result> itR = visibleResultset.getIterator();
				int i = 1;
				while (itCb.hasNext() && itR.hasNext()) {
					JCheckBox cb = itCb.next();
					Result r = itR.next();
					if (cb.isSelected()) {
						System.out.println("Added relevant result #" + i);
						relevantResults.add(r);
					}
					i++;
				}
				currentQuery = qe.apply(currentQuery, new Resultset(relevantResults));
				
				// execute the expanded query
				log.info("Executing query " + currentQuery.toString());
				Resultset rs = currentQuery.execute();
				if (rs == null) {
					log.error("Error executing query. Please wait a few moments and try again.");
					JOptionPane.showMessageDialog(null,
							"Error executing query. Please wait a few moments and try again.",
							"Error executing query", JOptionPane.ERROR_MESSAGE);
				} else {
					setResultset(rs);
				}
			}
		});
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = i;
		c.gridwidth = 4;
		resultsPanel.add(expandButton, c);
	
		frame.pack();
		frame.pack(); // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6223727
	}
	
	/**
	 * Setup the UI layout
	 */
	public void init() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// The panel where user types query
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		queryTextField = new JTextField(40);
		queryTextField.addActionListener(new QueryActionListener());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		inputPanel.add(queryTextField, c);
		
		JLabel queryLabel = new JLabel("Your query: ");
		queryLabel.setLabelFor(queryTextField);
		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(queryLabel, c);
		
		// the Spinner control for picking target precision
		SpinnerModel targetPrecisionModel = new SpinnerNumberModel(0.5, //initial value
                                       0.0, //min
                                       1.0, //max
                                       0.1);                //step
		targetPrecisionSpinner = new JSpinner(targetPrecisionModel);
		JFormattedTextField ftf = null;
        ftf = getTextField(targetPrecisionSpinner);
        if (ftf != null ) {
            ftf.setColumns(4);
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
		
        c.gridx = 3;
        c.gridy = 0;
        inputPanel.add(targetPrecisionSpinner, c);
        
        JLabel targetPrecisionLabel = new JLabel("Precision:");
        targetPrecisionLabel.setLabelFor(targetPrecisionSpinner);
        c.gridx = 2;
        c.gridy = 0;
        inputPanel.add(targetPrecisionLabel, c);
		
		// Submit button
        submitButton = new JButton("Submit");
		submitButton.addActionListener(new QueryActionListener());
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 1;
		inputPanel.add(submitButton, c);
		
		// Other buttons -- "About" and "Exit"
		JPanel otherPanel = new JPanel();
		JButton aboutButton = new JButton("About");
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Authors:\n"
						+ "Ran Bi <rb2651@columbia.edu>\n"
						+ "Andrew Shu <ans2120@columbia.edu>\n\n"
						+ "Columbia University\n"
						+ "COMS 6111: Advanced Database Systems\n"
						+ "Spring 2009");
			}
		});
		otherPanel.add(aboutButton);
		
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		otherPanel.add(exitButton);
		
		resultsPanel = new JPanel(new GridBagLayout());
		
		// Add stuff to container
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(inputPanel, BorderLayout.NORTH);
		content.add(resultsPanel, BorderLayout.CENTER);
		content.add(otherPanel, BorderLayout.SOUTH);

		this.pack();
		this.setVisible(true);
	}

	/**
     * Return the formatted text field used by the editor, or
     * null if the editor doesn't descend from JSpinner.DefaultEditor.
     */
    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

	private class QueryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			currentQuery = new Query(queryTextField.getText());
			Resultset rs = currentQuery.execute();
			if (currentQuery == null) {
				log.error("Error executing query. Please wait a few moments and try again.");
				JOptionPane.showMessageDialog(null,
						"Error executing query. Please wait a few moments and try again.",
						"Error executing query", JOptionPane.ERROR_MESSAGE);
			} else {
				setResultset(rs);
			}
		}
	}

	public static void main(String[] args) {
		frame = new RunnerGUI("Super Relevant Info Retrieval");
		frame.init();

		// TODO Allow user to specify the algorithm for query expansion
		//      Aside from cmdline args, have drop-down list
//		qe = new VectorFreqQueryExpander();
	}
}
