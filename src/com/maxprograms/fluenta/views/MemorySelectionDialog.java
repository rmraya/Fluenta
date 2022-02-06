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
import java.util.Iterator;
import java.util.Vector;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.utils.Locator;

public class MemorySelectionDialog extends Dialog {

	protected Shell shell;
	private Display display;
	protected boolean cancelled = true;
	protected Table table;
	protected Vector<Memory> selected;

	public MemorySelectionDialog(Shell parent, int style, Vector<Memory> existing) {
		super(parent, style);
		shell = new Shell(parent, style);
		shell.setText(Messages.getString("MemorySelectionDialog.0")); //$NON-NLS-1$
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "MemorySelectionDialog"); //$NON-NLS-1$
			}
		});
		display = shell.getDisplay();
		
		table = new Table(shell, SWT.CHECK | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.heightHint = table.getItemHeight() * 10;
		tableData.widthHint = 350;
		table.setLayoutData(tableData);
		
		TableColumn description = new TableColumn(table, SWT.NONE);
		description.setText(Messages.getString("MemorySelectionDialog.2")); //$NON-NLS-1$
		
		table.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				description.setWidth(table.getClientArea().width);
			}
		});
		
		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button add = new Button(bottom, SWT.PUSH);
		add.setText(Messages.getString("MemorySelectionDialog.4")); //$NON-NLS-1$
		add.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				cancelled = false;
				selected = new Vector<>();
				for (int i=0 ; i<table.getItemCount() ; i++) {
					if (table.getItem(i).getChecked()) {
						selected.add((Memory)table.getItem(i).getData("memory")); //$NON-NLS-1$
					}
				}
				shell.close();
			}
		});
		
		try {
			Vector<Memory> memories = MainView.getController().getMemories();
			Iterator<Memory> it = memories.iterator();
			while (it.hasNext()) {
				Memory mem = it.next();
				boolean exists = false;
				for (int i=0 ; i<existing.size() ; i++) {
					Memory m = existing.get(i);
					if (mem.getId() == m.getId()) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(mem.getName());
					item.setData("memory", mem); //$NON-NLS-1$
				}
			}
		} catch (IOException e) {
			Logger logger = System.getLogger(MemorySelectionDialog.class.getName());
			logger.log(Level.WARNING, "Error selecting memory", e); //$NON-NLS-1$
			MessageBox box = new MessageBox(shell, SWT.OK|SWT.ICON_ERROR);
			box.setMessage(Messages.getString("MemorySelectionDialog.7")); //$NON-NLS-1$
			box.open();
			return;
		}
		
		shell.pack();
	}


	public void show() {
		Locator.setLocation(shell, "MemorySelectionDialog"); //$NON-NLS-1$
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public boolean wasCancelled() {
		return cancelled ;
	}

	public Vector<Memory> getSelected() {
		return selected;
	}
}
