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

package com.maxprograms.fluenta.models;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.converters.Utils;

public class Project {

	public static final String NEW = "0";
	public static final String IN_PROGRESS = "1";
	public static final String COMPLETED = "2";
	private static final String UNTRANSLATED = "3";
	private static final String TRANSLATED = "4";

	private long id;
	private String title;
	private String description;
	private String map;
	private Date creationDate;
	private Date lastUpdate;
	private String srcLanguage;
	private List<String> tgtLanguages;
	private List<Long> memories;
	private List<ProjectEvent> history;
	private Map<String, String> languageStatus;

	public Project(long id, String title, String description, String map, Date creationDate, Date lastUpdate,
			String srcLanguage, List<String> tgtLanguages, List<Long> memories, List<ProjectEvent> history,
			Map<String, String> languageStatus) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.map = map;
		this.creationDate = creationDate;
		this.lastUpdate = lastUpdate;
		this.srcLanguage = srcLanguage;
		this.tgtLanguages = tgtLanguages;
		this.memories = memories;
		this.history = history;
		this.languageStatus = languageStatus;
		for (int i = 0; i < tgtLanguages.size(); i++) {
			String lang = tgtLanguages.get(i);
			languageStatus.computeIfAbsent(lang, l -> UNTRANSLATED);
		}
	}

	public Project(JSONObject json) throws JSONException, IOException, ParseException {
		id = json.getLong("id");
		title = json.getString("title");
		description = json.getString("description");
		map = json.getString("map");
		File mapFile = new File(map);
		if (!mapFile.isAbsolute()) {
			map = Utils.getAbsolutePath(System.getProperty("user.home"), map);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		creationDate = df.parse(json.getString("creationDate"));
		lastUpdate = df.parse(json.getString("lastUpdate"));
		srcLanguage = json.getString("srcLanguage");
		tgtLanguages = new Vector<>();
		JSONArray tgtLangs = json.getJSONArray("tgtLanguages");
		for (int i = 0; i < tgtLangs.length(); i++) {
			tgtLanguages.add(tgtLangs.getString(i));
		}
		memories = new Vector<>();
		JSONArray memArray = json.getJSONArray("memories");
		for (int i = 0; i < memArray.length(); i++) {
			memories.add(memArray.getLong(i));
		}
		history = new Vector<>();
		JSONArray eventArray = json.getJSONArray("history");
		for (int i = 0; i < eventArray.length(); i++) {
			history.add(new ProjectEvent(eventArray.getJSONObject(i)));
		}
		languageStatus = new Hashtable<>();
		JSONObject langStatus = json.getJSONObject("languageStatus");
		Iterator<String> langs = langStatus.keys();
		while (langs.hasNext()) {
			String lang = langs.next();
			languageStatus.put(lang, langStatus.getString(lang));
		}
		for (int i = 0; i < tgtLanguages.size(); i++) {
			String l = tgtLanguages.get(i);
			if (!languageStatus.containsKey(l)) {
				languageStatus.put(l, UNTRANSLATED);
			}
		}
	}

	public JSONObject toJSON() throws JSONException, IOException {
		JSONObject json = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		json.put("id", id);
		json.put("title", title);
		json.put("description", description);
		json.put("map", Utils.getRelativePath(System.getProperty("user.home"), map));
		json.put("creationDate", df.format(creationDate));
		json.put("status", getStatus());
		json.put("lastUpdate", df.format(lastUpdate));
		json.put("srcLanguage", srcLanguage);
		JSONArray tgtArray = new JSONArray();
		for (int i = 0; i < tgtLanguages.size(); i++) {
			tgtArray.put(tgtLanguages.get(i));
		}
		json.put("tgtLanguages", tgtArray);
		JSONArray memsArray = new JSONArray();
		for (int i = 0; i < memories.size(); i++) {
			memsArray.put(memories.get(i));
		}
		json.put("memories", memsArray);
		JSONArray eventArray = new JSONArray();
		for (int i = 0; i < history.size(); i++) {
			eventArray.put(history.get(i).toJSON());
		}
		json.put("history", eventArray);
		JSONObject langStatus = new JSONObject();
		Set<String> langs = languageStatus.keySet();
		Iterator<String> it = langs.iterator();
		while (it.hasNext()) {
			String lang = it.next();
			langStatus.put(lang, languageStatus.get(lang));
		}
		json.put("languageStatus", langStatus);
		return json;
	}

	private String getStatus() {
		if (history.isEmpty()) {
			return NEW;
		}
		Map<String, Set<Integer>> builds = new Hashtable<>();
		for (int i = 0; i < history.size(); i++) {
			ProjectEvent event = history.get(i);
			builds.computeIfAbsent(event.getLanguage(), b -> new TreeSet<Integer>());
			Set<Integer> langBuilds = builds.get(event.getLanguage());
			int build = event.getBuild();
			if (event.getType().equals(ProjectEvent.XLIFF_CREATED)) {
				langBuilds.add(build);
			} else {
				if (langBuilds.contains(build)) {
					langBuilds.remove(build);
				}
			}
		}
		Set<String> langs = builds.keySet();
		Iterator<String> it = langs.iterator();
		while (it.hasNext()) {
			String lang = it.next();
			Set<Integer> langBuilds = builds.get(lang);
			if (!langBuilds.isEmpty()) {
				return IN_PROGRESS;
			}
		}
		return TRANSLATED;
	}

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getMap() {
		return map;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<String> getLanguages() {
		return tgtLanguages;
	}

	public List<Long> getMemories() {
		return memories;
	}

	public String getTitle() {
		return title;
	}

	public String getSrcLanguage() {
		return srcLanguage;
	}

	public List<ProjectEvent> getHistory() {
		return history;
	}

	public int getNextBuild(String langCode) {
		int count = 0;
		for (int i = 0; i < history.size(); i++) {
			ProjectEvent event = history.get(i);
			if (event.getLanguage().equals(langCode) && event.getType().equals(ProjectEvent.XLIFF_CREATED)) {
				count++;
			}
		}
		return count;
	}

	public void setLanguageStatus(String langCode, String status) {
		languageStatus.put(langCode, status);
	}
}
