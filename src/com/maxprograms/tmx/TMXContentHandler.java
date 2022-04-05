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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import com.maxprograms.tmengine.ILogger;
import com.maxprograms.tmengine.InternalDatabase;
import com.maxprograms.utils.TMUtils;
import com.maxprograms.xml.Element;

class TMXContentHandler implements ContentHandler, LexicalHandler {

	protected static final Logger LOGGER = Logger.getLogger(TMXContentHandler.class.getName());

	private Element current;
	Stack<Element> stack;
	private boolean inCDATA = false;
	private int count;
	private int discarded;
	private InternalDatabase db;
	private String srcLang;
	private ILogger iLogger;
	private MessageFormat mf1;
	
	public TMXContentHandler(InternalDatabase internalDatabase, ILogger logger){
		db = internalDatabase;
		stack = new Stack<>();
		this.iLogger = logger;
		mf1 = new MessageFormat(Messages.getString("TMXContentHandler.0")); //$NON-NLS-1$
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (!inCDATA && current != null) {
			current.addContent(new String(ch,start,length));	
		}
	}

	@Override
	public void endDocument() throws SAXException {
		stack = null;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("tu")) { //$NON-NLS-1$
			try {
				db.storeTU(current, srcLang);
				if (count % 400 == 0) {
					db.commit();
					if (iLogger != null) {
						if (iLogger.isCancelled()) {
							throw new SAXException(Messages.getString("TMXContentHandler.1")); //$NON-NLS-1$
						}
						iLogger.log(mf1.format(new Object[]{"" + count})); //$NON-NLS-1$
					}
				}				
			} catch (Exception e) {
				discarded++;
			}
			count++;
			current = null;
			stack.clear();
		} else {
			if (!stack.isEmpty()) {
				current = stack.pop();
			}
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// do nothing
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// do nothing
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// do nothing
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// do nothing		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// do nothing, the entity resolver must support this
	}

	@Override
	public void startDocument() throws SAXException {
		// do nothing
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (current == null) {
			current = new Element(qName);
			stack.push(current);
		} else {
			Element child = new Element(qName);
			if (!qName.equals("ut")) { //$NON-NLS-1$
				current.addContent(child);
			}
			stack.push(current);
			current = child;
		}
		for (int i=0 ; i<atts.getLength() ; i++) {
			current.setAttribute(atts.getQName(i), atts.getValue(i));
		}
		if (qName.equals("header")) { //$NON-NLS-1$
			srcLang = current.getAttributeValue("srclang","*all*"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!srcLang.equals("*all*")) { //$NON-NLS-1$
				try {
					srcLang = TMUtils.normalizeLang(srcLang);
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
					srcLang = "*all*"; //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// do nothing
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		// do nothing
	}

	@Override
	public void endCDATA() throws SAXException {
		inCDATA = false;
	}

	@Override
	public void endDTD() throws SAXException {
		// do nothing
	}

	@Override
	public void endEntity(String arg0) throws SAXException {
		// do nothing, let the EntityResolver handle this
	}

	@Override
	public void startCDATA() throws SAXException {
		inCDATA = true;		
	}

	@Override
	public void startDTD(String name, String publicId1, String systemId1) throws SAXException {
		// do nothing
	}

	@Override
	public void startEntity(String arg0) throws SAXException {
		// do nothing, let the EntityResolver handle this
	}

	public int getDiscarded() {
		return discarded;
	}

	public int getCount() {
		return count;
	}
}
