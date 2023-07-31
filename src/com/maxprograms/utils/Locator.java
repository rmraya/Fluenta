/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
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

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;

public class Locator {

	protected static Logger logger = System.getLogger(Locator.class.getName());

	private Locator() {
		// do not instantiate this class
	}

	public static void setLocation(Shell shell, String type) {
		try {
			JSONObject values = Preferences.getInstance().get(type);
			if (values.has("X") && values.has("Y")) {
				Point location = new Point(Integer.parseInt(values.getString("X")),
						Integer.parseInt(values.getString("Y")));
				shell.setLocation(location);
			}
		} catch (IOException ioe) {
			logger.log(Level.WARNING, Messages.getString("Locator.0"), ioe);
		}
	}

	public static void position(Shell shell, String type) {
		try {
			JSONObject values = Preferences.getInstance().get(type);
			if (values.has("X") && values.has("Y")) {
				Point location = new Point(Integer.parseInt(values.getString("X")),
						Integer.parseInt(values.getString("Y")));
				shell.setLocation(location);
			}
			if (values.has("Width") && values.has("Height")) {
				Point size = new Point(Integer.parseInt(values.getString("Width")),
						Integer.parseInt(values.getString("Height")));
				shell.setSize(size);
			}
		} catch (IOException ioe) {
			logger.log(Level.WARNING, Messages.getString("Locator.1"), ioe);
		}
	}

	public static void remember(Shell shell, String type) {
		try {
			JSONObject values = new JSONObject();
			values.put("X", "" + shell.getLocation().x);
			values.put("Y", "" + shell.getLocation().y);
			values.put("Width", "" + shell.getSize().x);
			values.put("Height", "" + shell.getSize().y);
			Preferences.getInstance().save(type, values);
		} catch (IOException ioe) {
			logger.log(Level.WARNING, Messages.getString("Locator.2"), ioe);
		}
	}

}
