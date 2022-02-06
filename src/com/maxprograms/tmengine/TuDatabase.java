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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Vector;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.maxprograms.xml.Element;
import com.maxprograms.xml.XMLNode;

public class TuDatabase {

	private DB mapdb;
	private HTreeMap<Integer, Element> tumap;
	private Set<String> projects;
	private Set<String> subjects;
	private Set<String> customers;

	public TuDatabase(File folder) throws IOException {
		try {
			mapdb =  DBMaker.newFileDB(new File(folder, "tudata")).closeOnJvmShutdown().asyncWriteEnable().make(); //$NON-NLS-1$
			tumap = mapdb.getHashMap("tuvmap"); //$NON-NLS-1$
			projects = mapdb.getHashSet("projects"); //$NON-NLS-1$
			subjects = mapdb.getHashSet("subjects"); //$NON-NLS-1$
			customers = mapdb.getHashSet("customers"); //$NON-NLS-1$
		} catch (Error ioe) {
			throw new IOException(ioe.getMessage());
		}
	}

	synchronized public void commit() {
		mapdb.commit();
	}

	public void compact() {
		mapdb.compact();
	}
	
	synchronized public void close() {
		mapdb.close();
		mapdb = null;
	}

	synchronized public void store(String tuid, Element tu) {
		tu.removeChild("tuv"); //$NON-NLS-1$
		if (tu.getChildren().isEmpty()) {
			tu.setContent(new Vector<XMLNode>());
		}
		tumap.put(tuid.hashCode(),tu);
	}
	
	public Element getTu(String tuid) {
		return tumap.get(tuid.hashCode());
	}

	public void rollback() {
		mapdb.rollback();
	}

	public void storeSubject(String sub) {
		subjects.add(sub);
	}

	public void storeCustomer(String cust) {
		customers.add(cust);
	}

	public void storeProject(String proj) {
		projects.add(proj);
	}

	public Set<String> getCustomers() {
		return customers;
	}

	public Set<String> getProjects() {
		return projects;
	}

	public Set<String> getSubjects() {
		return subjects;
	}

	public Set<Integer> getKeys() {
		return tumap.keySet();
	}

	public Element getTu(Integer hashCode) {
		return tumap.get(hashCode);
	}

	public void remove(String tuid) {
		tumap.remove(tuid.hashCode());
	}
}
