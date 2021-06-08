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

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Hashtable;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class Preferences {

	private DB mapdb;
	private HTreeMap<String, Hashtable<String,String>> hashmap;
	private static Preferences instance;
	private static Hashtable<String, Preferences> instances;
	private static File workDir;
	
	public static Preferences getInstance(String file) throws IOException {
		if (instances == null) {
			instances = new Hashtable<>();
		}
		instance = instances.get(file);
		if (instance == null) {
			instance = new Preferences(file);
			instances.put(file, instance);
		}
		return instance;
	}
	
	private Preferences(String file) throws IOException {
		File out = new File(getPreferencesDir(), file);
		try {
			mapdb =  DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
		} catch (IOError ex) {
			if (out.exists()) {
				try {
					Files.delete(Paths.get(out.toURI()));
					File p = new File(getPreferencesDir(), file + ".p"); //$NON-NLS-1$
					if (p.exists()) {
						Files.delete(Paths.get(p.toURI()));
					}
					File t = new File(getPreferencesDir(), file + ".t"); //$NON-NLS-1$
					if (t.exists()) {
						Files.delete(Paths.get(t.toURI()));
					}
					mapdb =  DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
				} catch (IOError ex2) {
					throw new IOException(ex2.getMessage());
				}
			} else {
				throw new IOException(ex.getMessage());
			}			
		}
		hashmap = mapdb.getHashMap("preferences"); //$NON-NLS-1$
	}
	
	public static synchronized File getPreferencesDir() throws IOException {
		if (workDir == null) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("mac")) {
				workDir = new File(System.getProperty("user.home") + "/Library/Application Support/Fluenta/");
			} else if (os.startsWith("windows")) {
				workDir = new File(System.getenv("AppData") + "\\Fluenta\\");
			} else {
				workDir = new File(System.getProperty("user.home") + "/.config/Fluenta/");
			}
			if (!workDir.exists()) {
				Files.createDirectories(workDir.toPath());
			}
		}
		return workDir;
	}

	public synchronized void save(String group, String name, String value) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			g = new Hashtable<String, String>();
		}
		g.put(name, value);
		hashmap.put(group, g);
		mapdb.commit();
	}
	
	public String get(String group, String name, String defaultValue) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			return defaultValue;
		}
		String value = g.get(name);
		if ( value == null) {
			return defaultValue;
		} 		
		return value;
	}

	public synchronized void save(String group, Hashtable<String, String> table) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g != null) {
			Enumeration<String> keys = table.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				g.put(key, table.get(key));
			}
			hashmap.put(group, g);
		} else {
			hashmap.put(group, table);
		}
		mapdb.commit();
	}

	public Hashtable<String, String> get(String group) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			g = new Hashtable<>();
		}
		return g;
	}

	public synchronized void remove(String group) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g != null) {
			hashmap.remove(group);
			mapdb.commit();
		}
	}

	public void close() {
		mapdb.commit();
		mapdb.close();
	}
}
