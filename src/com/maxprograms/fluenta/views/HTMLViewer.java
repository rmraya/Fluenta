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

package com.maxprograms.fluenta.views;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;

public class HTMLViewer extends Dialog {

	protected Shell shell;
	private Display display;
	private Browser browser;

	public HTMLViewer(Shell parent) throws Exception {
		super(parent,SWT.NONE);
		shell = new Shell(parent,SWT.CLOSE|SWT.TITLE|SWT.MODELESS|SWT.BORDER|SWT.RESIZE);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Locator.remember(shell, "HTMLViewer"); //$NON-NLS-1$
			}
		});

		try {
			if (System.getProperty("file.separator").equals("/")) {  //$NON-NLS-1$ //$NON-NLS-2$
				browser = new Browser(shell, SWT.WEBKIT);			
			} else {
				browser = new Browser(shell, SWT.NONE);	
			}
		} catch (SWTError e) {
			Logger logger = System.getLogger(HTMLViewer.class.getName());
			logger.log(Level.WARNING, "Error creating browser", e); //$NON-NLS-1$
			String message = "";  //$NON-NLS-1$
			if (System.getProperty("file.separator").equals("/") ) {  //$NON-NLS-1$ //$NON-NLS-2$
				if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {  //$NON-NLS-1$ //$NON-NLS-2$
					// Mac
					message = Messages.getString("HTMLViewer.8");  //$NON-NLS-1$
				} else {
					// Linux
					message = Messages.getString("HTMLViewer.9");   //$NON-NLS-1$
				}
			} else {
				message = Messages.getString("HTMLViewer.10");  //$NON-NLS-1$
			}
			
			throw new Exception(message);
		}
		browser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		
		shell.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// do nothing 				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.ESC) {
					shell.dispose();
				}
			}
		});
	}
	
	public void show() {
		Locator.position(shell, "HTMLViewer"); //$NON-NLS-1$
		shell.open();
		while (!shell.isDisposed()) {
			if ( !display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void display(String string) {
		browser.setUrl(string);
	}

	public void setTitle(String title) {
		shell.setText(title);
	}

	public void setContent(String content) {
		browser.setText(content);
	}
}
