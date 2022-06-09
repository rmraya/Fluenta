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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.FileUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLOutputter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.xml.sax.SAXException;

public class XmlPreferences extends Composite {

	private static Document catalogDoc;
	protected Table catalogTable;
	private List<Element> holder;
	private int count;
	protected Table filesTable;

	public XmlPreferences(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Group configurationFiles = new Group(this, SWT.NONE);
		configurationFiles.setText(Messages.getString("XmlPreferences.0")); 
		configurationFiles.setLayout(new GridLayout());
		configurationFiles.setLayoutData(new GridData(GridData.FILL_BOTH));

		filesTable = new Table(configurationFiles,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.READ_ONLY | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = filesTable.getItemHeight() * 10;
		filesTable.setLayoutData(data);
		filesTable.setLinesVisible(true);
		filesTable.setHeaderVisible(false);

		TableColumn filesColumn = new TableColumn(filesTable, SWT.NONE);
		filesColumn.setText(Messages.getString("XmlPreferences.1")); 
		filesColumn.setWidth(250);
		try {
			fillFilesTable();
		} catch (IOException ioe) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(ioe.getMessage());
			box.open();
			return;
		}

		filesTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				try {
					editConfigurationFile();
				} catch (IOException ioe) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(ioe.getMessage());
					box.open();
				}
			}
		});

		//
		// Buttons
		//

		Composite bottom = new Composite(configurationFiles, SWT.NONE);
		bottom.setLayout(new GridLayout(4, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); 
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button add = new Button(bottom, SWT.PUSH);
		add.setText(Messages.getString("XmlPreferences.3")); 
		add.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				GetRootElementDialog dtd = new GetRootElementDialog(getShell());
				dtd.show();
				if (dtd.wasCancelled()) {
					return;
				}
				String newFile = dtd.getRootElement();
				if (newFile != null) {
					if (newFile.indexOf(".") > 0) { 
						// remove extension, if any
						newFile = newFile.substring(0, newFile.indexOf(".")); 
					}
					try {
						newFile = new File(Fluenta.getFiltersFolder(), "config_" + newFile + ".xml").getAbsolutePath();  
						File tmp = new File(newFile);
						if (tmp.exists()) {
							MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
							MessageFormat mf = new MessageFormat(Messages.getString("XmlPreferences.8")); 
							Object[] args = { newFile };
							box.setMessage(mf.format(args));
							if (box.open() == SWT.NO) {
								return;
							}
						}
						Document doc = new Document(null, "ini-file", 
								"-//Maxprograms//Converters 2.0.0//EN", 
								"configuration.dtd"); 
						XMLOutputter outputter = new XMLOutputter();
						try (FileOutputStream output = new FileOutputStream(newFile)) {
							outputter.output(doc, output);
						}
						fillFilesTable();
						DTDConfigurationDialog config = new DTDConfigurationDialog(getShell(), newFile);
						config.show();
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
						box.setMessage(e.getMessage());
						box.open();
					}
				}
			}
		});

		Button edit = new Button(bottom, SWT.PUSH);
		edit.setText(Messages.getString("XmlPreferences.12")); 
		edit.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					editConfigurationFile();
				} catch (IOException ioe) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(ioe.getMessage());
					box.open();
				}

			}
		});

		Button remove = new Button(bottom, SWT.PUSH);
		remove.setText(Messages.getString("XmlPreferences.13")); 
		remove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] selection = filesTable.getSelection();
				if (selection.length == 0) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("XmlPreferences.14")); 
					box.open();
					return;
				}
				try {
					String name = new File(Fluenta.getFiltersFolder(), selection[0].getText()).getAbsolutePath();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					MessageFormat mf = new MessageFormat(Messages.getString("XmlPreferences.15")); 
					Object[] args = { name };
					box.setMessage(mf.format(args));
					if (box.open() == SWT.YES) {
						Files.delete(new File(name).toPath());
						fillFilesTable();
					}
				} catch (IOException ioe) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(ioe.getMessage());
					box.open();
				}
			}
		});

		configurationFiles.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent arg0) {
				filesColumn.setWidth(filesTable.getClientArea().width);
			}
		});

		// XML CATALOG

		Group catalogGroup = new Group(this, SWT.NONE);
		catalogGroup.setText(Messages.getString("XmlPreferences.16")); 
		catalogGroup.setLayout(new GridLayout());
		catalogGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		try {
			loadCatalogue(Fluenta.getCatalogFile());
		} catch (SAXException | IOException | ParserConfigurationException e1) {
			e1.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("XmlPreferences.17")); 
			box.open();
			getShell().close();
		}

		catalogTable = new Table(catalogGroup,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.heightHint = 10 * catalogTable.getItemHeight();
		tableData.widthHint = 650;
		catalogTable.setLayoutData(tableData);
		catalogTable.setLinesVisible(true);
		catalogTable.setHeaderVisible(true);

		final TableColumn dtdFile = new TableColumn(catalogTable, SWT.NONE);
		dtdFile.setText(Messages.getString("XmlPreferences.20")); 

		fillCatalogTable();

		//
		// Buttons
		//

		Composite catalogBottom = new Composite(catalogGroup, SWT.NONE);
		catalogBottom.setLayout(new GridLayout(4, false));
		catalogBottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label catalogFiller = new Label(catalogBottom, SWT.NONE);
		catalogFiller.setText(""); 
		catalogFiller.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button addButton = new Button(catalogBottom, SWT.PUSH);
		addButton.setText(Messages.getString("XmlPreferences.22")); 
		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addCatalogue();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Button removeButton = new Button(catalogBottom, SWT.PUSH);
		removeButton.setText(Messages.getString("XmlPreferences.24")); 
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				deleteCatalogEntry();
			}
		});

		catalogGroup.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent arg0) {
				dtdFile.setWidth(catalogTable.getClientArea().width);
			}
		});

		// XML COMMENTS

		Button commentsButton = new Button(this, SWT.CHECK);
		commentsButton.setText(Messages.getString("XmlPreferences.25")); 
		try {
			commentsButton.setSelection(getTranslateComments());
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("XmlPreferences.26")); 
			box.open();
			getShell().close();
		}
		commentsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Preferences prefs;
				try {
					prefs = Preferences.getInstance();
					prefs.save("XMLOptions", "TranslateComments", (commentsButton.getSelection() ? "Yes" : "No"));    
				} catch (IOException e) {
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("XmlPreferences.31")); 
					box.open();
					getShell().close();
				}

			}
		});
	}

	public static boolean getTranslateComments() throws IOException {
		Preferences prefs = Preferences.getInstance();
		return prefs.get("XMLOptions", "TranslateComments", "No").equalsIgnoreCase("Yes");    
	}

	private static void loadCatalogue(String catalogueFile)
			throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		catalogDoc = builder.build(catalogueFile);
	}

	private static void saveCatalog() throws IOException {
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		try (FileOutputStream output = new FileOutputStream(Fluenta.getCatalogFile())) {
			outputter.output(catalogDoc, output);
		}
	}

	private void fillCatalogTable() {
		catalogTable.removeAll();
		holder = null;
		holder = new Vector<>();
		count = 0;
		Element root = catalogDoc.getRootElement();
		getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
		recurseCatalog(root);
		getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
	}

	private void recurseCatalog(Element e) {
		List<Element> entries = e.getChildren();
		Iterator<Element> d = entries.iterator();
		while (d.hasNext()) {
			Element entry = d.next();
			String type = entry.getName();
			if (type.equals("nextCatalog")) { 
				String content = entry.getAttributeValue("catalog"); 
				TableItem item = new TableItem(catalogTable, SWT.NONE);
				item.setText(content);
				holder.add(count++, entry);
			}
			recurseCatalog(entry);
		}
	}

	protected void deleteCatalogEntry() {
		if (catalogTable.getSelectionIndices().length == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("XmlPreferences.68")); 
			box.open();
			return;
		}
		MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		box.setMessage(Messages.getString("XmlPreferences.69")); 
		if (box.open() == SWT.NO) {
			return;
		}
		int index = catalogTable.getSelectionIndices()[0];
		Element e = holder.get(index);
		remove(catalogDoc.getRootElement(), e);
		try {
			saveCatalog();
			fillCatalogTable();
		} catch (Exception e1) {
			e1.printStackTrace();
			MessageBox ebox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			ebox.setMessage(Messages.getString("XmlPreferences.70")); 
			ebox.open();
		}

	}

	private void remove(Element main, Element e) {
		try {
			main.removeChild(e);
		} catch (Exception e1) {
			List<Element> content = main.getChildren();
			Iterator<Element> i = content.iterator();
			while (i.hasNext()) {
				Element child = i.next();
				remove(child, e);
			}
		}
	}

	protected void editConfigurationFile() throws IOException {
		TableItem[] selection = filesTable.getSelection();
		if (selection == null || selection.length == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage(Messages.getString("XmlPreferences.77")); 
			box.open();
			return;
		}
		String name = new File(Fluenta.getFiltersFolder(), selection[0].getText()).getAbsolutePath();
		DTDConfigurationDialog config = new DTDConfigurationDialog(getShell(), name);
		config.show();
	}

	protected void addCatalogue() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] names = { Messages.getString("XmlPreferences.78"), Messages.getString("XmlPreferences.79") };  
		String[] extensions = { "*.xml", "*.*" };  
		fd.setFilterNames(names);
		fd.setFilterExtensions(extensions);
		String name = fd.open();
		if (name != null) {
			try {
				Element e = new Element("nextCatalog"); 
				File catalog = new File(Fluenta.getCatalogFile());
				e.setAttribute("catalog", FileUtils.getRelativePath(catalog.getAbsolutePath(), name)); 
				catalogDoc.getRootElement().addContent(e);
				catalogDoc.getRootElement().addContent("\n"); 
				saveCatalog();
				fillCatalogTable();
			} catch (Exception e1) {
				MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				box.setMessage(e1.getMessage());
				box.open();
			}
		}
	}

	protected void fillFilesTable() throws IOException {
		File[] array = Fluenta.getFiltersFolder().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("config_") && name.endsWith(".xml");  
			}
		});
		TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < array.length; i++) {
			set.add(array[i].getName());
		}
		filesTable.removeAll();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			TableItem item = new TableItem(filesTable, SWT.NONE);
			item.setText(it.next());
		}
	}
}
