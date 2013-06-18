package com.obsglobal.util.regex;

import com.obsglobal.util.ApplicationRunner;
import com.obsglobal.util.RunnableApplication;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic regex editor.
 */
public class RegexEditor extends JPanel implements RunnableApplication {

	private Highlighter highlighter;
	private Highlighter.HighlightPainter painter;
	private JTextField regexTextField;
	private JTextArea inputTextArea;
	private JLabel statusMessageLabel;
	private JCheckBox[] optionBoxes;

	@Override
	public String getTitle() {
		return "OBS Regex Editor";
	}

	@Override
	public JComponent getViewComponent() {
		initialize();
		return this;
	}

	protected void applyRegularExpression() {
		try {
			//noinspection MagicConstant
			Pattern pattern = Pattern.compile(getRegularExpressionText(), calculatePatternOptions());
			Matcher matcher = pattern.matcher(getInputText());
			highlightMatches(matcher);
		}
		catch (Exception ex) {
			message(ex.getMessage());
		}

		regexTextField.grabFocus();
	}

	protected void highlightMatches(Matcher matcher) {
		highlighter.removeAllHighlights();

		int index = 0, matches = 0;
		while (matcher.find(index)) {
			try {
				highlighter.addHighlight(matcher.start(), matcher.end(), painter);
				++matches;
			}
			catch (BadLocationException ex) {
				message("ERROR: ", ex.getMessage());
			}

			index = matcher.end();
		}

		message(matches, " matches");
	}

	protected void pasteInput() {
		try {
			inputTextArea.setText(StringUtils.defaultString((String)
					Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor), ""));
		}
		catch (Exception ex) {
			message("Failed to paste input: ", ex.getMessage());
		}
	}
	protected void message(Object... messageParts) {
		statusMessageLabel.setText(StringUtils.join(messageParts));
	}

	protected void initialize() {
		setLayout(new BorderLayout(5, 5));

		Box regexBox = Box.createVerticalBox();
		JPanel regexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		regexPanel.add(new JLabel("Test Expression:"));
		regexTextField = new JTextField("", 80);
		regexPanel.add(regexTextField);
		JButton applyButton = new JButton("Apply");
		applyButton.setMnemonic('A');
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyRegularExpression();
			}
		});
		regexPanel.add(applyButton);

		regexBox.add(regexPanel);
		regexBox.add(createPatternOptionsPanel());
		regexBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 5, 10),
				BorderFactory.createLineBorder(Color.lightGray, 1)));
		add(regexBox, BorderLayout.NORTH);

		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		statusMessageLabel = new JLabel("0 Matches");
		labelPanel.add(new JLabel("Target Text:"));
		labelPanel.add(Box.createHorizontalStrut(10));
		JButton pasteButton = new JButton("Paste");
		pasteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteInput();
			}
		});
		labelPanel.add(pasteButton);
		labelPanel.add(Box.createHorizontalStrut(50));
		labelPanel.add(statusMessageLabel);
		
		Box inputBox = Box.createVerticalBox();
		highlighter = new DefaultHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.green);
		inputTextArea = new JTextArea("<Enter the text to evaluate here>", 25, 100);
		inputTextArea.setHighlighter(highlighter);
		inputBox.add(new JScrollPane(inputTextArea));

		Box centerBox = Box.createVerticalBox();
		centerBox.add(labelPanel);
		centerBox.add(inputBox);
		centerBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 10, 10),
				BorderFactory.createLineBorder(Color.lightGray, 1)));
		add(centerBox, BorderLayout.CENTER);
	}

	private static int[] OPTIONS = {
			Pattern.DOTALL, Pattern.MULTILINE, Pattern.CANON_EQ, Pattern.CASE_INSENSITIVE, Pattern.COMMENTS,
			Pattern.LITERAL, Pattern.UNICODE_CASE, Pattern.UNIX_LINES
	};
	private static final int OPTIONS_LENGTH = OPTIONS.length;
	private static String[] OPTION_DESCRIPTIONS = {
			"DOTALL", "MULTILINE", "CANON_EQ", "CASE INSENSITIVE", "COMMENTS",
			"LITERAL", "UNICODE CASE", "UNIX_LINES"
	};
	private static String[] OPTION_TOOLTIPS = {
			"In dotall mode, the expression . matches any character, including a line terminator. By default this expression does not match line terminators.\n" +
			"Dotall mode can also be enabled via the embedded flag expression (?s).",
			"In multiline mode the expressions ^ and $ match just after or just before, respectively, a line terminator or the end of the input sequence. By default these expressions only match at the beginning and the end of the entire input sequence.\n" +
			"Multiline mode can also be enabled via the embedded flag expression (?m).",
			"When this flag is specified then two characters will be considered to match if, and only if, their full canonical decompositions match. The expression \"a\\u030A\", for example, will match the string \"\\u00E5\" when this flag is specified. By default, matching does not take canonical equivalence into account.\n" +
			"There is no embedded flag character for enabling canonical equivalence.",
			"By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched. Unicode-aware case-insensitive matching can be enabled by specifying the UNICODE_CASE flag in conjunction with this flag.\n" +
			"Case-insensitive matching can also be enabled via the embedded flag expression (?i).",
			"In this mode, whitespace is ignored, and embedded comments starting with # are ignored until the end of a line.\n" +
			"Comments mode can also be enabled via the embedded flag expression (?x).",
			"When this flag is specified then the input string that specifies the pattern is treated as a sequence of literal characters. Metacharacters or escape sequences in the input sequence will be given no special meaning.\n" +
			"The flags CASE_INSENSITIVE and UNICODE_CASE retain their impact on matching when used in conjunction with this flag. The other flags become superfluous.\n" +
			"There is no embedded flag character for enabling literal parsing.",
			"When this flag is specified then case-insensitive matching, when enabled by the CASE_INSENSITIVE flag, is done in a manner consistent with the Unicode Standard. By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.\n" +
			"Unicode-aware case folding can also be enabled via the embedded flag expression (?u).",
			"In this mode, only the '\\n' line terminator is recognized in the behavior of ., ^, and $.\n" +
			"Unix lines mode can also be enabled via the embedded flag expression (?d)."
	};

	private JPanel createPatternOptionsPanel() {
		optionBoxes = new JCheckBox[OPTIONS_LENGTH];
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		for (int index = 0; index < OPTIONS_LENGTH; index++) {
			optionBoxes[index] = new JCheckBox(OPTION_DESCRIPTIONS[index], false);
			optionBoxes[index].setToolTipText(OPTION_TOOLTIPS[index]);
			panel.add(optionBoxes[index]);
		}

		JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		panel.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		outerPanel.add(new JLabel("Pattern Options:"));
		outerPanel.add(panel);
		return outerPanel;
	}

	private int calculatePatternOptions() {
		int options = 0;
		for (int index = 0; index < OPTIONS_LENGTH; index++)
			if (optionBoxes[index].isSelected())
				options |= OPTIONS[index];

		return options;
	}
	private String getInputText() {
		return StringUtils.defaultString(inputTextArea.getText(), "");
	}

	private String getRegularExpressionText() {
		return StringUtils.defaultString(regexTextField.getText(), "");
	}

	public static void main(String[] args) {
		ApplicationRunner.runApplication(new RegexEditor());
	}
}
