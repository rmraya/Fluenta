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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.jsoup.Jsoup;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;

public class HTMLViewer extends Dialog {

	protected Shell shell;
	private Display display;
	private Browser browser;
	private StyledText styled;
	private boolean isLinux;

	public HTMLViewer(Shell parent) throws Exception {
		super(parent, SWT.NONE);
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.MODELESS | SWT.BORDER | SWT.RESIZE);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Locator.remember(shell, "HTMLViewer");
			}
		});
		isLinux = System.getProperty("file.separator").equals("/")
				&& System.getProperty("os.name").toLowerCase().startsWith("linux");
		if (!isLinux) {
			try {
				browser = new Browser(shell, SWT.NONE);
			} catch (SWTError e) {
				Logger logger = System.getLogger(HTMLViewer.class.getName());
				logger.log(Level.WARNING, "Error creating browser", e);
				String message = "Error embedding browser";
				throw new Exception(message);
			}
			browser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		} else {
			styled = new StyledText(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
			styled.setEditable(false);
			styled.setMargins(12, 12, 12, 12);
			styled.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			styled.setAlwaysShowScrollBars(true);
		}
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
		Locator.position(shell, "HTMLViewer");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void display(String string) throws IOException {
		if (isLinux) {
			if (string.startsWith("file:")) {
				string = string.substring("file:".length());
			}
			StringBuilder sb = new StringBuilder();
			try (FileReader reader = new FileReader(new File(string), StandardCharsets.UTF_8)) {
				try (BufferedReader buffer = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffer.readLine()) != null) {
						if (!sb.isEmpty()) {
							sb.append('\n');
						}
						sb.append(line);
					}
				}
			}
			styled.setText(Jsoup.parse(sb.toString()).wholeText());
			return;
		}
		browser.setUrl(string);
	}

	public void setTitle(String title) {
		shell.setText(title);
	}

	public void setContent(String content) {
		if (isLinux) {
			styled.setText(content);
			return;
		}
		browser.setText(content);
	}

	public boolean isLinux() {
		return isLinux;
	}
}
