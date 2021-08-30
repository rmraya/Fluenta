/*******************************************************************************
 * Copyright (c) 2015-2021 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.widgets;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import com.maxprograms.tmengine.ILogger;

public class LogPanel extends Composite implements ILogger {

	private Display display;
	private Label stage;
	private Label log;
	private String home = System.getProperty("user.home"); //$NON-NLS-1$
	private Vector<String> errors;

	public LogPanel(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		display = parent.getDisplay();

		stage = new Label(this, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		stage.setLayoutData(data);
		stage.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));

		log = new Label(this, SWT.NONE);
		log.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	public void log(String message) {
		if (message.startsWith(home)) {
			message = "~" + message.substring(home.length()); //$NON-NLS-1$
		}
		if (message.length() > 60) {
			message = message.substring(0, 6) + " ... " + message.substring(message.length() - 45); //$NON-NLS-1$
		}
		log.setText(message);
		display.update();
		display.sleep();
	}

	@Override
	public void setStage(String value) {
		stage.setText(value);
		display.update();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void logError(String error) {
		if (errors == null) {
			errors = new Vector<String>();
		}
		errors.add(error);
	}

	@Override
	public Vector<String> getErrors() {
		return errors;
	}

	@Override
	public void displayError(String string) {
		MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
		box.setMessage(string);
		box.open();
	}

	@Override
	public void displaySuccess(String string) {
		// do nothing
	}

}
