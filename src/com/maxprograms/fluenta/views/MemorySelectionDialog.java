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
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
import org.json.JSONException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.utils.Locator;

public class MemorySelectionDialog extends Dialog {

	protected Shell shell;
	private Display display;
	protected boolean cancelled = true;
	protected Table table;
	protected List<Memory> selected;

	public MemorySelectionDialog(Shell parent, int style, List<Memory> existing, MainView mainView) {
		super(parent, style);
		shell = new Shell(parent, style);
		shell.setText("Additional Memories");
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "MemorySelectionDialog");
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
		description.setText("Description");

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
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button add = new Button(bottom, SWT.PUSH);
		add.setText("Add Selected Memories");
		add.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				cancelled = false;
				selected = new Vector<>();
				for (int i = 0; i < table.getItemCount(); i++) {
					if (table.getItem(i).getChecked()) {
						selected.add((Memory) table.getItem(i).getData("memory"));
					}
				}
				shell.close();
			}
		});

		try {
			List<Memory> memories = mainView.getController().getMemories();
			Iterator<Memory> it = memories.iterator();
			while (it.hasNext()) {
				Memory mem = it.next();
				boolean exists = false;
				for (int i = 0; i < existing.size(); i++) {
					Memory m = existing.get(i);
					if (mem.getId() == m.getId()) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(mem.getName());
					item.setData("memory", mem);
				}
			}
		} catch (IOException | JSONException | ParseException e) {
			Logger logger = System.getLogger(MemorySelectionDialog.class.getName());
			logger.log(Level.WARNING, "Error selecting memory", e);
			MessageBox box = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			box.setMessage("Error loading memories");
			box.open();
			return;
		}

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "MemorySelectionDialog");
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

	public List<Memory> getSelected() {
		return selected;
	}
}
