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

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.TextUtils;

public class ProjectDialog extends Dialog {

	private static Logger logger = System.getLogger(ProjectDialog.class.getName());

	protected Shell shell;
	private Display display;
	protected Text titleText;
	protected Text mapText;
	protected Combo sourceLanguages;
	protected Project project;

	public ProjectDialog(Shell parent, int style, Project proj, MainView mainView) {
		super(parent, style);
		project = proj;
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		if (project == null) {
			shell.setText("Create Project");
		} else {
			shell.setText("Update Project");
		}
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "ProjectDialog");
			}
		});
		display = shell.getDisplay();

		Composite mapComposite = new Composite(shell, SWT.NONE);
		mapComposite.setLayout(new GridLayout(3, false));
		mapComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mapLabel = new Label(mapComposite, SWT.NONE);
		mapLabel.setText("DITA Map");

		mapText = new Text(mapComposite, SWT.BORDER);
		mapText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (project != null) {
			mapText.setText(project.getMap());
		}

		Button mapBrowse = new Button(mapComposite, SWT.PUSH);
		mapBrowse.setText("Browse...");
		mapBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.SINGLE);
				fd.setFilterExtensions(new String[] { "*.ditamap", "*.xml", "*.*" });
				fd.setFilterNames(
						new String[] { "DITA Map Files [*.ditamap]", "XML Files [*.xml]", "ALL Files [*.*]" });
				if (mapText.getText() != null && !mapText.getText().isEmpty()) {
					File f = new File(mapText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getParentFile().getAbsolutePath());
						fd.setFileName(f.getName());
					}
				}
				String map = fd.open();
				if (map != null) {
					mapText.setText(map);
				}
			}
		});

		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label descriptionLabel = new Label(top, SWT.NONE);
		descriptionLabel.setText("Project Name");

		titleText = new Text(top, SWT.BORDER);
		titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (project != null) {
			titleText.setText(project.getTitle());
		}

		Group descriptionGroup = new Group(shell, SWT.NONE);
		descriptionGroup.setText("Project Description");
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		descriptionGroup.setLayout(groupLayout);
		descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		StyledText descriptionText = new StyledText(descriptionGroup, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData descData = new GridData(GridData.FILL_HORIZONTAL);
		descData.heightHint = descriptionText.getLineHeight() * 5;
		descriptionText.setLayoutData(descData);
		if (project != null) {
			descriptionText.setText(project.getDescription());
		}

		Label sourceLabel = new Label(top, SWT.NONE);
		sourceLabel.setText("Source Language");

		sourceLanguages = new Combo(top, SWT.READ_ONLY | SWT.DROP_DOWN);
		sourceLanguages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLanguages.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage("Error retrieving language list");
			box.open();
			shell.close();
		}
		if (project != null) {
			try {
				sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(),
						LanguageUtils.getLanguage(project.getSrcLanguage()).getDescription()));
			} catch (IOException e) {
				logger.log(Level.ERROR, e);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage("Error retrieving source language");
				box.open();
				shell.close();
			}
		} else {
			try {
				sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(),
						LanguageUtils.getLanguage(GeneralPreferences.getDefaultSource().getCode()).getDescription()));
			} catch (IOException e) {
				logger.log(Level.ERROR, e);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage("Error retrieving default source language");
				box.open();
				shell.close();
			}
		}

		CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem languagesItem = new CTabItem(tabFolder, SWT.NONE);
		languagesItem.setText("Target Languages");

		Composite languagesComposite = new Composite(tabFolder, SWT.NONE);
		languagesComposite.setLayout(new GridLayout());
		languagesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		languagesItem.setControl(languagesComposite);

		Table langsTable = new Table(languagesComposite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		langsTable.setLinesVisible(true);
		langsTable.setHeaderVisible(false);
		GridData langData = new GridData(GridData.FILL_BOTH);
		langData.heightHint = langsTable.getItemHeight() * 8;
		langsTable.setLayoutData(langData);

		TableColumn langColumn = new TableColumn(langsTable, SWT.FILL);
		if (project != null) {
			try {
				List<String> tgtLangs = project.getLanguages();
				Iterator<String> it = tgtLangs.iterator();
				while (it.hasNext()) {
					Language lang = LanguageUtils.getLanguage(it.next());
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguage(lang.getCode()).getDescription());
					item.setData("language", lang);
				}
			} catch (IOException e) {
				logger.log(Level.ERROR, e);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage("Error getting target languages");
				box.open();
				shell.close();
			}

		} else {
			try {
				List<Language> defaultTargets = GeneralPreferences.getDefaultTargets();
				Iterator<Language> it = defaultTargets.iterator();
				while (it.hasNext()) {
					Language l = it.next();
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
					item.setData("language", l);
				}
			} catch (IOException e) {
				logger.log(Level.ERROR, e);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage("Error retrieving default target languages");
				box.open();
				shell.close();
			}

		}
		langsTable.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				int width = langsTable.getClientArea().width;
				int scroll = langsTable.getVerticalBar().isVisible() ? langsTable.getVerticalBar().getSize().x : 0;
				langColumn.setWidth(width - scroll);
			}
		});

		Composite langBottom = new Composite(languagesComposite, SWT.NONE);
		langBottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		langBottom.setLayout(new GridLayout(3, false));

		Label filler = new Label(langBottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button addLang = new Button(langBottom, SWT.PUSH);
		addLang.setText("Add Target Language");
		addLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					LanguageAddDialog dialog = new LanguageAddDialog(shell, SWT.DIALOG_TRIM);
					dialog.show();
					if (!dialog.wasCancelled()) {
						Language l = dialog.getLanguage();
						TableItem[] oldItems = langsTable.getItems();
						for (int i = 0; i < oldItems.length; i++) {
							Language lang = (Language) oldItems[i].getData("language");
							if (l.getCode().equals(lang.getCode())) {
								MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
								box.setMessage("Duplicated language");
								box.open();
								return;
							}
						}
						TableItem item = new TableItem(langsTable, SWT.NONE);
						item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
						item.setData("language", l);
					}
				} catch (IOException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage("Error adding language");
					box.open();
					shell.close();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Button deleteLang = new Button(langBottom, SWT.PUSH);
		deleteLang.setText("Remove Selected Languages");
		deleteLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					TableItem[] oldItems = langsTable.getItems();
					List<Language> defaultTargets = new Vector<>();
					for (int i = 0; i < oldItems.length; i++) {
						if (!oldItems[i].getChecked()) {
							defaultTargets.add((Language) oldItems[i].getData("language"));
						}
					}
					langsTable.removeAll();
					for (int i = 0; i < defaultTargets.size(); i++) {
						Language l = defaultTargets.get(i);
						TableItem item = new TableItem(langsTable, SWT.NONE);
						item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
						item.setData("language", l);
					}
				} catch (IOException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage("Error deleting language");
					box.open();
					shell.close();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		CTabItem memoriesItem = new CTabItem(tabFolder, SWT.NONE);
		memoriesItem.setText("Memories");

		Composite memoriesComposite = new Composite(tabFolder, SWT.NONE);
		memoriesComposite.setLayout(new GridLayout());
		memoriesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		memoriesItem.setControl(memoriesComposite);

		Table memoriesTable = new Table(memoriesComposite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		memoriesTable.setLinesVisible(true);
		langsTable.setHeaderVisible(false);
		GridData memData = new GridData(GridData.FILL_BOTH);
		memData.heightHint = memoriesTable.getItemHeight() * 8;
		memoriesTable.setLayoutData(memData);

		TableColumn memColumn = new TableColumn(memoriesTable, SWT.FILL);
		List<Long> mems = project == null ? new Vector<>() : project.getMemories();
		List<Memory> memories = new Vector<>();
		for (int i = 0; i < mems.size(); i++) {
			try {
				memories.add(mainView.getController().getMemory(mems.get(i)));
			} catch (JSONException | IOException | ParseException e) {
				logger.log(Level.ERROR, e);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage("Error retrieving memory");
				box.open();
				return;
			}
		}
		for (int i = 0; i < memories.size(); i++) {
			Memory m = memories.get(i);
			TableItem item = new TableItem(memoriesTable, SWT.NONE);
			item.setText(m.getName());
			item.setData("memory", m);
		}
		memColumn.setWidth(400);

		Composite memBottom = new Composite(memoriesComposite, SWT.NONE);
		memBottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		memBottom.setLayout(new GridLayout(3, false));

		Label memFiller = new Label(memBottom, SWT.NONE);
		memFiller.setText("");
		memFiller.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button associateMemories = new Button(memBottom, SWT.PUSH);
		associateMemories.setText("Associate Other Memories");
		associateMemories.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				List<Long> mems = project == null ? new Vector<>() : project.getMemories();
				List<Memory> memories = new Vector<>();
				for (int i = 0; i < mems.size(); i++) {
					try {
						memories.add(mainView.getController().getMemory(mems.get(i)));
					} catch (JSONException | IOException | ParseException e) {
						logger.log(Level.ERROR, e);
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
						box.setMessage("Error retrieving memory");
						box.open();
						return;
					}
				}
				MemorySelectionDialog dialog = new MemorySelectionDialog(shell, SWT.DIALOG_TRIM, memories, mainView);
				dialog.show();
				if (!dialog.wasCancelled()) {
					if (project != null) {
						List<Memory> selected = dialog.getSelected();
						for (int i = 0; i < selected.size(); i++) {
							Memory m = selected.get(i);
							TableItem item = new TableItem(memoriesTable, SWT.NONE);
							item.setText(m.getName());
							item.setData("memory", m);
							project.getMemories().add(m.getId());
						}
						memoriesTable.layout();
					} else {
						// TODO
					}
				}
			}
		});

		Button removeMemories = new Button(memBottom, SWT.PUSH);
		removeMemories.setText("Remove Selected Memories");
		removeMemories.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				List<Integer> indices = new Vector<>();
				List<Long> newMems = new Vector<>();
				for (int i = 0; i < memoriesTable.getItemCount(); i++) {
					if (memoriesTable.getItem(i).getChecked()) {
						newMems.add(((Memory) memoriesTable.getItem(i).getData("memory")).getId());
						indices.add(i);
					}
				}
				memoriesTable.remove(toArray(indices));
				memoriesTable.layout();
				if (project != null) {
					project.getMemories().removeAll(newMems);
				}
			}

			private int[] toArray(List<Integer> indices) {
				int[] array = new int[indices.size()];
				for (int i = 0; i < indices.size(); i++) {
					array[i] = indices.get(i);
				}
				return array;
			}
		});

		tabFolder.setSelection(languagesItem);

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler2 = new Label(bottom, SWT.NONE);
		filler2.setText(" ");
		filler2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button create = new Button(bottom, SWT.PUSH);
		if (project == null) {
			create.setText("Create Project");
		} else {
			create.setText("Update Project");
		}

		create.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (titleText.getText() == null || titleText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Enter project name");
					box.open();
					return;
				}
				if (mapText.getText() == null || mapText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select a DITA map");
					box.open();
					return;
				}
				File f = new File(mapText.getText());
				if (!f.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Selected DITA map does not exist");
					box.open();
					return;
				}
				if (sourceLanguages.getText() == null || sourceLanguages.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select source language");
					box.open();
					return;
				}
				Language srcLang;
				try {
					srcLang = LanguageUtils.languageFromName(sourceLanguages.getText());
				} catch (IOException | SAXException | ParserConfigurationException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Error getting source language");
					box.open();
					return;
				}
				TableItem[] items = langsTable.getItems();
				if (items.length == 0) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select target languages");
					box.open();
					return;
				}
				List<Language> targetLangs = new Vector<>();
				List<String> tgtCodes = new Vector<>();
				for (int i = 0; i < items.length; i++) {
					targetLangs.add((Language) items[i].getData("language"));
					tgtCodes.add(((Language) items[i].getData("language")).getCode());
				}
				if (project == null) {
					long id = System.currentTimeMillis();
					Project p = new Project(id, titleText.getText(), descriptionText.getText(),
							System.getProperty("user.name"), mapText.getText(), new Date(),
							Project.NEW, new Date(), srcLang.getCode(), tgtCodes, new Vector<>(),
							new Vector<>(), new Hashtable<>());
					p.getMemories().add(id);
					try {
						mainView.getController().createProject(p);
					} catch (IOException | ClassNotFoundException | SQLException | SAXException
							| ParserConfigurationException e) {
						logger.log(Level.ERROR, e);
						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						box.setMessage("Error creating project");
						box.open();
					}
				} else {
					project.setTitle(titleText.getText());
					project.setDescription(descriptionText.getText());
					project.setMap(mapText.getText());
					project.setTgtLanguages(tgtCodes);
					try {
						mainView.getController().updateProject(project);
					} catch (IOException | JSONException | ParseException e) {
						logger.log(Level.ERROR, e);
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
						box.setMessage("Error updating project");
						box.open();
						return;
					}
				}
				mainView.getProjectsView().loadProjects();
				mainView.getMemoriesView().loadMemories();
				shell.close();
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "ProjectDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
