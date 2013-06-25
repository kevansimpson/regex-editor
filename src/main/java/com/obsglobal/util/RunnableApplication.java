package com.obsglobal.util;

import javax.swing.*;

/**
 * Todo: add javadoc
 */
public interface RunnableApplication<C extends JComponent> {
	String getTitle();

	C getViewComponent();
}
