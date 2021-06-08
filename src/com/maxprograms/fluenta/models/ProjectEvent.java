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

package com.maxprograms.fluenta.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.maxprograms.languages.Language;
import com.maxprograms.utils.TextUtils;

public class ProjectEvent implements Serializable {
	

	private static final long serialVersionUID = -8899002949209768303L;

	public static final String XLIFF_CREATED = "XLIFF Created"; //$NON-NLS-1$
	public static final String XLIFF_IMPORTED = "XLIFF Imported"; //$NON-NLS-1$
	public static final String XLIFF_CANCELLED = "XLIFF Cancelled"; //$NON-NLS-1$
	
	private Language language;
	private Date date;
	private String type;
	private int build;
	
	public ProjectEvent() {
		// empty constructor for GWT
	}
	
	public ProjectEvent(String type, Date date, Language language, int build) {
		this.type = type;
		this.date = date;
		this.language = language;
		this.build = build;
	}

	public Language getLanguage() {
		return language;
	}

	public Date getDate() {
		return date;
	}

	public String getType() {
		return type;
	}

	public int getBuild() {
		return build;
	}
	
	public String getDateString() {
		Calendar c =Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-" + TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2) + //$NON-NLS-1$ //$NON-NLS-2$
				" " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY) + 1, 2) + ":" + TextUtils.pad(c.get(Calendar.MINUTE), 2);	 //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getDescription(String event) {
		switch (event) {
		case XLIFF_CREATED: 
			return Messages.getString("ProjectEvent.0"); //$NON-NLS-1$
		case XLIFF_CANCELLED:
			return Messages.getString("ProjectEvent.1"); //$NON-NLS-1$
		case XLIFF_IMPORTED:
			return Messages.getString("ProjectEvent.2"); //$NON-NLS-1$
		default:
			return Messages.getString("ProjectEvent.3"); //$NON-NLS-1$
		}
	}
}
