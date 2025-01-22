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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;

public class Memory {

	public static final int VERSION = 1;

	private long id;
	private String name;
	private String description;
	private Date creationDate;
	private Date lastUpdate;
	private Language srcLanguage;

	public Memory(long id, String name, String description, Date creationDate, Date lastUpdate, Language srcLanguage) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.creationDate = creationDate;
		this.lastUpdate = lastUpdate;
		this.srcLanguage = srcLanguage;
	}

	public Memory(JSONObject json)
			throws JSONException, ParseException, IOException, SAXException, ParserConfigurationException {
		this.id = json.getLong("id");
		this.name = json.getString("name");
		this.description = json.getString("description");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		this.creationDate = df.parse(json.getString("creationDate"));
		this.lastUpdate = df.parse(json.getString("lastUpdate"));
		this.srcLanguage = LanguageUtils.getLanguage(json.getString("srcLanguage"));
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		json.put("id", id);
		json.put("name", name);
		json.put("description", description);
		json.put("creationDate", df.format(creationDate));
		json.put("lastUpdate", df.format(lastUpdate));
		json.put("srcLanguage", srcLanguage.getCode());
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

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Language getSrcLanguage() {
		return srcLanguage;
	}
}
