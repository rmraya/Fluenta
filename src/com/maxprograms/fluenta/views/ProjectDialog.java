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
import java.sql.SQLException;
import java.util.Date;
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

	protected Shell shell;
	private Display display;
	protected Text titleText;
	protected Text mapText;
	protected Combo sourceLanguages;
	protected Project project;

	public ProjectDialog(Shell parent, int style, Project proj) {
		super(parent, style);
		project = proj;
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		if (project.getId() == 0l) {
			shell.setText(Messages.getString("ProjectDialog.0")); 
		} else {
			shell.setText(Messages.getString("ProjectDialog.1")); 
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
		mapLabel.setText(Messages.getString("ProjectDialog.3")); 

		mapText = new Text(mapComposite, SWT.BORDER);
		mapText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mapText.setText(project.getMap());

		Button mapBrowse = new Button(mapComposite, SWT.PUSH);
		mapBrowse.setText(Messages.getString("ProjectDialog.4")); 
		mapBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.SINGLE);
				fd.setFilterExtensions(new String[] { "*.ditamap", "*.xml", "*.*" });   
				fd.setFilterNames(new String[] { Messages.getString("ProjectDialog.8"), 
						Messages.getString("ProjectDialog.9"), Messages.getString("ProjectDialog.10") });  
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
		descriptionLabel.setText(Messages.getString("ProjectDialog.12")); 

		titleText = new Text(top, SWT.BORDER);
		titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		titleText.setText(project.getTitle());

		Group descriptionGroup = new Group(shell, SWT.NONE);
		descriptionGroup.setText(Messages.getString("ProjectDialog.13")); 
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		descriptionGroup.setLayout(groupLayout);
		descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		StyledText descriptionText = new StyledText(descriptionGroup, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData descData = new GridData(GridData.FILL_HORIZONTAL);
		descData.heightHint = descriptionText.getLineHeight() * 5;
		descriptionText.setLayoutData(descData);
		descriptionText.setText(project.getDescription());

		Label sourceLabel = new Label(top, SWT.NONE);
		sourceLabel.setText(Messages.getString("ProjectDialog.14")); 

		sourceLanguages = new Combo(top, SWT.READ_ONLY | SWT.DROP_DOWN);
		sourceLanguages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLanguages.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectDialog.15")); 
			box.open();
			shell.close();
		}
		if (project.getSrcLanguage() != null) {
			try {
				sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(),
						LanguageUtils.getLanguage(project.getSrcLanguage().getCode()).getDescription()));
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("ProjectDialog.16")); 
				box.open();
				shell.close();
			}
		} else {
			try {
				sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(),
						LanguageUtils.getLanguage(ProjectPreferences.getDefaultSource().getCode()).getDescription()));
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("ProjectDialog.16")); 
				box.open();
				shell.close();
			}
		}

		CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem languagesItem = new CTabItem(tabFolder, SWT.NONE);
		languagesItem.setText(Messages.getString("ProjectDialog.17")); 

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
		if (!project.getLanguages().isEmpty()) {
			try {
				List<Language> tgtLangs = project.getLanguages();
				Iterator<Language> it = tgtLangs.iterator();
				while (it.hasNext()) {
					Language l = it.next();
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
					item.setData("language", l); 
				}
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("ProjectDialog.2")); 
				box.open();
				shell.close();
			}

		} else {
			try {
				List<Language> defaultTargets = ProjectPreferences.getDefaultTargets();
				Iterator<Language> it = defaultTargets.iterator();
				while (it.hasNext()) {
					Language l = it.next();
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
					item.setData("language", l); 
				}
			} catch (IOException e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("ProjectDialog.20")); 
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
		addLang.setText(Messages.getString("ProjectDialog.22")); 
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
								box.setMessage(Messages.getString("ProjectDialog.7")); 
								box.open();
								return;
							}
						}
						TableItem item = new TableItem(langsTable, SWT.NONE);
						item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
						item.setData("language", l); 
					}
				} catch (IOException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.5")); 
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
		deleteLang.setText(Messages.getString("ProjectDialog.24")); 
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
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.6")); 
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
		memoriesItem.setText(Messages.getString("ProjectDialog.27")); 

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
		List<Memory> memories = project.getMemories();
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
		associateMemories.setText(Messages.getString("ProjectDialog.30")); 
		associateMemories.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				MemorySelectionDialog dialog = new MemorySelectionDialog(shell, SWT.DIALOG_TRIM, project.getMemories());
				dialog.show();
				if (!dialog.wasCancelled()) {
					List<Memory> selected = dialog.getSelected();
					for (int i = 0; i < selected.size(); i++) {
						Memory m = selected.get(i);
						TableItem item = new TableItem(memoriesTable, SWT.NONE);
						item.setText(m.getName());
						item.setData("memory", m); 
						project.getMemories().add(m);
					}
					memoriesTable.layout();
				}
			}
		});

		Button removeMemories = new Button(memBottom, SWT.PUSH);
		removeMemories.setText(Messages.getString("ProjectDialog.32")); 
		removeMemories.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				List<Integer> indices = new Vector<>();
				List<Memory> newMems = new Vector<>();
				for (int i = 0; i < memoriesTable.getItemCount(); i++) {
					if (memoriesTable.getItem(i).getChecked()) {
						newMems.add((Memory) memoriesTable.getItem(i).getData("memory")); 
						indices.add(i);
					}
				}
				memoriesTable.remove(toArray(indices));
				memoriesTable.layout();
				project.getMemories().removeAll(newMems);
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
		if (project.getId() == 0l) {
			create.setText(Messages.getString("ProjectDialog.35")); 
		} else {
			create.setText(Messages.getString("ProjectDialog.36")); 
		}

		create.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (titleText.getText() == null || titleText.getText().isEmpty()) { 
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.38")); 
					box.open();
					return;
				}
				if (mapText.getText() == null || mapText.getText().isEmpty()) { 
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.40")); 
					box.open();
					return;
				}
				File f = new File(mapText.getText());
				if (!f.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.41")); 
					box.open();
					return;
				}
				if (sourceLanguages.getText() == null || sourceLanguages.getText().isEmpty()) { 
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.43")); 
					box.open();
					return;
				}
				Language srcLang;
				try {
					srcLang = LanguageUtils.languageFromName(sourceLanguages.getText());
				} catch (IOException | SAXException | ParserConfigurationException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.44")); 
					box.open();
					return;
				}
				TableItem[] items = langsTable.getItems();
				if (items.length == 0) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectDialog.45")); 
					box.open();
					return;
				}
				List<Language> targetLangs = new Vector<>();
				for (int i = 0; i < items.length; i++) {
					targetLangs.add((Language) items[i].getData("language")); 
				}
				if (project.getId() == 0l) {
					long id = System.currentTimeMillis();
					Project p = new Project(id, titleText.getText(), descriptionText.getText(),
							System.getProperty("user.name"), mapText.getText(), new Date(), 
							Project.NEW, null, srcLang, targetLangs, new Vector<>());
					Memory m = new Memory(id, p.getTitle(), p.getDescription(), p.getOwner(), new Date(), null,
							p.getSrcLanguage(), new Vector<>());
					p.getMemories().add(m);
					try {
						MainView.getController().createProject(p);
					} catch (IOException | ClassNotFoundException | SQLException | SAXException
							| ParserConfigurationException e) {
						e.printStackTrace();
						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						box.setMessage(Messages.getString("ProjectDialog.48")); 
						box.open();
					}
				} else {
					project.setTitle(titleText.getText());
					project.setDescription(descriptionText.getText());
					project.setMap(mapText.getText());
					project.setTgtLanguages(targetLangs);
					MainView.getController().updateProject(project);
				}
				MainView.getProjectsView().loadProjects();
				MainView.getMemoriesView().loadMemories();
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
