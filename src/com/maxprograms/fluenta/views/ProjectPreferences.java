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

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
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
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.languages.Language;
import com.maxprograms.utils.LanguageUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.utils.TextUtils;

public class ProjectPreferences extends Composite {

	protected Vector<Language> defaultTargets;
	private Language defaultSource;
	protected Combo sourceLangCombo;
	protected Text srxText;

	public ProjectPreferences(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite sourceComposite = new Composite(this, SWT.NONE);
		sourceComposite.setLayout(new GridLayout(2, false));
		sourceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label sourceLabel = new Label(sourceComposite, SWT.NONE);
		sourceLabel.setText(Messages.getString("ProjectPreferences.0")); //$NON-NLS-1$

		sourceLangCombo = new Combo(sourceComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		sourceLangCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLangCombo.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.1")); //$NON-NLS-1$
			box.open();
			getShell().close();
		}

		try {
			defaultSource = getDefaultSource();
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.2")); //$NON-NLS-1$
			box.open();
			getShell().close();
		}
		if (defaultSource != null) {
			sourceLangCombo.select(TextUtils.geIndex(sourceLangCombo.getItems(),
					LanguageUtils.getLanguageName(defaultSource.getCode())));
		}

		Group targetLanguages = new Group(this, SWT.NONE);
		targetLanguages.setText(Messages.getString("ProjectPreferences.3")); //$NON-NLS-1$
		targetLanguages.setLayoutData(new GridData(GridData.FILL_BOTH));
		targetLanguages.setLayout(new GridLayout());

		Table langsTable = new Table(targetLanguages, SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		langsTable.setLinesVisible(true);
		langsTable.setHeaderVisible(false);
		GridData langData = new GridData(GridData.FILL_BOTH);
		langData.heightHint = langsTable.getItemHeight() * 8;
		langsTable.setLayoutData(langData);

		TableColumn langDescColumn = new TableColumn(langsTable, SWT.FILL);
		langDescColumn.setText(Messages.getString("ProjectPreferences.4")); //$NON-NLS-1$

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
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.5")); //$NON-NLS-1$
			box.open();
			getShell().close();
		}
		for (int i = 0; i < defaultTargets.size(); i++) {
			Language l = defaultTargets.get(i);
			TableItem item = new TableItem(langsTable, SWT.NONE);
			item.setText(LanguageUtils.getLanguageName(l.getCode()));
			item.setData("language", l); //$NON-NLS-1$
		}

		Composite targetButtons = new Composite(targetLanguages, SWT.NONE);
		targetButtons.setLayout(new GridLayout(3, false));
		targetButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(targetButtons, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button addLang = new Button(targetButtons, SWT.PUSH);
		addLang.setText(Messages.getString("ProjectPreferences.8")); //$NON-NLS-1$
		addLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				LanguageAddDialog dialog = new LanguageAddDialog(getShell(), SWT.DIALOG_TRIM);
				dialog.show();
				if (!dialog.wasCancelled()) {
					Language l = dialog.getLanguage();
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguageName(l.getCode()));
					item.setData("language", l); //$NON-NLS-1$
					defaultTargets.add(l);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Button removeLang = new Button(targetButtons, SWT.PUSH);
		removeLang.setText(Messages.getString("ProjectPreferences.10")); //$NON-NLS-1$
		removeLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] oldItems = langsTable.getItems();
				defaultTargets.clear();
				for (int i = 0; i < oldItems.length; i++) {
					if (!oldItems[i].getChecked()) {
						defaultTargets.add((Language) oldItems[i].getData("language")); //$NON-NLS-1$
					}
				}
				langsTable.removeAll();
				for (int i = 0; i < defaultTargets.size(); i++) {
					Language l = defaultTargets.get(i);
					TableItem item = new TableItem(langsTable, SWT.NONE);
					item.setText(LanguageUtils.getLanguageName(l.getCode()));
					item.setData("language", l); //$NON-NLS-1$
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Composite srxComposite = new Composite(this, SWT.NONE);
		srxComposite.setLayout(new GridLayout(3, false));
		srxComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label srxLabel = new Label(srxComposite, SWT.NONE);
		srxLabel.setText(Messages.getString("ProjectPreferences.13")); //$NON-NLS-1$

		srxText = new Text(srxComposite, SWT.BORDER);
		srxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			srxText.setText(getDefaultSRX());
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.14")); //$NON-NLS-1$
			box.open();
			getShell().close();
		}

		Button browse = new Button(srxComposite, SWT.PUSH);
		browse.setText(Messages.getString("ProjectPreferences.15")); //$NON-NLS-1$
		browse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
				fd.setFilterExtensions(new String[] { "*.srx", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterNames(new String[] { Messages.getString("ProjectPreferences.18"), //$NON-NLS-1$
						Messages.getString("ProjectPreferences.19") }); //$NON-NLS-1$
				if (!srxText.getText().equals("")) { //$NON-NLS-1$
					File f = new File(srxText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getParentFile().getAbsolutePath());
						fd.setFileName(f.getName());
					}
				}
				String map = fd.open();
				if (map != null) {
					srxText.setText(map);
				}
			}

		});

		Composite bottom = new Composite(this, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler2 = new Label(bottom, SWT.NONE);
		filler2.setText(""); //$NON-NLS-1$
		filler2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button save = new Button(bottom, SWT.PUSH);
		save.setText(Messages.getString("ProjectPreferences.22")); //$NON-NLS-1$
		save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (srxText.getText().equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.24")); //$NON-NLS-1$
					box.open();
					return;
				}
				try {
					File srx = new File(srxText.getText());
					if (!srx.exists()) {
						MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						box.setMessage(Messages.getString("ProjectPreferences.25")); //$NON-NLS-1$
						box.open();
						return;
					}
					Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
					Hashtable<String, String> targetLangs = new Hashtable<>();
					Iterator<Language> it = defaultTargets.iterator();
					while (it.hasNext()) {
						Language l = it.next();
						targetLangs.put(l.getCode(), LanguageUtils.getLanguageName(l.getCode()));
					}
					prefs.save("DefaultTargetLanguages", targetLangs); //$NON-NLS-1$
					if (sourceLangCombo.getSelectionIndex() != -1) {
						Hashtable<String, String> sourceLangs = new Hashtable<>();
						Language l = LanguageUtils
								.getLanguage(sourceLangCombo.getItem(sourceLangCombo.getSelectionIndex()));
						sourceLangs.put(l.getCode(), LanguageUtils.getLanguageName(l.getCode()));
						prefs.save("DefaultSourceLanguages", sourceLangs); //$NON-NLS-1$
					}
					Hashtable<String, String> srxTable = new Hashtable<>();
					srxTable.put("srx", srxText.getText()); //$NON-NLS-1$
					prefs.save("DefaultSRX", srxTable); //$NON-NLS-1$
				} catch (IOException | SAXException | ParserConfigurationException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.30")); //$NON-NLS-1$
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

	public static String getDefaultSRX() throws IOException {
		Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
		Hashtable<String, String> table = prefs.get("DefaultSRX"); //$NON-NLS-1$
		Enumeration<String> keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			return table.get(key);
		}
		File srxFolder = new File(Preferences.getPreferencesDir(), "srx"); //$NON-NLS-1$
		File defaultSrx = new File(srxFolder, "default.srx"); //$NON-NLS-1$
		return defaultSrx.getAbsolutePath();
	}

	public static Language getDefaultSource() throws IOException {
		Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
		Hashtable<String, String> table = prefs.get("DefaultSourceLanguages"); //$NON-NLS-1$
		Enumeration<String> keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			return new Language(key, table.get(key));
		}
		return new Language("en-US", LanguageUtils.getLanguageName("en-US")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static Vector<Language> getDefaultTargets() throws IOException {
		TreeSet<Language> tree = new TreeSet<>(new Comparator<Language>() {

			@Override
			public int compare(Language o1, Language o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}

		});
		Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
		Hashtable<String, String> table = prefs.get("DefaultTargetLanguages"); //$NON-NLS-1$
		Enumeration<String> keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			tree.add(new Language(key, table.get(key)));
		}
		if (tree.size() == 0) {
			tree.add(new Language("fr", LanguageUtils.getLanguageName("fr"))); //$NON-NLS-1$ //$NON-NLS-2$
			tree.add(new Language("de", LanguageUtils.getLanguageName("de"))); //$NON-NLS-1$ //$NON-NLS-2$
			tree.add(new Language("it", LanguageUtils.getLanguageName("it"))); //$NON-NLS-1$ //$NON-NLS-2$
			tree.add(new Language("es", LanguageUtils.getLanguageName("es"))); //$NON-NLS-1$ //$NON-NLS-2$
			tree.add(new Language("ja-JP", LanguageUtils.getLanguageName("ja-JP"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Vector<Language> result = new Vector<>();
		result.addAll(tree);
		return result;
	}

}
