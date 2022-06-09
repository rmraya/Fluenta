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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import org.json.JSONObject;

public class Preferences {

	private static Preferences instance;
	private static File preferencesFile;
	private static File workDir;
	JSONObject preferences;

	public static Preferences getInstance() throws IOException {
		if (instance == null) {
			instance = new Preferences();
		}
		return instance;
	}

	private Preferences() throws IOException {
		preferencesFile = new File(getPreferencesDir(), "preferences.json"); 
		if (!preferencesFile.exists()) {
			preferences = new JSONObject();
			savePreferences();
		}
		StringBuffer buffer = new StringBuffer();
		try (FileReader input = new FileReader(preferencesFile, StandardCharsets.UTF_8)) {
			try (BufferedReader reader = new BufferedReader(input)) {
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
			}
		}
		preferences = new JSONObject(buffer.toString());
	}

	private void savePreferences() throws IOException {
		try (FileOutputStream out = new FileOutputStream(preferencesFile)) {
			out.write(preferences.toString(2).getBytes(StandardCharsets.UTF_8));
		}
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

	public synchronized void save(String group, String name, String value) throws IOException {
		if (!preferences.has(group)) {
			JSONObject json = new JSONObject();
			preferences.put(group, json);
		}
		preferences.getJSONObject(group).put(name, value);
		savePreferences();
	}

	public String get(String group, String name, String defaultValue) {
		if (preferences.has(group)) {
			JSONObject json = preferences.getJSONObject(group);
			if (json.has(name)) {
				return json.getString(name);
			}
		}
		return defaultValue;
	}

	public synchronized void save(String group, JSONObject table) throws IOException {
		if (preferences.has(group)) {
			JSONObject old = preferences.getJSONObject(group);
			Iterator<String> it = table.keys();
			while (it.hasNext()) {
				String key = it.next();
				old.put(key, table.getString(key));
			}
			preferences.put(group, old);
		} else {
			preferences.put(group, table);
		}
		savePreferences();
	}

	public JSONObject get(String group) {
		if (preferences.has(group)) {
			return preferences.getJSONObject(group);
		}
		return new JSONObject();
	}

	public synchronized void remove(String group) throws IOException {
		if (preferences.has(group)) {
			preferences.remove(group);
			savePreferences();
		}
	}

}
