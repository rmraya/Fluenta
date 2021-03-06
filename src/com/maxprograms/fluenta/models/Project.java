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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.maxprograms.languages.Language;
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
	private Language srcLanguage;
	private List<Language> tgtLanguages;
	private List<Memory> memories;
	private String xliffFolder;
	private List<ProjectEvent> history;
	private Map<String, String> languageStatus;

	public Project() {
		// empty constructor for GWT
		id = 0l;
		title = ""; 
		description = ""; 
		map = ""; 
		xliffFolder = ""; 
		creationDate = new Date();
		status = NEW;
		tgtLanguages = new Vector<>();
		memories = new Vector<>();
		history = new Vector<>();
		languageStatus = new Hashtable<>();
	}

	public Project(long id, String title, String description, String owner, String map, Date creationDate,
			String status, Date lastUpdate, Language srcLanguage, List<Language> tgtLanguages, List<Memory> memories) {
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
		history = new Vector<>();
		languageStatus = new Hashtable<>();
		for (int i = 0; i < tgtLanguages.size(); i++) {
			Language l = tgtLanguages.get(i);
			if (!languageStatus.containsKey(l.getCode())) {
				languageStatus.put(l.getCode(), UNTRANSLATED);
			}
		}
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
		Calendar c = Calendar.getInstance();
		c.setTime(lastUpdate);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-"  
				+ TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2)
				+ " " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY), 2) + ":" + TextUtils.pad(c.get(Calendar.MINUTE), 2);  
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<Language> getLanguages() {
		Collections.sort(tgtLanguages, new Comparator<Language>() {

			@Override
			public int compare(Language o1, Language o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}

		});
		return tgtLanguages;
	}

	public void setLanguages(List<Language> languages) {
		this.tgtLanguages = languages;
	}

	public List<Memory> getMemories() {
		return memories;
	}

	public void setMemories(List<Memory> memories) {
		this.memories = memories;
	}

	public String getOwner() {
		return owner;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getCreationDateString() {
		Calendar c = Calendar.getInstance();
		c.setTime(creationDate);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-"  
				+ TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2)
				+ " " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY), 2) + ":" + TextUtils.pad(c.get(Calendar.MINUTE), 2);  
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Language getSrcLanguage() {
		return srcLanguage;
	}

	public void setSrcLanguage(Language srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	public void setTgtLanguages(List<Language> tgtLanguages) {
		this.tgtLanguages = tgtLanguages;
		for (int i = 0; i < tgtLanguages.size(); i++) {
			Language l = tgtLanguages.get(i);
			if (!languageStatus.containsKey(l.getCode())) {
				languageStatus.put(l.getCode(), UNTRANSLATED);
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

	public void setHistory(Vector<ProjectEvent> history) {
		this.history = history;
	}

	public int getNextBuild(String langCode) {
		int count = 0;
		for (int i = 0; i < history.size(); i++) {
			ProjectEvent event = history.get(i);
			if (event.getLanguage().getCode().equals(langCode) && event.getType().equals(ProjectEvent.XLIFF_CREATED)) {
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
