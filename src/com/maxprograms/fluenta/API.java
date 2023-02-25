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

package com.maxprograms.fluenta;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
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
import com.maxprograms.fluenta.models.ProjectEvent;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.FileUtils;
import com.maxprograms.utils.SimpleLogger;

public class API {

	private API() {
		// do not instantiate this class
	}

	protected static void addProject(String jsonFile)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException,
			JSONException, ParseException {
		File projectFile = new File(jsonFile);
		JSONObject jsonObject = FileUtils.readJSON(projectFile);
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
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException,
			JSONException, ParseException {
		List<String> tgtCodes = Arrays.asList(tgtLang);
		LocalController controller = new LocalController();
		List<Long> memories = new Vector<>();
		for (int i = 0; i < memIds.length; i++) {
			memories.add(memIds[i]);
		}
		Project project = new Project(id, title, description, System.getProperty("user.name"), map, new Date(),
				Project.NEW, new Date(), srcLang, tgtCodes, memories, new Vector<ProjectEvent>(),
				new Hashtable<String, String>());
		project.getMemories().add(id);
		controller.createProject(project);
	}

	public static String getProjects() throws IOException, JSONException, ParseException {
		LocalController controller = new LocalController();
		List<Project> projects = controller.getProjects();

		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("projects", array);
		for (int i = 0; i < projects.size(); i++) {
			array.put(projects.get(i).toJSON());
		}
		return result.toString(3);
	}

	public static String getMemories() throws IOException, JSONException, ParseException {
		LocalController controller = new LocalController();
		List<Memory> memories = controller.getMemories();
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		result.put("memories", array);
		for (int i = 0; i < memories.size(); i++) {
			array.put(memories.get(i).toJSON());
		}
		return result.toString(3);
	}

	protected static void addMemory(String jsonFile) throws IOException {
		File memoryFile = new File(jsonFile);
		JSONObject jsonObject = FileUtils.readJSON(memoryFile);
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
			throw new IOException("Missing target languages");
		}
		List<Language> tgtLanguages = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			tgtLanguages.add(LanguageUtils.getLanguage(tgtLang[i]));
		}

		Memory mem = new Memory(id, title, description, System.getProperty("user.name"), new Date(), new Date(),
				srcLanguage, tgtLanguages);
		LocalController controller = new LocalController();
		controller.createMemory(mem);
	}

	public static int importMemory(long id, String tmxFile) throws IOException, ClassNotFoundException, SQLException,
			SAXException, ParserConfigurationException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			throw new IOException("Unknown memory");
		}
		return controller.importTMX(memory, tmxFile);
	}

	public static void exportMemory(long id, String tmxFile) throws IOException, ClassNotFoundException, SQLException,
			SAXException, ParserConfigurationException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			throw new IOException("Unknown memory");
		}
		controller.exportTMX(memory, tmxFile);
	}

	public static void generateXLIFF(long id, String xliffFolder, String[] tgtLang, boolean useICE, boolean useTM,
			boolean generateCount, boolean verbose, String ditaval, boolean useXliff20, boolean embedSkeleton,
			boolean modifiedFilesOnly, boolean ignoreTrackedChanges, boolean paragraphSegmentation)
			throws IOException, ClassNotFoundException, SAXException, ParserConfigurationException, URISyntaxException,
			SQLException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			throw new IOException("Unknown project");
		}
		File f = new File(xliffFolder);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (tgtLang == null || tgtLang.length == 0) {
			throw new IOException("Missing target languages");
		}
		List<Language> langs = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			langs.add(LanguageUtils.getLanguage(tgtLang[i]));
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.generateXliff(project, xliffFolder, langs, useICE, useTM, generateCount, ditaval, useXliff20,
				embedSkeleton, modifiedFilesOnly, ignoreTrackedChanges, paragraphSegmentation, logger);
	}

	protected static void generateXLIFF(String jsonFile, boolean verbose) throws IOException, SAXException,
			ParserConfigurationException, URISyntaxException, ClassNotFoundException, SQLException, JSONException,
			ParseException {
		File projectFile = new File(jsonFile);
		JSONObject jsonObject = FileUtils.readJSON(projectFile);
		long id = jsonObject.getLong("id");
		String xliffFolder = jsonObject.getString("xliffFolder");
		String ditaval = jsonObject.has("ditaval") ? jsonObject.getString("ditaval") : "";
		boolean useICE = false;
		if (jsonObject.has("useICE")) {
			useICE = jsonObject.getBoolean("useICE");
		}
		boolean useTM = false;
		if (jsonObject.has("useTM")) {
			useTM = jsonObject.getBoolean("useTM");
		}
		boolean generateCount = false;
		if (jsonObject.has("generateCount")) {
			generateCount = jsonObject.getBoolean("generateCount");
		}
		boolean useXliff20 = false;
		if (jsonObject.has("useXLIFF20")) {
			useXliff20 = jsonObject.getBoolean("useXLIFF20");
		}
		boolean embedSkeleton = false;
		if (jsonObject.has("embedSkeleton")) {
			embedSkeleton = jsonObject.getBoolean("embedSkeleton");
		}
		boolean paragraphSegmentation = false;
		if (jsonObject.has("paragraph")) {
			paragraphSegmentation = jsonObject.getBoolean("paragraph");
		}
		boolean modifiedFilesOnly = false;
		if (jsonObject.has("modifiedFilesOnly")) {
			modifiedFilesOnly = jsonObject.getBoolean("modifiedFilesOnly");
		}
		boolean ignoreTrackedChanges = false;
		if (jsonObject.has("ignoreTrackedChanges")) {
			ignoreTrackedChanges = jsonObject.getBoolean("ignoreTrackedChanges");
		}
		JSONArray tgtArray = jsonObject.getJSONArray("tgtLang");
		String[] tgtLang = new String[tgtArray.length()];
		for (int i = 0; i < tgtArray.length(); i++) {
			tgtLang[i] = tgtArray.getString(i);
		}

		generateXLIFF(id, xliffFolder, tgtLang, useICE, useTM, generateCount, verbose, ditaval, useXliff20,
				embedSkeleton, modifiedFilesOnly, ignoreTrackedChanges, paragraphSegmentation);
	}

	public static void importXLIFF(long id, String xliffFile, String outputFolder, boolean updateTM,
			boolean acceptUnapproved, boolean ignoreTagErrors, boolean verbose)
			throws IOException, NumberFormatException, ClassNotFoundException, SAXException,
			ParserConfigurationException, SQLException, URISyntaxException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			throw new IOException("Unknown project");
		}
		File f = new File(outputFolder);
		if (!f.exists()) {
			f.mkdirs();
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.importXliff(project, xliffFile, outputFolder, updateTM, acceptUnapproved, ignoreTagErrors,
				logger);
	}

	protected static void importXLIFF(String jsonFile, boolean verbose) throws IOException, NumberFormatException,
			ClassNotFoundException, SAXException, ParserConfigurationException, SQLException, URISyntaxException,
			JSONException, ParseException {
		File projectFile = new File(jsonFile);
		JSONObject jsonObject = FileUtils.readJSON(projectFile);
		long id = jsonObject.getLong("id");

		String xliffFile = jsonObject.getString("xliffFile");
		String outputFolder = jsonObject.getString("outputFolder");
		boolean updateTM = jsonObject.getBoolean("updateTM");
		boolean acceptUnapproved = false;
		if (jsonObject.has("acceptUnapproved")) {
			acceptUnapproved = jsonObject.getBoolean("acceptUnapproved");
		}
		boolean ignoreTagErrors = false;
		if (jsonObject.has("ignoreTagErrors")) {
			ignoreTagErrors = jsonObject.getBoolean("ignoreTagErrors");
		}
		importXLIFF(id, xliffFile, outputFolder, updateTM, acceptUnapproved, ignoreTagErrors, verbose);
	}

	public static void removeProject(long id) throws IOException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			throw new IOException("Unknown project");
		}
		controller.removeProject(project);
	}
}