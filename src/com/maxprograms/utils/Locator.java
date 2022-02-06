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

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;

public class Locator {
	
	protected static final Logger LOGGER = System.getLogger(Locator.class.getName());
	
	public static void setLocation(Shell shell, String type) {
		try {
			JSONObject values = Preferences.getInstance().get(type);
			if (values.has("X") && values.has("Y")) { //$NON-NLS-1$ //$NON-NLS-2$
				Point location = new Point(Integer.parseInt(values.getString("X")), Integer.parseInt(values.getString("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
			}
		} catch (IOException ioe){
			LOGGER.log(Level.WARNING, "Error setting location", ioe); //$NON-NLS-1$
		}
	}

	public static void position(Shell shell, String type) {
		try {
			JSONObject values = Preferences.getInstance().get(type);
			if (values.has("X") && values.has("Y")) { //$NON-NLS-1$ //$NON-NLS-2$
				Point location = new Point(Integer.parseInt(values.getString("X")), Integer.parseInt(values.getString("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
			}
			if (values.has("Width") && values.has("Height")) { //$NON-NLS-1$ //$NON-NLS-2$
				Point size = new Point(Integer.parseInt(values.getString("Width")), Integer.parseInt(values.getString("Height"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setSize(size);
			}
		} catch (IOException ioe){
			LOGGER.log(Level.WARNING, "Error setting position", ioe); //$NON-NLS-1$
		}
	}

	public static void remember(Shell shell, String type) {
		try {
			JSONObject values = new JSONObject();
			values.put("X", "" + shell.getLocation().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Y", "" + shell.getLocation().y); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Width", "" + shell.getSize().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Height", "" + shell.getSize().y); //$NON-NLS-1$ //$NON-NLS-2$
			Preferences.getInstance().save(type, values);
		} catch (IOException ioe){
			LOGGER.log(Level.WARNING, "Error saving location", ioe); //$NON-NLS-1$
		}
	}

}
