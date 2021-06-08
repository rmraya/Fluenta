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


public class GetRootElementDialog extends Dialog {

    Shell shell;

    private Display display;
  
    Text rootText;

    String rootName;

	protected boolean cancelled = true;

    public GetRootElementDialog(Shell parent) {
        super(parent, SWT.NONE);

        shell = new Shell(parent, SWT.DIALOG_TRIM);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
        shell.setText(Messages.getString("GetRootElementDialog.0"));  //$NON-NLS-1$
        shell.setLayout(new GridLayout());
        shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "GetRootElementDialog"); //$NON-NLS-1$
			}
		});

        Composite top = new Composite(shell, SWT.NONE);
        top.setLayout(new GridLayout(2, false));
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label rootLabel = new Label(top,SWT.NONE);
        rootLabel.setText(Messages.getString("GetRootElementDialog.2"));  //$NON-NLS-1$
        
        rootText = new Text(top,SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 300;
        rootText.setLayoutData(data);
        
        Composite bottom = new Composite(shell, SWT.NONE);
        bottom.setLayout(new GridLayout(2, false));
        bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button accept = new Button(bottom, SWT.PUSH);
        accept.setText(Messages.getString("GetRootElementDialog.4"));  //$NON-NLS-1$
        accept.addSelectionListener(new SelectionListener() {

            @Override
			public void widgetSelected(SelectionEvent arg0) {
                if (rootText.getText().equals("")) {  //$NON-NLS-1$
                    MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
                    box.setMessage(Messages.getString("GetRootElementDialog.6"));  //$NON-NLS-1$
                    box.open();
                    return;
                }
                rootName = rootText.getText();
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
    	Locator.setLocation(shell, "GetRootElementDialog"); //$NON-NLS-1$
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * @return
     */
    public String getRootElement() {
        if (rootName.equals("")) {  //$NON-NLS-1$
            return null;
        } 
        return rootName;
    }

	public boolean wasCancelled() {
		return cancelled ;
	}
}