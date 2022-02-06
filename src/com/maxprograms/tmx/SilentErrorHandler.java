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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SilentErrorHandler implements org.xml.sax.ErrorHandler {

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// throw exception;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw new SAXException("[Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException("[Fatal Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

}
