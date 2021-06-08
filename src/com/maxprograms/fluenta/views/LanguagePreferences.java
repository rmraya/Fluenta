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

package com.maxprograms.fluenta.views;

import java.io.IOException;
import java.util.Locale;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.utils.Preferences;

public class LanguagePreferences extends Composite {

	protected Button english;
	protected Button japanese;
	protected Button spanish;
	protected Button german;
	
	protected static final Logger LOGGER = System.getLogger(LanguagePreferences.class.getName());

	public LanguagePreferences(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group langGroup = new Group(this, SWT.NONE);
		langGroup.setText(Messages.getString("LanguagePreferences.0")); //$NON-NLS-1$
		langGroup.setLayout(new GridLayout());
		langGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		english = new Button(langGroup, SWT.RADIO);
		english.setText(Messages.getString("LanguagePreferences.1")); //$NON-NLS-1$
		
		german = new Button(langGroup, SWT.RADIO);
		german.setText(Messages.getString("LanguagePreferences.4")); //$NON-NLS-1$
		
		japanese = new Button(langGroup, SWT.RADIO);
		japanese.setText(Messages.getString("LanguagePreferences.2")); //$NON-NLS-1$
		
		spanish = new Button(langGroup, SWT.RADIO);
		spanish.setText(Messages.getString("LanguagePreferences.3")); //$NON-NLS-1$

		Composite bottom = new Composite(this, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button save = new Button(bottom, SWT.PUSH);
		save.setText(Messages.getString("LanguagePreferences.5")); //$NON-NLS-1$
		save.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String lang = "en"; //$NON-NLS-1$
				if (german.getSelection()) {
					lang = "de"; //$NON-NLS-1$
				}
				if (japanese.getSelection()) {
					lang = "ja"; //$NON-NLS-1$
				}
				if (spanish.getSelection()) {
					lang = "es"; //$NON-NLS-1$
				}
				try {
					Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
					prefs.save("Fluenta", "uiLanguage", lang); //$NON-NLS-1$ //$NON-NLS-2$
					MessageBox box = new MessageBox(getShell(), SWT.ICON_INFORMATION|SWT.OK);
					box.setMessage(Messages.getString("LanguagePreferences.11")); //$NON-NLS-1$
					box.open();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Error saving language preferences", e);
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR|SWT.OK);
					box.setMessage(Messages.getString("LanguagePreferences.12")); //$NON-NLS-1$
					box.open();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});
		
		try {
			Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
			String lang = prefs.get("Fluenta", "uiLanguage", "en"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			switch (lang) {
			case "en":  //$NON-NLS-1$
				english.setSelection(true);
				break;
			case "ja" : //$NON-NLS-1$
				japanese.setSelection(true);
				break;
			case "es" : //$NON-NLS-1$
				spanish.setSelection(true);
				break;
			default: 
				english.setSelection(true);
			}
			Locale.setDefault(new Locale(lang));			
		} catch (IOException e1) {
			LOGGER.log(Level.WARNING, "Error setting language preferences", e1);
		}
		
	}

}
