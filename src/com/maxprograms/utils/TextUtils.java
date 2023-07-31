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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class TextUtils {

	private TextUtils() {
		// do not instantiate this class
	}

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
		StringBuilder result = new StringBuilder();
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			if (!Character.isSpaceChar(ch)) {
				if (ch != '\n') {
					result.append(ch);
				} else {
					result.append(' ');
					repeat = true;
				}
			} else {
				result.append(' ');
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		if (repeat) {
			return normalise(result.toString(), trim);
		}
		if (trim) {
			return result.toString().trim();
		}
		return result.toString();
	}

	public static long getGMTtime(String tmxDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try {
			int second = Integer.parseInt(tmxDate.substring(13, 15));
			int minute = Integer.parseInt(tmxDate.substring(11, 13));
			int hour = Integer.parseInt(tmxDate.substring(9, 11));
			int date = Integer.parseInt(tmxDate.substring(6, 8));
			int month = Integer.parseInt(tmxDate.substring(4, 6)) - 1;
			int year = Integer.parseInt(tmxDate.substring(0, 4));
			calendar.set(year, month, date, hour, minute, second);
			return calendar.getTimeInMillis();
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			Logger logger = System.getLogger(TextUtils.class.getName());
			logger.log(Level.WARNING, Messages.getString("TextUtils.0"), e);
			return 0l;
		}
	}

	public static String pad(int i, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append(i);
		while (sb.length() < length) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}

	public static String date2string(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR) + "-" + pad(c.get(Calendar.MONTH) + 1, 2) + "-"
				+ pad(c.get(Calendar.DAY_OF_MONTH), 2) + " " + pad(c.get(Calendar.HOUR_OF_DAY), 2) + ":"
				+ pad(c.get(Calendar.MINUTE), 2);
	}

}
