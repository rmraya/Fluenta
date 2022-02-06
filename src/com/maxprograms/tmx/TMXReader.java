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

package com.maxprograms.tmx;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.maxprograms.tmengine.ILogger;
import com.maxprograms.tmengine.InternalDatabase;
import com.maxprograms.xml.CustomErrorHandler;
import com.maxprograms.xml.EntityHandler;

public class TMXReader {

	private XMLReader parser;
	private TMXContentHandler handler;

	public TMXReader(InternalDatabase database, ILogger logger) throws SAXException, ParserConfigurationException {
		database.getAllLanguages();
		parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader(); 
		parser.setFeature("http://xml.org/sax/features/namespaces", true); //$NON-NLS-1$
		handler = new TMXContentHandler(database, logger);
		parser.setContentHandler(handler);
        parser.setEntityResolver(new TMXResolver());
       	parser.setErrorHandler(new CustomErrorHandler());
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //$NON-NLS-1$
        parser.setFeature("http://xml.org/sax/features/namespaces", true);  //$NON-NLS-1$
        parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);  //$NON-NLS-1$
        
        EntityHandler declhandler = new EntityHandler();
        parser.setProperty("http://xml.org/sax/properties/declaration-handler", declhandler); //$NON-NLS-1$
	}
	
	public void parse(URL url) throws IOException, SAXException {
		 parser.parse(new InputSource(url.openStream()));
	}
	
	public void parse(File file) throws IOException, SAXException {
		parse(file.toURI().toURL());
	}

	public int getdiscared() {
		return handler.getDiscarded();
	}

	public int getCount() {
		return handler.getCount();
	}
}
