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

package com.maxprograms.fluenta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.controllers.LocalController;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.SimpleLogger;

public class API {
	protected static void addProject(String jsonFile)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		File projectFile = new File(jsonFile);
		String string = "";
		try (FileInputStream input = new FileInputStream(projectFile)) {
			try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				try (BufferedReader buffer = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffer.readLine()) != null) {
						string = string + line + "\n";
					}
				}
			}
		}

		JSONObject jsonObject = new JSONObject(string);
		long id = jsonObject.getLong("id");
		String title = jsonObject.getString("title");
		String description = jsonObject.getString("description");
		String map = jsonObject.getString("map");
		String srcLang = jsonObject.getString("srcLang");
		JSONArray tgtArray = jsonObject.getJSONArray("tgtLang");
		String[] tgtLang = new String[tgtArray.length()];
		for (int i = 0; i < tgtArray.length(); i++) {
			tgtLang[i] = tgtArray.getString(i);
		}
		JSONArray memArray = null;
		long[] memIds = new long[0];
		try {
			memArray = jsonObject.getJSONArray("memories");
			memIds = new long[memArray.length()];
			for (int i = 0; i < memArray.length(); i++) {
				memIds[i] = memArray.getLong(i);
			}
		} catch (JSONException je) {
			// ignore
		}

		addProject(id, title, description, map, srcLang, tgtLang, memIds);
	}

	public static void addProject(long id, String title, String description, String map, String srcLang,
			String[] tgtLang, long[] memIds)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		Language srcLanguage = LanguageUtils.getLanguage(srcLang);

		List<Language> tgtLanguages = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			tgtLanguages.add(LanguageUtils.getLanguage(tgtLang[i]));
		}

		Memory mem = new Memory(id, title, description, System.getProperty("user.name"), new Date(), null, srcLanguage,
				tgtLanguages);
		LocalController controller = new LocalController();
		controller.createMemory(mem);

		List<Memory> memories = new Vector<>();
		memories.add(mem);
		for (int i = 0; i < memIds.length; i++) {
			memories.add(controller.getMemory(memIds[i]));
		}

		Project p = new Project(id, title, description, System.getProperty("user.name"), map, new Date(),
				Project.NEW, null, srcLanguage, tgtLanguages, memories);

		controller.createProject(p);
		controller.close();
	}

	public static String getProjects() throws IOException {
		LocalController controller = new LocalController();
		List<Project> projects = controller.getProjects();
		controller.close();

		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("projects", array);
		for (int i = 0; i < projects.size(); i++) {
			Project p = projects.get(i);
			JSONObject proj = new JSONObject();
			proj.put("id", p.getId());
			proj.put("title", p.getTitle());
			proj.put("description", p.getDescription());
			proj.put("owner", p.getOwner());
			proj.put("map", p.getMap());
			proj.put("creationDate", p.getCreationDateString());
			proj.put("status", p.getStatus());
			proj.put("srcLang", p.getSrcLanguage().getCode());
			JSONArray tgtArray = new JSONArray();
			JSONObject statusArray = new JSONObject();
			List<Language> tgtLanguages = p.getLanguages();
			Iterator<Language> lt = tgtLanguages.iterator();
			while (lt.hasNext()) {
				Language l = lt.next();
				tgtArray.put(l.getCode());
				statusArray.put(l.getCode(), p.getTargetStatus(l.getCode()));
			}
			proj.put("tgtLang", tgtArray);
			proj.put("targetStatus", statusArray);
			proj.put("lastUpdate", p.getLastUpdateString());
			JSONArray memArray = new JSONArray();
			List<Memory> mems = p.getMemories();
			Iterator<Memory> mt = mems.iterator();
			while (mt.hasNext()) {
				Memory m = mt.next();
				memArray.put(m.getId());
			}
			proj.put("memories", memArray);
			array.put(proj);
		}
		return result.toString(3);
	}

	public static String getMemories() throws IOException {
		LocalController controller = new LocalController();
		List<Memory> memories = controller.getMemories();
		controller.close();
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("memories", array);
		for (int i = 0; i < memories.size(); i++) {
			Memory m = memories.get(i);
			JSONObject mem = new JSONObject();
			mem.put("id", m.getId());
			mem.put("name", m.getName());
			mem.put("description", m.getDescription());
			mem.put("owner", m.getOwner());
			mem.put("creationDate", m.getCreationDateString());
			mem.put("lastUpdate", m.getLastUpdateString());
			mem.put("srcLang", m.getSrcLanguage().getCode());
			JSONArray tgtArray = new JSONArray();
			List<Language> langs = m.getTgtLanguages();
			Iterator<Language> it = langs.iterator();
			while (it.hasNext()) {
				Language l = it.next();
				tgtArray.put(l.getCode());
			}
			mem.put("tgtLang", tgtArray);
			array.put(mem);
		}
		return result.toString(3);
	}

	protected static void addMemory(String jsonFile) throws IOException {
		File memoryFile = new File(jsonFile);
		String string = "";
		try (FileInputStream input = new FileInputStream(memoryFile)) {
			try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				try (BufferedReader buffer = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffer.readLine()) != null) {
						string = string + line + "\n";
					}
				}
			}
		}

		JSONObject jsonObject = new JSONObject(string);
		long id = jsonObject.getLong("id");
		String title = jsonObject.getString("title");
		String description = jsonObject.getString("description");
		String srcLang = jsonObject.getString("srcLang");
		JSONArray tgtArray = jsonObject.getJSONArray("tgtLang");
		String[] tgtLang = new String[tgtArray.length()];
		for (int i = 0; i < tgtArray.length(); i++) {
			tgtLang[i] = tgtArray.getString(i);
		}

		addMemory(id, title, description, srcLang, tgtLang);
	}

	public static void addMemory(long id, String title, String description, String srcLang, String[] tgtLang)
			throws IOException {
		Language srcLanguage = LanguageUtils.getLanguage(srcLang);
		if (tgtLang == null || tgtLang.length == 0) {
			throw new IOException(Messages.getString("API.174"));
		}
		List<Language> tgtLanguages = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			tgtLanguages.add(LanguageUtils.getLanguage(tgtLang[i]));
		}

		Memory mem = new Memory(id, title, description, System.getProperty("user.name"), new Date(), null, srcLanguage,
				tgtLanguages);
		LocalController controller = new LocalController();
		controller.createMemory(mem);
		controller.close();
	}

	public static void importMemory(long id, String tmxFile, boolean verbose)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			controller.close();
			throw new IOException(Messages.getString("API.177"));
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.importTMX(memory, tmxFile, logger);
		controller.close();
		if (logger.getSuccess()) {
			return;
		}
		throw new IOException(logger.getError());
	}

	public static void exportMemory(long id, String tmxFile) throws Exception {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			controller.close();
			throw new Exception(Messages.getString("API.179"));
		}
		controller.exportTMX(memory, tmxFile);
		controller.close();
	}

	public static void generateXLIFF(long id, String xliffFolder, String[] tgtLang, boolean useICE, boolean useTM,
			boolean generateCount, boolean verbose, String ditaval, boolean useXliff20) throws IOException,
			ClassNotFoundException, SAXException, ParserConfigurationException, URISyntaxException, SQLException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			controller.close();
			throw new IOException(Messages.getString("API.181"));
		}
		File f = new File(xliffFolder);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (tgtLang == null || tgtLang.length == 0) {
			throw new IOException(Messages.getString("API.182"));
		}
		List<Language> langs = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			langs.add(LanguageUtils.getLanguage(tgtLang[i]));
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.generateXliff(project, xliffFolder, langs, useICE, useTM, generateCount, ditaval, useXliff20,
				logger);
		controller.close();
	}

	protected static void generateXLIFF(String jsonFile, boolean verbose) throws IOException, SAXException,
			ParserConfigurationException, URISyntaxException, ClassNotFoundException, SQLException {
		File projectFile = new File(jsonFile);
		String string = "";
		try (FileInputStream input = new FileInputStream(projectFile)) {
			try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				try (BufferedReader buffer = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffer.readLine()) != null) {
						string = string + line + "\n";
					}
				}
			}
		}

		JSONObject jsonObject = new JSONObject(string);
		long id = jsonObject.getLong("id");

		String xliffFolder = jsonObject.getString("xliffFolder");
		String ditaval = "";
		try {
			ditaval = jsonObject.getString("ditaval");
		} catch (JSONException jse) {
			// ignore
		}

		boolean useICE = jsonObject.getBoolean("useICE");
		boolean useTM = jsonObject.getBoolean("useTM");
		boolean generateCount = jsonObject.getBoolean("generateCount");
		boolean useXliff20 = jsonObject.getBoolean("useXLIFF20");

		JSONArray tgtArray = jsonObject.getJSONArray("tgtLang");
		String[] tgtLang = new String[tgtArray.length()];
		for (int i = 0; i < tgtArray.length(); i++) {
			tgtLang[i] = tgtArray.getString(i);
		}

		generateXLIFF(id, xliffFolder, tgtLang, useICE, useTM, generateCount, verbose, ditaval, useXliff20);
	}

	public static void importXLIFF(long id, String xliffFile, String outputFolder, boolean updateTM,
			boolean acceptUnapproved, boolean ignoreTagErrors, boolean cleanAttributes, boolean verbose)
			throws IOException, NumberFormatException, ClassNotFoundException, SAXException,
			ParserConfigurationException, SQLException, URISyntaxException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			controller.close();
			throw new IOException(Messages.getString("API.196"));
		}
		File f = new File(outputFolder);
		if (!f.exists()) {
			f.mkdirs();
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.importXliff(project, xliffFile, outputFolder, updateTM, acceptUnapproved, ignoreTagErrors,
				cleanAttributes, logger);
	}

	protected static void importXLIFF(String jsonFile, boolean verbose) throws IOException, NumberFormatException,
			ClassNotFoundException, SAXException, ParserConfigurationException, SQLException, URISyntaxException {
		File projectFile = new File(jsonFile);
		String string = "";
		try (FileInputStream input = new FileInputStream(projectFile)) {
			try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				try (BufferedReader buffer = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffer.readLine()) != null) {
						string = string + line + "\n";
					}
				}
			}
		}

		JSONObject jsonObject = new JSONObject(string);
		long id = jsonObject.getLong("id");

		String xliffFile = jsonObject.getString("xliffFile");
		String outputFolder = jsonObject.getString("outputFolder");
		boolean updateTM = jsonObject.getBoolean("updateTM");
		boolean acceptUnapproved = jsonObject.getBoolean("acceptUnapproved");
		boolean ignoreTagErrors = jsonObject.getBoolean("ignoreTagErrors");
		boolean cleanAttributes = jsonObject.getBoolean("cleanAttributes");
		importXLIFF(id, xliffFile, outputFolder, updateTM, acceptUnapproved, ignoreTagErrors, cleanAttributes, verbose);
	}

	public static void removeProject(long id) throws IOException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			controller.close();
			throw new IOException(Messages.getString("API.196"));
		}
		controller.removeProject(project);
		controller.close();
	}
}
