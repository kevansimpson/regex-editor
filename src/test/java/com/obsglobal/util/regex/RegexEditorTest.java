package com.obsglobal.util.regex;

import junit.framework.AssertionFailedError;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Unit test for {@link RegexEditor}.
 */
public class RegexEditorTest {

	@BeforeClass
	public static void initializeUISpec4J() throws Exception {
		UISpec4J.init();
	}

	@Test
	public void testApplyRegularExpression() throws Exception {
		final int[] applyCounters = new int[2];
		RegexEditor regexEditor = new RegexEditor() {
			@Override
			protected void highlightMatches(Pattern pattern) {
				applyCounters[0] += 1;
			}

			@Override
			protected void replaceMatches(Pattern pattern) {
				applyCounters[1] += 1;
			}
		};

		Panel panel = new Panel(regexEditor.getViewComponent());
		assertNull(regexEditor.getUnmodifiedInputText());
		assertEquals("", regexEditor.getInputText());
		CheckBox replaceCheckBox = panel.getCheckBox("checkbox-replace");
		assertNotNull(replaceCheckBox);
		assertFalse(replaceCheckBox.isSelected().isTrue());

		setText(panel, "textField-regex", "abc");
		Button applyButton = panel.getButton("button-apply");

		applyButton.click();
		assertEquals(1, applyCounters[0]);
		assertEquals(0, applyCounters[1]);

		replaceCheckBox.select();
		applyButton.click();
		assertEquals(1, applyCounters[0]);
		assertEquals(1, applyCounters[1]);
		applyButton.click();
		assertEquals(1, applyCounters[0]);
		assertEquals(2, applyCounters[1]);

		setText(panel, "textField-regex", ")))bad pattern))");
		applyButton.click();
		assertEquals(1, applyCounters[0]);
		assertEquals(2, applyCounters[1]);
	}

	@Test
	public void testReplaceMatches() throws Exception {
		Panel panel = new Panel(createRegexEditor());
		CheckBox replaceCheckBox = panel.getCheckBox("checkbox-replace");
		assertNotNull(replaceCheckBox);
		assertFalse(replaceCheckBox.isSelected().isTrue());
		replaceCheckBox.select();
		assertTrue(replaceCheckBox.isSelected().isTrue());

		setText(panel, "textField-regex", "abc");
		setText(panel, "textField-matching", "~");

		TextBox inputTextArea = panel.getTextBox("textArea-input");
		assertNotNull(inputTextArea);
		pasteText(panel, RegexUtilTest.INPUT);

		Button applyButton = panel.getButton("button-apply");
		applyButton.click();	// abcABCfooabcDEFbarabcGHIfooabcZYXbar
		assertEquals("~ABCfoo~DEFbar~GHIfoo~ZYXbar", inputTextArea.getText());
		Highlighter.Highlight[] highlights = ((JTextComponent) inputTextArea.getAwtComponent()).getHighlighter().getHighlights();
		assertNotNull(highlights);
		assertEquals(4, highlights.length);
		assertEquals(0, highlights[0].getStartOffset());
		assertEquals(1, highlights[0].getEndOffset());
		assertEquals(7, highlights[1].getStartOffset());
		assertEquals(8, highlights[1].getEndOffset());
		assertEquals(14, highlights[2].getStartOffset());
		assertEquals(15, highlights[2].getEndOffset());
		assertEquals(21, highlights[3].getStartOffset());
		assertEquals(22, highlights[3].getEndOffset());
	}

	@Test
	public void testHighlightMatches() throws Exception {
		Panel panel = new Panel(createRegexEditor());
		setText(panel, "textField-regex", "abc");

		TextBox inputTextArea = panel.getTextBox("textArea-input");
		assertNotNull(inputTextArea);
		pasteText(panel, RegexUtilTest.INPUT);

		Button applyButton = panel.getButton("button-apply");
		applyButton.click();	// abcABCfooabcDEFbarabcGHIfooabcZYXbar
		assertEquals(RegexUtilTest.INPUT, inputTextArea.getText());
		Highlighter.Highlight[] highlights = ((JTextComponent) inputTextArea.getAwtComponent()).getHighlighter().getHighlights();
		assertNotNull(highlights);
		assertEquals(4, highlights.length);
		assertEquals(0, highlights[0].getStartOffset());
		assertEquals(3, highlights[0].getEndOffset());
		assertEquals(9, highlights[1].getStartOffset());
		assertEquals(12, highlights[1].getEndOffset());
		assertEquals(18, highlights[2].getStartOffset());
		assertEquals(21, highlights[2].getEndOffset());
		assertEquals(27, highlights[3].getStartOffset());
		assertEquals(30, highlights[3].getEndOffset());
	}

	@Test
	public void testPasteInput() throws Exception {
		// clear clipboard before test
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(null), null);

		final int expectedMessageCount = 2;
		final Object[] expectedErrors = new Object[expectedMessageCount];
		RegexEditor regexEditor = new RegexEditor() {
			@Override
			protected void postError(Object... messageParts) {
				assertEquals(expectedMessageCount, messageParts.length);
				System.arraycopy(messageParts, 0, expectedErrors, 0, expectedMessageCount);
			}
		};

		regexEditor.pasteInput();	// not initialized, will throw NullPointerException
		assertNotNull(expectedErrors[0]);
		assertEquals("ERROR", String.valueOf(expectedErrors[0]).substring(0, 5));
		assertNull(expectedErrors[1]);	// the exception message is actually null
	}

	@Test
	public void testEditInput() throws Exception {
		RegexEditor regexEditor = createRegexEditor();
		assertNull(regexEditor.getUnmodifiedInputText());

		Panel panel = new Panel(regexEditor);
		TextBox inputTextArea = panel.getTextBox("textArea-input");
		ToggleButton editButton = panel.getToggleButton("button-edit");
		assertNotNull(inputTextArea);
		assertNotNull(editButton);

		assertEquals("", inputTextArea.getText());
		assertFalse(inputTextArea.isEditable().isTrue());
		try {
			inputTextArea.setText(RegexUtilTest.INPUT);
			fail("Edited Input TextArea!");
		}
		catch (AssertionFailedError assertionFailedError) {
			assertNotNull(assertionFailedError);
		}

		editButton.click();
		assertTrue(inputTextArea.isEditable().isTrue());
		try {
			inputTextArea.setText(RegexUtilTest.INPUT);
		}
		catch (Exception ex) {
			fail("Failed to Edit Input TextArea!");
		}

		editButton.click();
		assertFalse(inputTextArea.isEditable().isTrue());
		try {
			inputTextArea.setText(RegexUtilTest.INPUT);
			fail("Edited Input TextArea!");
		}
		catch (AssertionFailedError assertionFailedError) {
			assertNotNull(assertionFailedError);
		}
	}

	@Test
	public void testCalculateOptions() throws Exception {
		RegexEditor regexEditor = createRegexEditor();
		Panel panel = new Panel(regexEditor);
		CheckBox[] checkBoxes = new CheckBox[RegexEditor.OPTIONS_LENGTH];
		for (int index = 0; index < RegexEditor.OPTIONS_LENGTH; index++) {
			checkBoxes[index] = panel.getCheckBox("checkbox-"+ RegexEditor.OPTION_DESCRIPTIONS[index]);
			assertFalse(checkBoxes[index].isSelected().isTrue());
		}

		for (int index = 0, count = (int) Math.pow(2, RegexEditor.OPTIONS_LENGTH); index < count; index++) {
			for (int optionIndex = 0; optionIndex < RegexEditor.OPTIONS_LENGTH; optionIndex++) {
				int optionBit = RegexEditor.OPTIONS[optionIndex];
				if ((index & optionBit) == optionBit)
					checkBoxes[optionIndex].select();
				else
					checkBoxes[optionIndex].unselect();
			}

			assertEquals(index, regexEditor.calculatePatternOptions());
		}
	}

	@Test
	public void testMain() throws Exception {
		WindowInterceptor.init(new Trigger() {
			@Override
			public void run() throws Exception {
				RegexEditor.main(new String[0]);
			}
		}).process(new WindowHandler() {
			@Override
			public Trigger process(final org.uispec4j.Window window) throws Exception {
				assertNotNull(window);
				window.titleEquals("OBS Regex Editor");
				return new Trigger() {
					@Override
					public void run() throws Exception {
						window.getAwtComponent().setVisible(false);
					}
				};
			}
		}).run();
	}

	protected RegexEditor createRegexEditor() {
		return (new RegexEditor()).getViewComponent();
	}

	protected void pasteText(Panel panel, String input) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);
		Button pasteButton = panel.getButton("button-paste");
		pasteButton.click();
	}

	protected void setText(Panel panel, String textFieldName, String text) {
		assertNotNull(panel);
		TextBox textBox = panel.getTextBox(textFieldName);
		assertNotNull(textBox);
		assertNotNull(text);
		textBox.setText(text);
	}
}
