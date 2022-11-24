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

package com.maxprograms.widgets;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import com.maxprograms.fluenta.views.HTMLViewer;

public class CustomLink  {

	Label link;
	private String url;
	private Composite parent;

	public CustomLink(Composite parent, int style) {
		
		link = new Label(parent, style);
		
		this.parent = parent;
		
		link.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		link.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
		link.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				displayLink();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing				
			}
		});
	}
	

	protected void displayLink() {
		try {
			HTMLViewer viewer = new HTMLViewer(link.getShell());
			viewer.setTitle(link.getText());
			viewer.display(url);
			viewer.show();
			link.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_MAGENTA));
		} catch (SWTException | IOException e) {
			Logger logger = System.getLogger(CustomLink.class.getName());
			logger.log(Level.WARNING, "Error displaying link", e); 
			MessageBox box = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
			box.setMessage(e.getMessage());
			box.open();
		}
	}

	public void setURL(String value) {
		url = value;
	}

	public void setForeground(Color value) {
		link.setForeground(value);
	}

	public void setText(String string) {
		link.setText(string);
	}
}
