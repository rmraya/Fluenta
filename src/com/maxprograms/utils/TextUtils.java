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

package com.maxprograms.utils;

import java.util.Calendar;
import java.util.TimeZone;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;


public class TextUtils {

	public static int geIndex(String[] items, String description) {
		for (int i=0 ; i<items.length ; i++) {
			if (items[i].equals(description)) {
				return i;
			}
		}
		return -1;
	}

	public static String cleanString(String input) {
		input = input.replaceAll("&","&amp;");  //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("<","&lt;");  //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll(">","&gt;" );  //$NON-NLS-1$ //$NON-NLS-2$
		return validChars(input);
	} // end cleanString

	public static String validChars(String input) {
		// Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
		// [#x10000-#x10FFFF]
		// Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
		//
		StringBuffer buffer = new StringBuffer();
		char c;
		int length = input.length();
		for (int i = 0; i < length; i++) {
			c = input.charAt(i);
			if ( c == '\t' || c == '\n' || c == '\r'
					|| c >= '\u0020' && c <= '\uD7DF' 
					|| c >= '\uE000' && c <= '\uFFFD' )
			{
				// normal character
				buffer.append(c);
			} else  if   (c >= '\u007F' && c <= '\u0084'
					|| c >= '\u0086' && c <= '\u009F' 
					|| c >= '\uFDD0' && c <= '\uFDDF') 
			{
				// Control character
				buffer.append("&#x" + Integer.toHexString(c) + ";");  //$NON-NLS-1$ //$NON-NLS-2$
			} else if (c >= '\uDC00' && c <= '\uDFFF' || c >= '\uD800' && c <= '\uDBFF') {
				// Multiplane character
				buffer.append(input.substring(i,i+1));
			} 
		}    
		return buffer.toString();
	}
	
	public static String getTMXDate() {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		String sec =
			(calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.SECOND);
		String min =
			(calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.MINUTE);
		String hour =
			(calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday =
			(calendar.get(Calendar.DATE) < 10 ? "0" : "") + calendar.get(Calendar.DATE); //$NON-NLS-1$ //$NON-NLS-2$
		String mon =
			(calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}

	public static String normalise(String string) {
        return normalise(string,true);
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
			if (repeat == true) {
				return normalise(rs, trim);
			}
			if (trim) {
				return rs.trim();
			}
			return rs;
		}

	public static long getGMTtime(String TMXDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		try{
			int second = Integer.parseInt(TMXDate.substring(13,15));
			int minute = Integer.parseInt(TMXDate.substring(11,13));
			int hour = Integer.parseInt(TMXDate.substring(9,11));		
			int date = Integer.parseInt(TMXDate.substring(6,8));		
			int month = Integer.parseInt(TMXDate.substring(4,6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0,4));		
			calendar.set(year, month, date, hour, minute, second);
			return calendar.getTimeInMillis();
		} catch (Exception e) {
			Logger logger = System.getLogger(TextUtils.class.getName());
			logger.log(Level.WARNING, "Error getting GMT time", e);
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
