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

package com.maxprograms.fluenta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.controllers.LocalController;
import com.maxprograms.fluenta.views.AboutBox;
import com.maxprograms.fluenta.views.MemoriesView;
import com.maxprograms.fluenta.views.PreferencesDialog;
import com.maxprograms.fluenta.views.ProjectsView;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.Preferences;

public class MainView {

	Logger logger = System.getLogger(MainView.class.getName());

	private Display display;
	protected Shell shell;
	private Menu systemMenu;
	private boolean isMac;
	private MemoriesView memoriesView;
	private ProjectsView projectsView;
	private static LocalController controller = new LocalController();

	public MainView(Display display) {
		this.display = display;
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("Fluenta");
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 0;
		shellLayout.marginHeight = 0;
		shell.setLayout(shellLayout);
		shell.setImage(Fluenta.getResourceManager().getIcon());

		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "MainView");
			}
		});

		systemMenu = display.getSystemMenu();

		if (systemMenu != null && System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			isMac = true;
			MenuItem aboutItem = getItem(systemMenu, SWT.ID_ABOUT);
			if (aboutItem != null) {
				aboutItem.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						AboutBox box = new AboutBox(shell, SWT.DIALOG_TRIM);
						box.show();
					}
				});
			}
			MenuItem quitItem = getItem(systemMenu, SWT.ID_QUIT);
			if (quitItem != null) {
				quitItem.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						shell.close();
					}
				});
			}
			MenuItem preferencesItem = getItem(systemMenu, SWT.ID_PREFERENCES);
			if (preferencesItem != null) {
				preferencesItem.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						PreferencesDialog dialog = new PreferencesDialog(shell, SWT.CLOSE | SWT.RESIZE);
						dialog.show();
					}
				});
			}
		}

		Menu bar = display.getMenuBar();
		if (bar == null) {
			bar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(bar);
		}
		createMenu(bar);

		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem projectsTab = new CTabItem(folder, SWT.NONE);
		projectsTab.setText(Messages.getString("MainView.0"));
		projectsView = new ProjectsView(folder, SWT.NONE, this);
		projectsTab.setControl(projectsView);

		CTabItem memoriesTab = new CTabItem(folder, SWT.NONE);
		memoriesTab.setText(Messages.getString("MainView.1"));
		memoriesView = new MemoriesView(folder, SWT.NONE, this);
		memoriesTab.setControl(memoriesView);

		folder.setSelection(projectsTab);
		projectsView.setFocus();

	}

	public void show() {
		Locator.position(shell, "MainView");
		shell.open();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				checkUpdates(true);
			}
		});
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public LocalController getController() {
		return controller;
	}

	static MenuItem getItem(Menu menu, int id) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getID() == id)
				return items[i];
		}
		return null;
	}

	private void createMenu(Menu bar) {

		if (!isMac) {
			MenuItem file = new MenuItem(bar, SWT.CASCADE);
			file.setText(Messages.getString("MainView.2"));
			Menu fileMenu = new Menu(file);
			file.setMenu(fileMenu);

			new MenuItem(fileMenu, SWT.SEPARATOR);

			MenuItem close = new MenuItem(fileMenu, SWT.PUSH);
			if (System.getProperty("file.separator").equals("\\")) {
				close.setText(Messages.getString("MainView.3"));
				close.setAccelerator(SWT.ALT | SWT.F4);
			} else {
				close.setText(Messages.getString("MainView.4"));
				close.setAccelerator(SWT.CTRL | 'Q');
			}
			close.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent event) {
					shell.close();
				}
			});
		}

		MenuItem projects = new MenuItem(bar, SWT.CASCADE);
		projects.setText(Messages.getString("MainView.5"));
		Menu projectsMenu = new Menu(projects);
		projects.setMenu(projectsMenu);

		MenuItem createProject = new MenuItem(projectsMenu, SWT.PUSH);
		createProject.setText(Messages.getString("MainView.6"));
		createProject.setImage(Fluenta.getResourceManager().getAdd());
		createProject.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.addProject();
			}
		});

		MenuItem updateProject = new MenuItem(projectsMenu, SWT.PUSH);
		updateProject.setText(Messages.getString("MainView.7"));
		updateProject.setImage(Fluenta.getResourceManager().getEdit());
		updateProject.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.updateProject();
			}
		});

		MenuItem projectDetails = new MenuItem(projectsMenu, SWT.PUSH);
		projectDetails.setText(Messages.getString("MainView.8"));
		projectDetails.setImage(Fluenta.getResourceManager().getInfo());
		projectDetails.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.projectDetails();
			}
		});

		new MenuItem(projectsMenu, SWT.SEPARATOR);

		MenuItem generateXliff = new MenuItem(projectsMenu, SWT.PUSH);
		generateXliff.setText(Messages.getString("MainView.9"));
		generateXliff.setImage(Fluenta.getResourceManager().getRight());
		generateXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.generateXliff();
			}
		});

		MenuItem importXliff = new MenuItem(projectsMenu, SWT.PUSH);
		importXliff.setText(Messages.getString("MainView.10"));
		importXliff.setImage(Fluenta.getResourceManager().getLeft());
		importXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.importXliff();
			}
		});

		new MenuItem(projectsMenu, SWT.SEPARATOR);

		MenuItem removeProject = new MenuItem(projectsMenu, SWT.PUSH);
		removeProject.setText(Messages.getString("MainView.11"));
		removeProject.setImage(Fluenta.getResourceManager().getRemove());
		removeProject.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				projectsView.removeProject();
			}
		});

		MenuItem memory = new MenuItem(bar, SWT.CASCADE);
		memory.setText(Messages.getString("MainView.12"));
		Menu memoryMenu = new Menu(memory);
		memory.setMenu(memoryMenu);

		MenuItem addMemory = new MenuItem(memoryMenu, SWT.PUSH);
		addMemory.setText(Messages.getString("MainView.13"));
		addMemory.setImage(Fluenta.getResourceManager().getAdd());
		addMemory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				memoriesView.addMemory();
			}

		});

		MenuItem editMemory = new MenuItem(memoryMenu, SWT.PUSH);
		editMemory.setText(Messages.getString("MainView.14"));
		editMemory.setImage(Fluenta.getResourceManager().getEdit());
		editMemory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					memoriesView.editMemory();
				} catch (IOException | SAXException | ParserConfigurationException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("MainView.15"));
					box.open();
				}
			}
		});

		MenuItem importMemory = new MenuItem(memoryMenu, SWT.PUSH);
		importMemory.setText(Messages.getString("MainView.16"));
		importMemory.setImage(Fluenta.getResourceManager().getLeft());
		importMemory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				memoriesView.importMemory();
			}

		});

		MenuItem exportMemory = new MenuItem(memoryMenu, SWT.PUSH);
		exportMemory.setText(Messages.getString("MainView.17"));
		exportMemory.setImage(Fluenta.getResourceManager().getRight());
		exportMemory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				memoriesView.exportMemory();
			}

		});

		MenuItem removeMemory = new MenuItem(memoryMenu, SWT.PUSH);
		removeMemory.setText(Messages.getString("MainView.18"));
		removeMemory.setImage(Fluenta.getResourceManager().getRemove());
		removeMemory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				memoriesView.removeMemory();
			}

		});

		if (!isMac) {
			MenuItem settings = new MenuItem(bar, SWT.CASCADE);
			settings.setText(Messages.getString("MainView.19"));
			Menu settingsMenu = new Menu(settings);
			settings.setMenu(settingsMenu);

			MenuItem preferences = new MenuItem(settingsMenu, SWT.PUSH);
			preferences.setText(Messages.getString("MainView.20"));
			preferences.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					PreferencesDialog dialog = new PreferencesDialog(shell, SWT.CLOSE | SWT.RESIZE);
					dialog.show();
				}
			});

		}

		MenuItem help = new MenuItem(bar, SWT.CASCADE);
		help.setText(Messages.getString("MainView.21"));
		Menu helpMenu = new Menu(help);
		help.setMenu(helpMenu);

		MenuItem helpItem = new MenuItem(helpMenu, SWT.PUSH);
		helpItem.setText(Messages.getString("MainView.22"));
		helpItem.setAccelerator(SWT.F1);
		helpItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					Preferences preferences = Preferences.getInstance();
					String lang = preferences.getApplicationLanguage();
					Program.launch(new File("fluenta_" + lang +".pdf").toURI().toURL().toString());
				} catch (IOException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("MainView.23"));
					box.open();
				}
			}
		});

		new MenuItem(helpMenu, SWT.SEPARATOR);

		MenuItem updatesItem = new MenuItem(helpMenu, SWT.PUSH);
		updatesItem.setText(Messages.getString("MainView.24"));
		updatesItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				checkUpdates(false);
			}
		});

		MenuItem releaseHistory = new MenuItem(helpMenu, SWT.PUSH);
		releaseHistory.setText(Messages.getString("MainView.25"));
		releaseHistory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Program.launch("https://www.maxprograms.com/products/fluentalog.html");
			}
		});

		if (!isMac) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
			aboutItem.setText(Messages.getString("MainView.27"));
			aboutItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					AboutBox box = new AboutBox(shell, SWT.DIALOG_TRIM);
					box.show();
				}
			});
		}
	}

	protected void checkUpdates(boolean silent) {
		try {
			URL url = new URL("https://www.maxprograms.com/fluenta");
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);
			byte[] array = new byte[2048];
			int read = 0;
			try (InputStream input = connection.getInputStream()) {
				if ((read = input.read(array)) == 0) {
					throw new IOException(Messages.getString("MainView.29"));
				}
			}
			String version = new String(array, 0, read).trim();
			String installedVersion = Constants.VERSION + " (" + Constants.BUILD + ")";
			if (!version.equals(installedVersion)) {
				MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				box.setText(Messages.getString("MainView.30"));
				MessageFormat mf = new MessageFormat(
						Messages.getString("MainView.31"));
				box.setMessage(mf.format(new String[] { installedVersion, version }));
				if (box.open() == SWT.YES) {
					Program.launch("https://www.maxprograms.com/downloads/");
				}
			} else {
				if (!silent) {
					MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
					box.setMessage(Messages.getString("MainView.32"));
					box.open();
				}
			}
		} catch (Exception e) {
			if (!silent) {
				MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				box.setMessage(Messages.getString("MainView.33"));
				box.open();
			}
		}
	}

	public ProjectsView getProjectsView() {
		return projectsView;
	}

	public MemoriesView getMemoriesView() {
		return memoriesView;
	}

}
