package com.obsglobal.util.regex;

import com.obsglobal.util.ApplicationRunner;
import com.obsglobal.util.RunnableApplication;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Basic regex editor.
 */
public class RegexEditor extends JPanel implements RunnableApplication<RegexEditor> {

	private Highlighter highlighter;
	private Highlighter.HighlightPainter painter;
	private JTextField regexTextField, matchingTextField;
	private JTextArea inputTextArea;
	private JLabel statusMessageLabel;
	private JCheckBox optionBoxes[], replaceMatchToggle;

	private String unmodifiedInputText = null;

	@Override
	public String getTitle() {
		return "OBS Regex Editor";
	}

	@Override
	public RegexEditor getViewComponent() {
		initialize();
		return this;
	}

	protected void applyRegularExpression() {
		try {
			//noinspection MagicConstant
			Pattern pattern = Pattern.compile(getRegularExpressionText(), calculatePatternOptions());
			if (replaceMatchToggle.isSelected())
				replaceMatches(pattern);
			else
				highlightMatches(pattern);
		}
		catch (Exception ex) {
			postError("ERROR - Failed to apply regex: ", ex.getMessage());
		}

		regexTextField.grabFocus();
	}

	protected void replaceMatches(Pattern pattern) throws BadLocationException {
		resetInput();

		int matches = 0;
		String input = getInputText();
		StringBuilder builder = new StringBuilder();
		List<int[]> replacedResults = new ArrayList<int[]>();

		String[] splits = pattern.split(input);
		// append initial non-match
		builder.append(splits[0]);

		List<MatchResult> matchResults = RegexUtil.findAllMatches(pattern, input);
		for (MatchResult result : matchResults) {
			String replacementText = result.group().replaceFirst(pattern.pattern(), getMatchingExpressionText());
			replacedResults.add(new int[] { builder.length(), builder.length() + replacementText.length() });

			builder.append(replacementText);
			++matches;
			builder.append(splits[matches]);
		}

		inputTextArea.setText(builder.toString());
		postMatches(matches);

		for (int[] replacementPoints : replacedResults)
			highlighter.addHighlight(replacementPoints[0], replacementPoints[1], painter);
	}

	protected void highlightMatches(Pattern pattern) throws BadLocationException {
		resetInput();

		int matches = 0;
		List<MatchResult> matchResults = RegexUtil.findAllMatches(pattern, getInputText());
		for (MatchResult result : matchResults) {
			highlighter.addHighlight(result.start(), result.end(), painter);
			++matches;
		}

		postMatches(matches);
	}

	protected void editInput(JToggleButton editButton) {
		inputTextArea.setEditable(editButton.isSelected());
		if (!editButton.isSelected())
			setUnmodifiedInputText(inputTextArea.getText());
	}

	protected void pasteInput() {
		try {
			setUnmodifiedInputText(StringUtils.defaultString((String)
					Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor), ""));
			inputTextArea.setText(getUnmodifiedInputText());
		}
		catch (Exception ex) {
			postError("ERROR - Failed to paste input: ", ex.getMessage());
		}
	}

	protected void resetInput() {
		highlighter.removeAllHighlights();
		inputTextArea.setText(getUnmodifiedInputText());
	}

	protected void postMatches(int tally) {
		if (tally >= 0) {
			statusMessageLabel.setForeground(Color.green.darker());
			postMessage(tally, tally == 1 ? " match" : " matches");
		}
	}

	protected void postError(Object... messageParts) {
		statusMessageLabel.setForeground(Color.red);
		postMessage(messageParts);
	}

	protected void postMessage(Object... messageParts) {
		statusMessageLabel.setText(StringUtils.join(messageParts));
	}

	protected void initialize() {
		setLayout(new BorderLayout(5, 5));

		add(createHeaderPanel(), BorderLayout.NORTH);

		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		statusMessageLabel = new JLabel("0 Matches");
		labelPanel.add(new JLabel("Target Text:"));
		labelPanel.add(Box.createHorizontalStrut(10));
		final JToggleButton editButton = new JToggleButton("Edit", false);
		editButton.setName("button-edit");
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editInput(editButton);
			}
		});
		JButton pasteButton = new JButton("Paste");
		pasteButton.setName("button-paste");
		pasteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteInput();
			}
		});
		labelPanel.add(editButton);
		labelPanel.add(Box.createHorizontalStrut(25));
		labelPanel.add(pasteButton);
		labelPanel.add(Box.createHorizontalStrut(50));
		labelPanel.add(statusMessageLabel);
		
		Box inputBox = Box.createVerticalBox();
		highlighter = new DefaultHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.green);
		inputTextArea = new JTextArea("", 25, 100);
		inputTextArea.setHighlighter(highlighter);
		inputTextArea.setEditable(false);
		inputTextArea.setName("textArea-input");
		inputBox.add(new JScrollPane(inputTextArea));

		Box centerBox = Box.createVerticalBox();
		centerBox.add(labelPanel);
		centerBox.add(inputBox);
		centerBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 10, 10),
				BorderFactory.createLineBorder(Color.lightGray, 1)));
		add(centerBox, BorderLayout.CENTER);
	}

	protected JPanel createPatternOptionsPanel() {
		optionBoxes = new JCheckBox[OPTIONS_LENGTH];
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		for (int index = 0; index < OPTIONS_LENGTH; index++) {
			optionBoxes[index] = new JCheckBox(OPTION_DESCRIPTIONS[index], false);
			optionBoxes[index].setToolTipText(OPTION_TOOLTIPS[index]);
			optionBoxes[index].setName("checkbox-"+ OPTION_DESCRIPTIONS[index]);
			panel.add(optionBoxes[index]);
		}

		panel.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));

		return panel;
	}

	protected int calculatePatternOptions() {
		int options = 0;
		for (int index = 0; index < OPTIONS_LENGTH; index++)
			if (optionBoxes[index].isSelected())
				options |= OPTIONS[index];

		return options;
	}

	protected JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;
		// regex label
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);
		headerPanel.add(new JLabel("Test Expression:", JLabel.LEFT), constraints);
		// regex input
		regexTextField = new JTextField("", 75);
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 4;
		constraints.insets = new Insets(5, 5, 5, 5);
		regexTextField.setName("textField-regex");
		headerPanel.add(regexTextField, constraints);
		// apply button
		JButton applyButton = new JButton("Apply");
		applyButton.setMnemonic('A');
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyRegularExpression();
			}
		});
		constraints.gridx = 5;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		applyButton.setName("button-apply");
		headerPanel.add(wrap(applyButton), constraints);

		// options label
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		headerPanel.add(new JLabel("Pattern Options:"), constraints);
		// options panel
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 4;
		constraints.insets = new Insets(5, 5, 5, 5);
		headerPanel.add(createPatternOptionsPanel(), constraints);
		// filler
		constraints.gridx = 5;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		headerPanel.add(Box.createVerticalBox(), constraints);

		// match label
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.insets = new Insets(5, 5, 5, 5);
		headerPanel.add(new JLabel("Replacement Text:"), constraints);
		// match input
		matchingTextField = new JTextField("", 75);
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 4;
		constraints.insets = new Insets(5, 5, 5, 5);
		matchingTextField.setEnabled(false);
		matchingTextField.setName("textField-matching");
		headerPanel.add(matchingTextField, constraints);
		// filler
		replaceMatchToggle = new JCheckBox("Replace");
		constraints.gridx = 5;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		replaceMatchToggle.setName("checkbox-replace");
		replaceMatchToggle.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				matchingTextField.setEnabled(((JCheckBox) e.getSource()).isSelected());
			}
		});
		headerPanel.add(replaceMatchToggle, constraints);

		headerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 5, 10),
				BorderFactory.createLineBorder(Color.lightGray, 1)));
		return headerPanel;
	}

	private JPanel wrap(JComponent component) {
		JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		outerPanel.add(component);
		return outerPanel;
	}

	static int[] OPTIONS = {
			Pattern.UNIX_LINES, Pattern.CASE_INSENSITIVE, Pattern.COMMENTS, Pattern.MULTILINE,
			Pattern.LITERAL, Pattern.DOTALL, Pattern.UNICODE_CASE, Pattern.CANON_EQ
	};
	static final int OPTIONS_LENGTH = OPTIONS.length;
	static String[] OPTION_DESCRIPTIONS = {
			"UNIX_LINES", "CASE_INSENSITIVE", "COMMENTS", "MULTILINE",
			"LITERAL", "DOTALL", "UNICODE_CASE", "CANON_EQ"
	};
	static String[] OPTION_TOOLTIPS = {
			"In this mode, only the '\\n' line terminator is recognized in the behavior of ., ^, and $.\n" +
			"Unix lines mode can also be enabled via the embedded flag expression (?d).",
			"By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched. Unicode-aware case-insensitive matching can be enabled by specifying the UNICODE_CASE flag in conjunction with this flag.\n" +
			"Case-insensitive matching can also be enabled via the embedded flag expression (?i).",
			"In this mode, whitespace is ignored, and embedded comments starting with # are ignored until the end of a line.\n" +
			"Comments mode can also be enabled via the embedded flag expression (?x).",
			"In multiline mode the expressions ^ and $ match just after or just before, respectively, a line terminator or the end of the input sequence. By default these expressions only match at the beginning and the end of the entire input sequence.\n" +
			"Multiline mode can also be enabled via the embedded flag expression (?m).",
			"When this flag is specified then the input string that specifies the pattern is treated as a sequence of literal characters. Metacharacters or escape sequences in the input sequence will be given no special meaning.\n" +
			"The flags CASE_INSENSITIVE and UNICODE_CASE retain their impact on matching when used in conjunction with this flag. The other flags become superfluous.\n" +
			"There is no embedded flag character for enabling literal parsing.",
			"In dotall mode, the expression . matches any character, including a line terminator. By default this expression does not match line terminators.\n" +
			"Dotall mode can also be enabled via the embedded flag expression (?s).",
			"When this flag is specified then case-insensitive matching, when enabled by the CASE_INSENSITIVE flag, is done in a manner consistent with the Unicode Standard. By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.\n" +
			"Unicode-aware case folding can also be enabled via the embedded flag expression (?u).",
			"When this flag is specified then two characters will be considered to match if, and only if, their full canonical decompositions match. The expression \"a\\u030A\", for example, will match the string \"\\u00E5\" when this flag is specified. By default, matching does not take canonical equivalence into account.\n" +
			"There is no embedded flag character for enabling canonical equivalence."
	};

	protected String getInputText() {
		if (unmodifiedInputText == null)
			unmodifiedInputText = StringUtils.defaultString(inputTextArea.getText(), "");

		return unmodifiedInputText;
	}

	
	protected String getUnmodifiedInputText() {
		return unmodifiedInputText;
	}

	protected void setUnmodifiedInputText(String unmodifiedInput) {
		unmodifiedInputText = unmodifiedInput;
	}

	protected String getRegularExpressionText() {
		return StringUtils.defaultString(regexTextField.getText(), "");
	}

	protected String getMatchingExpressionText() {
		return StringUtils.defaultString(matchingTextField.getText(), "");
	}

	public static void main(String[] args) {
		ApplicationRunner.runApplication(new RegexEditor());
	}
}
