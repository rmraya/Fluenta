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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class TuData implements Serializable {

	private static final long serialVersionUID = -4527612217830153690L;
	private Set<String> langs;
	private Hashtable<String, String> props;
	private Vector<String> notes;
	private String creationdate;
	private String userid;

	public TuData(String userid, String creationdate, Set<String> langs, Hashtable<String,String> props, Vector<String> notes) {
		this.userid = userid;
		this.creationdate = creationdate;
		this.langs = langs;
		this.props = props;
		this.notes = notes;
	}
		
	public String getUser() {
		return userid;
	}
	
	public String getCreationDate() {
		return creationdate;
	}
	
	public Set<String> getLangs() {
		return langs;
	}
	
	public Hashtable<String,String> getProps() {
		return props;
	}
	
	public Vector<String> getNotes() {
		return notes;
	}
}
