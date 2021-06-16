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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Preferences;

public class ProxySettings extends Dialog {

	protected Shell shell;
	private Display display;
	protected Text server;
	protected Text port;
	protected Text user;
	protected Text password;

	public ProxySettings(Shell parent) {
		super(parent, SWT.NONE);
		
		shell = new Shell(parent, SWT.DIALOG_TRIM);
		shell.setText(Messages.getString("ProxySettings.0"));  //$NON-NLS-1$
		shell.setLayout(new GridLayout());
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
		
		Composite top = new Composite(shell,SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label sLabel = new Label(top,SWT.NONE);
		sLabel.setText(Messages.getString("ProxySettings.1"));  //$NON-NLS-1$
		
		server = new Text(top,SWT.BORDER);
		GridData sdata = new GridData(GridData.FILL_HORIZONTAL);
		sdata.widthHint = 350;
		server.setLayoutData(sdata);
		
		Label pLabel = new Label(top,SWT.NONE);
		pLabel.setText(Messages.getString("ProxySettings.2"));  //$NON-NLS-1$
		
		port = new Text(top,SWT.BORDER);
		port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label uLabel = new Label(top, SWT.NONE);
		uLabel.setText(Messages.getString("ProxySettings.3"));  //$NON-NLS-1$
		
		user = new Text(top,SWT.BORDER);
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label psLabel = new Label(top, SWT.NONE);
		psLabel.setText(Messages.getString("ProxySettings.4"));  //$NON-NLS-1$
		
		password = new Text(top,SWT.BORDER);
		password.setEchoChar('*');
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite bottom = new Composite(shell,SWT.NONE);
		bottom.setLayout(new GridLayout(2,false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button ok = new Button(bottom,SWT.PUSH);
		ok.setText(Messages.getString("ProxySettings.6"));  //$NON-NLS-1$
		ok.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
					prefs.save("proxySettings", "server", server.getText());   //$NON-NLS-1$ //$NON-NLS-2$
					prefs.save("proxySettings", "port", port.getText());   //$NON-NLS-1$ //$NON-NLS-2$
					prefs.save("proxySettings", "user", user.getText());   //$NON-NLS-1$ //$NON-NLS-2$
					prefs.save("proxySettings", "password", password.getText());   //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception ex) {
					MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
					if (ex.getMessage() != null ) {
						box.setMessage(ex.getMessage());
					} else {
						Logger logger = System.getLogger(ProxySettings.class.getName());
						logger.log(Level.WARNING, "Error saving proxy settings", e); //$NON-NLS-1$
						box.setMessage(Messages.getString("ProxySettings.15"));						  //$NON-NLS-1$
					}
					box.open();
				}
				shell.close();
			}
			
		});

		loadSettings();
		
		shell.pack();
	}

	private void loadSettings() {
		try {
			Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
			server.setText(prefs.get("proxySettings", "server", ""));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			port.setText(prefs.get("proxySettings", "port", ""));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			user.setText(prefs.get("proxySettings", "user", ""));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			password.setText(prefs.get("proxySettings", "password", ""));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (Exception e) {
			// do nothing			
		}
	}

	public void show() {
		shell.open();
		while (!shell.isDisposed() ) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
