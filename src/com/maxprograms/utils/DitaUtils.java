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

package com.maxprograms.utils;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.SilentErrorHandler;

public class DitaUtils {

	private static Hashtable<String, String> filesTable;
	private static Vector<String> filesMap;


	public static TreeSet<String> getFiles(String map) {
		filesMap = new Vector<String>();
		filesTable = new Hashtable<String,String>();
		File f = new File(map);
		parseMap(map, f.getParent());
		TreeSet<String> filesTree = new TreeSet<String>();
		filesTree.addAll(filesMap);
		return filesTree;
	}

	private static void parseMap(String map, String home) {
		if (map.startsWith("#")) {  //$NON-NLS-1$
			// self file
			return;
		}		
		try {
			String path = map; 
			File f = new File(map);
			if (!f.isAbsolute()) { 
				if (map.indexOf("#") != -1) {  //$NON-NLS-1$
					path = FileUtils.getAbsolutePath(home, map.substring(0,map.indexOf("#")));  //$NON-NLS-1$
				} else {
					path = FileUtils.getAbsolutePath(home, map);
				}
				f = new File(path);				
			}
			if (!f.exists()) {
				return;
			}
			if (filesTable.containsKey(path)) {
				return;
			}
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
			builder.setErrorHandler(new SilentErrorHandler());
			Document doc = builder.build(path); 
			Element mapRoot = doc.getRootElement();
			if (!filesTable.containsKey(path)) {
				filesTable.put(path,"");  //$NON-NLS-1$
				filesMap.add(path);
			} else {
				return;
			}
			recurse(mapRoot, path);
		} catch (Exception e) {
			// do nothing
		}
	}
	
	private static void recurse(Element root, String parent) throws SAXException, IOException, ParserConfigurationException {
		String href = root.getAttributeValue("href","");   //$NON-NLS-1$ //$NON-NLS-2$
		if (!href.equals("")) {  //$NON-NLS-1$
			parseMap(href, parent);
		}
		String conref = root.getAttributeValue("conref","");   //$NON-NLS-1$ //$NON-NLS-2$
		if (!conref.equals("")) {  //$NON-NLS-1$
			if (conref.indexOf("#") != -1) {  //$NON-NLS-1$
				conref = conref.substring(0,conref.indexOf("#"));  //$NON-NLS-1$
			}
			parseMap(conref, parent);
		}
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next(), parent);
		}
	}

	
}
