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

import com.maxprograms.xml.Element;

public class Match implements Comparable<Match> {
	Float number;
	Element match;
	private String creationdate;
	private String origin;
	
	public Match(Float n, Element e, String c, String o) {
		number = n;
		match = e;
		creationdate = c;
		origin = o;
	}
	
	public Element getMatch() {
		return match;
	}
	
	public float getNumber() {
		return number;
	}

	public String getCreationdate() {
		return creationdate;
	}

	public long getGMTtime() {
		if (creationdate.equals("")) { //$NON-NLS-1$
			return 0l;
		}
		return TextUtils.getGMTtime(creationdate);
	}
	
	@Override
	public int compareTo(Match o) {
		if (number < o.getNumber()) {
			return 1;
		}
		if (number > o.getNumber()) {
			return -1;
		}
		if (getGMTtime() < o.getGMTtime()) {
			return 1;
		}
		if (getGMTtime() > o.getGMTtime()) {
			return -1;
		}
		return origin.compareTo(o.getOrigin());
	}

	private String getOrigin() {
		return origin;
	}

}

