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

package com.maxprograms.fluenta.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.maxprograms.converters.Convert;
import com.maxprograms.converters.EncodingResolver;
import com.maxprograms.converters.FileFormats;
import com.maxprograms.converters.ILogger;
import com.maxprograms.converters.TmxExporter;
import com.maxprograms.converters.Utils;
import com.maxprograms.converters.ditamap.DitaMap2Xliff;
import com.maxprograms.converters.ditamap.Xliff2DitaMap;
import com.maxprograms.converters.html.Xliff2Html;
import com.maxprograms.converters.idml.Xliff2Idml;
import com.maxprograms.converters.javaproperties.Xliff2Properties;
import com.maxprograms.converters.javascript.Xliff2jscript;
import com.maxprograms.converters.json.Xliff2json;
import com.maxprograms.converters.mif.Xliff2Mif;
import com.maxprograms.converters.office.Xliff2Office;
import com.maxprograms.converters.php.Xliff2Php;
import com.maxprograms.converters.plaintext.Xliff2Text;
import com.maxprograms.converters.po.Xliff2Po;
import com.maxprograms.converters.rc.Xliff2Rc;
import com.maxprograms.converters.resx.Xliff2Resx;
import com.maxprograms.converters.sdlppx.Xliff2Sdlrpx;
import com.maxprograms.converters.sdlxliff.Xliff2Sdl;
import com.maxprograms.converters.srt.Xliff2Srt;
import com.maxprograms.converters.ts.Xliff2Ts;
import com.maxprograms.converters.txlf.Xliff2Txlf;
import com.maxprograms.converters.txml.Xliff2Txml;
import com.maxprograms.converters.wpml.Xliff2Wpml;
import com.maxprograms.converters.xliff.FromOpenXliff;
import com.maxprograms.converters.xml.Xliff2Xml;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.fluenta.models.ProjectEvent;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.stats.RepetitionAnalysis;
import com.maxprograms.swordfish.tm.InternalDatabase;
import com.maxprograms.swordfish.tm.Match;
import com.maxprograms.swordfish.tm.MatchQuality;
import com.maxprograms.swordfish.tm.TMUtils;
import com.maxprograms.utils.FileUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.widgets.AsyncLogger;
import com.maxprograms.xliff2.FromXliff2;
import com.maxprograms.xliff2.ToXliff2;
import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.Indenter;
import com.maxprograms.xml.PI;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLOutputter;

public class LocalController {

	private Map<String, String> validCtypes;
	private Map<String, String> phCtypes;

	private ProjectsManager projectsManager;
	private MemoriesManager memoriesManager;
	private static double penalty = 1;

	public List<Project> getProjects() throws IOException, JSONException, ParseException {
		if (projectsManager == null) {
			Preferences preferences = Preferences.getInstance();
			projectsManager = new ProjectsManager(preferences.getProjectsFolder());
		}
		return projectsManager.getProjects();
	}

	public void createProject(Project project)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		if (projectsManager == null) {
			Preferences preferences = Preferences.getInstance();
			projectsManager = new ProjectsManager(preferences.getProjectsFolder());
		}
		projectsManager.add(project);
		if (memoriesManager == null) {
			Preferences preferences = Preferences.getInstance();
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		Language sourceLang = LanguageUtils.getLanguage(project.getSrcLanguage());
		List<String> langsList = project.getLanguages();
		List<Language> targetLangs = new Vector<>();
		for (int i = 0; i < langsList.size(); i++) {
			targetLangs.add(LanguageUtils.getLanguage(langsList.get(i)));
		}
		Memory memory = new Memory(project.getId(), project.getTitle(), project.getDescription(), project.getOwner(),
				new Date(), new Date(), sourceLang, targetLangs);
		memoriesManager.add(memory);
	}

	public void createMemory(Memory memory) throws IOException {
		if (memoriesManager == null) {
			Preferences preferences = Preferences.getInstance();
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		memoriesManager.add(memory);
	}

	public List<Memory> getMemories() throws IOException, JSONException, ParseException {
		if (memoriesManager == null) {
			Preferences preferences = Preferences.getInstance();
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		return memoriesManager.getMemories();
	}

	public void updateProject(Project project) throws JSONException, IOException, ParseException {
		if (projectsManager == null) {
			Preferences preferences = Preferences.getInstance();
			projectsManager = new ProjectsManager(preferences.getProjectsFolder());
		}
		project.setLastUpdate(new Date());
		projectsManager.update(project);
	}

	public InternalDatabase getTMEngine(long memoryId)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		Preferences preferences = Preferences.getInstance();
		return new InternalDatabase("" + memoryId, preferences.getMemoriesFolder().getAbsolutePath());
	}

	public void generateXliff(Project project, String xliffFolder, List<Language> tgtLangs, boolean useICE,
			boolean useTM, boolean generateCount, String ditavalFile, boolean useXliff20, boolean embedSkeleton,
			ILogger logger)
			throws IOException, SAXException, ParserConfigurationException, URISyntaxException, ClassNotFoundException,
			SQLException, JSONException, ParseException {

		Map<String, String> params = new Hashtable<>();
		params.put("source", project.getMap());
		File map = new File(project.getMap());
		String name = map.getName();
		File folder = new File(xliffFolder);
		File xliffFile = new File(folder, name + ".xlf");
		params.put("xliff", xliffFile.getAbsolutePath());
		if (ditavalFile != null && !ditavalFile.isEmpty()) {
			params.put("ditaval", ditavalFile);
		}
		File skldir;
		Preferences preferences = Preferences.getInstance();
		skldir = new File(preferences.getProjectsFolder(), "" + project.getId());
		if (!skldir.exists()) {
			skldir.mkdirs();
		}
		boolean translateComments = preferences.get("XMLOptions", "TranslateComments", "No")
				.equalsIgnoreCase("Yes");
		File skl;
		skl = File.createTempFile("temp", ".skl", skldir);
		params.put("skeleton", skl.getAbsolutePath());
		params.put("catalog", preferences.getCatalogFile());
		params.put("customer", "");
		params.put("subject", "");
		params.put("project", project.getTitle());
		params.put("srcLang", project.getSrcLanguage());
		params.put("tgtLang", project.getSrcLanguage()); // use src language in master
		params.put("srcEncoding", EncodingResolver.getEncoding(project.getMap(), FileFormats.DITA).name());
		params.put("paragraph", "no");
		params.put("format", FileFormats.DITA);
		params.put("srxFile", preferences.getDefaultSRX());
		params.put("translateComments", translateComments ? "yes" : "no");
		params.put("xmlfilter", preferences.getFiltersFolder());
		logger.setStage("Generating Master XLIFF");

		DitaMap2Xliff.setDataLogger(logger);
		List<String> result = DitaMap2Xliff.run(params);
		if (!result.get(0).equals(Constants.SUCCESS)) {
			throw new IOException(result.get(1));
		}
		if (embedSkeleton) {
			logger.setStage("Embedding Skeletons");
			logger.log(xliffFile.getAbsolutePath());
			result = Convert.addSkeleton(xliffFile.getAbsolutePath(), Preferences.getInstance().getCatalogFile());
			if (!result.get(0).equals(Constants.SUCCESS)) {
				throw new IOException(result.get(1));
			}
		}
		makeFilesRelative(xliffFile);
		logger.setStage("Writing Target XLIFF Files");
		MessageFormat mf = new MessageFormat("Target Language: {0}");
		for (int i = 0; i < tgtLangs.size(); i++) {
			logger.log(
					mf.format(new String[] {
							LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
			String newName = getName(map.getName(), tgtLangs.get(i).getCode());
			File newFile = new File(folder, newName);
			Files.copy(xliffFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			changeTargetLanguage(newFile, tgtLangs.get(i).getCode(), project);
			int build = project.getNextBuild(tgtLangs.get(i).getCode());
			project.getHistory()
					.add(new ProjectEvent(ProjectEvent.XLIFF_CREATED, new Date(), tgtLangs.get(i).getCode(),
							build));
			project.setLanguageStatus(tgtLangs.get(i).getCode(), Project.IN_PROGRESS);
			updateProject(project);
		}
		Files.deleteIfExists(xliffFile.toPath());
		if (useICE) {
			MessageFormat icem = new MessageFormat("Applying ICE Matches - {0}");
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(icem.format(
						new String[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
				logger.log("Reading Previous XLIFF file...");
				String newName = getName(map.getName(), tgtLangs.get(i).getCode());
				File xliff = new File(folder, newName);
				File previousBuild = getPreviousBuild(project, tgtLangs.get(i).getCode());
				if (previousBuild != null) {
					leverage(xliff, previousBuild, logger);
				}
			}
		}
		if (useTM) {
			MessageFormat mftm = new MessageFormat("Applying TM - {0}");
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(mftm.format(
						new String[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
				logger.log("Reading XLIFF file...");
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				SAXBuilder builder = new SAXBuilder();
				builder.setEntityResolver(new Catalog(Preferences.getInstance().getCatalogFile()));
				Document doc1 = builder.build(targetXliff);
				Element root1 = doc1.getRootElement();
				Element firstFile = root1.getChild("file");
				if (firstFile == null) {
					logger.displayError("Empty XLIFF, check your DITA customizations");
					return;
				}
				String sourceLang = firstFile.getAttributeValue("source-language");
				String targetLang = firstFile.getAttributeValue("target-language");
				List<Element> segments = new Vector<>();
				recurse(root1, segments);
				List<Long> mems = project.getMemories();
				List<InternalDatabase> dbs = new Vector<>();
				for (int i2 = 0; i2 < mems.size(); i2++) {
					dbs.add(getTMEngine(mems.get(i2)));
				}
				MessageFormat mf2 = new MessageFormat("{0} of {1} segments processed");
				Iterator<Element> it = segments.iterator();
				int count = 0;
				while (it.hasNext()) {
					if (count % 200 == 0) {
						logger.log(mf2.format(new String[] { "" + count, "" + segments.size() }));
					}
					Element seg = it.next();
					if (seg.getAttributeValue("approved", "no").equalsIgnoreCase("yes")) {
						continue;
					}
					List<Element> matches = new Vector<>();
					List<Element> res = null;
					for (int i2 = 0; i2 < dbs.size(); i2++) {
						res = searchText(dbs.get(i2), seg, sourceLang, targetLang, 70f, true);
						if (res != null && !res.isEmpty()) {
							matches.addAll(res);
						}
					}
					matches = sortMatches(matches);
					int max = matches.size();
					if (max > 10) {
						max = 10;
					}
					for (int i2 = 0; i2 < max; i2++) {
						Element match = matches.get(i2);
						try {
							if (Float.parseFloat(match.getAttributeValue("match-quality")) >= 70) {
								seg.addContent(match);
								seg.addContent("\n");
							}
						} catch (NumberFormatException e) {
							// do nothing
						}
					}
					count++;
				}
				logger.log(mf2.format(new String[] { "" + segments.size(), "" + segments.size() }));
				for (int i2 = 0; i2 < dbs.size(); i2++) {
					InternalDatabase db = dbs.get(i2);
					db.close();
				}
				try (FileOutputStream out = new FileOutputStream(targetXliff)) {
					XMLOutputter outputter = new XMLOutputter();
					outputter.preserveSpace(true);
					outputter.output(doc1, out);
				}
			}
		}
		if (generateCount) {
			MessageFormat mf3 = new MessageFormat("Generating Word Counts - {0}");
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(mf3.format(
						new String[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				RepetitionAnalysis analysis = new RepetitionAnalysis();
				analysis.analyse(targetXliff.getAbsolutePath(), Preferences.getInstance().getCatalogFile());
			}
		}

		if (useXliff20) {
			logger.setStage("Generating XLIFF 2.0");
			for (int i = 0; i < tgtLangs.size(); i++) {
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				logger.log(targetXliff.getAbsolutePath());
				result = ToXliff2.run(targetXliff, Preferences.getInstance().getCatalogFile());
				if (!result.get(0).equals(Constants.SUCCESS)) {
					throw new IOException(result.get(1));
				}
			}
		}
		logger.displaySuccess("XLIFF files generated");
		List<String> issues = DitaMap2Xliff.getIssues();
		Iterator<String> it = issues.iterator();
		while (it.hasNext()) {
			logger.logError(it.next());
		}
	}

	private void leverage(File xliff, File previousBuild, ILogger logger)
			throws IOException, SAXException, ParserConfigurationException {

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xliff);
		Element root = doc.getRootElement();
		List<Element> segments = new Vector<>();

		Document doc2 = builder.build(previousBuild);
		Element root2 = doc2.getRootElement();
		List<Element> leveraged = new Vector<>();

		List<Element> originalFiles = root.getChildren("file");
		List<Element> oldFiles = root2.getChildren("file");

		for (int fi = 0; fi < originalFiles.size(); fi++) {
			Element currentFile = originalFiles.get(fi);
			if (logger != null) {
				logger.log(currentFile.getAttributeValue("original"));
				if (logger.isCancelled()) {
					throw new IOException("User cancelled");
				}
			}
			Element oldFile = null;
			for (int j = 0; j < oldFiles.size(); j++) {
				if (oldFiles.get(j).getAttributeValue("original").equals(currentFile.getAttributeValue("original"))) {
					oldFile = oldFiles.get(j);
					break;
				}
			}
			if (oldFile == null) {
				continue;
			}
			segments.clear();
			recurseSegments(currentFile, segments);

			leveraged.clear();
			recurseSegments(oldFile, leveraged);

			Element previous = null;
			Element current = null;
			Element next = null;
			int size = segments.size();
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					previous = segments.get(i - 1).getChild("source");
				}
				if (segments.get(i).getAttributeValue("approved", "no").equalsIgnoreCase("yes")) {
					continue;
				}
				if (segments.get(i).getAttributeValue("translate", "yes").equalsIgnoreCase("no")) {
					continue;
				}
				current = segments.get(i).getChild("source");
				String pureText = TMUtils.pureText(current);
				if (i + 1 < segments.size()) {
					next = segments.get(i + 1).getChild("source");
				} else {
					next = null;
				}
				for (int j = 0; j < leveraged.size(); j++) {
					Element newUnit = leveraged.get(j);
					if (newUnit.getAttributeValue("approved", "no").equalsIgnoreCase("no")) {
						continue;
					}
					Element newSource = newUnit.getChild("source");
					if (pureText.equals(TMUtils.pureText(newSource))) {
						double mismatches = wrongTags(current, newSource, 1.0);
						if (mismatches > 0.0) {
							continue;
						}
						if (previous != null) {
							if (j == 0) {
								continue;
							}
							Element e = leveraged.get(j - 1).getChild("source");
							if (!TMUtils.pureText(previous).equals(TMUtils.pureText(e))) {
								continue;
							}
						}
						if (next != null) {
							if (j + 1 == leveraged.size()) {
								continue;
							}
							Element e = leveraged.get(j + 1).getChild("source");
							if (!TMUtils.pureText(next).equals(TMUtils.pureText(e))) {
								continue;
							}
						}
						Element newTarget = newUnit.getChild("target");
						if (newTarget != null) {
							Element target = segments.get(i).getChild("target");
							if (target == null) {
								target = new Element("target");
								addTarget(segments.get(i), target);
							}
							target.clone(newTarget);
							target.setAttribute("state", "signed-off");
							target.setAttribute("state-qualifier", "leveraged-inherited");
							segments.get(i).setAttribute("approved", "yes");
						}
					}
				}
			}
		}
		try (FileOutputStream output = new FileOutputStream(xliff)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
	}

	private static void addTarget(Element el, Element tg) {
		el.removeChild("target");
		List<XMLNode> content = el.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode o = content.get(i);
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) o;
				if (e.getName().equals("source")) {
					content.add(i + 1, tg);
					break;
				}
			}
		}
		el.setContent(content);
	}

	private void recurseSegments(Element root, List<Element> segments) {
		if (root.getName().equals("trans-unit")) {
			segments.add(root);
		} else {
			List<Element> list = root.getChildren();
			Iterator<Element> it = list.iterator();
			while (it.hasNext()) {
				recurseSegments(it.next(), segments);
			}
		}
	}

	private static File getPreviousBuild(Project project, String code) throws IOException {
		Preferences preferences = Preferences.getInstance();
		File projectFolder = new File(preferences.getProjectsFolder(), "" + project.getId());
		File languageFolder = new File(projectFolder, code);
		if (!languageFolder.exists()) {
			return null;
		}
		File[] files = languageFolder.listFiles();
		if (files.length == 0) {
			return null;
		}
		int lastBuild = -1;
		File bestMatch = files[0];
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			int build = Integer.parseInt(f.getName().substring("build_".length(), f.getName().indexOf('.')));
			if (build > lastBuild) {
				lastBuild = build;
				bestMatch = f;
			}
		}
		return bestMatch;
	}

	private static void makeFilesRelative(File xliffFile)
			throws IOException, SAXException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xliffFile);
		Element root = doc.getRootElement();
		List<Element> files1 = root.getChildren("file");
		TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < files1.size(); i++) {
			Element file = files1.get(i);
			String original = file.getAttributeValue("original");
			set.add(original);
		}
		String treeRoot = FileUtils.findTreeRoot(set);
		for (int i = 0; i < files1.size(); i++) {
			Element file = files1.get(i);
			String original = file.getAttributeValue("original");
			file.setAttribute("original", FileUtils.getRelativePath(treeRoot, original));
		}
		set = null;
		try (FileOutputStream output = new FileOutputStream(xliffFile)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
	}

	private static String getName(String name, String code) {
		String result = name.substring(0, name.lastIndexOf('.')) + "@@@@" + name.substring(name.lastIndexOf('.'));
		return result.replace("@@@@", "_" + code) + ".xlf";
	}

	private static void changeTargetLanguage(File newFile, String code, Project project)
			throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(newFile);
		Element root = doc.getRootElement();
		List<Element> files = root.getChildren("file");
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			Element file = it.next();
			file.setAttribute("target-language", code);
			file.setAttribute("product-name", project.getTitle());
			file.setAttribute("product-version", "" + project.getId());
			file.setAttribute("build-num", "" + project.getNextBuild(code));
		}
		try (FileOutputStream output = new FileOutputStream(newFile)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
	}

	private void recurse(Element e, List<Element> segments) {
		if (e.getName().equals("trans-unit")) {
			if (e.getAttributeValue("translate", "yes").equals("yes")
					&& !e.getAttributeValue("approved", "no").equals("yes")) {
				segments.add(e);
			}
		} else {
			List<Element> children = e.getChildren();
			Iterator<Element> it = children.iterator();
			while (it.hasNext()) {
				recurse(it.next(), segments);
			}
		}
	}

	public void importXliff(Project project, String xliffDocument, String targetFolder, boolean updateTM,
			boolean acceptUnapproved, boolean ignoreTagErrors, boolean cleanAttributes, ILogger logger)
			throws NumberFormatException, IOException, SAXException, ParserConfigurationException,
			ClassNotFoundException, SQLException, URISyntaxException, JSONException, ParseException {

		logger.setStage("Loading XLIFF");

		String workDocument = checkXliffVersion(xliffDocument);

		Document doc = loadXliff(workDocument);
		Element root = doc.getRootElement();
		removeAltTrans(root);
		if (!xliffDocument.equals(workDocument)) {
			// XLIFF 2.0
			acceptUnapproved = true;
		}
		if (acceptUnapproved) {
			approveAll(root);
		}
		if (!ignoreTagErrors) {
			String tagErrors = checkTags(root);
			if (!tagErrors.isEmpty()) {
				tagErrors = "There are segments with tag errors\n\n";
				String report = TagErrorsReport.run(workDocument);
				if (logger instanceof AsyncLogger aLogger) {
					aLogger.displayReport(tagErrors, report);
				} else {
					logger.displayError(tagErrors);
				}
				return;
			}
		}
		if (!xliffDocument.equals(workDocument)) {
			// save changes
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			Indenter.indent(root, 2);
			try (FileOutputStream outputFile = new FileOutputStream(workDocument)) {
				outputter.output(doc, outputFile);
			}
		}
		String[] toolData = getToolData(root);
		String targetLanguage = toolData[0];
		List<String> langs = project.getLanguages();
		boolean found = false;
		for (int i = 0; i < langs.size(); i++) {
			if (langs.get(i).equals(targetLanguage)) {
				found = true;
				break;
			}
		}
		if (!found) {
			for (int i = 0; i < langs.size(); i++) {
				if (targetLanguage.toLowerCase().startsWith(langs.get(i).toLowerCase())) {
					targetLanguage = langs.get(i);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			logger.displayError("Wrong target language");
			return;
		}
		String projectID = toolData[1];
		String build = toolData[2];
		if (!projectID.equals("" + project.getId())) {
			logger.displayError("XLIFF file does not correspond to selected project");
			return;
		}
		String encoding = getEncoding(root);
		TreeSet<String> fileSet = getFileSet(root);
		if (fileSet.size() != 1) {
			File f = new File(targetFolder);
			if (f.exists()) {
				if (!f.isDirectory()) {
					logger.displayError("A file with the name of selected output folder exists");
					return;
				}
			} else {
				f.mkdirs();
			}
		}
		Iterator<String> it = fileSet.iterator();
		List<Map<String, String>> paramsList = new Vector<>();
		logger.setStage("Splitting XLIFF");
		List<String> targetFiles = new Vector<>();
		while (it.hasNext()) {
			if (logger.isCancelled()) {
				logger.displayError("User cancelled");
				return;
			}
			String file = it.next();
			File xliff = File.createTempFile("temp", ".xlf");
			encoding = saveXliff(file, xliff, root);
			Map<String, String> params = new Hashtable<>();
			params.put("xliff", xliff.getAbsolutePath());
			if (fileSet.size() == 1) {
				params.put("backfile", targetFolder);
			} else {
				String backfile = FileUtils.getAbsolutePath(targetFolder, file);
				logger.log(backfile);
				params.put("backfile", backfile);
			}
			params.put("encoding", encoding);
			params.put("catalog", Preferences.getInstance().getCatalogFile());
			String dataType = root.getChild("file").getAttributeValue("datatype", FileFormats.DITA);
			params.put("format", dataType);
			paramsList.add(params);
			targetFiles.add(params.get("backfile"));
		}
		logger.setStage("Converting XLIFF");
		for (int i = 0; i < paramsList.size(); i++) {
			if (logger.isCancelled()) {
				logger.displayError("User cancelled");
				return;
			}
			Map<String, String> par = paramsList.get(i);
			String backfile = par.get("backfile");
			logger.log(backfile.substring(backfile.lastIndexOf(System.getProperty("file.separator"))));
			List<String> result = xliffToOriginal(par);
			if (!Constants.SUCCESS.equals(result.get(0))) {
				String error = result.get(1);
				if (error == null) {
					error = "Unknown error converting file";
				}
				root = null;
				logger.displayError(error);
				return;
			}
			// remove referenced content imported on XLIFF creation
			SAXBuilder builder = new SAXBuilder();
			Catalog catalog = new Catalog(Preferences.getInstance().getCatalogFile());
			builder.setEntityResolver(catalog);
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			Document d = builder.build(backfile);
			Element r = d.getRootElement();
			removeContent(r);
			Indenter.indent(r, 2);
			try (FileOutputStream out = new FileOutputStream(new File(backfile))) {
				outputter.output(d, out);
			}
			// remove temporay XLIFF
			File f = new File(paramsList.get(i).get("xliff"));
			Files.delete(Paths.get(f.toURI()));
		}
		if (updateTM) {
			if (logger.isCancelled()) {
				logger.displayError("User cancelled");
				return;
			}
			logger.setStage("Generating TMX");
			logger.log("");
			logger.log(xliffDocument.substring(0, xliffDocument.lastIndexOf('.')) + ".tmx");
			String tmxFile = xliffDocument.substring(0, xliffDocument.lastIndexOf('.')) + ".tmx";
			TmxExporter.export(workDocument, tmxFile, Preferences.getInstance().getCatalogFile());
			logger.setStage("Importing TMX");
			Memory m = getMemory(project.getId());
			if (m != null) {
				InternalDatabase database = getTMEngine(m.getId());
				int result = database.storeTMX(tmxFile, project.getTitle(), "", "");
				database.close();
				MessageFormat mf = new MessageFormat("Imported: {0} segments");
				logger.log(mf.format(new String[] { "" + result }));
				m.setLastUpdate(new Date());
				updateMemory(m);
			} else {
				logger.displayError("Project memory does not exist");
				return;
			}
		}
		if (cleanAttributes) {
			logger.setStage("Cleaning Default Attributes");
			logger.log("");

			SAXBuilder builder = new SAXBuilder();
			Catalog catalog = new Catalog(Preferences.getInstance().getCatalogFile());
			builder.setEntityResolver(catalog);

			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);

			for (int i = 0; i < targetFiles.size(); i++) {
				if (logger.isCancelled()) {
					logger.displayError("User cancelled");
					return;
				}
				String target = targetFiles.get(i);
				logger.log(target);
				Document d = builder.build(target);
				Element r = d.getRootElement();
				if (r.getName().equals("svg")) {
					continue;
				}
				recurse(r);
				try (FileOutputStream out = new FileOutputStream(new File(target))) {
					outputter.output(d, out);
				}
			}
		}
		Preferences preferences = Preferences.getInstance();
		File projectFolder = new File(preferences.getProjectsFolder(), "" + project.getId());
		File languageFolder = new File(projectFolder, targetLanguage);
		if (!languageFolder.exists()) {
			languageFolder.mkdirs();
		}
		if (logger.isCancelled()) {
			logger.displayError("User cancelled");
			return;
		}
		logger.setStage("Saving XLIFF as future reference");
		logger.log("Finishing...");
		try (FileOutputStream output = new FileOutputStream(new File(languageFolder, "build_" + build + ".xlf"))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
		if (!xliffDocument.equals(workDocument)) {
			File f = new File(workDocument);
			Files.delete(Paths.get(f.toURI()));
		}
		project.getHistory().add(new ProjectEvent(ProjectEvent.XLIFF_IMPORTED, new Date(),
				targetLanguage, Integer.parseInt(build)));
		project.setLanguageStatus(targetLanguage, Project.COMPLETED);
		updateProject(project);
		logger.displaySuccess("XLIFF file imported");
	}

	private static String checkXliffVersion(String xliffDocument)
			throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalog(Preferences.getInstance().getCatalogFile()));
		Document doc = builder.build(xliffDocument);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) {
			throw new IOException("Selected file is not an XLIFF document");
		}
		if (root.getAttributeValue("version").equals("1.2")) {
			return xliffDocument;
		}
		if (root.getAttributeValue("version").startsWith("2.")) {
			String name = xliffDocument.substring(0, xliffDocument.lastIndexOf(".")) + "_12"
					+ xliffDocument.substring(xliffDocument.lastIndexOf("."));
			FromXliff2.run(xliffDocument, name, Preferences.getInstance().getCatalogFile());
			return name;
		}
		return null;
	}

	private void approveAll(Element e) {
		if (e.getName().equals("trans-unit")) {
			Element target = e.getChild("target");
			if (target != null) {
				e.setAttribute("approved", "yes");
			}
			return;
		}
		List<Element> children = e.getChildren();
		for (int i = 0; i < children.size(); i++) {
			approveAll(children.get(i));
		}
	}

	private String checkTags(Element root) {

		StringBuilder result = new StringBuilder();

		Element source;
		Element target;
		List<String> srclist;
		List<String> trglist;
		List<Element> segments = new Vector<>();

		createList(root, segments);

		int size = segments.size();
		for (int i = 0; i < size; i++) {

			Element e = segments.get(i);
			if ("no".equals(e.getAttributeValue("approved", "no"))) {
				continue;
			}
			source = e.getChild("source");
			target = e.getChild("target");
			if (target == null) {
				continue;
			}
			srclist = buildTagList(source);
			trglist = buildTagList(target);

			/* check empty target */
			if (!trglist.isEmpty()) {
				int tLength = trglist.size();
				int j;
				if (tLength > srclist.size()) {
					result.append(i + 1);
					result.append(": Extra Tag\n");
				} else if (tLength < srclist.size()) {
					result.append(i + 1);
					result.append(": Missing Tag\n");
				} else {
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						boolean paired = false;
						for (int k = 0; k < trglist.size(); k++) {
							String et = trglist.get(k);
							if (es.equals(et)) {
								paired = true;
								trglist.remove(k);
								break;
							}
						}
						if (!paired) {
							result.append(i + 1);
							result.append(": Different Tag\n");
						}
					}
					trglist = buildTagList(target);
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						String et = trglist.get(j);
						if (!es.equals(et)) {
							result.append(i + 1);
							result.append(": Tags in wrong order\n");
						}
					}
				}
			} else {
				// empty target
				if (!srclist.isEmpty()) {
					result.append(i + 1);
					result.append(": Empty Target\n");
				}
			}
		}
		return result.toString();
	}

	private void createList(Element root, List<Element> segments) {
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals("trans-unit")) {
				segments.add(el);
			} else {
				createList(el, segments);
			}
		}
	}

	public static List<String> buildTagList(Element e) {
		List<String> result = new Vector<>();
		List<XMLNode> content = e.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element el = (Element) o;
				if (el.getName().equals("ph")
						|| el.getName().equals("bpt")
						|| el.getName().equals("ept")
						|| el.getName().equals("it")) {
					if (!el.getChildren().isEmpty()) {
						String open = "<" + el.getName() + " ";
						List<Attribute> att = el.getAttributes();
						for (int j = 0; j < att.size(); j++) {
							Attribute a = att.get(j);
							open = open + a.getName() + "=\"" + a.getValue().replace("\"", "&quot;") + "\" ";
						}
						result.add(open.substring(0, open.length() - 1) + ">");
						List<XMLNode> list = el.getContent();
						for (int j = 0; j < list.size(); j++) {
							XMLNode n = list.get(j);
							if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
								result.addAll(buildTagList((Element) n));
							}
						}
						result.add("</" + el.getName() + ">");
					} else {
						result.add(el.toString());
					}
				} else if (el.getName().equals("mrk")
						|| el.getName().equals("g")
						|| el.getName().equals("sub")) {
					String open = "<" + el.getName() + " ";
					List<Attribute> att = el.getAttributes();
					for (int j = 0; j < att.size(); j++) {
						Attribute a = att.get(j);
						open = open + a.getName() + "=\"" + a.getValue().replace("\"", "&quot;") + "\" ";
					}
					result.add(open.substring(0, open.length() - 1) + ">");
					List<XMLNode> list = el.getContent();
					for (int j = 0; j < list.size(); j++) {
						XMLNode n = list.get(j);
						if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
							result.addAll(buildTagList((Element) n));
						}
					}
					result.add("</" + el.getName() + ">");
				} else if (el.getName().equals("x") || el.getName().equals("bx") || el.getName().equals("ex")) {
					result.add(el.toString());
				} else {
					// foreign element?
					result.add(el.toString());
				}
			}
		}
		return result;
	}

	private void removeAltTrans(Element e) {
		List<Element> children = e.getChildren();
		List<Element> matches = e.getChildren("alt-trans");
		if (!matches.isEmpty()) {
			for (int i = 0; i < matches.size(); i++) {
				e.removeChild(matches.get(i));
			}
			children = e.getChildren();
		}
		for (int i = 0; i < children.size(); i++) {
			removeAltTrans(children.get(i));
		}
	}

	private static String[] getToolData(Element root) {
		Element file = root.getChild("file");
		return new String[] { file.getAttributeValue("target-language"),
				file.getAttributeValue("product-version"), file.getAttributeValue("build-num") };
	}

	private static Element joinGroup(Element child) {
		List<Element> pair = child.getChildren();
		Element left = pair.get(0);
		if (left.getName().equals("group")) {
			left = joinGroup(left);
		}
		Element right = pair.get(1);
		if (right.getName().equals("group")) {
			right = joinGroup(right);
		}
		List<XMLNode> srcContent = right.getChild("source").getContent();
		for (int k = 0; k < srcContent.size(); k++) {
			XMLNode n = srcContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("source").addContent(n);
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("source").addContent(n);
			}
		}
		List<XMLNode> tgtContent = right.getChild("target").getContent();
		for (int k = 0; k < tgtContent.size(); k++) {
			XMLNode n = tgtContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("target").addContent(n);
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("target").addContent(n);
			}
		}
		left.setAttribute("id", child.getAttributeValue("id"));
		if (left.getAttributeValue("approved").equalsIgnoreCase("yes")
				&& right.getAttributeValue("approved").equalsIgnoreCase("yes")) {
			left.setAttribute("approved", "yes");
		} else {
			left.setAttribute("approved", "no");
		}
		return left;
	}

	private List<String> xliffToOriginal(Map<String, String> params) {
		List<String> result = new ArrayList<>();
		File temporary = null;
		try {
			String dataType = params.get("format");
			Document doc = loadXliff(params.get("xliff"));
			Element root = doc.getRootElement();
			params.put("skeleton", getSkeleton(root));
			if (checkGroups(root)) {
				temporary = File.createTempFile("group", ".xlf");
				removeGroups(root, doc);
				try (FileOutputStream out = new FileOutputStream(temporary.getAbsolutePath())) {
					doc.writeBytes(out, doc.getEncoding());
				}
				params.put("xliff", temporary.getAbsolutePath());
			}

			if (dataType.equals(FileFormats.INX) || dataType.equals("x-inx")) {
				params.put("InDesign", "yes");
				result = Xliff2Xml.run(params);
			} else if (dataType.equals(FileFormats.ICML) || dataType.equals("x-icml")) {
				params.put("IDML", "true");
				result = Xliff2Xml.run(params);
			} else if (dataType.equals(FileFormats.IDML) || dataType.equals("x-idml")) {
				result = Xliff2Idml.run(params);
			} else if (dataType.equals(FileFormats.DITA) || dataType.equals("x-ditamap")) {
				result = Xliff2DitaMap.run(params);
			} else if (dataType.equals(FileFormats.HTML) || dataType.equals("html")) {
				File folder = new File("xmlfilter");
				params.put("iniFile", new File(folder, "init_html.xml").getAbsolutePath());
				result = Xliff2Html.run(params);
			} else if (dataType.equals(FileFormats.JS) || dataType.equals("javascript")) {
				result = Xliff2jscript.run(params);
			} else if (dataType.equals(FileFormats.JSON) || dataType.endsWith("json")) {
				result = Xliff2json.run(params);
			} else if (dataType.equals(FileFormats.JAVA) || dataType.equals("javapropertyresourcebundle")
					|| dataType.equals("javalistresourcebundle")) {
				result = Xliff2Properties.run(params);
			} else if (dataType.equals(FileFormats.MIF) || dataType.equals("mif")) {
				result = Xliff2Mif.run(params);
			} else if (dataType.equals(FileFormats.OFF) || dataType.equals("x-office")) {
				result = Xliff2Office.run(params);
			} else if (dataType.equals(FileFormats.PO) || dataType.equals("po")) {
				result = Xliff2Po.run(params);
			} else if (dataType.equals(FileFormats.PHPA) || dataType.equals("x-phparray")) {
				result = Xliff2Php.run(params);
			} else if (dataType.equals(FileFormats.RC) || dataType.equals("winres")) {
				result = Xliff2Rc.run(params);
			} else if (dataType.equals(FileFormats.RESX) || dataType.equals("resx")) {
				result = Xliff2Resx.run(params);
			} else if (dataType.equals(FileFormats.SDLPPX) || dataType.equals("x-sdlpackage")) {
				result = Xliff2Sdlrpx.run(params);
			} else if (dataType.equals(FileFormats.SDLXLIFF) || dataType.equals("x-sdlxliff")) {
				result = Xliff2Sdl.run(params);
			} else if (dataType.equals(FileFormats.SRT) || dataType.equals("x-srt")) {
				result = Xliff2Srt.run(params);
			} else if (dataType.equals(FileFormats.TEXT) || dataType.equals("plaintext")) {
				result = Xliff2Text.run(params);
			} else if (dataType.equals(FileFormats.TS) || dataType.equals("x-ts")) {
				result = Xliff2Ts.run(params);
			} else if (dataType.equals(FileFormats.TXML) || dataType.equals("x-txml")) {
				result = Xliff2Txml.run(params);
			} else if (dataType.equals(FileFormats.TXLF) || dataType.equals("x-txlf")) {
				result = Xliff2Txlf.run(params);
			} else if (dataType.equals(FileFormats.WPML) || dataType.equals("x-wpmlxliff")) {
				result = Xliff2Wpml.run(params);
			} else if (dataType.equals(FileFormats.XML) || dataType.equals("xml")) {
				result = Xliff2Xml.run(params);
			} else if (dataType.equals(FileFormats.XLIFF) || dataType.equals("x-xliff")) {
				result = FromOpenXliff.run(params);
			} else {
				result.add(Constants.ERROR);
				result.add("Unsupported XLIFF file.");
			}
			if (temporary != null) {
				Files.delete(Paths.get(temporary.toURI()));
			}
		} catch (IOException | SAXException | ParserConfigurationException | URISyntaxException e) {
			result.add(0, Constants.ERROR);
			result.add(1, e.getMessage());
		}
		return result;
	}

	private boolean checkGroups(Element e) {
		if (e.getName().equals("group") && e.getAttributeValue("ts").equals("hs-split")) {
			return true;
		}
		List<Element> children = e.getChildren();
		Iterator<Element> i = children.iterator();
		while (i.hasNext()) {
			Element child = i.next();
			if (checkGroups(child)) {
				return true;
			}
		}
		return false;
	}

	private static String getSkeleton(Element root) throws IOException {
		String result = "";
		Element file = root.getChild("file");
		if (file != null) {
			Element header = file.getChild("header");
			if (header != null) {
				Element skl = header.getChild("skl");
				if (skl != null) {
					Element external = skl.getChild("external-file");
					if (external != null) {
						result = external.getAttributeValue("href");
						result = result.replace("&amp;", "&");
						result = result.replace("&lt;", "<");
						result = result.replace("&gt;", ">");
						result = result.replace("&apos;", "\'");
						result = result.replace("&quot;", "\"");
					} else {
						Element internal = skl.getChild("internal-file");
						if (internal != null) {
							File tmp = File.createTempFile("internal", ".skl");
							tmp.deleteOnExit();
							Utils.decodeToFile(internal.getText(), tmp.getAbsolutePath());
							return tmp.getAbsolutePath();
						}
						return result;
					}
				}
			}
		}
		return result;
	}

	private void removeGroups(Element e, Document d) {
		List<XMLNode> children = e.getContent();
		for (int i = 0; i < children.size(); i++) {
			XMLNode n = children.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element child = (Element) n;
				if (child.getName().equals("group") && child.getAttributeValue("ts").equals("hs-split")) {
					child = joinGroup(child);
					Element tu = new Element("trans-unit");
					tu.clone(child);
					children.remove(i);
					children.add(i, tu);
					e.setContent(children);
				} else {
					removeGroups(child, d);
				}
			}
		}
	}

	private static Document loadXliff(String fileName)
			throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalog(Preferences.getInstance().getCatalogFile()));
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) {
			throw new IOException("Selected file is not an XLIFF document");
		}
		Element tool = root.getChild("file").getChild("header").getChild("tool");
		if (tool == null) {
			throw new IOException("Selected file was not created with Fluenta");
		}
		if (!tool.getAttributeValue("tool-id").equals("OpenXLIFF")) {
			throw new IOException("Selected file was not created with this version of Fluenta");
		}
		checkXliffMarkup(doc.getRootElement());
		return doc;
	}

	private static void checkXliffMarkup(Element e) {
		if (e.getName().equals("trans-unit")) {
			Element seg = e.getChild("seg-source");
			if (seg != null) {
				e.removeChild(seg);
				Element t = e.getChild("target");
				if (t != null) {
					removeSegMrk(e.getChild("target"));
					e.setAttribute("approved", "yes");
				}
			}
		}
		List<Element> files = e.getChildren();
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			checkXliffMarkup(it.next());
		}
	}

	private static void removeSegMrk(Element target) {
		if (target == null) {
			return;
		}
		List<XMLNode> vector = new Vector<>();
		List<XMLNode> content = target.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (e.getName().equals("mrk") && e.getAttributeValue("mtype").equals("seg")) {
					List<XMLNode> children = e.getContent();
					for (int j = 0; j < children.size(); j++) {
						vector.add(children.get(j));
					}
				}
			} else {
				vector.add(node);
			}
		}
		target.setContent(vector);
	}

	private TreeSet<String> getFileSet(Element root) {
		List<Element> files = root.getChildren("file");
		TreeSet<String> fileSet = new TreeSet<>();
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			Element file = it.next();
			fileSet.add(file.getAttributeValue("original"));
		}
		return fileSet;
	}

	private static String getEncoding(Element root) {
		String encoding = StandardCharsets.UTF_8.name();
		List<PI> pis = root.getPI("encoding");
		if (!pis.isEmpty()) {
			encoding = pis.get(0).getData();
		}
		return encoding;
	}

	private static String saveXliff(String fileName, File xliff, Element root) throws IOException {
		String encoding = StandardCharsets.UTF_8.name();
		try (FileOutputStream out = new FileOutputStream(xliff)) {
			writeStr(out, "<xliff version=\"1.2\">\n");
			List<Element> files = root.getChildren("file");
			Iterator<Element> it = files.iterator();
			while (it.hasNext()) {
				Element file = it.next();
				if (file.getAttributeValue("original").equals(fileName)) {
					List<PI> pis = file.getPI();
					Iterator<PI> pt = pis.iterator();
					while (pt.hasNext()) {
						PI pi = pt.next();
						if (pi.getTarget().equals("encoding")) {
							encoding = pi.getData();
						}
						writeStr(out, pi.toString());
					}
					writeStr(out, file.toString());
				}
			}
			writeStr(out, "</xliff>\n");
		}
		return encoding;
	}

	private static void writeStr(FileOutputStream out, String string) throws IOException {
		out.write(string.getBytes(StandardCharsets.UTF_8));
	}

	public int importTMX(Memory memory, String tmxFile)
			throws SQLException, ClassNotFoundException, IOException, SAXException, ParserConfigurationException,
			JSONException, ParseException {
		InternalDatabase database = getTMEngine(memory.getId());
		int result = database.storeTMX(tmxFile, "", "", "");
		database.close();
		memory.setLastUpdate(new Date());
		updateMemory(memory);
		return result;
	}

	public void updateMemory(Memory memory) throws IOException, JSONException, ParseException {
		if (memoriesManager == null) {
			Preferences preferences = Preferences.getInstance();
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		memoriesManager.update(memory);
	}

	public Memory getMemory(long id) throws IOException, JSONException, ParseException {
		if (memoriesManager == null) {
			Preferences preferences = Preferences.getInstance();
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		return memoriesManager.getMemory(id);
	}

	private List<Element> searchText(InternalDatabase db, Element seg, String sourcelang, String targetlang,
			float fuzzyLevel, boolean caseSensitive)
			throws SAXException, IOException, ParserConfigurationException, SQLException {
		if (validCtypes == null) {
			validCtypes = new Hashtable<>();
			validCtypes.put("image", "");
			validCtypes.put("pb", "");
			validCtypes.put("lb", "");
			validCtypes.put("bold", "");
			validCtypes.put("italic", "");
			validCtypes.put("underlined", "");
			validCtypes.put("link", "");
		}

		if (phCtypes == null) {
			phCtypes = new Hashtable<>();
			phCtypes.put("image", "");
			phCtypes.put("pb", "");
			phCtypes.put("lb", "");
		}
		return searchTranslations(db, seg, sourcelang, targetlang, fuzzyLevel, caseSensitive);
	}

	private List<Element> searchTranslations(InternalDatabase database, Element seg, String srcLang, String tgtLang,
			float fuzzyLevel, boolean caseSensitive)
			throws SAXException, IOException, ParserConfigurationException, SQLException {

		List<Element> result = new Vector<>();
		List<Match> res = database.searchTranslation(TMUtils.pureText(seg.getChild("source")),
				srcLang, tgtLang, (int) fuzzyLevel, caseSensitive);

		Iterator<Match> r = res.iterator();
		while (r.hasNext()) {
			Match match = r.next();
			int quality = match.getSimilarity();

			Element alttrans = new Element("alt-trans");

			Element srcTuv = match.getSource();
			Element tgtTuv = match.getTarget();
			Element src = new Element("source");
			src.setContent(toXliff(srcTuv.getChild("seg").getContent()));
			Element tgt = new Element("target");
			tgt.setContent(toXliff(tgtTuv.getChild("seg").getContent()));

			alttrans.addContent("\n      ");
			alttrans.addContent(src);
			alttrans.addContent("\n      ");
			alttrans.addContent(tgt);
			alttrans.addContent("\n   ");
			alttrans = fixTags(seg.getChild("source"), alttrans);
			quality = MatchQuality.similarity(TMUtils.pureText(seg.getChild("source")),
					TMUtils.pureText(alttrans.getChild("source")));
			double discount = wrongTags(alttrans.getChild("source"), seg.getChild("source"), penalty);
			quality = (int) Math.floor(quality - discount);

			alttrans.setAttribute("match-quality", "" + quality);
			alttrans.setAttribute("xml:space", "default");
			alttrans.setAttribute("origin", match.getOrigin());

			result.add(alttrans);
		}
		return sortMatches(result);
	}

	private List<XMLNode> toXliff(List<XMLNode> content)
			throws SAXException, IOException, ParserConfigurationException {
		List<XMLNode> result = new Vector<>();
		Iterator<XMLNode> it = content.iterator();
		while (it.hasNext()) {
			XMLNode node = it.next();
			if (node.getNodeType() == XMLNode.TEXT_NODE) {
				result.add(node);
			}
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element tag = (Element) node;
				if ("ph".equals(tag.getName()) && !"xliff-x".equals(tag.getAttributeValue("type"))) {
					tag.setAttribute("id", tag.getAttributeValue("x"));
					tag.removeAttribute("x");
					result.add(tag);
				}
				if ("ph".equals(tag.getName()) && "xliff-x".equals(tag.getAttributeValue("type"))) {
					String x = tag.getText();
					result.add(new SAXBuilder().build(new ByteArrayInputStream(x.getBytes(StandardCharsets.UTF_8)))
							.getRootElement());
				}
				if ("bpt".equals(tag.getName()) && "xliff-g".equals(tag.getAttributeValue("type"))) {
					Element g = new Element("g");
					g.setAttributes(parseAttributes(tag.getText()));
					String i = tag.getAttributeValue("i");
					while (it.hasNext()) {
						node = it.next();
						if (node.getNodeType() == XMLNode.TEXT_NODE) {
							g.addContent(node);
						}
						if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
							Element child = (Element) node;
							if ("ept".equals(child.getName()) && i.equals(child.getAttributeValue("i"))) {
								break;
							}
							g.addContent(child);
						}
					}
					g.setContent(toXliff(g.getContent()));
					result.add(g);
				}
			}
		}
		return result;
	}

	private List<Attribute> parseAttributes(String text)
			throws SAXException, IOException, ParserConfigurationException {
		String element = text + "</g>";
		return new SAXBuilder().build(new ByteArrayInputStream(element.getBytes(StandardCharsets.UTF_8)))
				.getRootElement().getAttributes();
	}

	private void cleanCtype(Element e) {
		if (e.getName().equals("ph")
				|| e.getName().equals("x")) {
			String value = e.getAttributeValue("ctype");
			if (!value.isEmpty()) {
				if (!phCtypes.containsKey(value) && !value.startsWith("x-")) {
					e.setAttribute("ctype", "x-" + value);
				}
			}
		}
		if (e.getName().equals("bpt")
				|| e.getName().equals("sub")
				|| e.getName().equals("bx")
				|| e.getName().equals("g")
				|| e.getName().equals("it")) {
			String value = e.getAttributeValue("ctype");
			if (!value.isEmpty() && !validCtypes.containsKey(value) && !value.startsWith("x-")) {
				e.setAttribute("ctype", "x-" + value);
			}
		}
		List<XMLNode> content = e.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode n = i.next();
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				cleanCtype((Element) n);
			}
		}
	}

	private static double wrongTags(Element x, Element y, double tagPenalty) {
		List<Element> tags = new Vector<>();
		int count = 0;
		int errors = 0;
		List<XMLNode> content = x.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode n = i.next();
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) n;
				tags.add(e);
				count++;
			}
		}
		content = y.getContent();
		i = content.iterator();
		int c2 = 0;
		while (i.hasNext()) {
			XMLNode n = i.next();
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) n;
				c2++;
				boolean found = false;
				for (int j = 0; j < count; j++) {
					if (e.equals(tags.get(j))) {
						tags.set(j, null);
						found = true;
						break;
					}
				}
				if (!found) {
					errors++;
				}
			}
		}
		if (c2 > count) {
			errors += c2 - count;
		}
		if (count > c2) {
			errors += count - c2;
		}
		return errors * tagPenalty;
	}

	private Element fixTags(Element src, Element match) {
		Element altSrc = match.getChild("source");
		Element altTgt = match.getChild("target");
		List<Element> srcList = src.getChildren();
		List<Element> altSrcList = altSrc.getChildren();
		List<Element> altTgtList = altTgt.getChildren();
		if (altSrcList.size() != altTgtList.size()) {
			cleanCtype(match);
			return match;
		}
		if (srcList.size() == 1 && altSrcList.isEmpty()) {
			// source has one tag more than alt-source
			List<XMLNode> content = src.getContent();
			XMLNode initial = content.get(0);
			if (initial.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = new Element(((Element) initial).getName());
				e.clone((Element) initial);
				content = altSrc.getContent();
				content.add(0, e);
				altSrc.setContent(content);
				Element t = new Element(e.getName());
				t.clone(e);
				content = altTgt.getContent();
				content.add(0, t);
				altTgt.setContent(content);
			}
			XMLNode last = content.get(content.size() - 1);
			if (last.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = new Element();
				e.clone((Element) last);
				content = altSrc.getContent();
				content.add(e);
				altSrc.setContent(content);
				Element t = new Element(e.getName());
				t.clone(e);
				content = altTgt.getContent();
				content.add(t);
				altTgt.setContent(content);

			}
			cleanCtype(match);
			return match;
		}
		if (srcList.isEmpty() && !altSrcList.isEmpty()) {
			// remove all tags from the match
			List<XMLNode> content = altSrc.getContent();
			Iterator<XMLNode> i = content.iterator();
			List<XMLNode> newContent = new Vector<>();
			while (i.hasNext()) {
				XMLNode o = i.next();
				if (o.getNodeType() != XMLNode.ELEMENT_NODE) {
					newContent.add(o);
				}
			}
			altSrc.setContent(newContent);
			newContent = new Vector<>();
			content = altTgt.getContent();
			i = content.iterator();
			while (i.hasNext()) {
				XMLNode o = i.next();
				if (o.getNodeType() != XMLNode.ELEMENT_NODE) {
					newContent.add(o);
				}
			}
			altTgt.setContent(newContent);
			return match;
		}
		Map<String, Element> srcTable = new Hashtable<>();
		for (int i = 0; i < srcList.size(); i++) {
			Element e = srcList.get(i);
			srcTable.put(e.getAttributeValue("id", "-1"), e);
		}
		List<XMLNode> content = altSrc.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getName().equals("ph")) {
					Element o = srcTable.get(e.getAttributeValue("id", "-2"));
					if (o != null && !o.equals(e)) {
						e.clone(o);
					}
				}
			}
		}
		content = altTgt.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getName().equals("ph")) {
					Element o = srcTable.get(e.getAttributeValue("id", "-2"));
					if (o != null && !e.equals(o)) {
						e.clone(o);
					}
				}
			}
		}
		cleanCtype(match);
		return match;
	}

	public void removeProject(Project project) throws IOException, JSONException, ParseException {
		Preferences preferences = Preferences.getInstance();
		long id = project.getId();
		File projectFolder = new File(preferences.getProjectsFolder(), "" + id);
		deltree(projectFolder);
		if (projectsManager == null) {
			projectsManager = new ProjectsManager(preferences.getProjectsFolder());
		}
		projectsManager.remove(id);
		try {
			removeMemory(id);
		} catch (IOException ioe) {
			// do nothing
		}
	}

	public void removeMemory(long id) throws IOException, JSONException, ParseException {
		List<Project> projects = getProjects();
		for (int i = 0; i < projects.size(); i++) {
			List<Long> memories = projects.get(i).getMemories();
			for (int j = 0; j < memories.size(); j++) {
				if (memories.get(j) == id) {
					throw new IOException("Memory is in use");
				}
			}
		}
		Preferences preferences = Preferences.getInstance();
		File memoryFolder = new File(preferences.getMemoriesFolder(), "" + id);
		deltree(memoryFolder);
		if (memoriesManager == null) {
			memoriesManager = new MemoriesManager(preferences.getMemoriesFolder());
		}
		memoriesManager.remove(id);
	}

	private void deltree(File file) throws IOException {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					deltree(list[i]);
				}
			}

		}
		Files.deleteIfExists(Paths.get(file.toURI()));
	}

	public void exportTMX(Memory memory, String file)
			throws ClassNotFoundException, IOException, SQLException, SAXException, ParserConfigurationException {
		InternalDatabase database = getTMEngine(memory.getId());
		Set<String> languages = database.getAllLanguages();
		database.exportMemory(file, languages, memory.getSrcLanguage().getCode());
		database.close();
	}

	public Project getProject(long id) throws IOException, JSONException, ParseException {
		if (projectsManager == null) {
			Preferences preferences = Preferences.getInstance();
			projectsManager = new ProjectsManager(preferences.getProjectsFolder());
		}
		return projectsManager.getProject(id);
	}

	private void removeContent(Element e) {
		if ("removeContent".equals(e.getAttributeValue("state"))) {
			e.setContent(new ArrayList<>());
			e.removeAttribute("state");
		}
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			removeContent(it.next());
		}
	}

	private void recurse(Element e) {
		e.removeAttribute("class");
		e.removeAttribute("xmlns:ditaarch");
		e.removeAttribute("ditaarch:DITAArchVersion");
		e.removeAttribute("domains");
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next());
		}
	}

	public static List<Element> sortMatches(List<Element> matches) {
		if (matches.isEmpty() || matches.size() == 1) {
			return matches;
		}
		Collections.sort(matches, new Comparator<Element>() {

			@Override
			public int compare(Element o1, Element o2) {
				Double db1 = Double.parseDouble(o1.getAttributeValue("match-quality", "0"));
				Double db2 = Double.parseDouble(o2.getAttributeValue("match-quality", "0"));
				return db1.compareTo(db2);
			}

		});
		return matches;
	}
}
