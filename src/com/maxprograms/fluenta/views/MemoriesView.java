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
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.widgets.CustomBar;
import com.maxprograms.widgets.CustomItem;

public class MemoriesView extends Composite {

	protected Table table;
	protected int sortField;

	public MemoriesView(Composite parent, int style) {
		super(parent, style);

		GridLayout memoriesLayout = new GridLayout();
		memoriesLayout.marginWidth = 0;
		memoriesLayout.marginHeight = 0;
		setLayout(memoriesLayout);

		CustomBar bar = new CustomBar(this, SWT.NONE);
		bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CustomItem create = bar.addItem(SWT.PUSH);
		create.setText(Messages.getString("MemoriesView.1")); //$NON-NLS-1$
		create.setImage(Fluenta.getResourceManager().getAdd());
		create.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				addMemory();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem editItem = bar.addItem(SWT.PUSH);
		editItem.setText(Messages.getString("MemoriesView.0")); //$NON-NLS-1$
		editItem.setImage(Fluenta.getResourceManager().getEdit());
		editItem.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				try {
					editMemory();
				} catch (IOException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("MemoriesView.2")); //$NON-NLS-1$
					box.open();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem importItem = bar.addItem(SWT.PUSH);
		importItem.setText(Messages.getString("MemoriesView.3")); //$NON-NLS-1$
		importItem.setImage(Fluenta.getResourceManager().getLeft());

		importItem.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				importMemory();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem export = bar.addItem(SWT.PUSH);
		export.setText(Messages.getString("MemoriesView.5")); //$NON-NLS-1$
		export.setImage(Fluenta.getResourceManager().getRight());
		export.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				exportMemory();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem remove = bar.addItem(SWT.PUSH);
		remove.setText(Messages.getString("MemoriesView.7")); //$NON-NLS-1$
		remove.setImage(Fluenta.getResourceManager().getRemove());
		remove.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				removeMemory();
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

		});

		table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFocus();
		table.setBackgroundImage(Fluenta.getResourceManager().getBackground());

		TableColumn description = new TableColumn(table, SWT.NONE);
		description.setText(Messages.getString("MemoriesView.9")); //$NON-NLS-1$
		description.setWidth(250);

		TableColumn created = new TableColumn(table, SWT.NONE);
		created.setText(Messages.getString("MemoriesView.10")); //$NON-NLS-1$
		created.setWidth(200);

		TableColumn updated = new TableColumn(table, SWT.NONE);
		updated.setText(Messages.getString("MemoriesView.11")); //$NON-NLS-1$
		updated.setWidth(150);

		Listener sortListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (table.getSortDirection() == SWT.UP) {
					table.setSortDirection(SWT.DOWN);
				} else {
					table.setSortDirection(SWT.UP);
				}
				TableColumn column = (TableColumn) e.widget;
				table.setSortColumn(column);
				if (column == description)
					sortField = 0;
				if (column == created)
					sortField = 1;
				if (column == updated)
					sortField = 2;
				loadMemories();
			}
		};

		description.addListener(SWT.Selection, sortListener);
		created.addListener(SWT.Selection, sortListener);
		updated.addListener(SWT.Selection, sortListener);

		table.setSortColumn(description);
		table.setSortDirection(SWT.UP);

		loadMemories();

	}

	public void editMemory() throws IOException {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("MemoriesView.12")); //$NON-NLS-1$
			box.open();
			return;
		}
		EditMemoryDialog dialog = new EditMemoryDialog(getShell(), SWT.DIALOG_TRIM);
		dialog.setMemory((Memory) table.getSelection()[0].getData("memory")); //$NON-NLS-1$
		dialog.show();
	}

	public void removeMemory() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("MemoriesView.12")); //$NON-NLS-1$
			box.open();
			return;
		}
		MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		box.setMessage(Messages.getString("MemoriesView.13")); //$NON-NLS-1$
		int result = box.open();
		if (result == SWT.YES) {
			try {
				MainView.getController().removeMemory(((Memory) table.getSelection()[0].getData("memory")).getId()); //$NON-NLS-1$
				loadMemories();
				MainView.getMemoriesView().loadMemories();
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box2 = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				box2.setMessage(e.getMessage());
				box2.open();
			}
		}

	}

	public void exportMemory() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("MemoriesView.15")); //$NON-NLS-1$
			box.open();
			return;
		}
		exportTMX((Memory) table.getSelection()[0].getData("memory")); //$NON-NLS-1$
	}

	public void importMemory() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("MemoriesView.17")); //$NON-NLS-1$
			box.open();
			return;
		}
		importTMX((Memory) table.getSelection()[0].getData("memory")); //$NON-NLS-1$
	}

	private void importTMX(Memory memory) {
		ImportTmxDialog dialog = new ImportTmxDialog(getShell(), SWT.DIALOG_TRIM, memory);
		dialog.show();
	}

	public void addMemory() {
		AddMemoryDialog dialog = new AddMemoryDialog(getShell(), SWT.DIALOG_TRIM);
		dialog.show();
	}

	public void loadMemories() {
		table.removeAll();
		try {
			Vector<Memory> memories = MainView.getController().getMemories();
			Memory[] array = memories.toArray(new Memory[memories.size()]);
			final Collator collator = Collator.getInstance(new Locale("en")); //$NON-NLS-1$
			Arrays.sort(array, new Comparator<Memory>() {

				@Override
				public int compare(Memory o1, Memory o2) {
					if (table.getSortDirection() == SWT.UP) {
						switch (sortField) {
							case 0:
								return collator.compare(o1.getName().toLowerCase(Locale.getDefault()),
										o2.getName().toLowerCase(Locale.getDefault()));
							case 1:
								return collator.compare(o1.getCreationDateString(), o2.getCreationDateString());
							case 2:
								return collator.compare(o1.getLastUpdateString(), o2.getLastUpdateString());
						}
					}
					switch (sortField) {
						case 0:
							return collator.compare(o2.getName().toLowerCase(Locale.getDefault()),
									o1.getName().toLowerCase(Locale.getDefault()));
						case 1:
							return collator.compare(o2.getCreationDateString(), o1.getCreationDateString());
						case 2:
							return collator.compare(o2.getLastUpdateString(), o1.getLastUpdateString());
					}
					return 0;
				}
			});
			for (int i = 0; i < array.length; i++) {
				Memory m = array[i];
				TableItem item = new TableItem(table, SWT.NONE);
				item.setData("memory", m); //$NON-NLS-1$
				item.setText(
						new String[] { m.getName(), m.getCreationDateString().toString(), m.getLastUpdateString() });
			}
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("MemoriesView.20")); //$NON-NLS-1$
			box.open();
		}

	}

	private void exportTMX(Memory memory) {
		FileDialog fd = new FileDialog(getShell(), SWT.SINGLE | SWT.SAVE);
		fd.setFileName(memory.getName() + ".tmx"); //$NON-NLS-1$
		fd.setFilterNames(
				new String[] { Messages.getString("MemoriesView.22"), Messages.getString("MemoriesView.23") }); //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(new String[] { "*.tmx", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		fd.setOverwrite(true);
		String file = fd.open();
		if (file != null) {
			try {
				getShell().setCursor(new Cursor(getDisplay(), SWT.CURSOR_WAIT));
				MainView.getController().exportTMX(memory, file);
				getShell().setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				MessageBox box = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
				box.setMessage(Messages.getString("MemoriesView.26")); //$NON-NLS-1$
				box.open();
			} catch (Exception e) {
				e.printStackTrace();
				getShell().setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("MemoriesView.27")); //$NON-NLS-1$
				box.open();
			}
		}
	}

}