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

package com.maxprograms.tmengine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class TU {
	private Set<String> langs;
	private Hashtable<String, String> props;
	private Vector<String> notes;
	private String creationdate;
	private String userid;
	Hashtable<String, Tuv> tuvs;
	
	public TU() {
		langs = Collections.synchronizedSet(new HashSet<>());
		props = new Hashtable<>();
		notes = new Vector<>();
		tuvs = new Hashtable<>();		
		userid = System.getProperty("user.name"); //$NON-NLS-1$
	}
	
	public void setData(TuData data) {
		langs = data.getLangs();
		props = data.getProps();
		notes = data.getNotes();
		creationdate = data.getCreationDate();
		userid = data.getUser();
	}

	public void setProps(Hashtable<String,String> values) {
		props = values;
		if (creationdate != null && props != null) {
			props.put("creationdate", creationdate); //$NON-NLS-1$
		}
	}
	
	public String getUser() {
		return userid;
	}
	
	public String getCreationDate() {
		return creationdate;
	}
	
	public void setCreationDate(String value) {
		creationdate = value;
		if (props != null) {
			props.put("creationdate", value); //$NON-NLS-1$
		}
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

	public void addTuv(String lang, Tuv tuv) {
		langs.add(lang);
		tuvs.put(lang, tuv);
	}

	public void setProperty(String name, String value) {
		props.put(name, value);
	}

	public void setLangs(Set<String> set) {
		langs = set;
	}

	public void setTuvs(Hashtable<String, Tuv> values) {
		tuvs = values;
	}

	public Tuv getTuv(String lang) {
		return tuvs.get(lang);
	}

	public String getProperty(String name) {
		return props.get(name);
	}

	public Hashtable<String, Tuv> getTuvs() {
		return tuvs;
	}
	
	public void setNotes(Vector<String> vector) {
		notes = vector;
	}
}
