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

import com.maxprograms.fluenta.views.ImportXliffDialog;
import com.maxprograms.tmengine.ILogger;

import org.eclipse.swt.widgets.Display;

public class AsyncLogger implements ILogger {

	protected ILogger parent;

	private List<String> errors;

	private Display display;

	public AsyncLogger(ILogger parent) {
		this.parent = parent;
		display = Display.getCurrent();
	}

	@Override
	public synchronized void log(String message) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				parent.log(message);
			}
		});
	}

	@Override
	public synchronized void setStage(String stage) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				parent.setStage(stage);
			}
		});
	}

	@Override
	public synchronized boolean isCancelled() {
		return parent.isCancelled();
	}

	@Override
	public synchronized void logError(String error) {
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
		parent.displayError(string);
	}

	@Override
	public void displaySuccess(String string) {
		parent.displaySuccess(string);
	}

	public void displayReport(String string, String report) {
		if (parent instanceof ImportXliffDialog dialog) {
			dialog.displayReport(string, report);
		} else {
			parent.displayError(string);
		}
	}

}
