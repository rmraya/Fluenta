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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;

public class LoginDialog extends Dialog {

	protected Shell shell;
	private Display display;
	protected boolean cancelled = true;
	private boolean local = false;
	protected Text userText;
	protected Text serverText;
	protected Text passwordText;
	protected String user;
	protected String password;
	protected String server;

	public LoginDialog(Shell parent, int style) {
		super(parent, style);
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setText(Messages.getString("LoginDialog.0")); //$NON-NLS-1$
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "LoginDialog"); //$NON-NLS-1$
			}
		});
		display = shell.getDisplay();
		
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label userLabel = new Label(top, SWT.NONE);
		userLabel.setText(Messages.getString("LoginDialog.2")); //$NON-NLS-1$
		
		userText = new Text(top, SWT.BORDER);
		GridData userData = new GridData(GridData.FILL_HORIZONTAL);
		userData.widthHint = 250;
		userText.setLayoutData(userData);
		
		Label passwordLabel = new Label(top, SWT.NONE);
		passwordLabel.setText(Messages.getString("LoginDialog.3")); //$NON-NLS-1$
		
		passwordText = new Text(top, SWT.PASSWORD|SWT.BORDER);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label serverlabel = new Label(top, SWT.NONE);
		serverlabel.setText(Messages.getString("LoginDialog.4")); //$NON-NLS-1$
		
		serverText = new Text(top, SWT.BORDER);
		serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button loginButton = new Button(bottom, SWT.PUSH);
		loginButton.setText(Messages.getString("LoginDialog.6")); //$NON-NLS-1$
		loginButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				user = userText.getText();
				if (user == null || user.equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("LoginDialog.8")); //$NON-NLS-1$
					box.open();
					return;
				}
				password = passwordText.getText();
				if (password == null || password.equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("LoginDialog.10")); //$NON-NLS-1$
					box.open();
					return;
				}
				server = serverText.getText();
				if (server == null || server.equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("LoginDialog.12")); //$NON-NLS-1$
					box.open();
					return;
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
		Locator.setLocation(shell, "LoginDialog"); //$NON-NLS-1$
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

	public boolean isLocal() {
		return local;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getServer() {
		return server;
	}
}
