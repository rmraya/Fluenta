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

package com.maxprograms.utils;

import java.util.List;

import com.maxprograms.converters.ILogger;



public class SimpleLogger implements ILogger{

	String error;
	private boolean success;
	private boolean verbose;
	
	public SimpleLogger(boolean verbose) {
		this.verbose = verbose;
	}
	
	@Override
	public void log(String message) {
		if (verbose) {
			System.out.println(message);
		}
	}

	@Override
	public void setStage(String stage) {
		if (verbose) {
			System.out.println(" ** " + stage + " **");  
		}
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void logError(String string) {
		if (verbose) {
			System.err.println(string);
		}
	}

	@Override
	public List<String> getErrors() {
		return null;
	}

	@Override
	public void displayError(String string) {
		if (verbose) {
			System.err.println(string);
		}
		error = string;
		success = false;
	}

	@Override
	public void displaySuccess(String string) {
		if (verbose) {
			System.out.println(string);
		}
		success = true;
	}

	public boolean getSuccess() {
		return success;
	}
	
	public String getError() {
		return error;
	}
}
