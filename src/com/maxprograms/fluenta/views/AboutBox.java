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

import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;
import com.maxprograms.widgets.CustomLink;

public class AboutBox {

	protected Shell shell;
	private Display display;

	protected static final Logger LOGGER = System.getLogger(AboutBox.class.getName());

	public AboutBox(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		MessageFormat mf = new MessageFormat(Messages.getString("AboutBox.0")); 
		shell.setText(mf.format(new Object[] { Constants.VERSION, Constants.BUILD }));
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 0;
		shellLayout.marginHeight = 0;
		shell.setLayout(shellLayout);
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "AboutBox"); 
			}
		});
		display = shell.getDisplay();
		shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label image = new Label(shell, SWT.NONE);
		image.setImage(Fluenta.getResourceManager().getSplash());
		image.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label copyright = new Label(shell, SWT.CENTER);
		copyright.setText(Messages.getString("AboutBox.1")); 
		copyright.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		copyright.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
		folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CTabItem systemTab = new CTabItem(folder, SWT.NONE);
		systemTab.setText(Messages.getString("AboutBox.3")); 

		Composite info = new Composite(folder, SWT.NONE);
		info.setLayout(new GridLayout());
		systemTab.setControl(info);

		Label os1 = new Label(info, SWT.NONE);
		MessageFormat mf3 = new MessageFormat(Messages.getString("AboutBox.4")); 
		os1.setText(mf3.format(new Object[] { System.getProperty("os.name"), System.getProperty("os.version") }));  

		Label java1 = new Label(info, SWT.NONE);
		MessageFormat mf1 = new MessageFormat(Messages.getString("AboutBox.7")); 
		java1.setText(
				mf1.format(new Object[] { System.getProperty("java.version"), System.getProperty("java.vendor") }));  

		Label openXliffLabel = new Label(info, SWT.NONE);
		MessageFormat mfox = new MessageFormat(Messages.getString("AboutBox.8")); 
		openXliffLabel.setText(mfox.format(new Object[] { com.maxprograms.converters.Constants.VERSION,
				com.maxprograms.converters.Constants.BUILD }));

		Label java2 = new Label(info, SWT.NONE);
		MessageFormat mf2 = new MessageFormat(Messages.getString("AboutBox.10")); 
		java2.setText(mf2.format(new Object[] { Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB", 
				Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB", 
				Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB" })); 

		CTabItem licensesTab = new CTabItem(folder, SWT.NONE);
		licensesTab.setText(Messages.getString("AboutBox.14")); 

		Composite licenses = new Composite(folder, SWT.NONE);
		licenses.setLayout(new GridLayout(2, false));
		licensesTab.setControl(licenses);

		Label fluenta = new Label(licenses, SWT.NONE);
		fluenta.setText(Messages.getString("AboutBox.15")); 

		CustomLink fluentaLink = new CustomLink(licenses, SWT.NONE);
		fluentaLink.setText("Eclipse Public License Version 1.0"); 
		try {
			fluentaLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label openXliff = new Label(licenses, SWT.NONE);
		openXliff.setText("OpenXLIFF Filters"); 

		CustomLink openXliffLink = new CustomLink(licenses, SWT.NONE);
		openXliffLink.setText("Eclipse Public License Version 1.0"); 
		try {
			openXliffLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label java = new Label(licenses, SWT.NONE);
		java.setText("Java"); 

		CustomLink javaLink = new CustomLink(licenses, SWT.NONE);
		javaLink.setText("GPL2 with Classpath Exception"); 
		try {
			javaLink.setURL(new File("lib/licenses/Java.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}
		Label swt = new Label(licenses, SWT.NONE);
		swt.setText("SWT"); 

		CustomLink swtLink = new CustomLink(licenses, SWT.NONE);
		swtLink.setText("Eclipse Public License Version 1.0"); 
		try {
			swtLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label mapDB = new Label(licenses, SWT.NONE);
		mapDB.setText("MapDB"); 

		CustomLink mapdbLink = new CustomLink(licenses, SWT.NONE);
		mapdbLink.setText("Apache License 2.0"); 
		try {
			mapdbLink.setURL(new File("lib/licenses/Apache2.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label h2db = new Label(licenses, SWT.NONE);
		h2db.setText("H2"); 

		CustomLink h2Link = new CustomLink(licenses, SWT.NONE);
		h2Link.setText("Eclipse Public License Version 1.0"); 
		try {
			h2Link.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label json = new Label(licenses, SWT.NONE);
		json.setText("JSON"); 

		CustomLink jsonLink = new CustomLink(licenses, SWT.NONE);
		jsonLink.setText("JSON.org"); 
		try {
			jsonLink.setURL(new File("lib/licenses/JSON.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label jsoup = new Label(licenses, SWT.NONE);
		jsoup.setText("jsoup"); 

		CustomLink jsoupLink = new CustomLink(licenses, SWT.NONE);
		jsoupLink.setText("MIT License"); 
		try {
			jsoupLink.setURL(new File("lib/licenses/jsoup.txt").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		Label dtdParser = new Label(licenses, SWT.NONE);
		dtdParser.setText("DTDParser"); 

		CustomLink dtdLink = new CustomLink(licenses, SWT.NONE);
		dtdLink.setText("LGPL 2.1"); 
		try {
			dtdLink.setURL(new File("lib/licenses/LGPL2.1.txt").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, "Error setting license link", e); 
		}

		folder.setSelection(systemTab);
		systemTab.getControl().setFocus();

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "AboutBox"); 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
