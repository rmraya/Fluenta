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
import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.xml.sax.SAXException;

import com.maxprograms.converters.Convert;
import com.maxprograms.converters.EncodingResolver;
import com.maxprograms.converters.FileFormats;
import com.maxprograms.converters.Utils;
import com.maxprograms.converters.ditamap.Xliff2DitaMap;
import com.maxprograms.converters.html.Xliff2Html;
import com.maxprograms.converters.idml.Xliff2Idml;
import com.maxprograms.converters.javaproperties.Xliff2Properties;
import com.maxprograms.converters.javascript.Xliff2jscript;
import com.maxprograms.converters.mif.Xliff2Mif;
import com.maxprograms.converters.office.Xliff2Office;
import com.maxprograms.converters.plaintext.Xliff2Text;
import com.maxprograms.converters.po.Xliff2Po;
import com.maxprograms.converters.rc.Xliff2Rc;
import com.maxprograms.converters.resx.Xliff2Resx;
import com.maxprograms.converters.sdlxliff.Xliff2Sdl;
import com.maxprograms.converters.ts.Xliff2Ts;
import com.maxprograms.converters.txml.Xliff2Txml;
import com.maxprograms.converters.xml.Xliff2Xml;
import com.maxprograms.fluenta.Constants;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.fluenta.models.ProjectEvent;
import com.maxprograms.fluenta.views.ProjectPreferences;
import com.maxprograms.fluenta.views.XmlPreferences;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.stats.RepetitionAnalysis;
import com.maxprograms.tmengine.ILogger;
import com.maxprograms.tmengine.InternalDatabase;
import com.maxprograms.tmengine.MatchQuality;
import com.maxprograms.tmengine.TU;
import com.maxprograms.tmengine.Tuv;
import com.maxprograms.utils.FileUtils;
import com.maxprograms.utils.MemUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.utils.TMUtils;
import com.maxprograms.utils.TMXExporter;
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
import com.maxprograms.xml.XMLUtils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class LocalController {

	private static Logger LOGGER = System.getLogger(LocalController.class.getName());

	private DB projectdb;
	private DB memorydb;
	private HTreeMap<Long, Project> projectsMap;
	private HTreeMap<Long, Memory> memoriesMap;
	private String projectsFile = "Projects.db"; //$NON-NLS-1$
	private String memoriesFile = "Memories.db"; //$NON-NLS-1$
	private String sourceLang;
	private String targetLang;

	private Hashtable<String, String> usedIDs;
	private Hashtable<String, String> validCtypes;
	private Hashtable<String, String> phCtypes;
	private Hashtable<String, String> xliffbpts;
	private static double penalty = 1;

	public Vector<Project> getProjects() throws IOException {
		Vector<Project> result = new Vector<>();
		if (projectdb == null) {
			openProjects();
		}
		Set<Long> keys = projectsMap.keySet();
		Iterator<Long> it = keys.iterator();
		while (it.hasNext()) {
			result.add(projectsMap.get(it.next()));
		}
		return result;
	}

	private void openProjects() throws IOException {
		File out = new File(Preferences.getPreferencesDir(), projectsFile);
		try {
			projectdb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
		} catch (IOError ex) {
			if (out.exists()) {
				try {
					Files.delete(Paths.get(out.toURI()));
					File p = new File(Preferences.getPreferencesDir(), projectsFile + ".p"); //$NON-NLS-1$
					if (p.exists()) {
						Files.delete(Paths.get(p.toURI()));
					}
					File t = new File(Preferences.getPreferencesDir(), projectsFile + ".t"); //$NON-NLS-1$
					if (t.exists()) {
						Files.delete(Paths.get(t.toURI()));
					}
					projectdb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
				} catch (IOError ex2) {
					LOGGER.log(Level.ERROR, ex2);
					throw new IOException(ex2.getMessage());
				}
			} else {
				LOGGER.log(Level.ERROR, ex);
				throw new IOException(ex.getMessage());
			}
		}
		projectsMap = projectdb.getHashMap("projects"); //$NON-NLS-1$
	}

	public void close() {
		if (projectdb != null) {
			projectdb.commit();
			projectdb.close();
			projectdb = null;
		}
		if (memorydb != null) {
			memorydb.commit();
			memorydb.close();
			memorydb = null;
		}
	}

	public void createProject(Project p)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		if (projectdb == null) {
			openProjects();
		}
		projectsMap.put(p.getId(), p);
		Vector<Memory> mems = p.getMemories();
		Iterator<Memory> it = mems.iterator();
		while (it.hasNext()) {
			Memory m = it.next();
			if (m.getId() == p.getId()) {
				memoriesMap.put(m.getId(), m);
				memorydb.commit();
				InternalDatabase database = getTMEngine(m.getId());
				database.close();
				projectdb.commit();
				break;
			}
		}
	}

	public void createMemory(Memory m) throws IOException {
		if (memorydb == null) {
			openMemories();
		}
		memoriesMap.put(m.getId(), m);
		memorydb.commit();
	}

	public Vector<Memory> getMemories() throws IOException {
		Vector<Memory> result = new Vector<>();
		if (memorydb == null) {
			openMemories();
		}
		Set<Long> keys = memoriesMap.keySet();
		Iterator<Long> it = keys.iterator();
		while (it.hasNext()) {
			result.add(memoriesMap.get(it.next()));
		}
		return result;
	}

	private void openMemories() throws IOException {
		File out = new File(Preferences.getPreferencesDir(), memoriesFile);
		try {
			memorydb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
		} catch (IOError ex) {
			if (out.exists()) {
				try {
					Files.delete(Paths.get(out.toURI()));
					File p = new File(Preferences.getPreferencesDir(), memoriesFile + ".p"); //$NON-NLS-1$
					if (p.exists()) {
						Files.delete(Paths.get(p.toURI()));
					}
					File t = new File(Preferences.getPreferencesDir(), memoriesFile + ".t"); //$NON-NLS-1$
					if (t.exists()) {
						Files.delete(Paths.get(t.toURI()));
					}
					memorydb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
				} catch (IOError ex2) {
					LOGGER.log(Level.ERROR, ex2);
					throw new IOException(ex2.getMessage());
				}
			} else {
				LOGGER.log(Level.ERROR, ex);
				throw new IOException(ex.getMessage());
			}
		}
		memoriesMap = memorydb.getHashMap("memories"); //$NON-NLS-1$
	}

	public void updateProject(Project p) {
		p.setLastUpdate(new Date());
		projectsMap.put(p.getId(), p);
		projectdb.commit();
	}

	public InternalDatabase getTMEngine(long memoryId)
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		File f = new File(Preferences.getPreferencesDir(), "TMEngines"); //$NON-NLS-1$
		return new InternalDatabase("" + memoryId, f.getAbsolutePath()); //$NON-NLS-1$
	}

	public void generateXliff(Project project, String xliffFolder, Vector<Language> tgtLangs, boolean useICE,
			boolean useTM, boolean generateCount, String ditavalFile, boolean useXliff20, ILogger logger)
			throws IOException, SAXException, ParserConfigurationException, URISyntaxException, ClassNotFoundException,
			SQLException {
		Hashtable<String, String> params = new Hashtable<>();
		params.put("source", project.getMap()); //$NON-NLS-1$
		File map = new File(project.getMap());
		String name = map.getName();
		File folder = new File(xliffFolder);
		File xliffFile = new File(folder, name + ".xlf"); //$NON-NLS-1$
		params.put("xliff", xliffFile.getAbsolutePath()); //$NON-NLS-1$
		if (ditavalFile != null && !ditavalFile.equals("")) { //$NON-NLS-1$
			params.put("ditaval", ditavalFile); //$NON-NLS-1$
		}
		File skldir;
		skldir = new File(Preferences.getPreferencesDir(), "" + project.getId()); //$NON-NLS-1$
		if (!skldir.exists()) {
			skldir.mkdirs();
		}
		File skl;
		skl = File.createTempFile("temp", ".skl", skldir); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("skeleton", skl.getAbsolutePath()); //$NON-NLS-1$
		params.put("catalog", Fluenta.getCatalogFile()); //$NON-NLS-1$
		params.put("customer", ""); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("subject", ""); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("project", project.getTitle()); //$NON-NLS-1$
		params.put("srcLang", project.getSrcLanguage().getCode()); //$NON-NLS-1$
		params.put("tgtLang", project.getSrcLanguage().getCode()); // use src language in master //$NON-NLS-1$
		params.put("srcEncoding", EncodingResolver.getEncoding(project.getMap(), FileFormats.DITA).name()); //$NON-NLS-1$
		params.put("paragraph", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("format", FileFormats.DITA); //$NON-NLS-1$
		params.put("srxFile", ProjectPreferences.getDefaultSRX()); //$NON-NLS-1$
		params.put("translateComments", XmlPreferences.getTranslateComments() ? "yes" : "no"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		logger.setStage(Messages.getString("LocalController.34")); //$NON-NLS-1$
		logger.log(Messages.getString("LocalController.35")); //$NON-NLS-1$

		List<String> result = Convert.run(params);
		if (!result.get(0).equals(Constants.SUCCESS)) {
			throw new IOException(result.get(1));
		}

		makeFilesRelative(xliffFile);
		logger.setStage(Messages.getString("LocalController.38")); //$NON-NLS-1$
		MessageFormat mf = new MessageFormat(Messages.getString("LocalController.39")); //$NON-NLS-1$
		for (int i = 0; i < tgtLangs.size(); i++) {
			logger.log(
					mf.format(new Object[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
			String newName = getName(map.getName(), tgtLangs.get(i).getCode());
			File newFile = new File(folder, newName);
			Files.copy(xliffFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			changeTargetLanguage(newFile, tgtLangs.get(i).getCode(), project);
			int build = project.getNextBuild(tgtLangs.get(i).getCode());
			project.getHistory().add(new ProjectEvent(ProjectEvent.XLIFF_CREATED, new Date(), tgtLangs.get(i), build));
			project.setLanguageStatus(tgtLangs.get(i).getCode(), Project.IN_PROGRESS);
			updateProject(project);
		}
		xliffFile.delete();
		if (useICE) {
			MessageFormat icem = new MessageFormat(Messages.getString("LocalController.40")); //$NON-NLS-1$
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(icem.format(
						new Object[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() })); // $NON-NLS-1$
				logger.log(Messages.getString("LocalController.41")); //$NON-NLS-1$
				String newName = getName(map.getName(), tgtLangs.get(i).getCode());
				File xliff = new File(folder, newName);
				File previousBuild = getPreviousBuild(project, tgtLangs.get(i).getCode());
				if (previousBuild != null) {
					leverage(xliff, previousBuild, tgtLangs.get(i).getCode(), logger);
				}
			}
		}
		if (useTM) {
			MessageFormat mftm = new MessageFormat(Messages.getString("LocalController.42")); //$NON-NLS-1$
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(mftm.format(
						new Object[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));

				logger.log(Messages.getString("LocalController.43")); //$NON-NLS-1$
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				SAXBuilder builder = new SAXBuilder();
				builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
				Document doc1 = builder.build(targetXliff);
				Element root1 = doc1.getRootElement();
				Element firstFile = root1.getChild("file"); //$NON-NLS-1$
				if (firstFile == null) {
					logger.displayError(Messages.getString("LocalController.45")); //$NON-NLS-1$
					return;
				}
				sourceLang = firstFile.getAttributeValue("source-language"); //$NON-NLS-1$
				targetLang = firstFile.getAttributeValue("target-language"); //$NON-NLS-1$
				Vector<Element> segments = new Vector<>();
				recurse(root1, segments);
				Vector<Memory> mems = project.getMemories();
				Vector<InternalDatabase> dbs = new Vector<>();
				for (int i2 = 0; i2 < mems.size(); i2++) {
					dbs.add(getTMEngine(mems.get(i2).getId()));
				}
				MessageFormat mf2 = new MessageFormat(Messages.getString("LocalController.46")); //$NON-NLS-1$
				Iterator<Element> it = segments.iterator();
				int count = 0;
				while (it.hasNext()) {
					if (count % 200 == 0) {
						logger.log(mf2.format(new Object[] { "" + count, "" + segments.size() })); //$NON-NLS-1$ //$NON-NLS-2$
					}
					Element seg = it.next();
					if (seg.getAttributeValue("approved", "no").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						continue;
					}
					Vector<Element> matches = new Vector<>();
					Vector<Element> res = null;
					for (int i2 = 0; i2 < dbs.size(); i2++) {
						res = searchText(dbs.get(i2), seg, sourceLang, targetLang, 70f, true);
						if (res != null && res.size() > 0) {
							matches.addAll(res);
						}
					}
					if (matches.size() > 1) {
						matches = MemUtils.sortMatches(matches);
					}
					int max = matches.size();
					if (max > 10) {
						max = 10;
					}
					for (int i2 = 0; i2 < max; i2++) {
						Element match = matches.get(i2);
						try {
							if (Float.parseFloat(match.getAttributeValue("match-quality")) >= 70) { //$NON-NLS-1$
								seg.addContent(match);
							}
						} catch (Exception e) {
							// do nothing
						}
					}
					count++;
				}
				logger.log(mf2.format(new Object[] { "" + segments.size(), "" + segments.size() })); //$NON-NLS-1$ //$NON-NLS-2$
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
			MessageFormat mf3 = new MessageFormat(Messages.getString("LocalController.54")); //$NON-NLS-1$
			for (int i = 0; i < tgtLangs.size(); i++) {
				logger.setStage(mf3.format(
						new Object[] { LanguageUtils.getLanguage(tgtLangs.get(i).getCode()).getDescription() }));
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				RepetitionAnalysis analysis = new RepetitionAnalysis();
				analysis.analyse(targetXliff.getAbsolutePath(), Fluenta.getCatalogFile());
			}
		}
		logger.displaySuccess(Messages.getString("LocalController.55")); //$NON-NLS-1$
		if (useXliff20) {
			logger.setStage(Messages.getString("LocalController.1")); //$NON-NLS-1$
			for (int i = 0; i < tgtLangs.size(); i++) {
				String targetName = getName(map.getName(), tgtLangs.get(i).getCode());
				File targetXliff = new File(folder, targetName);
				logger.log(targetXliff.getAbsolutePath());
				result = ToXliff2.run(targetXliff, Fluenta.getCatalogFile());
				if (!result.get(0).equals(Constants.SUCCESS)) { // $NON-NLS-1$
					throw new IOException(result.get(1));
				}
			}
		}
	}

	private void leverage(File xliff, File previousBuild, String targetLanguage, ILogger logger)
			throws IOException, SAXException, ParserConfigurationException {

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xliff);
		Element root = doc.getRootElement();
		Vector<Element> segments = new Vector<>();

		Document doc2 = builder.build(previousBuild);
		Element root2 = doc2.getRootElement();
		Vector<Element> leveraged = new Vector<>();

		List<Element> originalFiles = root.getChildren("file"); //$NON-NLS-1$
		List<Element> oldFiles = root2.getChildren("file"); //$NON-NLS-1$

		for (int fi = 0; fi < originalFiles.size(); fi++) {
			Element currentFile = originalFiles.get(fi);
			if (logger != null) {
				logger.log(currentFile.getAttributeValue("original")); //$NON-NLS-1$
				if (logger.isCancelled()) {
					throw new IOException(Messages.getString("LocalController.59")); //$NON-NLS-1$
				}
			}
			Element oldFile = null;
			for (int j = 0; j < oldFiles.size(); j++) {
				if (oldFiles.get(j).getAttributeValue("original").equals(currentFile.getAttributeValue("original"))) { //$NON-NLS-1$ //$NON-NLS-2$
					oldFile = oldFiles.get(j);
					break;
				}
			}
			if (oldFile == null) {
				continue;
			}
			segments.removeAllElements();
			recurseSegments(currentFile, segments);

			leveraged.removeAllElements();
			recurseSegments(oldFile, leveraged);

			Element previous = null;
			Element current = null;
			Element next = null;
			int size = segments.size();
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					previous = segments.get(i - 1).getChild("source"); //$NON-NLS-1$
				}
				if (segments.get(i).getAttributeValue("approved", "no").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					continue;
				}
				if (segments.get(i).getAttributeValue("translate", "yes").equalsIgnoreCase("no")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					continue;
				}
				current = segments.get(i).getChild("source"); //$NON-NLS-1$
				String pureText = TMUtils.pureText(current);
				if (i + 1 < segments.size()) {
					next = segments.get(i + 1).getChild("source"); //$NON-NLS-1$
				} else {
					next = null;
				}
				for (int j = 0; j < leveraged.size(); j++) {
					Element newUnit = leveraged.get(j);
					if (newUnit.getAttributeValue("approved", "no").equalsIgnoreCase("no")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						continue;
					}
					Element newSource = newUnit.getChild("source"); //$NON-NLS-1$
					if (pureText.equals(TMUtils.pureText(newSource))) {
						double mismatches = wrongTags(current, newSource, 1.0);
						if (mismatches > 0.0) {
							continue;
						}
						if (previous != null) {
							if (j == 0) {
								continue;
							}
							Element e = leveraged.get(j - 1).getChild("source"); //$NON-NLS-1$
							if (!TMUtils.pureText(previous).equals(TMUtils.pureText(e))) {
								continue;
							}
						}
						if (next != null) {
							if (j + 1 == leveraged.size()) {
								continue;
							}
							Element e = leveraged.get(j + 1).getChild("source"); //$NON-NLS-1$
							if (!TMUtils.pureText(next).equals(TMUtils.pureText(e))) {
								continue;
							}
						}
						Element newTarget = newUnit.getChild("target"); //$NON-NLS-1$
						if (newTarget != null) {
							Element target = segments.get(i).getChild("target"); //$NON-NLS-1$
							if (target == null) {
								target = new Element("target"); //$NON-NLS-1$
								addTarget(segments.get(i), target);
							}
							target.clone(newTarget);
							target.setAttribute("xml:lang", targetLanguage); //$NON-NLS-1$
							target.setAttribute("state", "signed-off"); //$NON-NLS-1$//$NON-NLS-2$
							target.setAttribute("state-qualifier", "leveraged-inherited"); //$NON-NLS-1$ //$NON-NLS-2$
							segments.get(i).setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
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
		el.removeChild("target"); //$NON-NLS-1$
		List<XMLNode> content = el.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode o = content.get(i);
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) o;
				if (e.getName().equals("source")) { //$NON-NLS-1$
					content.add(i + 1, tg);
					break;
				}
			}
		}
		el.setContent(content);
	}

	private void recurseSegments(Element root, Vector<Element> segments) {
		if (root.getName().equals("trans-unit")) { //$NON-NLS-1$
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
		File fluenta = Preferences.getPreferencesDir();
		File projectFolder = new File(fluenta, "" + project.getId()); //$NON-NLS-1$
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
			int build = Integer.parseInt(f.getName().substring("build_".length(), f.getName().indexOf('.'))); //$NON-NLS-1$
			if (build > lastBuild) {
				build = lastBuild;
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
		List<Element> files1 = root.getChildren("file"); //$NON-NLS-1$
		TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < files1.size(); i++) {
			Element file = files1.get(i);
			String original = file.getAttributeValue("original"); //$NON-NLS-1$
			set.add(original);
		}
		String treeRoot = FileUtils.findTreeRoot(set);
		for (int i = 0; i < files1.size(); i++) {
			Element file = files1.get(i);
			String original = file.getAttributeValue("original"); //$NON-NLS-1$
			file.setAttribute("original", FileUtils.getRelativePath(treeRoot, original)); //$NON-NLS-1$
		}
		set = null;
		try (FileOutputStream output = new FileOutputStream(xliffFile)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
	}

	private static String getName(String name, String code) {
		String result = name.substring(0, name.lastIndexOf('.')) + "@@@@" + name.substring(name.lastIndexOf('.')); //$NON-NLS-1$
		return result.replaceAll("@@@@", "_" + code) + ".xlf"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static void changeTargetLanguage(File newFile, String code, Project project)
			throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(newFile);
		Element root = doc.getRootElement();
		List<Element> files = root.getChildren("file"); //$NON-NLS-1$
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			Element file = it.next();
			file.setAttribute("target-language", code); //$NON-NLS-1$
			file.setAttribute("product-name", project.getTitle()); //$NON-NLS-1$
			file.setAttribute("product-version", "" + project.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			file.setAttribute("build-num", "" + project.getNextBuild(code)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try (FileOutputStream output = new FileOutputStream(newFile)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
	}

	private void recurse(Element e, Vector<Element> segments) {
		if (e.getName().equals("trans-unit")) { //$NON-NLS-1$
			if (e.getAttributeValue("translate", "yes").equals("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!e.getAttributeValue("approved", "no").equals("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					segments.add(e);
				}
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
			ClassNotFoundException, SQLException, URISyntaxException {

		logger.setStage(Messages.getString("LocalController.123")); //$NON-NLS-1$

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
			if (!tagErrors.equals("")) { //$NON-NLS-1$
				tagErrors = Messages.getString("LocalController.2") + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
				String report = TagErrorsReport.run(workDocument);
				if (logger instanceof AsyncLogger) {
					((AsyncLogger) logger).displayReport(tagErrors, report);
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
		Vector<Language> langs = project.getTgtLanguages();
		boolean found = false;
		for (int i = 0; i < langs.size(); i++) {
			Language l = langs.get(i);
			if (l.getCode().equals(targetLanguage)) {
				found = true;
				break;
			}
		}
		if (!found) {
			for (int i = 0; i < langs.size(); i++) {
				Language l = langs.get(i);
				if (targetLanguage.toLowerCase().startsWith(l.getCode().toLowerCase())) {
					targetLanguage = l.getCode();
					found = true;
					break;
				}
			}
		}
		if (!found) {
			logger.displayError(Messages.getString("LocalController.0")); //$NON-NLS-1$
			return;
		}
		String projectID = toolData[1];
		String build = toolData[2];
		if (!projectID.equals("" + project.getId())) { //$NON-NLS-1$
			logger.displayError(Messages.getString("LocalController.125")); //$NON-NLS-1$
			return;
		}
		String encoding = getEncoding(root);
		TreeSet<String> fileSet = getFileSet(root);
		if (fileSet.size() != 1) {
			File f = new File(targetFolder);
			if (f.exists()) {
				if (!f.isDirectory()) {
					logger.displayError(Messages.getString("LocalController.126")); //$NON-NLS-1$
					return;
				}
			} else {
				f.mkdirs();
			}
		}
		Iterator<String> it = fileSet.iterator();
		Vector<Hashtable<String, String>> paramsList = new Vector<>();
		logger.setStage(Messages.getString("LocalController.127")); //$NON-NLS-1$
		Vector<String> targetFiles = new Vector<>();
		while (it.hasNext()) {
			if (logger.isCancelled()) {
				logger.displayError(Messages.getString("LocalController.59")); //$NON-NLS-1$
				return;
			}
			String file = it.next();
			File xliff = File.createTempFile("temp", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
			encoding = saveXliff(file, xliff, root);
			Hashtable<String, String> params = new Hashtable<>();
			params.put("xliff", xliff.getAbsolutePath()); //$NON-NLS-1$
			if (fileSet.size() == 1) {
				params.put("backfile", targetFolder); //$NON-NLS-1$
			} else {
				String backfile = FileUtils.getAbsolutePath(targetFolder, file);
				logger.log(backfile);
				params.put("backfile", backfile); //$NON-NLS-1$
			}
			params.put("encoding", encoding); //$NON-NLS-1$
			params.put("catalog", Fluenta.getCatalogFile()); //$NON-NLS-1$
			String dataType = root.getChild("file").getAttributeValue("datatype", FileFormats.DITA); //$NON-NLS-1$ //$NON-NLS-2$
			params.put("format", dataType); //$NON-NLS-1$
			paramsList.add(params);
			targetFiles.add(params.get("backfile")); //$NON-NLS-1$
		}
		logger.setStage(Messages.getString("LocalController.138")); //$NON-NLS-1$
		for (int i = 0; i < paramsList.size(); i++) {
			if (logger.isCancelled()) {
				logger.displayError(Messages.getString("LocalController.59")); //$NON-NLS-1$
				return;
			}
			Hashtable<String, String> par = paramsList.get(i);
			String backfile = par.get("backfile"); //$NON-NLS-1$
			logger.log(backfile.substring(backfile.lastIndexOf(System.getProperty("file.separator")))); //$NON-NLS-1$
			List<String> result = xliffToOriginal(par);
			if (!"0".equals(result.get(0))) { //$NON-NLS-1$
				String error = result.get(1);
				if (error == null) {
					error = Messages.getString("LocalController.142"); //$NON-NLS-1$
				}
				root = null;
				logger.displayError(error);
				return;
			}
			File f = new File(paramsList.get(i).get("xliff")); //$NON-NLS-1$
			Files.delete(Paths.get(f.toURI()));
		}
		if (updateTM) {
			if (logger.isCancelled()) {
				logger.displayError(Messages.getString("LocalController.59")); //$NON-NLS-1$
				return;
			}
			logger.setStage(Messages.getString("LocalController.144")); //$NON-NLS-1$
			logger.log(""); //$NON-NLS-1$
			logger.log(xliffDocument.substring(0, xliffDocument.lastIndexOf('.')) + ".tmx"); //$NON-NLS-1$
			String tmxFile = xliffDocument.substring(0, xliffDocument.lastIndexOf('.')) + ".tmx"; //$NON-NLS-1$
			TMXExporter.export(workDocument, tmxFile, acceptUnapproved);
			logger.setStage(Messages.getString("LocalController.148")); //$NON-NLS-1$
			Memory m = getMemory(project.getId());
			if (m != null) {
				InternalDatabase database = getTMEngine(m.getId());
				int[] result = database.storeTMX(tmxFile, System.getProperty("user.name"), project.getTitle(), "", //$NON-NLS-1$ //$NON-NLS-2$
						"", false, logger); //$NON-NLS-1$
				database.close();
				database = null;
				MessageFormat mf = new MessageFormat(Messages.getString("LocalController.152")); //$NON-NLS-1$
				logger.log(mf.format(new Object[] { result[0], result[1] }));
				m.setLastUpdate(new Date());
				updateMemory(m);
			} else {
				logger.displayError(Messages.getString("LocalController.153")); //$NON-NLS-1$
				return;
			}
		}
		if (cleanAttributes) {
			logger.setStage(Messages.getString("LocalController.3")); //$NON-NLS-1$
			logger.log(""); //$NON-NLS-1$

			SAXBuilder builder = new SAXBuilder();
			Catalog catalog = new Catalog(Fluenta.getCatalogFile());
			builder.setEntityResolver(catalog);

			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);

			for (int i = 0; i < targetFiles.size(); i++) {
				if (logger.isCancelled()) {
					logger.displayError(Messages.getString("LocalController.59")); //$NON-NLS-1$
					return;
				}
				String target = targetFiles.get(i);
				logger.log(target);
				Document d = builder.build(target);
				Element r = d.getRootElement();
				if (r.getName().equals("svg")) { //$NON-NLS-1$
					continue;
				}
				recurse(r);
				try (FileOutputStream out = new FileOutputStream(new File(target))) {
					outputter.output(d, out);
				}
			}
		}
		File fluenta = Preferences.getPreferencesDir();
		File projectFolder = new File(fluenta, "" + project.getId()); //$NON-NLS-1$
		File languageFolder = new File(projectFolder, targetLanguage);
		if (!languageFolder.exists()) {
			languageFolder.mkdirs();
		}
		if (logger.isCancelled()) {
			logger.displayError(Messages.getString("LocalController.59")); //$NON-NLS-1$
			return;
		}
		logger.setStage(Messages.getString("LocalController.155")); //$NON-NLS-1$
		logger.log(Messages.getString("LocalController.156")); //$NON-NLS-1$
		try (FileOutputStream output = new FileOutputStream(new File(languageFolder, "build_" + build + ".xlf"))) { //$NON-NLS-1$ //$NON-NLS-2$
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(doc, output);
		}
		if (!xliffDocument.equals(workDocument)) {
			File f = new File(workDocument);
			Files.delete(Paths.get(f.toURI()));
		}
		project.getHistory().add(new ProjectEvent(ProjectEvent.XLIFF_IMPORTED, new Date(),
				LanguageUtils.getLanguage(targetLanguage), Integer.parseInt(build)));
		project.setLanguageStatus(targetLanguage, Project.COMPLETED);
		updateProject(project);
		logger.displaySuccess(Messages.getString("LocalController.160")); //$NON-NLS-1$
	}

	private static String checkXliffVersion(String xliffDocument)
			throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
		Document doc = builder.build(xliffDocument);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) { //$NON-NLS-1$
			throw new IOException(Messages.getString("LocalController.250")); //$NON-NLS-1$
		}
		if (root.getAttributeValue("version").equals("1.2")) { //$NON-NLS-1$ //$NON-NLS-2$
			return xliffDocument;
		}
		if (root.getAttributeValue("version").equals("2.0")) { //$NON-NLS-1$ //$NON-NLS-2$
			String name = xliffDocument.substring(0, xliffDocument.lastIndexOf(".")) + "_12" //$NON-NLS-1$ //$NON-NLS-2$
					+ xliffDocument.substring(xliffDocument.lastIndexOf(".")); //$NON-NLS-1$
			FromXliff2.run(xliffDocument, name, Fluenta.getCatalogFile());
			return name;
		}
		return null;
	}

	private void approveAll(Element e) {
		if (e.getName().equals("trans-unit")) { //$NON-NLS-1$
			Element target = e.getChild("target"); //$NON-NLS-1$
			if (target != null) {
				e.setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}
		List<Element> children = e.getChildren();
		for (int i = 0; i < children.size(); i++) {
			approveAll(children.get(i));
		}
	}

	private String checkTags(Element root) {

		String result = ""; //$NON-NLS-1$

		Element source;
		Element target;
		List<String> srclist;
		List<String> trglist;
		List<Element> segments = new Vector<>();

		createList(root, segments);

		int size = segments.size();
		for (int i = 0; i < size; i++) {

			Element e = segments.get(i);
			if (!e.getAttributeValue("approved", "no").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			source = e.getChild("source"); //$NON-NLS-1$
			target = e.getChild("target"); //$NON-NLS-1$
			if (target == null) {
				continue;
			}
			srclist = buildTagList(source);
			trglist = buildTagList(target);

			/* check empty target */
			if (trglist.size() != 0) {
				int tLength = trglist.size();
				int j;
				if (tLength > srclist.size()) {
					result = result + (i + 1) + ": " + Messages.getString("LocalController.9") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else if (tLength < srclist.size()) {
					result = result + (i + 1) + ": " + Messages.getString("LocalController.12") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
						if (paired == false) {
							result = result + (i + 1) + ": " + Messages.getString("LocalController.15") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
					trglist = buildTagList(target);
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						String et = trglist.get(j);
						if (!es.equals(et)) {
							result = result + (i + 1) + ": " + Messages.getString("LocalController.18") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			} else {
				// empty target
				if (srclist.size() > 0) {
					result = result + (i + 1) + ": " + Messages.getString("LocalController.21") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		return result;
	}

	private void createList(Element root, List<Element> segments) {
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals("trans-unit")) { //$NON-NLS-1$
				segments.add(el);
			} else {
				createList(el, segments);
			}
		}
	}

	public static Vector<String> buildTagList(Element e) {
		Vector<String> result = new Vector<>();
		List<XMLNode> content = e.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element el = (Element) o;
				if (el.getName().equals("ph") //$NON-NLS-1$
						|| el.getName().equals("bpt") //$NON-NLS-1$
						|| el.getName().equals("ept") //$NON-NLS-1$
						|| el.getName().equals("it")) //$NON-NLS-1$
				{
					if (el.getChildren().size() > 0) {
						String open = "<" + el.getName() + " "; //$NON-NLS-1$ //$NON-NLS-2$
						List<Attribute> att = el.getAttributes();
						for (int j = 0; j < att.size(); j++) {
							Attribute a = att.get(j);
							open = open + a.getName() + "=\"" + a.getValue().replaceAll("\"", "&quot;") + "\" "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						}
						result.add(open.substring(0, open.length() - 1) + ">"); //$NON-NLS-1$
						List<XMLNode> list = el.getContent();
						for (int j = 0; j < list.size(); j++) {
							XMLNode n = list.get(j);
							if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
								result.addAll(buildTagList((Element) n));
							}
						}
						result.add("</" + el.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						result.add(el.toString());
					}
				} else if (el.getName().equals("mrk") //$NON-NLS-1$
						|| el.getName().equals("g") //$NON-NLS-1$
						|| el.getName().equals("sub")) //$NON-NLS-1$
				{
					String open = "<" + el.getName() + " "; //$NON-NLS-1$ //$NON-NLS-2$
					List<Attribute> att = el.getAttributes();
					for (int j = 0; j < att.size(); j++) {
						Attribute a = att.get(j);
						open = open + a.getName() + "=\"" + a.getValue().replaceAll("\"", "&quot;") + "\" "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
					result.add(open.substring(0, open.length() - 1) + ">"); //$NON-NLS-1$
					List<XMLNode> list = el.getContent();
					for (int j = 0; j < list.size(); j++) {
						XMLNode n = list.get(j);
						if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
							result.addAll(buildTagList((Element) n));
						}
					}
					result.add("</" + el.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (el.getName().equals("x") || el.getName().equals("bx") || el.getName().equals("ex")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		List<Element> matches = e.getChildren("alt-trans"); //$NON-NLS-1$
		if (matches.size() > 0) {
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
		Element file = root.getChild("file"); //$NON-NLS-1$
		String[] result = new String[] { file.getAttributeValue("target-language", ""), //$NON-NLS-1$ //$NON-NLS-2$
				file.getAttributeValue("product-version", ""), file.getAttributeValue("build-num", "") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return result;
	}

	private static Element joinGroup(Element child) {
		List<Element> pair = child.getChildren();
		Element left = pair.get(0);
		if (left.getName().equals("group")) { //$NON-NLS-1$
			left = joinGroup(left);
		}
		Element right = pair.get(1);
		if (right.getName().equals("group")) { //$NON-NLS-1$
			right = joinGroup(right);
		}
		List<XMLNode> srcContent = right.getChild("source").getContent(); //$NON-NLS-1$
		for (int k = 0; k < srcContent.size(); k++) {
			XMLNode n = srcContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("source").addContent(n); //$NON-NLS-1$
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("source").addContent(n); //$NON-NLS-1$
			}
		}
		List<XMLNode> tgtContent = right.getChild("target").getContent(); //$NON-NLS-1$
		for (int k = 0; k < tgtContent.size(); k++) {
			XMLNode n = tgtContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("target").addContent(n); //$NON-NLS-1$
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("target").addContent(n); //$NON-NLS-1$
			}
		}
		left.setAttribute("id", child.getAttributeValue("id")); //$NON-NLS-1$ //$NON-NLS-2$
		if (left.getAttributeValue("approved").equalsIgnoreCase("yes") //$NON-NLS-1$ //$NON-NLS-2$
				&& right.getAttributeValue("approved").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$
			left.setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			left.setAttribute("approved", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return left;
	}

	private List<String> xliffToOriginal(Hashtable<String, String> params) {
		List<String> result = new ArrayList<>();
		File temporary = null;
		try {
			String dataType = params.get("format"); //$NON-NLS-1$
			Document doc = loadXliff(params.get("xliff")); //$NON-NLS-1$
			Element root = doc.getRootElement();
			params.put("skeleton", getSkeleton(root)); //$NON-NLS-1$
			if (checkGroups(root) == true) {
				temporary = File.createTempFile("group", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
				removeGroups(root, doc);
				try (FileOutputStream out = new FileOutputStream(temporary.getAbsolutePath())) {
					doc.writeBytes(out, doc.getEncoding());
				}
				params.put("xliff", temporary.getAbsolutePath()); //$NON-NLS-1$
			}

			if (dataType.equals(FileFormats.HTML) || dataType.equals("html")) { //$NON-NLS-1$
				File folder = new File("xmlfilter"); //$NON-NLS-1$
				params.put("iniFile", new File(folder, "init_html.xml").getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
				result = Xliff2Html.run(params);
			} else if (dataType.equals(FileFormats.JS) || dataType.equals("javascript")) { //$NON-NLS-1$
				result = Xliff2jscript.run(params);
			} else if (dataType.equals(FileFormats.MIF) || dataType.equals("mif")) { //$NON-NLS-1$
				result = Xliff2Mif.run(params);
			} else if (dataType.equals(FileFormats.OFF) || dataType.equals("x-office")) { //$NON-NLS-1$
				result = Xliff2Office.run(params);
			} else if (dataType.equals(FileFormats.RESX) || dataType.equals("resx")) { //$NON-NLS-1$
				result = Xliff2Resx.run(params);
			} else if (dataType.equals(FileFormats.RC) || dataType.equals("winres")) { //$NON-NLS-1$
				result = Xliff2Rc.run(params);
			} else if (dataType.equals(FileFormats.TXML) || dataType.equals("x-txml")) { //$NON-NLS-1$
				result = Xliff2Txml.run(params);
			} else if (dataType.equals(FileFormats.SDLXLIFF) || dataType.equals("x-sdlxliff")) { //$NON-NLS-1$
				result = Xliff2Sdl.run(params);
			} else if (dataType.equals(FileFormats.TEXT) || dataType.equals("plaintext")) { //$NON-NLS-1$
				result = Xliff2Text.run(params);
			} else if (dataType.equals(FileFormats.XML) || dataType.equals("xml")) { //$NON-NLS-1$
				result = Xliff2Xml.run(params);
			} else if (dataType.equals(FileFormats.INX) || dataType.equals("x-inx")) { //$NON-NLS-1$
				params.put("InDesign", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
				result = Xliff2Xml.run(params);
			} else if (dataType.equals(FileFormats.IDML) || dataType.equals("x-idml")) { //$NON-NLS-1$
				result = Xliff2Idml.run(params);
			} else if (dataType.equals(FileFormats.PO) || dataType.equals("po")) { //$NON-NLS-1$
				result = Xliff2Po.run(params);
			} else if (dataType.equals(FileFormats.JAVA) || dataType.equals("javapropertyresourcebundle") //$NON-NLS-1$
					|| dataType.equals("javalistresourcebundle")) { //$NON-NLS-1$
				result = Xliff2Properties.run(params);
			} else if (dataType.equals(FileFormats.TS) || dataType.equals("x-ts")) { //$NON-NLS-1$
				result = Xliff2Ts.run(params);
			} else if (dataType.equals(FileFormats.DITA) || dataType.equals("x-ditamap")) { //$NON-NLS-1$
				result = Xliff2DitaMap.run(params);
			} else {
				result.add(0, "1"); //$NON-NLS-1$
				result.add(1, Messages.getString("LocalController.219")); //$NON-NLS-1$
			}
			if (temporary != null) {
				Files.delete(Paths.get(temporary.toURI()));
			}
		} catch (IOException | SAXException | ParserConfigurationException | URISyntaxException e) {
			result.add(0, "1"); //$NON-NLS-1$
			result.add(1, e.getMessage());
		}
		return result;
	}

	private boolean checkGroups(Element e) {
		if (e.getName().equals("group") && e.getAttributeValue("ts", "").equals("hs-split")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
		String result = ""; //$NON-NLS-1$
		Element file = root.getChild("file"); //$NON-NLS-1$
		Element header = null;
		if (file != null) {
			header = file.getChild("header"); //$NON-NLS-1$
			if (header != null) {
				Element mskl = header.getChild("skl"); //$NON-NLS-1$
				if (mskl != null) {
					Element external = mskl.getChild("external-file"); //$NON-NLS-1$
					if (external != null) {
						result = external.getAttributeValue("href"); //$NON-NLS-1$
						result = result.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&apos;", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						Element internal = mskl.getChild("internal-file"); //$NON-NLS-1$
						if (internal != null) {
							File tmp = File.createTempFile("internal", ".skl"); //$NON-NLS-1$ //$NON-NLS-2$
							tmp.deleteOnExit();
							Utils.decodeToFile(internal.getText(), tmp.getAbsolutePath());
							return tmp.getAbsolutePath();
						}
						return result;
					}
					external = null;
					mskl = null;
				} else {
					return result;
				}
			} else {
				return result;
			}
		} else {
			return result;
		}
		header = null;
		file = null;
		return result;
	}

	private void removeGroups(Element e, Document d) {
		List<XMLNode> children = e.getContent();
		for (int i = 0; i < children.size(); i++) {
			XMLNode n = children.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element child = (Element) n;
				if (child.getName().equals("group") && child.getAttributeValue("ts", "").equals("hs-split")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					child = joinGroup(child);
					Element tu = new Element("trans-unit"); //$NON-NLS-1$
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
		builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) { //$NON-NLS-1$
			throw new IOException(Messages.getString("LocalController.250")); //$NON-NLS-1$
		}
		Element tool = root.getChild("file").getChild("header").getChild("tool"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (tool == null) {
			throw new IOException(Messages.getString("LocalController.254")); //$NON-NLS-1$
		}
		if (!tool.getAttributeValue("tool-id").equals("OpenXLIFF")) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new IOException(Messages.getString("LocalController.254")); //$NON-NLS-1$
		}
		checkXliffMarkup(doc.getRootElement());
		return doc;
	}

	private static void checkXliffMarkup(Element e) {
		if (e.getName().equals("trans-unit")) { //$NON-NLS-1$
			Element seg = e.getChild("seg-source"); //$NON-NLS-1$
			if (seg != null) {
				e.removeChild(seg);
				Element t = e.getChild("target"); //$NON-NLS-1$
				if (t != null) {
					removeSegMrk(e.getChild("target")); //$NON-NLS-1$
					e.setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
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
		Vector<XMLNode> vector = new Vector<>();
		List<XMLNode> content = target.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (e.getName().equals("mrk") && e.getAttributeValue("mtype", "").equals("seg")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
		List<Element> files = root.getChildren("file"); //$NON-NLS-1$
		TreeSet<String> fileSet = new TreeSet<>();
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			Element file = it.next();
			if (targetLang == null) {
				targetLang = file.getAttributeValue("target-language", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			fileSet.add(file.getAttributeValue("original")); //$NON-NLS-1$
		}
		return fileSet;
	}

	private static String getEncoding(Element root) {
		String encoding = "UTF-8"; //$NON-NLS-1$
		List<PI> pis = root.getPI("encoding"); //$NON-NLS-1$
		if (pis.size() > 0) {
			encoding = pis.get(0).getData();
		}
		return encoding;
	}

	private static String saveXliff(String fileName, File xliff, Element root) throws IOException {
		String encoding = "UTF-8"; //$NON-NLS-1$
		try (FileOutputStream out = new FileOutputStream(xliff)) {
			writeStr(out, "<xliff version=\"1.2\">\n"); //$NON-NLS-1$
			List<Element> files = root.getChildren("file"); //$NON-NLS-1$
			Iterator<Element> it = files.iterator();
			while (it.hasNext()) {
				Element file = it.next();
				if (file.getAttributeValue("original").equals(fileName)) { //$NON-NLS-1$
					List<PI> pis = file.getPI();
					Iterator<PI> pt = pis.iterator();
					while (pt.hasNext()) {
						PI pi = pt.next();
						if (pi.getTarget().equals("encoding")) { //$NON-NLS-1$
							encoding = pi.getData();
						}
						writeStr(out, pi.toString());
					}
					writeStr(out, file.toString());
				}
			}
			writeStr(out, "</xliff>\n"); //$NON-NLS-1$
		}
		return encoding;
	}

	private static void writeStr(FileOutputStream out, String string) throws UnsupportedEncodingException, IOException {
		out.write(string.getBytes("UTF-8")); //$NON-NLS-1$
	}

	public void importTMX(Memory memory, String tmxFile, ILogger logger)
			throws SQLException, ClassNotFoundException, IOException, SAXException, ParserConfigurationException {
		logger.setStage(Messages.getString("LocalController.272")); //$NON-NLS-1$
		logger.log(Messages.getString("LocalController.273")); //$NON-NLS-1$
		InternalDatabase database = getTMEngine(memory.getId());
		int[] res = database.storeTMX(tmxFile, System.getProperty("user.name"), "", "", "", false, logger); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		database.close();
		database = null;
		MessageFormat mf = new MessageFormat(
				Messages.getString("LocalController.278") + "\n" + Messages.getString("LocalController.279")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String result = mf.format(new Object[] { "" + res[0], "" + res[1] }); //$NON-NLS-1$ //$NON-NLS-2$
		memory.setLastUpdate(new Date());
		updateMemory(memory);
		logger.displaySuccess(result);
	}

	public void updateMemory(Memory m) {
		memoriesMap.put(m.getId(), m);
		memorydb.commit();
	}

	public Memory getMemory(long id) throws IOException {
		if (memorydb == null) {
			openMemories();
		}
		return memoriesMap.get(id);
	}

	private Vector<Element> searchText(InternalDatabase db, Element seg, String sourcelang, String targetlang,
			float fuzzyLevel, boolean caseSensitive) throws SAXException, IOException, ParserConfigurationException {
		if (validCtypes == null) {
			validCtypes = new Hashtable<>();
			validCtypes.put("image", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("pb", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("lb", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("bold", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("italic", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("underlined", ""); //$NON-NLS-1$ //$NON-NLS-2$
			validCtypes.put("link", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (phCtypes == null) {
			phCtypes = new Hashtable<>();
			phCtypes.put("image", ""); //$NON-NLS-1$ //$NON-NLS-2$
			phCtypes.put("pb", ""); //$NON-NLS-1$ //$NON-NLS-2$
			phCtypes.put("lb", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return searchTranslations(db, seg, sourcelang, targetlang, fuzzyLevel, caseSensitive);
	}

	private Vector<Element> searchTranslations(InternalDatabase database, Element seg, String srcLang, String tgtLang,
			float fuzzyLevel, boolean caseSensitive) throws SAXException, IOException, ParserConfigurationException {

		Hashtable<String, Element> existingMatches = new Hashtable<>();
		List<Element> translations = seg.getChildren("alt-trans"); //$NON-NLS-1$
		Iterator<Element> t = translations.iterator();
		while (t.hasNext()) {
			Element trans = t.next();
			if (!trans.getAttributeValue("tool", "TM Search").equals("TT")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				List<PI> pis = trans.getPI("id"); //$NON-NLS-1$
				if (pis.size() == 0) {
					trans.addContent(new PI("id", MemUtils.createId())); //$NON-NLS-1$
					pis = trans.getPI("id"); //$NON-NLS-1$
				}
				String pid = pis.get(0).getData();
				if (!existingMatches.containsKey(pid)) {
					existingMatches.put(pid, trans);
				}
			}
		}

		Vector<TU> res = database.searchTranslation(MemUtils.pureText(seg.getChild("source")), //$NON-NLS-1$
				srcLang, tgtLang, (int) fuzzyLevel, caseSensitive);

		Iterator<TU> r = res.iterator();
		while (r.hasNext()) {
			TU tu = r.next();
			String tid = tu.getProperty("tuid"); //$NON-NLS-1$
			int quality = Integer.parseInt(tu.getProperty("similarity")); //$NON-NLS-1$
			Tuv srcTuv = tu.getTuv(srcLang);
			Tuv tgtTuv = tu.getTuv(tgtLang);
			if (tgtTuv == null) {
				continue;
			}

			Element alttrans = new Element("alt-trans"); //$NON-NLS-1$

			Element src = buildElement("<source>" + srcTuv.getSegment() + "</source>"); //$NON-NLS-1$ //$NON-NLS-2$
			src.setAttribute("xml:lang", srcLang); //$NON-NLS-1$
			Element tgt = buildElement("<target>" + tgtTuv.getSegment() + "</target>"); //$NON-NLS-1$ //$NON-NLS-2$
			tgt.setAttribute("xml:lang", tgtLang); //$NON-NLS-1$

			alttrans.addContent("\n"); //$NON-NLS-1$
			alttrans.addContent(new PI("id", tid)); //$NON-NLS-1$
			alttrans.addContent("\n"); //$NON-NLS-1$
			alttrans.addContent(src);
			alttrans.addContent("\n"); //$NON-NLS-1$
			alttrans.addContent(tgt);
			alttrans.addContent("\n"); //$NON-NLS-1$

			alttrans = fixTags(seg.getChild("source"), alttrans); //$NON-NLS-1$
			quality = MatchQuality.similarity(MemUtils.pureText(seg.getChild("source")), //$NON-NLS-1$
					MemUtils.pureText(alttrans.getChild("source"))); //$NON-NLS-1$
			double discount = wrongTags(alttrans.getChild("source"), seg.getChild("source"), penalty); //$NON-NLS-1$ //$NON-NLS-2$
			quality = (int) Math.floor(quality - discount);

			alttrans.setAttribute("match-quality", "" + quality); //$NON-NLS-1$ //$NON-NLS-2$
			alttrans.setAttribute("xml:space", "default"); //$NON-NLS-1$ //$NON-NLS-2$
			alttrans.setAttribute("origin", database.getName()); //$NON-NLS-1$

			Hashtable<String, String> props = tu.getProps();
			if (props.containsKey("similarity")) { //$NON-NLS-1$
				props.remove("similarity"); //$NON-NLS-1$
			}
			if (props.size() > 1) {
				// contains at least tuid
				Element group = new Element("prop-group"); //$NON-NLS-1$
				alttrans.addContent(group);
				alttrans.addContent("\n"); //$NON-NLS-1$
				Enumeration<String> keys = props.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (!key.equals("similarity")) { //$NON-NLS-1$
						Element prop = new Element("prop"); //$NON-NLS-1$
						prop.setAttribute("prop-type", key); //$NON-NLS-1$
						prop.addContent(props.get(key));
						group.addContent("\n"); //$NON-NLS-1$
						group.addContent(prop);
					}
				}
				group.addContent("\n"); //$NON-NLS-1$
			}

			if (!existingMatches.containsKey(tid)) {
				existingMatches.put(tid, alttrans);
			}
			alttrans = null;
			src = null;
			tgt = null;
		}

		Vector<Element> result = new Vector<>();
		Enumeration<Element> en = existingMatches.elements();
		while (en.hasMoreElements()) {
			Element e = en.nextElement();
			result.add(e);
		}
		existingMatches = null;

		if (result.size() > 0) {
			return MemUtils.sortMatches(result);
		}
		return null;
	}

	private Element buildElement(String src) throws SAXException, IOException, ParserConfigurationException {

		ByteArrayInputStream stream = new ByteArrayInputStream(src.getBytes("UTF-8")); //$NON-NLS-1$
		SAXBuilder bld = new SAXBuilder(false);

		Document d = bld.build(stream);
		stream = null;
		usedIDs = null;
		usedIDs = new Hashtable<>();

		Element e = d.getRootElement();
		String transformed = tmx2xlf(e);
		stream = new ByteArrayInputStream(transformed.getBytes("UTF-8")); //$NON-NLS-1$
		d = bld.build(stream);
		e = d.getRootElement();
		return e;
	}

	private String tmx2xlf(Element e) {
		String type = e.getName();
		if (type.equals("source") || type.equals("target")) { //$NON-NLS-1$ //$NON-NLS-2$
			String text = "<" + type; //$NON-NLS-1$
			List<Attribute> atts = e.getAttributes();
			for (int i = 0; i < atts.size(); i++) {
				Attribute a = atts.get(i);
				text = text + " " + a.toString(); //$NON-NLS-1$
			}
			text = text + ">"; //$NON-NLS-1$
			List<XMLNode> content = e.getContent();
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					text = text + n.toString();
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					text = text + tmx2xlf((Element) n);
				}
			}
			return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (type.equals("ph")) { //$NON-NLS-1$
			if (e.toString().startsWith("<ph>&lt;ph ")) { //$NON-NLS-1$
				// may come from an old TM
				try {
					String s = e.getText();
					ByteArrayInputStream stream = new ByteArrayInputStream(s.getBytes("UTF-8")); //$NON-NLS-1$
					SAXBuilder bld = new SAXBuilder(false);
					Document d = bld.build(stream);
					Element r = d.getRootElement();
					String ctype = r.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if (!ctype.equals("")) { //$NON-NLS-1$
						r.setAttribute("type", ctype); //$NON-NLS-1$
					}
					String pid = r.getAttributeValue("id", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if (!pid.equals("")) { //$NON-NLS-1$
						r.setAttribute("x", pid); //$NON-NLS-1$
					}
					return tmx2xlf(r);
				} catch (Exception ex) {
					// do nothing
				}
			}
			String ctype = XMLUtils.cleanText(e.getAttributeValue("type", "")); //$NON-NLS-1$ //$NON-NLS-2$
			if (ctype.equals("mrk-protected")) { //$NON-NLS-1$
				return "<mrk mtype=\"protected\" mid=\"" + e.getAttributeValue("x", "-") + "\" ts=\"" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						+ clean(e.getText()) + "\">"; //$NON-NLS-1$
			}
			if (ctype.equals("mrk-close")) { //$NON-NLS-1$
				return "</mrk>"; //$NON-NLS-1$
			}
			if (!ctype.startsWith("xliff-")) { //$NON-NLS-1$
				if (!ctype.equals("")) { //$NON-NLS-1$
					if (!validCtypes.containsKey(ctype) && !ctype.startsWith("x-")) { //$NON-NLS-1$
						ctype = "x-" + ctype; //$NON-NLS-1$
					}
					ctype = " ctype=\"" + ctype + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
				String pid = XMLUtils.cleanText(e.getAttributeValue("x", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if (pid.equals("")) { //$NON-NLS-1$
					pid = newID();
				}
				String text = "<ph id=\"" + pid + "\"" + ctype + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				List<XMLNode> content = e.getContent();
				for (int i = 0; i < content.size(); i++) {
					XMLNode n = content.get(i);
					if (n.getNodeType() == XMLNode.TEXT_NODE) {
						text = text + n.toString();
					}
					if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
						text = text + tmx2xlf((Element) n);
					}
				}
				return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			// this <ph> was originated in an empty <g> or <x>
			// tag from XLIFF
			return e.getText();
		}

		if (type.equals("it")) { //$NON-NLS-1$
			if (!e.getAttributeValue("id", "").equals("")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				// assume it from xliff
				usedIDs.put(e.getAttributeValue("id"), ""); //$NON-NLS-1$ //$NON-NLS-2$
				return e.toString();
			}
			// it from TMX
			String xid = XMLUtils.cleanText(e.getAttributeValue("x", "")); //$NON-NLS-1$ //$NON-NLS-2$
			if (!xid.equals("")) { //$NON-NLS-1$
				xid = " xid=\"" + xid + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String ctype = XMLUtils.cleanText(e.getAttributeValue("type", "")); //$NON-NLS-1$ //$NON-NLS-2$
			if (!ctype.equals("")) { //$NON-NLS-1$
				if (!validCtypes.containsKey(ctype) && !ctype.startsWith("x-")) { //$NON-NLS-1$
					ctype = "x-" + ctype; //$NON-NLS-1$
				}
				ctype = " ctype=\"" + ctype + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String pos = XMLUtils.cleanText(e.getAttributeValue("pos", "")); //$NON-NLS-1$ //$NON-NLS-2$
			if (pos.equals("begin")) { //$NON-NLS-1$
				pos = "open"; //$NON-NLS-1$
			} else {
				pos = "close"; //$NON-NLS-1$
			}
			String text = "<it id=\"" + newID() + "\" pos=\"" + pos + "\"" + xid + ctype + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			List<XMLNode> content = e.getContent();
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					text = text + n.toString();
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					text = text + tmx2xlf((Element) n);
				}
			}
			return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (type.equals("bpt") || type.equals("ept")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (type.equals("bpt") && e.getAttributeValue("type", "").startsWith("xliff-")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				// assume bpt from xliff
				if (xliffbpts == null) {
					xliffbpts = new Hashtable<>();
				}
				xliffbpts.put(e.getAttributeValue("i", ""), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return e.getText();
			}
			if (e.getName().equals("bpt") && !e.getAttributeValue("type", "").startsWith("xliff-")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				// bpt from TMX
				String rid = " rid=\"" + XMLUtils.cleanText(e.getAttributeValue("i", "")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				String bid = " id=\"" + XMLUtils.cleanText(e.getAttributeValue("i", "")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				usedIDs.put(e.getAttributeValue("i"), ""); //$NON-NLS-1$ //$NON-NLS-2$
				String xid = XMLUtils.cleanText(e.getAttributeValue("x", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if (!xid.equals("")) { //$NON-NLS-1$
					xid = " xid=\"" + xid + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
				String ctype = XMLUtils.cleanText(e.getAttributeValue("type", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if (!ctype.equals("")) { //$NON-NLS-1$
					if (!validCtypes.containsKey(ctype) && !ctype.startsWith("x-")) { //$NON-NLS-1$
						ctype = "x-" + ctype; //$NON-NLS-1$
					}
					ctype = " ctype=\"" + ctype + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
				String text = "<" + type + bid + rid + xid + ctype + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				List<XMLNode> content = e.getContent();
				for (int i = 0; i < content.size(); i++) {
					XMLNode n = content.get(i);
					if (n.getNodeType() == XMLNode.TEXT_NODE) {
						text = text + n.toString();
					}
					if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
						text = text + tmx2xlf((Element) n);
					}
				}
				return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			// ept from TMX
			if (e.getName().equals("ept")) { //$NON-NLS-1$
				String eid = XMLUtils.cleanText(e.getAttributeValue("i")); //$NON-NLS-1$
				if (xliffbpts != null && xliffbpts.containsKey(eid)) {
					// <ept> that closes a previous <bpt> from xliff
					xliffbpts = null;
					return e.getText();
				}
				e.setAttribute("id", eid); //$NON-NLS-1$
				e.setAttribute("rid", eid); //$NON-NLS-1$
				e.removeAttribute("i"); //$NON-NLS-1$
				return e.toString();
			}
			// this <bpt>/<ept> pair was generated from
			// a <g> or <x> tag in XLIFF
			return e.getText();
		}

		if (type.equals("sub")) { //$NON-NLS-1$
			String text = "<" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			List<XMLNode> content = e.getContent();
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					text = text + n.toString();
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					text = text + tmx2xlf((Element) n);
				}
			}
			return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (type.equals("ut")) { //$NON-NLS-1$
			String text = "<ph id=\"" + newID() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$
			List<XMLNode> content = e.getContent();
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					text = text + n.toString();
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					text = text + tmx2xlf((Element) n);
				}
			}
			return text + "</ph>"; //$NON-NLS-1$
		}

		if (type.equals("hi")) { //$NON-NLS-1$
			String mtype = XMLUtils.cleanText(e.getAttributeValue("type", "")); //$NON-NLS-1$ //$NON-NLS-2$
			if (!mtype.equals("")) { //$NON-NLS-1$
				mtype = " mtype=\"" + mtype + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				mtype = " mtype=\"hi\""; //$NON-NLS-1$
			}
			String text = "<mrk" + mtype + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			List<XMLNode> content = e.getContent();
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					text = text + n.toString();
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					text = text + tmx2xlf((Element) n);
				}
			}
			return text + "</mrk>"; //$NON-NLS-1$
		}

		return e.toString();
	}

	private static String clean(String string) {
		String result = string.replaceAll("<", TMXExporter.MATHLT); //$NON-NLS-1$
		result = result.replaceAll(">", TMXExporter.MATHGT); //$NON-NLS-1$
		result = result.replaceAll("\"", TMXExporter.DOUBLEPRIME); //$NON-NLS-1$
		return replaceAmp(result);
	}

	private static String replaceAmp(String value) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '&') {
				result = result + TMXExporter.GAMP;
			} else {
				result = result + c;
			}
		}
		return result;
	}

	private void cleanCtype(Element e) {
		if (e.getName().equals("ph") //$NON-NLS-1$
				|| e.getName().equals("x")) //$NON-NLS-1$
		{
			String value = e.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!value.equals("")) { //$NON-NLS-1$
				if (!phCtypes.containsKey(value) && !value.startsWith("x-")) { //$NON-NLS-1$
					e.setAttribute("ctype", "x-" + value); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		if (e.getName().equals("bpt") //$NON-NLS-1$
				|| e.getName().equals("sub") //$NON-NLS-1$
				|| e.getName().equals("bx") //$NON-NLS-1$
				|| e.getName().equals("g") //$NON-NLS-1$
				|| e.getName().equals("it")) //$NON-NLS-1$
		{
			String value = e.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!value.equals("")) { //$NON-NLS-1$
				if (!validCtypes.containsKey(value) && !value.startsWith("x-")) { //$NON-NLS-1$
					e.setAttribute("ctype", "x-" + value); //$NON-NLS-1$ //$NON-NLS-2$
				}
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

	private String newID() {
		int i = 0;
		while (usedIDs.containsKey("" + i)) { //$NON-NLS-1$
			i++;
		}
		usedIDs.put("" + i, ""); //$NON-NLS-1$ //$NON-NLS-2$
		return "" + i; //$NON-NLS-1$
	}

	private static double wrongTags(Element x, Element y, double tagPenalty) {
		Vector<Element> tags = new Vector<>();
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
		Element altSrc = match.getChild("source"); //$NON-NLS-1$
		Element altTgt = match.getChild("target"); //$NON-NLS-1$
		List<Element> srcList = src.getChildren();
		List<Element> altSrcList = altSrc.getChildren();
		List<Element> altTgtList = altTgt.getChildren();
		if (altSrcList.size() != altTgtList.size()) {
			cleanCtype(match);
			return match;
		}
		if (srcList.size() == 1 && altSrcList.size() == 0) {
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
		if (srcList.size() == 0 && altSrcList.size() > 0) {
			// remove all tags from the match
			List<XMLNode> content = altSrc.getContent();
			Iterator<XMLNode> i = content.iterator();
			Vector<XMLNode> newContent = new Vector<>();
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
		Hashtable<String, Element> srcTable = new Hashtable<>();
		for (int i = 0; i < srcList.size(); i++) {
			Element e = srcList.get(i);
			srcTable.put(e.getAttributeValue("id", "-1"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		List<XMLNode> content = altSrc.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) n;
				if (e.getName().equals("ph")) { //$NON-NLS-1$
					Element o = srcTable.get(e.getAttributeValue("id", "-2")); //$NON-NLS-1$ //$NON-NLS-2$
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
				if (e.getName().equals("ph")) { //$NON-NLS-1$
					Element o = srcTable.get(e.getAttributeValue("id", "-2")); //$NON-NLS-1$ //$NON-NLS-2$
					if (o != null && !e.equals(o)) {
						e.clone(o);
					}
				}
			}
		}
		cleanCtype(match);
		return match;
	}

	public void removeProject(Project project) throws IOException {
		long id = project.getId();
		File fluenta = Preferences.getPreferencesDir();
		File projectFolder = new File(fluenta, "" + id); //$NON-NLS-1$
		deltree(projectFolder);
		projectsMap.remove(id);
		projectdb.commit();
		try {
			removeMemory(id);
		} catch (IOException ioe) {
			// do nothing
		}
	}

	public void removeMemory(long id) throws IOException {
		Vector<Project> projects = getProjects();
		for (int i = 0; i < projects.size(); i++) {
			Vector<Memory> memories = projects.get(i).getMemories();
			for (int j = 0; j < memories.size(); j++) {
				if (memories.get(j).getId() == id) {
					throw new IOException(Messages.getString("LocalController.50")); //$NON-NLS-1$
				}
			}
		}
		if (memoriesMap == null) {
			openMemories();
		}
		memoriesMap.remove(id);
		memorydb.commit();
		File fluenta = Preferences.getPreferencesDir();
		File tmFolder = new File(fluenta, "TMEngines/" + id); //$NON-NLS-1$
		deltree(tmFolder);
	}

	private void deltree(File file) throws IOException {
		if (file.isFile()) {
			Files.delete(Paths.get(file.toURI()));
		} else {
			File[] list = file.listFiles();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					deltree(list[i]);
				}
			}
			Files.delete(Paths.get(file.toURI()));
		}
	}

	public void exportTMX(Memory memory, String file)
			throws ClassNotFoundException, IOException, SQLException, SAXException, ParserConfigurationException {
		InternalDatabase database = getTMEngine(memory.getId());
		Set<String> languages = database.getAllLanguages();
		String langs = ""; //$NON-NLS-1$
		Iterator<String> it = languages.iterator();
		while (it.hasNext()) {
			String lang = it.next();
			if (langs.equals("")) { //$NON-NLS-1$
				langs = lang;
			} else {
				langs = langs + ";" + lang; //$NON-NLS-1$
			}
		}
		database.exportDatabase(file, langs, memory.getSrcLanguage().getCode(), null);
		database.close();
		database = null;
	}

	public Project getProject(long id) throws IOException {
		if (projectdb == null) {
			openProjects();
		}
		return projectsMap.get(id);
	}

	private void recurse(Element e) {
		e.removeAttribute("class"); //$NON-NLS-1$
		e.removeAttribute("xmlns:ditaarch"); //$NON-NLS-1$
		e.removeAttribute("ditaarch:DITAArchVersion"); //$NON-NLS-1$
		e.removeAttribute("domains"); //$NON-NLS-1$
		if (e.getName().equals("mapref") && e.getAttributeValue("format", "").equals("ditamap")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			e.removeAttribute("format"); //$NON-NLS-1$
		}
		if (e.getName().equals("keydef") && e.getAttributeValue("processing-role", "").equals("resource-only")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			e.removeAttribute("processing-role"); //$NON-NLS-1$
		}
		if (e.getName().equals("image") && e.getAttributeValue("placement", "").equals("inline")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			e.removeAttribute("placement"); //$NON-NLS-1$
		}
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next());
		}
	}

}
