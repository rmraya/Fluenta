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

package com.maxprograms.utils;

import java.io.IOException;
import java.text.Collator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.RegistryParser;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;

public class LanguageUtils {

	private static Document doc;
	private static Element root;
	private static Hashtable<String, String> descriptions;
	private static RegistryParser registry;

	private static void loadLanguages(String locale) throws SAXException, IOException, ParserConfigurationException  {
		if (registry == null) {
			registry = new RegistryParser();
		}
		doc = null;
		root = null;
		descriptions = null;
		SAXBuilder builder = new SAXBuilder();   
		String file = "lib/langCodes.xml";  //$NON-NLS-1$
		switch (locale) {
		case "de" : //$NON-NLS-1$
			file = "lib/langCodes_de.xml";  //$NON-NLS-1$
			break;
		case "es" : //$NON-NLS-1$
			file = "lib/langCodes_es.xml";  //$NON-NLS-1$
			break;
		case "ja" : //$NON-NLS-1$
			file = "lib/langCodes_ja.xml";  //$NON-NLS-1$
			break;
		default : 
			file = "lib/langCodes.xml";  //$NON-NLS-1$
		}
		doc = builder.build(file);
		root = doc.getRootElement();
		List<Element> list = root.getChildren("lang");  //$NON-NLS-1$
		Iterator<Element> i = list.iterator();
		descriptions = new Hashtable<String, String>();
		while (i.hasNext()) {
			Element e = i.next();
			String code = e.getAttributeValue("code");  //$NON-NLS-1$
			if (!locale.equals("en")) { //$NON-NLS-1$
				descriptions.put(code,e.getText());
			} else {
				descriptions.put(code,registry.getTagDescription(code));
			}
		}        
	}       

	public static String[] getLanguageNames() throws SAXException, IOException, ParserConfigurationException {
		if (registry == null) {
			loadLanguages(Fluenta.getLanguage());
		}

		Collator col = Collator.getInstance(new Locale(Fluenta.getLanguage()));
		TreeSet<String> set = new TreeSet<String>(col);
		Enumeration<String> keys = descriptions.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			set.add(descriptions.get(key)); 
		}

		Iterator<String> i = set.iterator();
		int j = 0;
		String langs[] = new String[set.size()];
		while (i.hasNext()) {
			langs[j++] = i.next();
		}

		return langs;
	}


	public static String getLanguageName(String language)  {
		if (language.equals("")) {  //$NON-NLS-1$
			return "";  //$NON-NLS-1$
		}

		if (registry == null) {
			try {
				loadLanguages(Fluenta.getLanguage());
			} catch (IOException |SAXException | ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (descriptions.containsKey(language)) {
			return descriptions.get(language);
		}
		return registry.getTagDescription(language);
	}

	public static Language getLanguage(String name) throws SAXException, IOException, ParserConfigurationException {
		if (registry == null) {
			loadLanguages(Fluenta.getLanguage());
		}
		Enumeration<String> keys = descriptions.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (descriptions.get(key).equals(name)) {
				return new Language(key, descriptions.get(key));
			}
		}
		return null;
	}

	public static Vector<Language> getLanguages() throws SAXException, IOException, ParserConfigurationException {
		
		if (registry == null) {
			loadLanguages(Fluenta.getLanguage());
		}

		Collator col = Collator.getInstance(new Locale(Fluenta.getLanguage()));
		TreeSet<String> set = new TreeSet<String>(col);
		Enumeration<String> keys = descriptions.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			set.add(descriptions.get(key)); 
		}

		Iterator<String> i = set.iterator();
		Vector<Language> langs = new Vector<Language>();
		while (i.hasNext()) {
			langs.add(getLanguage(i.next()));
		}

		return langs;
	}
}
