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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Locator;

public class LanguageAddDialog extends Dialog {

	protected Shell shell;
	protected boolean cancelled = true;
	private Display display;
	protected Combo langCombo;
	protected Language language;

	public LanguageAddDialog(Shell parent, int style) {
		super(parent, style);

		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setText(Messages.getString("LanguageAddDialog.0")); 
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "LanguageAddDialog"); 
			}
		});
		display = shell.getDisplay();

		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(2, false));

		Label sourceLabel = new Label(top, SWT.NONE);
		sourceLabel.setText(Messages.getString("LanguageAddDialog.2")); 

		langCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
		langCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			langCombo.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("LanguageAddDialog.3")); 
			box.open();
			shell.close();
		}

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler2 = new Label(bottom, SWT.NONE);
		filler2.setText(""); 
		filler2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button add = new Button(bottom, SWT.PUSH);
		add.setText(Messages.getString("LanguageAddDialog.5")); 
		add.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (langCombo.getSelectionIndex() == -1) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("LanguageAddDialog.6")); 
					box.open();
					return;
				}
				try {
					language = LanguageUtils.languageFromName(langCombo.getItem(langCombo.getSelectionIndex()));
				} catch (IOException | SAXException | ParserConfigurationException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("LanguageAddDialog.7")); 
					box.open();
					shell.close();
				}
				cancelled = false;
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "LanguageAddDialog"); 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public boolean wasCancelled() {
		return cancelled;
	}

	public Language getLanguage() {
		return language;
	}

}
