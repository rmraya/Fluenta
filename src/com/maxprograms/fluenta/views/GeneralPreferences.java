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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.utils.TextUtils;

public class GeneralPreferences extends Composite implements AddLanguageListener {

	Logger logger = System.getLogger(GeneralPreferences.class.getName());

	protected Combo applicationLangCombo;
	protected List<Language> defaultTargets;
	private Language defaultSource;
	protected Combo sourceLangCombo;
	protected Text projectsText;
	protected Text memoriesText;
	protected Text srxText;
	Table langsTable;
	AddLanguageListener instance;

	public GeneralPreferences(Composite parent, int style) {
		super(parent, style);
		instance = this;
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Group appLanguageGroup = new Group(this, SWT.NONE);
		appLanguageGroup.setLayout(new GridLayout(2, false));
		appLanguageGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label languageLabel = new Label(appLanguageGroup, SWT.NONE);
		languageLabel.setText(Messages.getString("GeneralPreferences.24"));

		applicationLangCombo = new Combo(appLanguageGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		applicationLangCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			applicationLangCombo.setItems(LanguageUtils.getLanguage("en").getDescription(),
					LanguageUtils.getLanguage("es").getDescription());
			applicationLangCombo.select(TextUtils.geIndex(applicationLangCombo.getItems(),
					LanguageUtils.getLanguage(Preferences.getInstance().getApplicationLanguage()).getDescription()));
		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.25"));
			box.open();
			getShell().close();
		}

		Group foldersGroup = new Group(this, SWT.NONE);
		foldersGroup.setLayout(new GridLayout(3, false));
		foldersGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label projectsLabel = new Label(foldersGroup, SWT.NONE);
		projectsLabel.setText(Messages.getString("GeneralPreferences.0"));

		projectsText = new Text(foldersGroup, SWT.BORDER);
		projectsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			Preferences preferences = Preferences.getInstance();
			projectsText.setText(preferences.getProjectsFolder().getAbsolutePath());
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.1"));
			box.open();
			getShell().close();
		}

		Button browseProjects = new Button(foldersGroup, SWT.PUSH);
		browseProjects.setText(Messages.getString("GeneralPreferences.2"));
		browseProjects.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SINGLE);
				if (!projectsText.getText().isEmpty()) {
					File f = new File(projectsText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getAbsolutePath());
					}
				}
				String folder = fd.open();
				if (folder != null) {
					projectsText.setText(folder);
				}
			}

		});

		Label memoriesLabel = new Label(foldersGroup, SWT.NONE);
		memoriesLabel.setText(Messages.getString("GeneralPreferences.3"));

		memoriesText = new Text(foldersGroup, SWT.BORDER);
		memoriesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			Preferences preferences = Preferences.getInstance();
			memoriesText.setText(preferences.getMemoriesFolder().getAbsolutePath());
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.4"));
			box.open();
			getShell().close();
		}

		Button browseMemories = new Button(foldersGroup, SWT.PUSH);
		browseMemories.setText(Messages.getString("GeneralPreferences.2"));
		browseMemories.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SINGLE);
				if (!memoriesText.getText().isEmpty()) {
					File f = new File(memoriesText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getAbsolutePath());
					}
				}
				String folder = fd.open();
				if (folder != null) {
					memoriesText.setText(folder);
				}
			}

		});

		Label srxLabel = new Label(foldersGroup, SWT.NONE);
		srxLabel.setText(Messages.getString("GeneralPreferences.5"));

		srxText = new Text(foldersGroup, SWT.BORDER);
		srxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			Preferences preferences = Preferences.getInstance();
			srxText.setText(preferences.getDefaultSRX());
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.6"));
			box.open();
			getShell().close();
		}

		Button browseSrx = new Button(foldersGroup, SWT.PUSH);
		browseSrx.setText(Messages.getString("GeneralPreferences.2"));
		browseSrx.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
				fd.setFilterExtensions(new String[] { "*.srx", "*.*" });
				fd.setFilterNames(new String[] { Messages.getString("GeneralPreferences.7"),
						Messages.getString("GeneralPreferences.8") });
				if (!srxText.getText().isEmpty()) {
					File f = new File(srxText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getParentFile().getAbsolutePath());
						fd.setFileName(f.getName());
					}
				}
				String srx = fd.open();
				if (srx != null) {
					srxText.setText(srx);
				}
			}

		});

		Group languagesGroup = new Group(this, SWT.NONE);
		languagesGroup.setText(Messages.getString("GeneralPreferences.9"));
		languagesGroup.setLayout(new GridLayout());
		languagesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite sourceComposite = new Composite(languagesGroup, SWT.NONE);
		sourceComposite.setLayout(new GridLayout(2, false));
		sourceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label sourceLabel = new Label(sourceComposite, SWT.NONE);
		sourceLabel.setText(Messages.getString("GeneralPreferences.10"));

		sourceLangCombo = new Combo(sourceComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		sourceLangCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLangCombo.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.11"));
			box.open();
			getShell().close();
		}

		try {
			defaultSource = getDefaultSource();
			if (defaultSource != null) {
				sourceLangCombo.select(TextUtils.geIndex(sourceLangCombo.getItems(),
						LanguageUtils.getLanguage(defaultSource.getCode()).getDescription()));
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.12"));
			box.open();
			getShell().close();
		}

		Label targetLabel = new Label(sourceComposite, SWT.NONE);
		targetLabel.setText(Messages.getString("GeneralPreferences.13"));

		Composite targetLanguages = new Composite(languagesGroup, SWT.NONE);
		targetLanguages.setLayoutData(new GridData(GridData.FILL_BOTH));
		targetLanguages.setLayout(new GridLayout());

		langsTable = new Table(targetLanguages, SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK | SWT.BORDER);
		langsTable.setLinesVisible(true);
		langsTable.setHeaderVisible(false);
		GridData langData = new GridData(GridData.FILL_BOTH);
		langData.heightHint = langsTable.getItemHeight() * 8;
		langsTable.setLayoutData(langData);

		TableColumn langDescColumn = new TableColumn(langsTable, SWT.FILL);
		langDescColumn.setText(Messages.getString("GeneralPreferences.14"));

		langsTable.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				int width = langsTable.getClientArea().width;
				int scroll = langsTable.getVerticalBar().isVisible() ? langsTable.getVerticalBar().getSize().x : 0;
				langDescColumn.setWidth(width - scroll);
			}
		});

		try {
			defaultTargets = getDefaultTargets();
			for (int i = 0; i < defaultTargets.size(); i++) {
				Language l = defaultTargets.get(i);
				TableItem item = new TableItem(langsTable, SWT.NONE);
				item.setText(l.getDescription());
				item.setData("language", l);
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.15"));
			box.open();
			getShell().close();
		}

		Composite targetButtons = new Composite(targetLanguages, SWT.NONE);
		targetButtons.setLayout(new GridLayout(3, false));
		targetButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(targetButtons, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button addLang = new Button(targetButtons, SWT.PUSH);
		addLang.setText(Messages.getString("GeneralPreferences.16"));
		addLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				AddLanguageDialog dialog = new AddLanguageDialog(getShell(), SWT.DIALOG_TRIM, instance);
				dialog.show();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Button removeLang = new Button(targetButtons, SWT.PUSH);
		removeLang.setText(Messages.getString("GeneralPreferences.17"));
		removeLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] oldItems = langsTable.getItems();
				defaultTargets.clear();
				for (int i = 0; i < oldItems.length; i++) {
					if (!oldItems[i].getChecked()) {
						defaultTargets.add((Language) oldItems[i].getData("language"));
					}
				}
				langsTable.removeAll();
				for (int i = 0; i < defaultTargets.size(); i++) {
					Language l = defaultTargets.get(i);
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(l.getDescription());
					item.setData("language", l);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Composite bottom = new Composite(this, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler2 = new Label(bottom, SWT.NONE);
		filler2.setText("");
		filler2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button save = new Button(bottom, SWT.PUSH);
		save.setText(Messages.getString("GeneralPreferences.18"));
		save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (srxText.getText().isEmpty()) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("GeneralPreferences.19"));
					box.open();
					return;
				}
				try {
					File srx = new File(srxText.getText());
					if (!srx.exists()) {
						MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						box.setMessage(Messages.getString("GeneralPreferences.20"));
						box.open();
						return;
					}
					Preferences preferences = Preferences.getInstance();

					if (applicationLangCombo.getSelectionIndex() != -1) {
						String name = applicationLangCombo.getItem(applicationLangCombo.getSelectionIndex());
						Language l = LanguageUtils.languageFromName(name);
						if (!l.getCode().equals(preferences.getApplicationLanguage())) {
							MessageBox box = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
							box.setMessage(Messages.getString("GeneralPreferences.26"));
							box.open();
						}
						preferences.setApplicationLanguage(l.getCode());
					}

					JSONObject workDir = preferences.get("workDir");
					workDir.put("projects", projectsText.getText());
					workDir.put("memories", memoriesText.getText());
					workDir.put("defaultSRX", srxText.getText());
					preferences.save("workDir", workDir);

					JSONObject targetLangs = new JSONObject();
					Iterator<Language> it = defaultTargets.iterator();
					while (it.hasNext()) {
						Language l = it.next();
						targetLangs.put(l.getCode(), LanguageUtils.getLanguage(l.getCode()).getDescription());
					}
					preferences.remove("DefaultTargetLanguages");
					preferences.save("DefaultTargetLanguages", targetLangs);
					if (sourceLangCombo.getSelectionIndex() != -1) {
						JSONObject sourceLangs = new JSONObject();
						Language l = LanguageUtils
								.languageFromName(sourceLangCombo.getItem(sourceLangCombo.getSelectionIndex()));
						sourceLangs.put("default", l.getCode());
						preferences.save("DefaultSourceLanguages", sourceLangs);
					}
				} catch (IOException | SAXException | ParserConfigurationException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("GeneralPreferences.21"));
					box.open();
					getShell().close();
				}
				getShell().close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});
		save.setFocus();
	}

	public static Language getDefaultSource() throws IOException, SAXException, ParserConfigurationException {
		Preferences preferences = Preferences.getInstance();
		return LanguageUtils.getLanguage(preferences.get("DefaultSourceLanguages", "default", "en-US"));
	}

	public static List<Language> getDefaultTargets() throws IOException, SAXException, ParserConfigurationException {
		List<Language> result = new Vector<>();
		Preferences preferences = Preferences.getInstance();
		JSONObject json = preferences.get("DefaultTargetLanguages");
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			result.add(LanguageUtils.getLanguage(keys.next()));
		}
		if (result.isEmpty()) {
			result.add(LanguageUtils.getLanguage("fr"));
			result.add(LanguageUtils.getLanguage("de"));
			result.add(LanguageUtils.getLanguage("it"));
			result.add(LanguageUtils.getLanguage("es"));
			result.add(LanguageUtils.getLanguage("ja-JP"));
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public void addLanguage(String language) {
		try {
			Language l = LanguageUtils.getLanguage(language);
			if (defaultTargets.contains(l)) {
				MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				box.setMessage(Messages.getString("GeneralPreferences.22"));
				box.open();
				return;
			}
			defaultTargets.add(l);
			Collections.sort(defaultTargets);
			langsTable.removeAll();
			langsTable.setItemCount(0);
			Iterator<Language> it = defaultTargets.iterator();
			while (it.hasNext()) {
				Language lang = it.next();
				TableItem item = new TableItem(langsTable, SWT.NONE);
				item.setText(lang.getDescription());
				item.setData("language", lang);
			}
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			logger.log(Level.ERROR, ex);
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GeneralPreferences.23"));
			box.open();
		}
	}

}
