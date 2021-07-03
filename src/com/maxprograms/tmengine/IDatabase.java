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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Element;

public interface IDatabase {

	public abstract void close() throws Exception;

	public abstract String getName();

	public abstract boolean getQuality() throws IOException;

	public abstract int[] storeTMX(String tmxFile, String user, String project,
			String customer, String subject, boolean update, ILogger dialog)
			throws SAXException, IOException, ParserConfigurationException;

	public abstract Vector<String> exportDatabase(String tmxfile, String langs,
			String srcLang, Hashtable<String, Set<String>> propFilters);

	public abstract Vector<String> flag(String tuid);
	
	public abstract Vector<String> removeTu(String tuid);

	public abstract Set<String> getAllCustomers();

	public abstract Set<String> getAllLanguages();

	public abstract Set<String> getAllProjects();

	public abstract Set<String> getAllSubjects();

	public abstract Vector<TU> searchAllTranslations(String searchStr,
			String srcLang, int similarity, boolean caseSensitive);

	public abstract Vector<TU> searchTranslation(String searchStr,
			String srcLang, String tgtLang, int similarity,
			boolean caseSensitive);

	public abstract Vector<TU> concordanceSearch(String searchStr,
			String srcLang, int limit, boolean isRegexp, boolean caseSensitive);

	public abstract void storeTU(Element tu, String srcLang) throws Exception;

	public abstract void commit()  throws SQLException;

	public abstract long getSize() throws SQLException;
	
	public abstract TU getTu(int index, String sortLanguage, boolean ascending) throws SQLException;

	public abstract Element getTu(String tuid) throws Exception;

	public abstract Vector<String> exportCSV(String csvFile, String langs, Hashtable<String, Set<String>> propFilters);

}