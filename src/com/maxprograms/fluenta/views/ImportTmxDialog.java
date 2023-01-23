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

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.utils.Locator;

public class ImportTmxDialog extends Dialog {

	Logger logger = System.getLogger(ImportTmxDialog.class.getName());

	protected Shell shell;
	private Display display;
	protected Text tmxText;

	public ImportTmxDialog(Shell parent, int style, Memory memory, MainView mainView) {
		super(parent, style);
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.setText("Import Memory");
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "ImportTmxDialog");
			}
		});
		display = shell.getDisplay();

		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label tmxLabel = new Label(top, SWT.NONE);
		tmxLabel.setText("TMX File");

		tmxText = new Text(top, SWT.BORDER);
		GridData tmxData = new GridData(GridData.FILL_HORIZONTAL);
		tmxData.widthHint = 250;
		tmxText.setLayoutData(tmxData);

		Button tmxBrowse = new Button(top, SWT.PUSH);
		tmxBrowse.setText("Browse...");
		tmxBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.SINGLE);
				if (tmxText.getText() != null && !tmxText.getText().isEmpty()) {
					File f = new File(tmxText.getText());
					fd.setFileName(f.getName());
					fd.setFilterPath(f.getAbsolutePath());
				}
				String file = fd.open();
				if (file != null) {
					tmxText.setText(file);
				}
			}
		});

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button importMemory = new Button(bottom, SWT.PUSH);
		importMemory.setText("Import Memory");
		importMemory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (tmxText.getText() == null || tmxText.getText().trim().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select TMX file");
					box.open();
					return;
				}
				String tmxFile = tmxText.getText();
				try {
					shell.setCursor(new Cursor(display, SWT.CURSOR_WAIT));
					int result = mainView.getController().importTMX(memory, tmxFile);
					shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
					MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
					MessageFormat mf = new MessageFormat("Imported {0} segments");
					box.setMessage(mf.format(new String[] { "" + result }));
					box.open();
				} catch (ClassNotFoundException | SQLException | IOException | SAXException
						| ParserConfigurationException | JSONException | ParseException e) {
					logger.log(Level.ERROR, e);
					shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(e.getMessage());
					box.open();
				}
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "ImportTmxDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
