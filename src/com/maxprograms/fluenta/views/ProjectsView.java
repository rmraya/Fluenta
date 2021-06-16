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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.widgets.CustomBar;
import com.maxprograms.widgets.CustomItem;

public class ProjectsView extends Composite {

	protected Table table;
	protected int sortField = 0;

	public ProjectsView(Composite parent, int style) {
		super(parent, style);

		GridLayout projectsLayout = new GridLayout();
		projectsLayout.marginWidth = 0;
		projectsLayout.marginHeight = 0;
		setLayout(projectsLayout);

		CustomBar bar = new CustomBar(this, SWT.NONE);
		bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		CustomItem create = bar.addItem(SWT.PUSH);
		create.setText(Messages.getString("ProjectsView.1")); //$NON-NLS-1$
		create.setImage(Fluenta.getResourceManager().getAdd());
		create.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				addProject();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem update = bar.addItem(SWT.PUSH);
		update.setText(Messages.getString("ProjectsView.3")); //$NON-NLS-1$
		update.setImage(Fluenta.getResourceManager().getEdit());
		update.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				updateProject();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem details = bar.addItem(SWT.PUSH);
		details.setText(Messages.getString("ProjectsView.5")); //$NON-NLS-1$
		details.setImage(Fluenta.getResourceManager().getInfo());
		details.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				projectDetails();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem generateXliff = bar.addItem(SWT.PUSH);
		generateXliff.setText(Messages.getString("ProjectsView.7")); //$NON-NLS-1$
		generateXliff.setImage(Fluenta.getResourceManager().getRight());
		generateXliff.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				generateXliff();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem importXliff = bar.addItem(SWT.PUSH);
		importXliff.setText(Messages.getString("ProjectsView.9")); //$NON-NLS-1$
		importXliff.setImage(Fluenta.getResourceManager().getLeft());
		importXliff.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				importXliff();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}
		});

		bar.addSeparator();

		CustomItem remove = bar.addItem(SWT.PUSH);
		remove.setText(Messages.getString("ProjectsView.11")); //$NON-NLS-1$
		remove.setImage(Fluenta.getResourceManager().getRemove());
		remove.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				removeProject();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
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
		description.setText(Messages.getString("ProjectsView.13")); //$NON-NLS-1$
		description.setWidth(250);

		TableColumn map = new TableColumn(table, SWT.NONE);
		map.setText(Messages.getString("ProjectsView.14")); //$NON-NLS-1$
		map.setWidth(350);

		TableColumn status = new TableColumn(table, SWT.NONE);
		status.setText(Messages.getString("ProjectsView.15")); //$NON-NLS-1$
		status.setWidth(150);

		TableColumn created = new TableColumn(table, SWT.NONE);
		created.setText(Messages.getString("ProjectsView.16")); //$NON-NLS-1$
		created.setWidth(200);

		TableColumn updated = new TableColumn(table, SWT.NONE);
		updated.setText(Messages.getString("ProjectsView.17")); //$NON-NLS-1$
		updated.setWidth(200);

		table.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				int size = map.getWidth() + status.getWidth() + created.getWidth() + updated.getWidth();
				int scrollbar = table.getVerticalBar().isVisible() ? table.getVerticalBar().getSize().x : 0;
				int width = table.getClientArea().width - scrollbar - size;
				if (width > 250) {
					description.setWidth(width);
				}
			}
		});

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
				if (column == map)
					sortField = 1;
				if (column == status)
					sortField = 2;
				if (column == created)
					sortField = 3;
				if (column == updated)
					sortField = 4;
				loadProjects();
			}
		};

		description.addListener(SWT.Selection, sortListener);
		map.addListener(SWT.Selection, sortListener);
		status.addListener(SWT.Selection, sortListener);
		created.addListener(SWT.Selection, sortListener);
		updated.addListener(SWT.Selection, sortListener);

		table.setSortColumn(description);
		table.setSortDirection(SWT.UP);

		loadProjects();
	}

	public void loadProjects() {
		table.removeAll();
		try {
			Vector<Project> projects = MainView.getController().getProjects();
			Project[] array = projects.toArray(new Project[projects.size()]);
			final Collator collator = Collator.getInstance(new Locale("en")); //$NON-NLS-1$
			Arrays.sort(array, new Comparator<Project>() {

				@Override
				public int compare(Project o1, Project o2) {
					if (table.getSortDirection() == SWT.UP) {
						switch (sortField) {
							case 0:
								return collator.compare(o1.getTitle().toLowerCase(Locale.getDefault()),
										o2.getTitle().toLowerCase(Locale.getDefault()));
							case 1:
								return collator.compare(o1.getMap().toLowerCase(Locale.getDefault()),
										o2.getMap().toLowerCase(Locale.getDefault()));
							case 2:
								return collator.compare(o1.getStatus(), o2.getStatus());
							case 3:
								return collator.compare(o1.getCreationDateString(), o2.getCreationDateString());
							case 4:
								return collator.compare(o1.getLastUpdateString(), o2.getLastUpdateString());
						}
					}
					switch (sortField) {
						case 0:
							return collator.compare(o2.getTitle().toLowerCase(Locale.getDefault()),
									o1.getTitle().toLowerCase(Locale.getDefault()));
						case 1:
							return collator.compare(o2.getMap().toLowerCase(Locale.getDefault()),
									o1.getMap().toLowerCase(Locale.getDefault()));
						case 2:
							return collator.compare(o2.getStatus(), o1.getStatus());
						case 3:
							return collator.compare(o2.getCreationDateString(), o1.getCreationDateString());
						case 4:
							return collator.compare(o2.getLastUpdateString(), o1.getLastUpdateString());
					}
					return 0;
				}
			});
			for (int i = 0; i < array.length; i++) {
				Project p = array[i];
				TableItem item = new TableItem(table, SWT.NONE);
				item.setData("project", p); //$NON-NLS-1$
				item.setText(new String[] { p.getTitle(), p.getMap(), p.getStatus(), p.getCreationDateString(),
						p.getLastUpdateString() });
			}
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.19")); //$NON-NLS-1$
			box.open();
			return;
		}
	}

	public void addProject() {
		ProjectDialog addProject = new ProjectDialog(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE, new Project());
		addProject.show();
	}

	public void updateProject() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.20")); //$NON-NLS-1$
			box.open();
			return;
		}
		ProjectDialog updateProject = new ProjectDialog(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE,
				(Project) table.getSelection()[0].getData("project")); //$NON-NLS-1$
		updateProject.show();
	}

	public void projectDetails() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.22")); //$NON-NLS-1$
			box.open();
			return;
		}
		ProjectInfoDialog dialog = new ProjectInfoDialog(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE,
				(Project) table.getSelection()[0].getData("project")); //$NON-NLS-1$
		dialog.show();
	}

	public void generateXliff() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.24")); //$NON-NLS-1$
			box.open();
			return;
		}
		GenerateXliffDialog dialog = new GenerateXliffDialog(getShell(), SWT.DIALOG_TRIM,
				(Project) table.getSelection()[0].getData("project")); //$NON-NLS-1$
		dialog.show();
	}

	public void importXliff() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.26")); //$NON-NLS-1$
			box.open();
			return;
		}
		ImportXliffDialog dialog = new ImportXliffDialog(getShell(), SWT.DIALOG_TRIM,
				(Project) table.getSelection()[0].getData("project")); //$NON-NLS-1$
		dialog.show();
	}

	public void removeProject() {
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("ProjectsView.28")); //$NON-NLS-1$
			box.open();
			return;
		}
		MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		box.setMessage(Messages.getString("ProjectsView.29")); //$NON-NLS-1$
		int result = box.open();
		if (result == SWT.YES) {
			try {
				MainView.getController().removeProject((Project) table.getSelection()[0].getData("project")); //$NON-NLS-1$
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box2 = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				box2.setMessage(Messages.getString("ProjectsView.31")); //$NON-NLS-1$
				box2.open();
			}
			loadProjects();
			MainView.getMemoriesView().loadMemories();
		}
	}

}
