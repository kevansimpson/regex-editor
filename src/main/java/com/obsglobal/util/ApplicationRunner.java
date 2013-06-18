package com.obsglobal.util;

import javax.swing.*;
import java.awt.*;

/**
 * Runs a Swing application by inserting its view into a {@link JFrame}.
 */
public class ApplicationRunner {
    public static void runApplication(final RunnableApplication runnableApplication) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createFrame(runnableApplication.getTitle(), runnableApplication.getViewComponent()).setVisible(true);
			}
		});
    }

	public static JFrame createFrame(String title, Component component) {
		JFrame frame = new JFrame(title);
		frame.getContentPane().add(component);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		return frame;
	}
}
