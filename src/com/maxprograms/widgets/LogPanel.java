/*******************************************************************************
 * Copyright (c) 2015-2022 Maxprograms.
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

import java.util.List;
import java.util.Vector;

import com.maxprograms.tmengine.ILogger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

public class LogPanel extends Composite implements ILogger {

	private Display panelDisplay;
	private Label stage;
	private Label log;
	private String home = System.getProperty("user.home"); //$NON-NLS-1$
	private List<String> errors;

	public LogPanel(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		panelDisplay = parent.getDisplay();

		stage = new Label(this, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		stage.setLayoutData(data);
		stage.setBackground(panelDisplay.getSystemColor(SWT.COLOR_LIST_SELECTION));

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
		panelDisplay.update();
		panelDisplay.sleep();
	}

	@Override
	public void setStage(String value) {
		stage.setText(value);
		panelDisplay.update();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void logError(String error) {
		if (errors == null) {
			errors = new Vector<>();
		}
		errors.add(error);
	}

	@Override
	public List<String> getErrors() {
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
