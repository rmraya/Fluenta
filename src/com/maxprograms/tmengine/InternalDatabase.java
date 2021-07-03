/*******************************************************************************
 * Copyright (c) 2015-2021 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

 package com.maxprograms.tmengine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.h2.tools.RunScript;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.maxprograms.languages.RegistryParser;
import com.maxprograms.tmx.TMXReader;
import com.maxprograms.utils.TMUtils;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.Indenter;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLOutputter;
import com.maxprograms.xml.XMLUtils;

public class InternalDatabase implements IDatabase {

	protected static final Logger LOGGER = System.getLogger(InternalDatabase.class.getName());

	private String dbname;
	private Connection conn;
	private PreparedStatement storeTUV;
	private PreparedStatement deleteTUV;
	private PreparedStatement searchTUV;
	private String currProject;
	private String currSubject;
	private String currCustomer;
	private FileOutputStream output;
	private String creationDate;
	private FuzzyIndex fuzzyIndex;
	private TuDatabase tuDb;
	private File database;
	private long next;
	private static Set<String> dblist;
	private static RegistryParser registry;

	public InternalDatabase(String dbname, String workFolder)
			throws SQLException, IOException, ClassNotFoundException, SAXException, ParserConfigurationException {
		this.dbname = dbname;
		creationDate = TMUtils.TMXDate();

		File wfolder = new File(workFolder);
		database = new File(wfolder, dbname);
		boolean exists = database.exists();
		if (!exists) {
			database.mkdirs();
		}
		String url = "jdbc:h2:" + database.getAbsolutePath() + "/db"; //$NON-NLS-1$ //$NON-NLS-2$
		conn = DriverManager.getConnection(url, "sa", "ppaass"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!exists) {
			createTables();
			LOGGER.log(Level.INFO, Messages.getString("InternalDatabase.2")); //$NON-NLS-1$
		}

		storeTUV = conn.prepareStatement("INSERT INTO tuv (tuid, lang, seg, puretext, textlength) VALUES (?,?,?,?,?)"); //$NON-NLS-1$
		searchTUV = conn.prepareStatement("SELECT textlength FROM tuv WHERE tuid=? AND lang=?"); //$NON-NLS-1$
		deleteTUV = conn.prepareStatement("DELETE FROM tuv WHERE tuid=? AND lang=?"); //$NON-NLS-1$
		try {
			tuDb = new TuDatabase(database);
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
			MessageFormat mf = new MessageFormat(Messages.getString("InternalDatabase.14")); //$NON-NLS-1$
			throw new IOException(mf.format(new String[] { dbname }));
		}
		try {
			fuzzyIndex = new FuzzyIndex(database);
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
			MessageFormat mf = new MessageFormat(Messages.getString("InternalDatabase.15")); //$NON-NLS-1$
			throw new IOException(mf.format(new String[] { dbname }));
		}
		registerDatabase(wfolder);
	}

	private void registerDatabase(File wfolder) throws IOException, SAXException, ParserConfigurationException {
		if (dblist == null) {
			loadDbList(wfolder);
		}
		if (!dblist.contains(dbname)) {
			dblist.add(dbname);
			saveDbList(wfolder);
		}
	}

	private static synchronized void saveDbList(File wfolder) throws IOException {
		Document doc = new Document(null, "dblist", null); //$NON-NLS-1$ //$NON-NLS-2$
		Element root = doc.getRootElement();
		Iterator<String> keys = dblist.iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Element db = new Element("db"); //$NON-NLS-1$
			db.setAttribute("quality", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			db.addContent(key);
			root.addContent(db);
		}
		File list = new File(wfolder, "dblist.xml"); //$NON-NLS-1$
		try (FileOutputStream output = new FileOutputStream(list)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			Indenter.indent(root, 2);
			outputter.output(doc, output);
		}
	}

	private static void loadDbList(File wfolder) throws SAXException, IOException, ParserConfigurationException {
		dblist = Collections.synchronizedSortedSet(new TreeSet<>());
		File list = new File(wfolder, "dblist.xml"); //$NON-NLS-1$
		if (!list.exists()) {
			return;
		}
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(list);
		List<Element> dbs = doc.getRootElement().getChildren("db"); //$NON-NLS-1$
		Iterator<Element> it = dbs.iterator();
		while (it.hasNext()) {
			Element db = it.next();
			dblist.add(db.getText());
		}
	}

	private void createTables() throws SQLException, IOException {
		URL url = InternalDatabase.class.getResource("internal.sql"); //$NON-NLS-1$
		RunScript.execute(conn, new InputStreamReader(url.openStream()));
	}

	@Override
	public void close() throws Exception {
		storeTUV.close();
		storeTUV = null;
		deleteTUV.close();
		deleteTUV = null;
		searchTUV.close();
		searchTUV = null;
		conn.commit();
		conn.close();
		conn = null;
		fuzzyIndex.commit();
		fuzzyIndex.close();
		fuzzyIndex = null;
		tuDb.commit();
		tuDb.close();
		tuDb = null;
	}

	@Override
	public String getName() {
		return dbname;
	}

	@Override
	public boolean getQuality() throws IOException {
		return true;
	}

	private void startTransaction() throws SQLException {
		conn.setAutoCommit(false);
	}

	@Override
	synchronized public void commit() throws SQLException {
		conn.commit();
		fuzzyIndex.commit();
		tuDb.commit();
	}

	private void rollback() throws SQLException {
		conn.rollback();
		fuzzyIndex.rollback();
		tuDb.rollback();
	}

	@Override
	public int[] storeTMX(String tmxFile, String user, String project, String customer, String subject, boolean update,
			ILogger logger) {
		int imported = 0;
		int failed = 0;
		next = 0l;
		if (customer == null) {
			customer = ""; //$NON-NLS-1$
		}
		if (subject == null) {
			subject = ""; //$NON-NLS-1$
		}
		if (project == null) {
			project = ""; //$NON-NLS-1$
		}
		currProject = project;
		currSubject = subject;
		currCustomer = customer;
		creationDate = creationDate();

		try {
			startTransaction();

			TMXReader reader = new TMXReader(this, logger);
			reader.parse(new File(tmxFile));
			imported = reader.getCount();
			failed = reader.getdiscared();

			commit();
			return new int[] { imported - failed, failed };
		} catch (SQLException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
			try {
				rollback();
				imported = 0;
			} catch (Exception e1) {
				// do nothing else
			}
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
		}
		currProject = null;
		currSubject = null;
		currCustomer = null;
		return new int[] { imported, failed };
	}

	@Override
	public Vector<String> exportDatabase(String tmxfile, String langs, String srcLang,
			Hashtable<String, Set<String>> propFilters) {
		boolean filter;
		Set<String> languages = Collections.synchronizedSet(new HashSet<>());
		if (langs.equals("")) { //$NON-NLS-1$
			filter = false;
		} else {
			filter = true;
			StringTokenizer tokenizer = new StringTokenizer(langs, ";"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				try {
					String lang = TMUtils.normalizeLang(tokenizer.nextToken());
					if (lang != null) {
						languages.add(lang);
					}
				} catch (Exception e) {
					LOGGER.log(Level.ERROR, e.getMessage(), e);
				}
			}
		}
		Vector<String> result = new Vector<>();

		try {

			output = new FileOutputStream(tmxfile);
			writeHeader(srcLang);
			writeString("<body>"); //$NON-NLS-1$

			try (PreparedStatement stmt = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
				SAXBuilder bld = new SAXBuilder(false);

				Set<Integer> set = tuDb.getKeys();
				Iterator<Integer> it = set.iterator();
				while (it.hasNext()) {
					Element t = tuDb.getTu(it.next());
					Element tu = new Element("tu"); //$NON-NLS-1$
					tu.clone(t);
					String tuid = tu.getAttributeValue("tuid"); //$NON-NLS-1$
					stmt.setString(1, tuid);
					int count = 0;
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							String lang = rs.getString(1);
							String seg = rs.getString(2);
							if (filter && !languages.contains(lang)) {
								continue;
							}
							if (seg.equals("<seg></seg>")) { //$NON-NLS-1$
								continue;
							}
							try {
								try (ByteArrayInputStream stream = new ByteArrayInputStream(
										seg.getBytes(StandardCharsets.UTF_8))) { // $NON-NLS-1$
									Document d = bld.build(stream);
									Element tuv = new Element("tuv"); //$NON-NLS-1$
									tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
									tuv.addContent("\n\t\t"); //$NON-NLS-1$
									tuv.addContent(d.getRootElement());
									tuv.addContent("\n\t"); //$NON-NLS-1$
									tu.addContent("\n\t"); //$NON-NLS-1$
									tu.addContent(tuv);
									count++;
								}
							} catch (Exception e) {
								System.err.println(e.getMessage());
								System.err.println("seg: " + seg); //$NON-NLS-1$
							}
						}
					}
					if (count >= 2) {
						tu.addContent("\n"); //$NON-NLS-1$
						if (propFilters == null) {
							writeString("\n"); //$NON-NLS-1$
							writeString(tu.toString());
						} else {
							Hashtable<String, String> properties = new Hashtable<>();
							List<Element> props = tu.getChildren("prop"); //$NON-NLS-1$
							Iterator<Element> pr = props.iterator();
							while (pr.hasNext()) {
								Element prop = pr.next();
								properties.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
							}
							Enumeration<String> en = properties.keys();
							boolean found = false;
							while (en.hasMoreElements()) {
								String key = en.nextElement();
								String value = properties.get(key);
								if (value != null && propFilters.containsKey(key)) {
									Set<String> values = propFilters.get(key);
									Iterator<String> val = values.iterator();
									while (val.hasNext()) {
										if (value.equals(val.next())) {
											found = true;
											break;
										}
									}
									values = null;
								}
								key = null;
								value = null;
							}
							if (found) {
								writeString("\n"); //$NON-NLS-1$
								writeString(tu.toString());
							}
						}
					}
				}
			}

			writeString("\n</body>\n"); //$NON-NLS-1$
			writeString("</tmx>\n"); //$NON-NLS-1$
			output.close();
			result.add(Constants.SUCCESS);
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
			result.add(Constants.ERROR);
			String message = e.getLocalizedMessage();
			if (message == null) {
				message = e.getMessage();
			}
			if (message == null) {
				message = Messages.getString("InternalDatabase.50"); //$NON-NLS-1$
			}
			result.add(message);
		}

		return result;
	}

	private void writeString(String string) throws IOException {
		output.write(string.getBytes(StandardCharsets.UTF_8)); // $NON-NLS-1$
	}

	private void writeString16(String string) throws IOException {
		output.write(string.getBytes("UTF-16LE")); //$NON-NLS-1$
	}

	private void writeHeader(String srcLang) throws IOException {
		writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		writeString(
				"<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >\n"); //$NON-NLS-1$
		writeString("<tmx version=\"1.4\">\n"); //$NON-NLS-1$
		writeString("<header \n" + //$NON-NLS-1$
				"      creationtool=\"" //$NON-NLS-1$
				+ Constants.TOOLNAME + "\" \n" + //$NON-NLS-1$
				"      creationtoolversion=\"" + //$NON-NLS-1$
				Constants.VERSION + "\" \n" + //$NON-NLS-1$
				"      srclang=\"" + //$NON-NLS-1$
				srcLang + "\" \n" + //$NON-NLS-1$
				"      adminlang=\"en\"  \n" + //$NON-NLS-1$
				"      datatype=\"xml\" \n" + //$NON-NLS-1$
				"      o-tmf=\"unknown\" \n" + //$NON-NLS-1$
				"      segtype=\"block\" \n" + //$NON-NLS-1$
				"      creationdate=\"" + //$NON-NLS-1$
				creationDate() + "\"\n>\n" + //$NON-NLS-1$
				"</header>\n"); //$NON-NLS-1$
	}

	@Override
	public Vector<String> flag(String tuid) {
		Vector<String> result = new Vector<>();
		Element tu = tuDb.getTu(tuid);
		if (tu != null) {
			Element prop = new Element("prop"); //$NON-NLS-1$
			prop.setAttribute("type", "x-flag"); //$NON-NLS-1$ //$NON-NLS-2$
			prop.setText("SW-Flag"); //$NON-NLS-1$
			List<XMLNode> list = tu.getContent();
			for (int i = 0; i < list.size(); i++) {
				XMLNode node = list.get(i);
				if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
					Element e = (Element) node;
					if (e.getName().equals("tuv")) { //$NON-NLS-1$
						list.add(i, prop);
						break;
					}
				}
			}
			tu.setContent(list);
			tuDb.store(tuid, tu);
			result.add(Constants.SUCCESS);
		} else {
			result.add(Constants.ERROR);
			result.add(Messages.getString("InternalDatabase.72")); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public Set<String> getAllCustomers() {
		return tuDb.getCustomers();
	}

	@Override
	public Set<String> getAllLanguages() {
		Set<String> result = Collections.synchronizedSortedSet(new TreeSet<>());
		try {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs = stmt.executeQuery("SELECT DISTINCT lang FROM tuv")) { //$NON-NLS-1$
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
		}
		return result;
	}

	@Override
	public Set<String> getAllProjects() {
		return tuDb.getProjects();
	}

	@Override
	public Set<String> getAllSubjects() {
		return tuDb.getSubjects();
	}

	@Override
	public Vector<TU> searchAllTranslations(String searchStr, String srcLang, int similarity, boolean caseSensitive) {
		// search for TUs with any target language
		Vector<TU> result = new Vector<>();
		try {
			int[] ngrams = null;
			ngrams = NGrams.getNGrams(searchStr, true);

			int size = ngrams.length;
			if (size == 0) {
				return result;
			}
			int min = size * similarity / 100;
			int max = size * (200 - similarity) / 100;

			int minLength = searchStr.length() * similarity / 100;
			int maxLength = searchStr.length() * (200 - similarity) / 100;

			Hashtable<String, Integer> candidates = new Hashtable<>();

			try (PreparedStatement stmt = conn.prepareStatement(
					"SELECT puretext, textlength FROM tuv WHERE lang=? AND tuid=? AND textlength>=? AND textlength<=?")) { //$NON-NLS-1$
				stmt.setString(1, srcLang);
				stmt.setInt(3, minLength);
				stmt.setInt(4, maxLength);

				try (PreparedStatement stmt2 = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$

					NavigableSet<Fun.Tuple2<Integer, String>> index = fuzzyIndex.getIndex(srcLang);
					for (int i = 0; i < ngrams.length; i++) {
						Iterable<String> keys = Fun.filter(index, ngrams[i]);
						Iterator<String> it = keys.iterator();
						while (it.hasNext()) {
							String tuid = it.next();
							if (candidates.containsKey(tuid)) {
								int count = candidates.get(tuid);
								candidates.put(tuid, count + 1);
							} else {
								candidates.put(tuid, 1);
							}
						}
					}
					Enumeration<String> tuids = candidates.keys();
					while (tuids.hasMoreElements()) {
						String tuid = tuids.nextElement();
						int count = candidates.get(tuid);
						if (count >= min && count <= max) {
							stmt.setString(2, tuid);
							try (ResultSet rs = stmt.executeQuery()) {
								while (rs.next()) {
									String pure = rs.getString(1);
									int distance;
									if (caseSensitive) {
										distance = MatchQuality.similarity(searchStr, pure);
									} else {
										distance = MatchQuality.similarity(searchStr.toLowerCase(), pure.toLowerCase());
									}
									if (distance >= similarity) {
										Element tu = tuDb.getTu(tuid);
										stmt2.setString(1, tuid);
										try (ResultSet rs2 = stmt2.executeQuery()) {
											while (rs2.next()) {
												String lang = rs2.getString(1);
												String seg = rs2.getNString(2);
												try (ByteArrayInputStream stream = new ByteArrayInputStream(
														seg.getBytes(StandardCharsets.UTF_8))) {
													SAXBuilder bld = new SAXBuilder(false);
													Document d = bld.build(stream);
													Element tuv = new Element("tuv"); //$NON-NLS-1$
													tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
													tuv.addContent("\n"); //$NON-NLS-1$
													tuv.addContent(d.getRootElement());
													tuv.addContent("\n"); //$NON-NLS-1$
													tu.addContent("\n"); //$NON-NLS-1$
													tu.addContent(tuv);
												}
											}
										}
										TU t = Element2TU(tu);
										t.setProperty("similarity", "" + distance); //$NON-NLS-1$ //$NON-NLS-2$
										result.add(t);
									}
								}
							}
						}
					}
				}
			}
		} catch (SQLException | SAXException | IOException | ParserConfigurationException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
		}
		return result;
	}

	private static synchronized TU Element2TU(Element tu) {
		TU result = new TU();
		String tuid = tu.getAttributeValue("tuid"); //$NON-NLS-1$
		Set<String> langs = Collections.synchronizedSet(new HashSet<>());
		Hashtable<String, Tuv> tuvs = new Hashtable<>();

		List<Element> tuvList = tu.getChildren("tuv"); //$NON-NLS-1$
		Iterator<Element> jt = tuvList.iterator();
		while (jt.hasNext()) {
			Element tuv = jt.next();
			String lang = tuv.getAttributeValue("xml:lang"); //$NON-NLS-1$
			langs.add(lang);
			String pureText = TMUtils.pureText(tuv.getChild("seg")); //$NON-NLS-1$
			String seg = tuv.getChild("seg").toString(); //$NON-NLS-1$
			if (seg.endsWith("</seg>")) { //$NON-NLS-1$
				seg = seg.substring(5, seg.length() - 6).trim();
			} else {
				// empty element, ends with "/>"
				seg = ""; //$NON-NLS-1$
			}
			Tuv tuvData = new Tuv(tuid, lang, seg, pureText);

			List<Element> props = tuv.getChildren("prop"); //$NON-NLS-1$
			if (!props.isEmpty()) {
				Hashtable<String, String> table = new Hashtable<>();
				Iterator<Element> pt = props.iterator();
				while (pt.hasNext()) {
					Element prop = pt.next();
					table.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
				}
				tuvData.setProperties(table);
			}
			tuvs.put(lang, tuvData);
		}
		Hashtable<String, String> props = new Hashtable<>();
		List<Element> properties = tu.getChildren("prop"); //$NON-NLS-1$
		props.put("tuid", tuid); //$NON-NLS-1$
		Iterator<Element> kt = properties.iterator();
		while (kt.hasNext()) {
			Element prop = kt.next();
			props.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
		}
		Vector<String> notes = new Vector<>();
		List<Element> children = tu.getChildren("note"); //$NON-NLS-1$
		Iterator<Element> nt = children.iterator();
		while (nt.hasNext()) {
			notes.add(nt.next().toString());
		}

		result.setLangs(langs);
		result.setNotes(notes);
		result.setTuvs(tuvs);
		result.setProps(props);
		result.setCreationDate(tu.getAttributeValue("creationdate", "")); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}

	@Override
	public Vector<TU> searchTranslation(String searchStr, String srcLang, String tgtLang, int similarity,
			boolean caseSensitive) {
		// search for TUs with a given source and target language
		Vector<TU> result = new Vector<>();
		try {
			int[] ngrams = null;
			ngrams = NGrams.getNGrams(searchStr, true);
			int size = ngrams.length;
			if (size == 0) {
				return result;
			}
			int min = size * similarity / 100;
			int max = size * (200 - similarity) / 100;

			int minLength = searchStr.length() * similarity / 100;
			int maxLength = searchStr.length() * (200 - similarity) / 100;

			Hashtable<String, Integer> candidates = new Hashtable<>();

			try (PreparedStatement stmt = conn.prepareStatement(
					"SELECT puretext, seg, textlength FROM tuv WHERE lang=? AND tuid=? AND textlength>=? AND textlength<=?")) { //$NON-NLS-1$
				stmt.setString(1, srcLang);
				stmt.setInt(3, minLength);
				stmt.setInt(4, maxLength);

				try (PreparedStatement stmt2 = conn
						.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=? AND lang=?")) { //$NON-NLS-1$
					stmt2.setString(2, tgtLang);

					NavigableSet<Fun.Tuple2<Integer, String>> index = fuzzyIndex.getIndex(srcLang);
					for (int i = 0; i < ngrams.length; i++) {
						Iterable<String> keys = Fun.filter(index, ngrams[i]);
						Iterator<String> it = keys.iterator();
						while (it.hasNext()) {
							String tuid = it.next();
							if (candidates.containsKey(tuid)) {
								int count = candidates.get(tuid);
								candidates.put(tuid, count + 1);
							} else {
								candidates.put(tuid, 1);
							}
						}
					}
					Enumeration<String> tuids = candidates.keys();
					while (tuids.hasMoreElements()) {
						String tuid = tuids.nextElement();
						int count = candidates.get(tuid);
						if (count >= min && count <= max) {
							stmt.setString(2, tuid);
							try (ResultSet rs = stmt.executeQuery()) {
								while (rs.next()) {
									String pure = rs.getString(1);
									String srcSeg = rs.getString(2);
									int distance;
									if (caseSensitive) {
										distance = MatchQuality.similarity(searchStr, pure);
									} else {
										distance = MatchQuality.similarity(searchStr.toLowerCase(), pure.toLowerCase());
									}
									if (distance >= similarity) {
										Element tu = tuDb.getTu(tuid);
										stmt2.setString(1, tuid);
										stmt2.setString(2, tgtLang);
										boolean tgtFound = false;
										try (ResultSet rs2 = stmt2.executeQuery()) {
											while (rs2.next()) {
												String lang = rs2.getString(1);
												String seg = rs2.getNString(2);
												ByteArrayInputStream stream = new ByteArrayInputStream(
														seg.getBytes(StandardCharsets.UTF_8)); // $NON-NLS-1$
												SAXBuilder bld = new SAXBuilder(false);
												try {
													Document d = bld.build(stream);
													stream.close();
													stream = null;

													Element tuv = new Element("tuv"); //$NON-NLS-1$
													tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
													tuv.addContent("\n"); //$NON-NLS-1$
													tuv.addContent(d.getRootElement());
													tuv.addContent("\n"); //$NON-NLS-1$
													tu.addContent("\n"); //$NON-NLS-1$
													tu.addContent(tuv);
													tgtFound = true;
												} catch (SAXException sax) {
													// ignore
												}
											}
										}
										if (tgtFound) {
											try (ByteArrayInputStream stream = new ByteArrayInputStream(
													srcSeg.getBytes(StandardCharsets.UTF_8))) { // $NON-NLS-1$
												SAXBuilder bld = new SAXBuilder(false);
												Document d = bld.build(stream);
												Element tuv = new Element("tuv"); //$NON-NLS-1$
												tuv.setAttribute("xml:lang", srcLang); //$NON-NLS-1$
												tuv.addContent("\n"); //$NON-NLS-1$
												tuv.addContent(d.getRootElement());
												tuv.addContent("\n"); //$NON-NLS-1$
												tu.addContent("\n"); //$NON-NLS-1$
												tu.addContent(tuv);
												TU t = Element2TU(tu);
												t.setProperty("similarity", "" + distance); //$NON-NLS-1$ //$NON-NLS-2$
												result.add(t);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (SAXException | SQLException | IOException | ParserConfigurationException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
		}
		return result;
	}

	@Override
	public Vector<TU> concordanceSearch(String searchStr, String srcLang, int limit, boolean isRegexp,
			boolean caseSensitive) {
		Vector<TU> result = new Vector<>();
		Vector<String> candidates = new Vector<>();
		if (isRegexp) {
			try {
				try (PreparedStatement stmt = conn.prepareStatement(
						"SELECT tuid, puretext FROM tuv WHERE lang=?  AND puretext REGEXP ? LIMIT ?")) { //$NON-NLS-1$
					stmt.setString(1, srcLang);
					stmt.setString(2, searchStr);
					stmt.setInt(3, limit);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							candidates.add(rs.getString(1));
						}
					}
				}
			} catch (SQLException e) {
				LOGGER.log(Level.ERROR, e.getMessage(), e);
			}
		} else {
			if (!caseSensitive) {
				try {
					try (PreparedStatement stmt = conn.prepareStatement(
							"SELECT tuid, puretext FROM tuv WHERE lang=? AND LOWER(puretext) LIKE ? LIMIT ?")) { //$NON-NLS-1$
						stmt.setString(1, srcLang);
						stmt.setString(2, "%" + searchStr.toLowerCase() + "%"); //$NON-NLS-1$ //$NON-NLS-2$
						stmt.setInt(3, limit);
						try (ResultSet rs = stmt.executeQuery()) {
							while (rs.next()) {
								candidates.add(rs.getString(1));
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.log(Level.ERROR, e.getMessage(), e);
				}
			} else {
				try {
					try (PreparedStatement stmt = conn.prepareStatement(
							"SELECT tuid, puretext FROM tuv WHERE lang=? AND puretext LIKE ? LIMIT ?")) { //$NON-NLS-1$
						stmt.setString(1, srcLang);
						stmt.setString(2, "%" + searchStr + "%"); //$NON-NLS-1$ //$NON-NLS-2$
						stmt.setInt(3, limit);
						try (ResultSet rs = stmt.executeQuery()) {
							while (rs.next()) {
								candidates.add(rs.getString(1));
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.log(Level.ERROR, e.getMessage(), e);
				}
			}
		}
		try {
			try (PreparedStatement stmt2 = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
				Iterator<String> it = candidates.iterator();
				SAXBuilder bld = new SAXBuilder(false);
				while (it.hasNext()) {
					String tuid = it.next();
					Element tu = tuDb.getTu(tuid);
					stmt2.setString(1, tuid);
					try (ResultSet rs2 = stmt2.executeQuery()) {
						while (rs2.next()) {
							String lang = rs2.getString(1);
							String seg = rs2.getNString(2);

							try (ByteArrayInputStream stream = new ByteArrayInputStream(
									seg.getBytes(StandardCharsets.UTF_8))) {
								Document d = bld.build(stream);
								Element tuv = new Element("tuv"); //$NON-NLS-1$
								tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
								tuv.addContent("\n"); //$NON-NLS-1$
								tuv.addContent(d.getRootElement());
								tuv.addContent("\n"); //$NON-NLS-1$
								tu.addContent("\n"); //$NON-NLS-1$
								tu.addContent(tuv);
							}
						}
					}
					result.add(Element2TU(tu));
				}
				bld = null;
			}
		} catch (SQLException | SAXException | IOException | ParserConfigurationException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
		}

		return result;
	}

	@Override
	public void storeTU(Element tu, String sourceLang) throws Exception {
		Set<String> tuLangs = Collections.synchronizedSortedSet(new TreeSet<>());
		List<Element> tuvs = tu.getChildren("tuv"); //$NON-NLS-1$
		String tuid = tu.getAttributeValue("tuid", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (tuid.equals("")) { //$NON-NLS-1$
			tuid = nextId();
			tu.setAttribute("tuid", tuid); //$NON-NLS-1$
		}

		Hashtable<String, String> props = new Hashtable<>();
		List<Element> properties = tu.getChildren("prop"); //$NON-NLS-1$
		Iterator<Element> kt = properties.iterator();
		while (kt.hasNext()) {
			Element prop = kt.next();
			props.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
		}
		if (currSubject != null && !currSubject.equals("")) { //$NON-NLS-1$
			if (!props.containsKey("subject")) { //$NON-NLS-1$
				Element prop = new Element("prop"); //$NON-NLS-1$
				prop.setAttribute("type", "subject"); //$NON-NLS-1$//$NON-NLS-2$
				prop.setText(XMLUtils.cleanText(currSubject));
				List<Element> content = tu.getChildren();
				content.add(0, prop);
				tu.setChildren(content);
				props.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
			}
		}
		String sub = props.get("subject"); //$NON-NLS-1$
		if (sub != null) {
			tuDb.storeSubject(sub);
		}
		if (currCustomer != null && !currCustomer.equals("")) { //$NON-NLS-1$
			if (!props.containsKey("customer")) { //$NON-NLS-1$
				Element prop = new Element("prop"); //$NON-NLS-1$
				prop.setAttribute("type", "customer"); //$NON-NLS-1$//$NON-NLS-2$
				prop.setText(XMLUtils.cleanText(currCustomer));
				List<Element> content = tu.getChildren();
				content.add(0, prop);
				tu.setChildren(content);
				props.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
			}
		}
		String cust = props.get("customer"); //$NON-NLS-1$
		if (cust != null) {
			tuDb.storeCustomer(cust);
		}
		if (currProject != null && !currProject.equals("")) { //$NON-NLS-1$
			if (!props.containsKey("project")) { //$NON-NLS-1$
				Element prop = new Element("prop"); //$NON-NLS-1$
				prop.setAttribute("type", "project"); //$NON-NLS-1$//$NON-NLS-2$
				prop.setText(XMLUtils.cleanText(currProject));
				List<Element> content = tu.getChildren();
				content.add(0, prop);
				tu.setChildren(content);
				props.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
			}
		}
		String proj = props.get("project"); //$NON-NLS-1$
		if (proj != null) {
			tuDb.storeProject(proj);
		}
		if (tu.getAttributeValue("creationdate", "").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tu.setAttribute("creationdate", creationDate); //$NON-NLS-1$
		}
		if (tu.getAttributeValue("creationid", "").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tu.setAttribute("creationid", System.getProperty("user.name")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		storeTUV.setString(1, tuid);

		Iterator<Element> it = tuvs.iterator();
		while (it.hasNext()) {
			Element tuv = it.next();
			String lang = normalize(tuv.getAttributeValue("xml:lang")); //$NON-NLS-1$
			if (lang == null) {
				// Invalid language code, ignore this tuv
				continue;
			}
			if (!tuLangs.contains(lang)) {
				if (exists(tuid, lang)) {
					delete(tuid, lang);
				}
				Element seg = tuv.getChild("seg"); //$NON-NLS-1$
				String puretext = extractText(seg);
				if (puretext.length() < 1) {
					continue;
				}
				String ele = seg.toString();
				if (ele.length() > 6000) {
					ele = puretext;
				}
				if (ele.length() > 6000) {
					throw new Exception(Messages.getString("InternalDatabase.150")); //$NON-NLS-1$
				}
				int length = puretext.length();
				storeTUV.setString(2, lang);
				storeTUV.setString(3, ele);
				storeTUV.setString(4, puretext);
				storeTUV.setInt(5, length);
				storeTUV.execute();
				tuLangs.add(lang);

				tuDb.store(tuid, tu);

				int[] ngrams = NGrams.getNGrams(puretext, true);
				NavigableSet<Fun.Tuple2<Integer, String>> index = fuzzyIndex.getIndex(lang);
				for (int i = 0; i < ngrams.length; i++) {
					Tuple2<Integer, String> entry = Fun.t2(ngrams[i], tuid);
					if (!index.contains(entry)) {
						index.add(entry);
					}
				}
				ngrams = null;
			}
		}
	}

	private String nextId() {
		if (next == 0l) {
			next = Calendar.getInstance().getTimeInMillis();
		}
		return "" + next++; //$NON-NLS-1$
	}

	private static String normalize(String lang) throws IOException {
		if (registry == null) {
			registry = new RegistryParser();
		}
		if (lang == null) {
			return null;
		}
		if (lang.length() == 2 || lang.length() == 3) {
			if (registry.getTagDescription(lang).length() > 0) {
				return lang.toLowerCase();
			}
			return null;
		}
		lang = lang.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] parts = lang.split("-"); //$NON-NLS-1$

		if (parts.length == 2) {
			if (parts[1].length() == 2) {
				// has country code
				String code = lang.substring(0, 2).toLowerCase() + "-" + lang.substring(3).toUpperCase(); //$NON-NLS-1$
				if (registry.getTagDescription(code).length() > 0) {
					return code;
				}
				return null;
			}
			// may have a script
			String code = lang.substring(0, 2).toLowerCase() + "-" + lang.substring(3, 4).toUpperCase() //$NON-NLS-1$
					+ lang.substring(4).toLowerCase();
			if (registry.getTagDescription(code).length() > 0) {
				return code;
			}
			return null;
		}
		// check if its a valid thing with more than 2 parts
		if (registry.getTagDescription(lang).length() > 0) {
			return lang;
		}
		return null;
	}

	private boolean exists(String tuid, String lang) throws SQLException {
		searchTUV.setString(1, tuid);
		searchTUV.setString(2, lang);
		boolean found = false;
		try (ResultSet rs = searchTUV.executeQuery()) {
			while (rs.next()) {
				found = true;
			}
		}
		return found;
	}

	private void delete(String tuid, String lang) throws SQLException {
		deleteTUV.setString(1, tuid);
		deleteTUV.setString(2, lang);
		deleteTUV.execute();
	}

	protected static String extractText(Element seg) {
		List<XMLNode> l = seg.getContent();
		Iterator<XMLNode> i = l.iterator();
		String text = ""; //$NON-NLS-1$
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.TEXT_NODE) {
				text = text + ((TextNode) o).getText();
			} else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				String type = ((Element) o).getName();
				// discard all inline elements
				// except <sub> and <hi>
				if (type.equals("sub") || type.equals("hi")) { //$NON-NLS-1$ //$NON-NLS-2$
					Element e = (Element) o;
					text = text + extractText(e);
				}
			}
		}
		i = null;
		l = null;
		return text;
	}

	public static void deleteDb(File workFolder, String name) throws Exception {
		deleteTree(new File(workFolder, name));
		if (dblist == null) {
			loadDbList(workFolder);
		}
		if (dblist.contains(name)) {
			dblist.remove(name);
			saveDbList(workFolder);
		}
	}

	static private void deleteTree(File file) throws IOException {
		if (file.isDirectory()) {
			String[] files = file.list();
			for (int i = 0; i < files.length; i++) {
				deleteTree(new File(file, files[i]));
			}
		}
		Files.delete(Paths.get(file.toURI()));
	}

	public void setProject(String project) throws SQLException {
		String query = "UPDATE databases SET project=? WHERE dbname=?"; //$NON-NLS-1$
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, project);
			stmt.setString(2, dbname);
			stmt.execute();
		}
	}

	public void setCustomer(String customer) throws SQLException {
		String query = "UPDATE databases SET client=? WHERE dbname=?"; //$NON-NLS-1$
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, customer);
			stmt.setString(2, dbname);
			stmt.execute();
		}
	}

	public void setSubject(String subject) throws SQLException {
		String query = "UPDATE databases SET subject=? WHERE dbname=?"; //$NON-NLS-1$
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, subject);
			stmt.setString(2, dbname);
			stmt.execute();
		}
	}

	public void setCreationDate(String date) {
		creationDate = date;
	}

	public static String creationDate() {
		// creationdate=""
		Calendar calendar = Calendar.getInstance(Locale.US);
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$//$NON-NLS-2$
				+ calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$//$NON-NLS-2$
				+ calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$//$NON-NLS-2$
				+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") //$NON-NLS-1$//$NON-NLS-2$
				+ calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$//$NON-NLS-2$
				+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		return longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$//$NON-NLS-2$
	}

	public static Set<String> getDbList(File workFolder)
			throws SAXException, IOException, ParserConfigurationException {
		if (dblist == null) {
			loadDbList(workFolder);
		}
		return dblist;
	}

	@Override
	public long getSize() throws SQLException {
		long result = 0l;
		String query = "SELECT COUNT(DISTINCT tuid) FROM tuv"; //$NON-NLS-1$
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				while (rs.next()) {
					result = rs.getLong(1);
				}
			}
		}
		return result;
	}

	@Override
	public TU getTu(int index, String sortLanguage, boolean ascending) throws SQLException {
		if (sortLanguage.equals("")) { //$NON-NLS-1$
			String tuid = ""; //$NON-NLS-1$
			String order = " "; //$NON-NLS-1$
			if (!ascending) {
				order = " DESC"; //$NON-NLS-1$
			}
			String query = "SELECT DISTINCT tuid FROM tuv ORDER BY 1" + order + " LIMIT 1 OFFSET ?"; //$NON-NLS-1$ //$NON-NLS-2$
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setInt(1, index);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						tuid = rs.getString(1);
					}
				}
			}

			Element tu = tuDb.getTu(tuid);
			SAXBuilder bld = new SAXBuilder(false);
			try (PreparedStatement stmt = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
				stmt.setString(1, tuid);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String lang = rs.getString(1);
						String seg = rs.getString(2);
						if (seg.equals("<seg></seg>")) { //$NON-NLS-1$
							continue;
						}
						try {
							try (ByteArrayInputStream stream = new ByteArrayInputStream(
									seg.getBytes(StandardCharsets.UTF_8))) {
								Document d = bld.build(stream);
								Element tuv = new Element("tuv"); //$NON-NLS-1$
								tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
								tuv.addContent(d.getRootElement());
								tu.addContent(tuv);
							}
						} catch (Exception e) {
							LOGGER.log(Level.ERROR, e.getMessage(), e);
						}
					}
				}
			}
			return Element2TU(tu);
		}
		// number of entries may be different for each language
		Hashtable<String, Integer> counts = new Hashtable<>();
		String query = "SELECT DISTINCT lang FROM tuv"; //$NON-NLS-1$
		int max = 0;
		try (Statement stmt = conn.createStatement()) {
			String query2 = "SELECT COUNT(tuid) FROM tuv WHERE lang=?"; //$NON-NLS-1$
			try (PreparedStatement stmt2 = conn.prepareStatement(query2)) {
				try (ResultSet rs = stmt.executeQuery(query)) {
					while (rs.next()) {
						String lang = rs.getString(1);
						stmt2.setString(1, lang);
						try (ResultSet rs2 = stmt2.executeQuery()) {
							while (rs2.next()) {
								int count = rs2.getInt(1);
								counts.put(lang, count);
								if (count > max) {
									max = count;
								}
							}
						}
					}
				}
			}
		}
		if (counts.get(sortLanguage) == max) {
			String tuid = ""; //$NON-NLS-1$
			String order = " "; //$NON-NLS-1$
			if (!ascending) {
				order = " DESC"; //$NON-NLS-1$
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT tuid, UPPER(puretext) FROM tuv WHERE lang='"); //$NON-NLS-1$
			sb.append(sortLanguage);
			sb.append("' ORDER BY 2"); //$NON-NLS-1$
			sb.append(order);
			sb.append(" LIMIT 1 OFFSET ?"); //$NON-NLS-1$
			try (PreparedStatement stmt3 = conn.prepareStatement(sb.toString())) {
				stmt3.setInt(1, index);
				try (ResultSet rs3 = stmt3.executeQuery()) {
					while (rs3.next()) {
						tuid = rs3.getString(1);
					}
				}
			}

			Element tu = tuDb.getTu(tuid);
			SAXBuilder bld = new SAXBuilder(false);
			try (PreparedStatement stmt3 = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
				stmt3.setString(1, tuid);
				try (ResultSet rs3 = stmt3.executeQuery()) {
					while (rs3.next()) {
						String lang = rs3.getString(1);
						String seg = rs3.getString(2);
						if (seg.equals("<seg></seg>")) { //$NON-NLS-1$
							continue;
						}
						try {
							try (ByteArrayInputStream stream = new ByteArrayInputStream(
									seg.getBytes(StandardCharsets.UTF_8))) {
								Document d = bld.build(stream);
								Element tuv = new Element("tuv"); //$NON-NLS-1$
								tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
								tuv.addContent(d.getRootElement());
								tu.addContent(tuv);
							}
						} catch (Exception e) {
							LOGGER.log(Level.ERROR, e.getMessage(), e);
						}
					}
				}
			}
			return Element2TU(tu);
		}
		// this language does not have enough entries
		// the database needs to be filled with empty entries before attempting to
		// deliver a result
		query = "SELECT DISTINCT tuid FROM tuv"; //$NON-NLS-1$
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				try (PreparedStatement stmt4 = conn.prepareStatement(
						"INSERT INTO tuv (tuid, lang, seg, puretext, textlength) VALUES (?,?,?,?,?)")) { //$NON-NLS-1$
					stmt4.setString(2, sortLanguage);
					stmt4.setString(3, "<seg></seg>"); //$NON-NLS-1$
					stmt4.setString(4, ""); //$NON-NLS-1$
					stmt4.setInt(5, 0);
					while (rs.next()) {
						String tuid = rs.getString(1);
						stmt4.setString(1, tuid);
						try {
							stmt4.execute();
						} catch (SQLException sql) {
							// ignore duplicate values
						}
					}
				}
			}
		}
		return getTu(index, sortLanguage, ascending);
	}

	@Override
	public Element getTu(String tuid) throws Exception {
		Element tu = tuDb.getTu(tuid);
		try (PreparedStatement stmt = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
			stmt.setString(1, tuid);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String lang = rs.getString(1);
					String seg = rs.getNString(2);
					try (ByteArrayInputStream stream = new ByteArrayInputStream(seg.getBytes(StandardCharsets.UTF_8))) {
						SAXBuilder bld = new SAXBuilder(false);
						Document d = bld.build(stream);
						Element tuv = new Element("tuv"); //$NON-NLS-1$
						tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
						tuv.addContent("\n"); //$NON-NLS-1$
						tuv.addContent(d.getRootElement());
						tuv.addContent("\n"); //$NON-NLS-1$
						tu.addContent("\n"); //$NON-NLS-1$
						tu.addContent(tuv);
					}
				}
			}
		}
		return tu;
	}

	@Override
	public Vector<String> exportCSV(String csvFile, String langs, Hashtable<String, Set<String>> propFilters) {
		boolean filter;
		Set<String> languages = Collections.synchronizedSet(new HashSet<>());
		if (langs.equals("")) { //$NON-NLS-1$
			filter = false;
			languages.addAll(getAllLanguages());
		} else {
			filter = true;
			StringTokenizer tokenizer = new StringTokenizer(langs, ";"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				try {
					String lang = TMUtils.normalizeLang(tokenizer.nextToken());
					if (lang != null) {
						languages.add(lang);
					}
				} catch (Exception e) {
					LOGGER.log(Level.ERROR, e.getMessage(), e);
				}
			}
		}
		Vector<String> result = new Vector<>();

		try {

			output = new FileOutputStream(csvFile);
			byte[] feff = { -1, -2 };
			output.write(feff);

			String s = ""; //$NON-NLS-1$
			Iterator<String> ls = languages.iterator();
			while (ls.hasNext()) {
				if (!s.equals("")) { //$NON-NLS-1$
					s = s + "\t"; //$NON-NLS-1$
				}
				s = s + ls.next();
			}
			writeString16(s);

			try (PreparedStatement stmt = conn.prepareStatement("SELECT lang, seg FROM tuv WHERE tuid=?")) { //$NON-NLS-1$
				SAXBuilder bld = new SAXBuilder(false);

				Set<Integer> set = tuDb.getKeys();
				Iterator<Integer> it = set.iterator();
				while (it.hasNext()) {
					Element tu = tuDb.getTu(it.next());
					String tuid = tu.getAttributeValue("tuid"); //$NON-NLS-1$
					stmt.setString(1, tuid);
					int count = 0;
					Hashtable<String, Element> tuvs = new Hashtable<>();
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							String lang = rs.getString(1);
							String seg = rs.getString(2);
							if (filter && !languages.contains(lang)) {
								continue;
							}
							if (seg.equals("<seg></seg>")) { //$NON-NLS-1$
								continue;
							}
							try {
								try (ByteArrayInputStream stream = new ByteArrayInputStream(
										seg.getBytes(StandardCharsets.UTF_8))) {
									Document d = bld.build(stream);
									Element tuv = new Element("tuv"); //$NON-NLS-1$
									tuv.setAttribute("xml:lang", lang); //$NON-NLS-1$
									tuv.addContent(d.getRootElement());
									tuvs.put(lang, tuv);
									count++;
								}
							} catch (Exception e) {
								LOGGER.log(Level.ERROR, e.getMessage(), e);
							}
						}
					}
					if (count >= 2) {
						if (propFilters == null) {
							writeString16("\n"); //$NON-NLS-1$
							String line = ""; //$NON-NLS-1$
							Iterator<String> lt = languages.iterator();
							while (lt.hasNext()) {
								String l = lt.next();
								if (!line.equals("")) { //$NON-NLS-1$
									line = line + "\t"; //$NON-NLS-1$
								}
								Element tuv = tuvs.get(l);
								if (tuv != null) {
									String text = tuv.getChild("seg").getTextNormalize(); //$NON-NLS-1$
									text = text.replace('\n', ' ');
									line = line + text;
								}
							}
							writeString16(line);
						} else {
							Hashtable<String, String> properties = new Hashtable<>();
							List<Element> props = tu.getChildren("prop"); //$NON-NLS-1$
							Iterator<Element> pr = props.iterator();
							while (pr.hasNext()) {
								Element prop = pr.next();
								properties.put(prop.getAttributeValue("type"), prop.getText()); //$NON-NLS-1$
							}
							Enumeration<String> en = properties.keys();
							boolean found = false;
							while (en.hasMoreElements()) {
								String key = en.nextElement();
								String value = properties.get(key);
								if (value != null && propFilters.containsKey(key)) {
									Set<String> values = propFilters.get(key);
									Iterator<String> val = values.iterator();
									while (val.hasNext()) {
										if (value.equals(val.next())) {
											found = true;
											break;
										}
									}
									values = null;
								}
								key = null;
								value = null;
							}
							if (found) {
								writeString16("\n"); //$NON-NLS-1$
								String line = ""; //$NON-NLS-1$
								Iterator<String> lt = languages.iterator();
								while (lt.hasNext()) {
									String l = lt.next();
									if (!line.equals("")) { //$NON-NLS-1$
										line = line + "\t"; //$NON-NLS-1$
									}
									Element tuv = tuvs.get(l);
									if (tuv != null) {
										String text = tuv.getChild("seg").getTextNormalize(); //$NON-NLS-1$
										text = text.replace('\n', ' ');
										line = line + text;
									}
								}
								writeString16(line);
							}
						}
					}
					tuvs = null;
				}
			}
			output.close();
			result.add(Constants.SUCCESS);
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);
			result.add(Constants.ERROR);
			String message = e.getLocalizedMessage();
			if (message == null) {
				message = e.getMessage();
			}
			if (message == null) {
				message = Messages.getString("InternalDatabase.229"); //$NON-NLS-1$
			}
			result.add(message);
		}
		return result;
	}

	@Override
	public Vector<String> removeTu(String tuid) {
		Vector<String> result = new Vector<>();
		Element tu = tuDb.getTu(tuid);
		if (tu != null) {
			try {
				List<Element> tuvs = tu.getChildren("tuv"); //$NON-NLS-1$
				Iterator<Element> it = tuvs.iterator();
				while (it.hasNext()) {
					Element tuv = it.next();
					String lang = normalize(tuv.getAttributeValue("xml:lang")); //$NON-NLS-1$
					delete(tuid, lang);
				}
				tuDb.remove(tuid);
				commit();
				result.add(Constants.SUCCESS);
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, e.getMessage(), e);
				result.add(Constants.ERROR);
				result.add(Messages.getString("InternalDatabase.5")); //$NON-NLS-1$
			}
		} else {
			result.add(Constants.ERROR);
			result.add(Messages.getString("InternalDatabase.72")); //$NON-NLS-1$
		}
		return result;
	}

}
