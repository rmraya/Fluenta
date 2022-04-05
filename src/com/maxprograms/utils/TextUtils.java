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

import java.util.Calendar;
import java.util.TimeZone;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class TextUtils {

	public static int geIndex(String[] items, String description) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(description)) {
				return i;
			}
		}
		return -1;
	}

	public static String normalise(String string) {
		return normalise(string, true);
	}

	public static String normalise(String string, boolean trim) {
		boolean repeat = false;
		String rs = ""; //$NON-NLS-1$
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			if (!Character.isSpaceChar(ch)) {
				if (ch != '\n') {
					rs = rs + ch;
				} else {
					rs = rs + " "; //$NON-NLS-1$
					repeat = true;
				}
			} else {
				rs = rs + " "; //$NON-NLS-1$
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		if (repeat) {
			return normalise(rs, trim);
		}
		if (trim) {
			return rs.trim();
		}
		return rs;
	}

	public static long getGMTtime(String TMXDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		try {
			int second = Integer.parseInt(TMXDate.substring(13, 15));
			int minute = Integer.parseInt(TMXDate.substring(11, 13));
			int hour = Integer.parseInt(TMXDate.substring(9, 11));
			int date = Integer.parseInt(TMXDate.substring(6, 8));
			int month = Integer.parseInt(TMXDate.substring(4, 6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0, 4));
			calendar.set(year, month, date, hour, minute, second);
			return calendar.getTimeInMillis();
		} catch (Exception e) {
			Logger logger = System.getLogger(TextUtils.class.getName());
			logger.log(Level.WARNING, "Error getting GMT time", e); //$NON-NLS-1$
			return 0l;
		}
	}

	public static String pad(int i, int length) {
		String res = "" + i; //$NON-NLS-1$
		while (res.length() < length) {
			res = "0" + res; //$NON-NLS-1$
		}
		return res;
	}
}
