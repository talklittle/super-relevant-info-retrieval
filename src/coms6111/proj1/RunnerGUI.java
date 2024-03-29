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
	static final long serialVersionUID = 10; // SVN rev
	
	protected static final Log log = LogFactory.getLog(RunnerGUI.class);
	private static RunnerGUI frame;
	
	private JTextField queryTextField;
	private JSpinner targetPrecisionSpinner;
	private JButton submitButton;
	
	private JScrollPane resultsScrollPane;
	private JPanel resultsPanel;
	private Query currentQuery;
	private Resultset visibleResultset;
	private List<JCheckBox> cbList; // Use this to get at the relevant Results
	
	private QueryExpander qe;
	
	/**
	 * Constructor.
	 * @param title Titlebar text
	 */
	public RunnerGUI(String title) {
		super(title);
		// TODO Allow user to specify the algorithm for query expansion
		//      Aside from cmdline args, have drop-down list?
		qe = new TermFreqQueryExpander();
	}

	/**
	 * Execute the query stored in variable currentQuery and update visible Resultset 
	 */
	public void executeCurrentQuery() {
		log.info("Executing query " + currentQuery.toString());
		Resultset rs = currentQuery.execute();
		if (rs == null) {
			log.error("Error executing query. Please wait a few moments and try again.");
			JOptionPane.showMessageDialog(null,
					"Error executing query. Please wait a few moments and try again.",
					"Error executing query", JOptionPane.ERROR_MESSAGE);
		} else {
			log.info("" + rs.getSize() + " results.");
			log.info("Target precision "
					+ targetPrecisionSpinner.getValue()
					+ " = " 
					+ (int)(((Double)targetPrecisionSpinner.getValue())*rs.getSize())
					+ " results");
			setResultset(rs);
		}
	}
	
	/**
	 * Update the visible Resultset
	 * @param rs New Resultset to replace current one
	 */
	public void setResultset(Resultset rs) {
		visibleResultset = rs;
		cbList = new ArrayList<JCheckBox>();
		
		resultsPanel.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		
		// Display current query at top
		JTextArea currentQueryTextArea = new JTextArea("Current query: " + currentQuery.toString());
		currentQueryTextArea.setBackground(Color.YELLOW);
		currentQueryTextArea.setEditable(false);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = 0;
		resultsPanel.add(currentQueryTextArea, c);
		
		// Headers
		JLabel cbHeader = new JLabel("<html><u>Relevant?</u></html>");
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		resultsPanel.add(cbHeader, c);
		JLabel titleHeader = new JLabel("<html><u>Title</u></html>");
		c.gridx = 1;
		resultsPanel.add(titleHeader, c);
		JLabel summaryHeader = new JLabel("<html><u>Summary</u></html>");
		c.gridx = 2;
		resultsPanel.add(summaryHeader, c);
		JLabel urlHeader = new JLabel("<html><u>URL</u></html>");
		c.gridx = 3;
		resultsPanel.add(urlHeader, c);
		
		// Add each result of the Resultset to the GUI
		Iterator<Result> it = rs.iterator();
		int resultsPanelRow = 2, resultIndex = 1;
		while (it.hasNext()) {
			Result r = (Result) it.next();
			Color bgcolor;
			if ((resultsPanelRow & 0x1) == 0)
				bgcolor = Color.decode("0xe7e7e7");
			else
				bgcolor = Color.WHITE;
			
			JCheckBox cb = new JCheckBox();
			cbList.add(cb);
			cb.setText(""+resultIndex);
			cb.setBackground(bgcolor);
			cb.setSelected(true);
			cb.setMnemonic(0x30 + (resultIndex % 10)); // ASCII '0' thru '9'
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = resultsPanelRow;
			resultsPanel.add(cb, c);
			
			JTextArea titleTextArea = 
				new JTextArea(StringEscapeUtils.unescapeHtml(r.title));
			titleTextArea.setEditable(false);
			titleTextArea.setFont(new Font(null, Font.BOLD, 12));
			titleTextArea.setWrapStyleWord(true);
			titleTextArea.setLineWrap(true);
			titleTextArea.setBackground(bgcolor);
			titleTextArea.setOpaque(true);
//			titleTextArea.setColumns(20);
			c.weightx = 0.25;
			c.gridx = 1;
			c.gridy = resultsPanelRow;
			resultsPanel.add(titleTextArea, c);
			
			JTextArea summaryTextArea =
				new JTextArea(StringEscapeUtils.unescapeHtml(r.summary));
			summaryTextArea.setBackground(bgcolor);
			summaryTextArea.setEditable(false);
			summaryTextArea.setWrapStyleWord(true);
			summaryTextArea.setLineWrap(true);
//			summaryTextArea.setColumns(40);
			c.weightx = 0.75;
			c.gridx = 2;
			c.gridy = resultsPanelRow;
			resultsPanel.add(summaryTextArea, c);
			
			// TODO make this clickable
			JTextArea urlTextArea = new JTextArea(r.url.toExternalForm());
			urlTextArea.setBackground(bgcolor);
			urlTextArea.setEditable(false);
			urlTextArea.setFont(new Font(null, Font.BOLD, 12));
			urlTextArea.setLineWrap(true);
			urlTextArea.setOpaque(true);
//			urlTextArea.setColumns(20);
			c.weightx = 0.25;
			c.gridx = 3;
			c.gridy = resultsPanelRow;
			resultsPanel.add(urlTextArea, c);
			
			resultsPanelRow++;
			resultIndex++;
		}
		
		JButton expandButton = new JButton("Expand query & Refine results");
		expandButton.setMnemonic(KeyEvent.VK_E);
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				List<Result> relevantResults = new ArrayList<Result>();
				List<Result> nonrelevantResults = new ArrayList<Result>();
				Iterator<JCheckBox> itCb = cbList.iterator();
				Iterator<Result> itR = visibleResultset.iterator();
				
				int i = 1;
				while (itCb.hasNext() && itR.hasNext()) {
					JCheckBox cb = itCb.next();
					Result r = itR.next();
					if (cb.isSelected()) {
						log.info("Added relevant result #" + i);
						relevantResults.add(r);
					} else {
						log.debug("Nonrelevant result #" + i);
						nonrelevantResults.add(r);
					}
					i++;
				}
				
				// Stop if target precision reached
				log.debug("" + (double)relevantResults.size()
						/ (double)visibleResultset.getSize());
				if ((double) relevantResults.size()
						/ (double)visibleResultset.getSize()
						>= (Double)targetPrecisionSpinner.getValue()) {
					log.info("Desired precision reached.");
					JOptionPane.showMessageDialog(null,
							"Your desired precision was reached!");
					return;
				}
				
				// Expand current query
				log.debug("Trying to expand the query using relevant results");
				Resultset relevantResultset = new Resultset(relevantResults);
				Resultset nonrelevantResultset = new Resultset(nonrelevantResults);
				currentQuery = qe.apply(currentQuery, relevantResultset, nonrelevantResultset);
				log.debug("Expanded query! Now execute new query.");
				// Execute the expanded query
				executeCurrentQuery();
			}
		});
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = resultsPanelRow;
		c.gridwidth = 4;
		resultsPanel.add(expandButton, c);
		resultsPanel.repaint();
		
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
        submitButton = new JButton("Submit initial query");
		submitButton.addActionListener(new QueryActionListener());
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 1;
		inputPanel.add(submitButton, c);
		
		// Other buttons -- "About" and "Exit"
		JPanel otherPanel = new JPanel(new FlowLayout());
		JLabel hintLabel = new JLabel("<html><em>Hint: Use Alt+[0-9] for faster selection of checkboxes</em></html>");
		otherPanel.add(hintLabel);
//		c.gridx = 0;
//		c.gridy = 0;
		JButton aboutButton = new JButton("About");
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				JOptionPane.showMessageDialog(null,
						"Authors:\n"
						+ "Ran Bi <rb2651@columbia.edu>\n"
						+ "Andrew Shu <ans2120@columbia.edu>\n\n"
						+ "Columbia University\n"
						+ "COMS 6111: Advanced Database Systems\n"
						+ "Spring 2009");
			}
		});
//		c.gridx = 0;
//		c.gridy = 1;
		otherPanel.add(aboutButton);
		
		JButton exitButton = new JButton("Exit");
		exitButton.setMnemonic(KeyEvent.VK_X);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});
//		c.gridx = 1;
		otherPanel.add(exitButton);
		
		resultsPanel = new JPanel(new GridBagLayout());
		resultsScrollPane = new JScrollPane();
		resultsScrollPane.setViewportView(resultsPanel);
		
		// Add stuff to container
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout(0,0));
		content.setPreferredSize(new Dimension(1024,0));
		content.add(inputPanel, BorderLayout.NORTH);
		content.add(resultsScrollPane, BorderLayout.CENTER);
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
            log.error("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    /**
     * Listener for pressing return or button click. Executes query.
     */
	private class QueryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			currentQuery = new Query(queryTextField.getText());
			executeCurrentQuery();
		}
	}

	/**
	 * Main entrypoint.
	 * @param args
	 */
	public static void main(String[] args) {
		frame = new RunnerGUI("Super Relevant Info Retrieval");
		frame.setPreferredSize(new Dimension(1024,768));
		frame.init();
	}
}
