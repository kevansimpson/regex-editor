package com.obsglobal.util;

import org.junit.Test;

import javax.swing.*;

import static junit.framework.Assert.*;

/**
 * Unit test for simple ApplicationRunner.
 */
public class ApplicationRunnerTest {
	@Test
	public void testCreateFrame() {
		String title = "testTitle";
		JLabel label = new JLabel("testLabel");
		JFrame frame = ApplicationRunner.createFrame(title, label);
		assertNotNull("frame", frame);
		assertEquals("frame title", title, frame.getTitle());
		assertNotNull("frame content", frame.getContentPane());
		assertEquals("frame label", label, frame.getContentPane().getComponents()[0]);
	}

	@Test(expected = NullPointerException.class)
	public void testBadInputCreateFrame() throws Exception {
		ApplicationRunner.createFrame(null, null);
	}
}
