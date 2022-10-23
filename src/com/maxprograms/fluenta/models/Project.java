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

package com.maxprograms.fluenta.models;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.utils.TextUtils;

public class Project implements Serializable {

	public static final String NEW = "New";
	public static final String NEEDS_UPDATE = "Needs Update";
	public static final String IN_PROGRESS = "In Progress";
	public static final String COMPLETED = "Completed";
	private static final String UNTRANSLATED = "Untranslated";

	private static final long serialVersionUID = 6996995538736280348L;

	private long id;
	private String title;
	private String description;
	private String owner;
	private String map;
	private Date creationDate;
	private String status;
	private Date lastUpdate;
	private String srcLanguage;
	private List<String> tgtLanguages;
	private List<Long> memories;
	private String xliffFolder;
	private List<ProjectEvent> history;
	private Map<String, String> languageStatus;

	public Project(long id, String title, String description, String owner, String map, Date creationDate,
			String status, Date lastUpdate, String srcLanguage, List<String> tgtLanguages, List<Long> memories,
			List<ProjectEvent> history, Map<String, String> languageStatus) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.owner = owner;
		this.map = map;
		this.creationDate = creationDate;
		this.status = status;
		this.lastUpdate = lastUpdate;
		this.srcLanguage = srcLanguage;
		this.tgtLanguages = tgtLanguages;
		this.memories = memories;
		this.history = history;
		this.languageStatus = languageStatus;
		for (int i = 0; i < tgtLanguages.size(); i++) {
			String lang = tgtLanguages.get(i);
			if (!languageStatus.containsKey(lang)) {
				languageStatus.put(lang, UNTRANSLATED);
			}
		}
	}

	public Project(JSONObject json) throws JSONException, ParseException, IOException {
		this.id = json.getLong("id");
		this.title = json.getString("title");
		this.description = json.getString("description");
		this.owner = json.getString("owner");
		this.map = json.getString("map");
		DateFormat df = DateFormat.getDateTimeInstance();
		this.creationDate = df.parse(json.getString("creationDate"));
		this.status = json.getString("status");
		this.lastUpdate = df.parse(json.getString("lastUpdate"));
		this.srcLanguage = json.getString("srcLanguage");
		this.tgtLanguages = new Vector<>();
		JSONArray tgtLangs = json.getJSONArray("tgtLanguages");
		for (int i = 0; i < tgtLangs.length(); i++) {
			this.tgtLanguages.add(tgtLangs.getString(i));
		}
		this.memories = new Vector<>();
		JSONArray memArray = json.getJSONArray("memories");
		for (int i = 0; i < memArray.length(); i++) {
			this.memories.add(memArray.getLong(i));
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

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		DateFormat df = DateFormat.getDateTimeInstance();
		json.put("id", id);
		json.put("title", title);
		json.put("description", description);
		json.put("owner", owner);
		json.put("map", map);
		json.put("creationDate", df.format(creationDate));
		json.put("status", status);
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

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}

	public String getStatus() {
		Set<String> keys = languageStatus.keySet();
		Iterator<String> it = keys.iterator();
		boolean completed = true;
		boolean inprogress = false;
		boolean needsupdate = false;
		while (it.hasNext()) {
			String langStatus = languageStatus.get(it.next());
			if (langStatus.equals(IN_PROGRESS) || langStatus.equals(UNTRANSLATED)) {
				completed = false;
			}
			if (langStatus.equals(IN_PROGRESS)) {
				inprogress = true;
			}
			if (langStatus.equals(UNTRANSLATED)) {
				needsupdate = true;
			}
		}
		if (completed) {
			status = COMPLETED;
		}
		if (inprogress) {
			status = IN_PROGRESS;
			if (needsupdate) {
				status = NEEDS_UPDATE;
			}
		}
		return getStatusString();
	}

	private String getStatusString() {
		switch (status) {
			case NEW:
				return Messages.getString("Project.0");
			case NEEDS_UPDATE:
				return Messages.getString("Project.1");
			case IN_PROGRESS:
				return Messages.getString("Project.2");
			case COMPLETED:
				return Messages.getString("Project.3");
			case UNTRANSLATED:
				return Messages.getString("Project.4");
			default:
				return Messages.getString("Project.5");
		}
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public String getLastUpdateString() {
		if (lastUpdate == null) {
			return "";
		}
		return TextUtils.date2string(lastUpdate);
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<String> getLanguages() {
		return tgtLanguages;
	}

	public void setLanguages(List<String> languages) {
		this.tgtLanguages = languages;
	}

	public List<Long> getMemories() {
		return memories;
	}

	public void setMemories(List<Long> memories) {
		this.memories = memories;
	}

	public String getOwner() {
		return owner;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getCreationDateString() {
		return TextUtils.date2string(creationDate);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSrcLanguage() {
		return srcLanguage;
	}

	public void setSrcLanguage(String srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	public void setTgtLanguages(List<String> tgtLanguages) {
		this.tgtLanguages = tgtLanguages;
		for (int i = 0; i < tgtLanguages.size(); i++) {
			String l = tgtLanguages.get(i);
			if (!languageStatus.containsKey(l)) {
				languageStatus.put(l, UNTRANSLATED);
			}
		}
	}

	public void setXliffFolder(String value) {
		xliffFolder = value;
	}

	public String getXliffFolder() {
		return xliffFolder;
	}

	public List<ProjectEvent> getHistory() {
		return history;
	}

	public void setHistory(List<ProjectEvent> history) {
		this.history = history;
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

	public String getTargetStatus(String langCode) {
		switch (languageStatus.get(langCode)) {
			case NEW:
				return Messages.getString("Project.0");
			case NEEDS_UPDATE:
				return Messages.getString("Project.1");
			case IN_PROGRESS:
				return Messages.getString("Project.2");
			case COMPLETED:
				return Messages.getString("Project.3");
			case UNTRANSLATED:
				return Messages.getString("Project.4");
			default:
				return Messages.getString("Project.5");
		}
	}

	public void setLanguageStatus(String langCode, String status) {
		languageStatus.put(langCode, status);
	}
}
