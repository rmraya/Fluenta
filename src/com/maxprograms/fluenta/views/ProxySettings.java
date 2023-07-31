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

import java.io.IOException;
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
		shell.setText(Messages.getString("ProxySettings.0"));  
		shell.setLayout(new GridLayout());
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
		
		Composite top = new Composite(shell,SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label sLabel = new Label(top,SWT.NONE);
		sLabel.setText(Messages.getString("ProxySettings.1"));  
		
		server = new Text(top,SWT.BORDER);
		GridData sdata = new GridData(GridData.FILL_HORIZONTAL);
		sdata.widthHint = 350;
		server.setLayoutData(sdata);
		
		Label pLabel = new Label(top,SWT.NONE);
		pLabel.setText(Messages.getString("ProxySettings.2"));  
		
		port = new Text(top,SWT.BORDER);
		port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label uLabel = new Label(top, SWT.NONE);
		uLabel.setText(Messages.getString("ProxySettings.3"));  
		
		user = new Text(top,SWT.BORDER);
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label psLabel = new Label(top, SWT.NONE);
		psLabel.setText(Messages.getString("ProxySettings.4"));  
		
		password = new Text(top,SWT.BORDER);
		password.setEchoChar('*');
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite bottom = new Composite(shell,SWT.NONE);
		bottom.setLayout(new GridLayout(2,false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); 
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button ok = new Button(bottom,SWT.PUSH);
		ok.setText(Messages.getString("ProxySettings.5"));  
		ok.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Preferences preferences = Preferences.getInstance();
					preferences.save("proxySettings", "server", server.getText());    
					preferences.save("proxySettings", "port", port.getText());    
					preferences.save("proxySettings", "user", user.getText());    
					preferences.save("proxySettings", "password", password.getText());    
				} catch (IOException ex) {
					MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
					if (ex.getMessage() != null ) {
						box.setMessage(ex.getMessage());
					} else {
						Logger logger = System.getLogger(ProxySettings.class.getName());
						logger.log(Level.WARNING, Messages.getString("ProxySettings.6"), e); 
						box.setMessage(Messages.getString("ProxySettings.7"));						  
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
			Preferences preferences = Preferences.getInstance();
			server.setText(preferences.get("proxySettings", "server", ""));     
			port.setText(preferences.get("proxySettings", "port", ""));     
			user.setText(preferences.get("proxySettings", "user", ""));     
			password.setText(preferences.get("proxySettings", "password", ""));     
		} catch (IOException e) {
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
