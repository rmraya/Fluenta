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

 package com.maxprograms.tmengine;

import java.util.Vector;

public interface ILogger {

	public void log(String message);
	public void setStage(String stage);
	public boolean isCancelled();
	public void logError(String error);
	public Vector<String> getErrors();
	public void displayError(String string);
	public void displaySuccess(String string);
}
