/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;

public class PreferencesDialog  {

	protected Shell shell;
	private Display display;

	public PreferencesDialog(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setText(Messages.getString("PreferencesDialog.0")); 
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginHeight = 0;
		shellLayout.marginWidth = 0;
		shell.setLayout(shellLayout);
		shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "PreferencesDialog"); 
			}
		});
		display = shell.getDisplay();
		
		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		CTabItem projsItem = new CTabItem(folder, SWT.NONE);
		projsItem.setText(Messages.getString("PreferencesDialog.1"));		 
		GeneralPreferences projectPreferences = new GeneralPreferences(folder, SWT.NONE);
		projsItem.setControl(projectPreferences);
	
		CTabItem xmlItem = new CTabItem(folder, SWT.NONE);
		xmlItem.setText(Messages.getString("PreferencesDialog.2")); 
		XmlPreferences xmlPreferences = new XmlPreferences(folder, SWT.NONE);
		xmlItem.setControl(xmlPreferences);
		
		folder.setSelection(0);
		projectPreferences.setFocus();
		
		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "PreferencesDialog"); 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
