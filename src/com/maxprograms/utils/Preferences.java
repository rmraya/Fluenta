/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import org.json.JSONObject;

public class Preferences {

	private static Preferences instance;
	private File preferencesFile;
	private static File workDir;
	private JSONObject json;

	public static Preferences getInstance() throws IOException {
		if (instance == null) {
			instance = new Preferences();
		}
		return instance;
	}

	private Preferences() throws IOException {
		preferencesFile = new File(workDir, "preferences.json");
		if (!preferencesFile.exists()) {
			try (InputStream in = Preferences.class.getResourceAsStream("preferences.json")) {
				try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
					try (BufferedReader buffered = new BufferedReader(reader)) {
						StringBuilder sb = new StringBuilder();
						String line = "";
						while ((line = buffered.readLine()) != null) {
							if (!sb.isEmpty()) {
								sb.append("\n");
							}
							sb.append(line);
						}
						json = new JSONObject(sb.toString());
					}
				}
			}
			File projectsFolder = new File(workDir, "projects");
			File memoriesFolder = new File(workDir, "memories");
			File srxFolder = new File(workDir, "srx");
			File srxFile = new File(srxFolder, "default.srx");
			json.put("projectsFolder", projectsFolder.getAbsolutePath());
			json.put("memoriesFolder", memoriesFolder.getAbsolutePath());
			json.put("srxFile", srxFile.getAbsolutePath());
			savePreferences();
		}
		json = FileUtils.readJSON(preferencesFile);
	}

	private void savePreferences() throws IOException {
		try (FileOutputStream out = new FileOutputStream(preferencesFile)) {
			out.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
		}
	}

	public synchronized File getPreferencesFolder() throws IOException {
		if (workDir == null) {
			String home = System.getenv("FLUENTA_HOME");
			if (home != null) {
				workDir = new File(home);
			} else {
				String os = System.getProperty("os.name").toLowerCase();
				if (os.startsWith("mac")) {
					workDir = new File(System.getProperty("user.home") + "/Library/Application Support/Fluenta-5/");
				} else if (os.startsWith("windows")) {
					workDir = new File(System.getenv("AppData") + "\\Fluenta-5\\");
				} else {
					workDir = new File(System.getProperty("user.home") + "/.config/Fluenta-5/");
				}
			}
			if (!workDir.exists()) {
				Files.createDirectories(workDir.toPath());
			}
		}
		return workDir;
	}

	public synchronized void save(String group, String name, String value) throws IOException {
		if (!json.has(group)) {
			JSONObject jsonGroup = new JSONObject();
			json.put(group, jsonGroup);
		}
		json.getJSONObject(group).put(name, value);
		savePreferences();
	}

	public String get(String group, String name, String defaultValue) {
		if (json.has(group)) {
			JSONObject jsonGroup = json.getJSONObject(group);
			if (jsonGroup.has(name)) {
				return jsonGroup.getString(name);
			}
		}
		return defaultValue;
	}

	public synchronized void save(String group, JSONObject table) throws IOException {
		if (json.has(group)) {
			JSONObject old = json.getJSONObject(group);
			Iterator<String> it = table.keys();
			while (it.hasNext()) {
				String key = it.next();
				old.put(key, table.getString(key));
			}
			json.put(group, old);
		} else {
			json.put(group, table);
		}
		savePreferences();
	}

	public JSONObject get(String group) {
		if (json.has(group)) {
			return json.getJSONObject(group);
		}
		return new JSONObject();
	}

	public synchronized void remove(String group) throws IOException {
		if (json.has(group)) {
			json.remove(group);
			savePreferences();
		}
	}

	public File getProjectsFolder() throws IOException {
		File folder = new File(get("workDir", "projects",
				new File(getPreferencesFolder(), "projects").getAbsolutePath()));
		if (!folder.exists()) {
			Files.createDirectories(folder.toPath());
		}
		return folder;
	}

	public File getMemoriesFolder() throws IOException {
		File folder = new File(get("workDir", "memories",
				new File(getPreferencesFolder(), "memories").getAbsolutePath()));
		if (!folder.exists()) {
			Files.createDirectories(folder.toPath());
		}
		return folder;
	}

	public String getDefaultSRX() throws IOException {
		File srxFolder = new File(getPreferencesFolder(), "srx");
		File defaultSRX = new File(srxFolder, "default.srx");
		return get("workDir", "defaultSRX", defaultSRX.getAbsolutePath());
	}

	public String getCatalogFile() throws IOException {
		File catalogFolder = new File(getPreferencesFolder(), "catalog");
		File defaultCatalog = new File(catalogFolder, "catalog.xml");
		return defaultCatalog.getAbsolutePath();
	}

	public String getFiltersFolder() throws IOException {
		File filtersFolder = new File(getPreferencesFolder(), "xmlfilter");
		return filtersFolder.getAbsolutePath();
	}

	public String getApplicationLanguage() {
		return get("application", "language", "en");
	}

	public void setApplicationLanguage(String language) throws IOException {
		save("application", "language", language);
	}
}
