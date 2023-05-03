/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.TextUtils;

public class Memory implements Serializable {

	private static final long serialVersionUID = -2993476438052822309L;

	public static final int VERSION = 1;

	private long id;
	private String name;
	private String description;
	private String owner;
	private Date creationDate;
	private Date lastUpdate;
	private Language srcLanguage;
	private List<Language> tgtLanguages;

	public Memory(long id, String name, String description, String owner, Date creationDate, Date lastUpdate,
			Language srcLanguage, List<Language> tgtLanguages) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.owner = owner;
		this.creationDate = creationDate;
		this.lastUpdate = lastUpdate;
		this.srcLanguage = srcLanguage;
		this.tgtLanguages = tgtLanguages;
	}

	public Memory(JSONObject json) throws JSONException, ParseException, IOException {
		this.id = json.getLong("id");
		this.name = json.getString("name");
		this.description = json.getString("description");
		this.owner = json.getString("owner");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		this.creationDate = df.parse(json.getString("creationDate"));
		this.lastUpdate = df.parse(json.getString("lastUpdate"));
		this.srcLanguage = LanguageUtils.getLanguage(json.getString("srcLanguage"));
		this.tgtLanguages = new Vector<>();
		JSONArray tgtLangs = json.getJSONArray("tgtLanguages");
		for (int i = 0; i < tgtLangs.length(); i++) {
			this.tgtLanguages.add(LanguageUtils.getLanguage(tgtLangs.getString(i)));
		}
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		json.put("id", id);
		json.put("name", name);
		json.put("description", description);
		json.put("owner", owner);
		json.put("creationDate", df.format(creationDate));
		json.put("lastUpdate", df.format(lastUpdate));
		json.put("srcLanguage", srcLanguage.getCode());
		JSONArray tgtLangs = new JSONArray();
		for (int i = 0; i < tgtLanguages.size(); i++) {
			tgtLangs.put(tgtLanguages.get(i).getCode());
		}
		json.put("tgtLanguages", tgtLangs);
		return json;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getCreationDateString() {
		Calendar c = Calendar.getInstance();
		c.setTime(creationDate);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-"
				+ TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2) + " " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY), 2)
				+ ":" + TextUtils.pad(c.get(Calendar.MINUTE), 2);
	}

	public String getLastUpdateString() {
		if (lastUpdate == null) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		c.setTime(lastUpdate);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-"
				+ TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2) + " " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY), 2)
				+ ":" + TextUtils.pad(c.get(Calendar.MINUTE), 2);
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Language getSrcLanguage() {
		return srcLanguage;
	}

	public void setSrcLanguage(Language srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	public List<Language> getTgtLanguages() {
		Collections.sort(tgtLanguages);
		return tgtLanguages;
	}

	public void setTgtLanguages(List<Language> tgtLanguages) {
		this.tgtLanguages = tgtLanguages;
	}
}
