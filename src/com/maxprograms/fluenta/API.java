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

package com.maxprograms.fluenta;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
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
import com.maxprograms.utils.FileUtils;
import com.maxprograms.utils.SimpleLogger;

public class API {

	private API() {
		// do not instantiate this class
	}

	public static int importMemory(long id, String tmxFile) throws IOException, SQLException, SAXException,
			ParserConfigurationException, JSONException, ParseException, URISyntaxException {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			throw new IOException(Messages.getString("API.15"));
		}
		return controller.importTMX(memory, tmxFile);
	}

	public static void exportMemory(long id, String tmxFile) throws IOException, SQLException, SAXException,
			ParserConfigurationException, JSONException, ParseException, URISyntaxException {
		LocalController controller = new LocalController();
		Memory memory = controller.getMemory(id);
		if (memory == null) {
			throw new IOException(Messages.getString("API.15"));
		}
		controller.exportTMX(memory, tmxFile);
	}

	private static void generateXLIFF(long id, String xliffFolder, String[] tgtLang, boolean useICE, boolean useTM,
			boolean generateCount, boolean verbose, String ditaval, String version, boolean embedSkeleton,
			boolean modifiedFilesOnly, boolean ignoreTrackedChanges, boolean ignoreSVG, boolean paragraphSegmentation)
			throws IOException, SAXException, ParserConfigurationException, URISyntaxException, SQLException,
			JSONException, ParseException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			throw new IOException(Messages.getString("API.17"));
		}
		File f = new File(xliffFolder);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (tgtLang == null || tgtLang.length == 0) {
			throw new IOException(Messages.getString("API.18"));
		}
		List<Language> langs = new Vector<>();
		for (int i = 0; i < tgtLang.length; i++) {
			langs.add(LanguageUtils.getLanguage(tgtLang[i]));
		}
		SimpleLogger logger = new SimpleLogger(verbose);
		controller.generateXliff(project, xliffFolder, langs, useICE, useTM, generateCount, ditaval, version,
				embedSkeleton, modifiedFilesOnly, ignoreTrackedChanges, ignoreSVG, paragraphSegmentation, logger);
		controller.updateProject(project);
	}

	protected static void generateXLIFF(String jsonFile, boolean verbose) throws IOException, SAXException,
			ParserConfigurationException, URISyntaxException, SQLException, JSONException,
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
		String version = "2.1";
		if (jsonObject.has("version")) {
			version = jsonObject.getString("version");
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
		boolean ignoreSVG = false;
		if (jsonObject.has("ignoreSVG")) {
			ignoreSVG = jsonObject.getBoolean("ignoreSVG");
		}
		JSONArray tgtArray = jsonObject.getJSONArray("tgtLang");
		String[] tgtLang = new String[tgtArray.length()];
		for (int i = 0; i < tgtArray.length(); i++) {
			tgtLang[i] = tgtArray.getString(i);
		}

		generateXLIFF(id, xliffFolder, tgtLang, useICE, useTM, generateCount, verbose, ditaval, version,
				embedSkeleton, modifiedFilesOnly, ignoreTrackedChanges, ignoreSVG, paragraphSegmentation);
	}

	private static void importXLIFF(long id, String xliffFile, String outputFolder, boolean updateTM,
			boolean acceptUnapproved, boolean ignoreTagErrors, boolean verbose)
			throws IOException, NumberFormatException, SAXException, ParserConfigurationException, SQLException,
			URISyntaxException, JSONException, ParseException {
		LocalController controller = new LocalController();
		Project project = controller.getProject(id);
		if (project == null) {
			throw new IOException(Messages.getString("API.17"));
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
			SAXException, ParserConfigurationException, SQLException, URISyntaxException, JSONException,
			ParseException {
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
}
