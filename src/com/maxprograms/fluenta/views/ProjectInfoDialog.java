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
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.fluenta.models.ProjectEvent;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Locator;

public class ProjectInfoDialog extends Dialog {

	Logger logger = System.getLogger(ProjectInfoDialog.class.getName());

	protected Shell shell;
	private Display display;
	protected Table statusTable;

	public ProjectInfoDialog(Shell parent, int style, Project project, MainView mainView) {
		super(parent, style);

		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setText(Messages.getString("ProjectInfoDialog.12"));
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout(layout);
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "ProjectInfoDialog");
			}
		});
		display = shell.getDisplay();

		Label projectLabel = new Label(shell, SWT.NONE);	
		MessageFormat mf = new MessageFormat(Messages.getString("ProjectInfoDialog.13"));
		projectLabel.setText(mf.format(new String[] { project.getTitle() }));

		CTabFolder folder = new CTabFolder(shell, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem statusItem = new CTabItem(folder, SWT.NONE);
		statusItem.setText(Messages.getString("ProjectInfoDialog.0"));

		Composite statusComposite = new Composite(folder, SWT.NONE);
		statusComposite.setLayout(new GridLayout());
		statusComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		statusItem.setControl(statusComposite);

		statusTable = new Table(statusComposite,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.CHECK | SWT.BORDER);
		statusTable.setHeaderVisible(true);
		statusTable.setLinesVisible(true);
		statusTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn language = new TableColumn(statusTable, SWT.NONE);
		language.setText(Messages.getString("ProjectInfoDialog.1"));
		language.setWidth(200);

		TableColumn statusColumn = new TableColumn(statusTable, SWT.CENTER);
		statusColumn.setText(Messages.getString("ProjectInfoDialog.2"));
		statusColumn.setWidth(120);

		try {
			populateStatusTable(project);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectInfoDialog.3"));
			box.open();
			shell.close();
		}

		Composite bottom = new Composite(statusComposite, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button translated = new Button(bottom, SWT.PUSH);
		translated.setText(Messages.getString("ProjectInfoDialog.4"));
		translated.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				for (int i = 0; i < statusTable.getItemCount(); i++) {
					if (statusTable.getItem(i).getChecked()) {
						TableItem item = statusTable.getItem(i);
						project.setLanguageStatus(((Language) item.getData("language")).getCode(), Project.COMPLETED);
					}
				}
				try {
					mainView.getController().updateProject(project);
					populateStatusTable(project);
				} catch (IOException | JSONException | ParseException | SAXException | ParserConfigurationException e) {
					logger.log(Level.ERROR, e);
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectInfoDialog.5"));
					box.open();
					shell.close();
				}

			}
		});

		CTabItem historyItem = new CTabItem(folder, SWT.NONE);
		historyItem.setText(Messages.getString("ProjectInfoDialog.6"));

		Composite historyComposite = new Composite(folder, SWT.NONE);
		historyComposite.setLayout(new GridLayout());
		historyComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		historyItem.setControl(historyComposite);

		Table eventsTable = new Table(historyComposite,
				SWT.V_SCROLL | SWT.SINGLE | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		eventsTable.setLinesVisible(true);
		eventsTable.setHeaderVisible(true);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.heightHint = eventsTable.getItemHeight() * 10;
		eventsTable.setLayoutData(tableData);

		TableColumn date = new TableColumn(eventsTable, SWT.CENTER);
		date.setText(Messages.getString("ProjectInfoDialog.7"));
		date.setWidth(150);
		date.setResizable(false);

		TableColumn eventBuild = new TableColumn(eventsTable, SWT.CENTER);
		eventBuild.setText(Messages.getString("ProjectInfoDialog.8"));
		eventBuild.setWidth(50);

		TableColumn eventLanguage = new TableColumn(eventsTable, SWT.NONE);
		eventLanguage.setText(Messages.getString("ProjectInfoDialog.9"));
		eventLanguage.setWidth(200);

		TableColumn events = new TableColumn(eventsTable, SWT.CENTER);
		events.setText(Messages.getString("ProjectInfoDialog.10"));
		events.setWidth(130);

		try {
			List<ProjectEvent> history = project.getHistory();
			Iterator<ProjectEvent> it = history.iterator();
			while (it.hasNext()) {
				ProjectEvent event = it.next();
				TableItem item = new TableItem(eventsTable, SWT.NONE);
				item.setText(new String[] { event.getDateString(), "" + event.getBuild(),
						LanguageUtils.getLanguage(event.getLanguage()).getDescription(),
						ProjectEvent.getDescription(event.getType()) });
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			logger.log(Level.ERROR, e);
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectInfoDialog.11"));
			box.open();
			shell.close();
		}

		folder.setSelection(statusItem);
		statusTable.setFocus();
		shell.pack();
	}

	protected void populateStatusTable(Project project) throws IOException, SAXException, ParserConfigurationException {
		statusTable.removeAll();
		List<String> tgtLangs = project.getLanguages();
		Iterator<String> tl = tgtLangs.iterator();
		while (tl.hasNext()) {
			Language lang = LanguageUtils.getLanguage(tl.next());
			TableItem item = new TableItem(statusTable, SWT.NONE);
			item.setText(new String[] { LanguageUtils.getLanguage(lang.getCode()).getDescription(),
					project.getTargetStatus(lang.getCode()) });
			item.setData("language", lang);
		}
	}

	public void show() {
		Locator.setLocation(shell, "ProjectInfoDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
