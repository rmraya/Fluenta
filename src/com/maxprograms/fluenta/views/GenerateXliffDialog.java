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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.converters.ILogger;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.Preferences;
import com.maxprograms.widgets.AsyncLogger;
import com.maxprograms.widgets.LogPanel;
import com.maxprograms.widgets.LogTable;
import com.maxprograms.widgets.LoggerComposite;

public class GenerateXliffDialog extends Dialog implements ILogger {

	protected Shell shell;
	protected Shell parentShell;
	private Display display;
	protected Text folderText;
	protected Button[] targets;
	protected Button generateCount;
	protected Button useTM;
	private LoggerComposite logger;
	protected boolean cancelled;
	protected AsyncLogger alogger;
	protected Listener closeListener;
	protected Thread thread;
	protected Button useICE;
	protected Text ditavalText;
	private long projectId;
	protected Button xliff20;

	public GenerateXliffDialog(Shell parent, int style, Project project) {
		super(parent, style);
		parentShell = parent;

		projectId = project.getId();

		alogger = new AsyncLogger(this);

		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.setText(Messages.getString("GenerateXliffDialog.0"));
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "GenerateXliffDialog");
				if (thread != null) {
					thread = null;
				}
				System.gc();
			}
		});
		display = shell.getDisplay();

		Label description = new Label(shell, SWT.NONE);
		description.setText(project.getTitle());

		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label xliffLabel = new Label(top, SWT.NONE);
		xliffLabel.setText(Messages.getString("GenerateXliffDialog.2"));

		folderText = new Text(top, SWT.BORDER);
		GridData xliffData = new GridData(GridData.FILL_HORIZONTAL);
		xliffData.widthHint = 250;
		folderText.setLayoutData(xliffData);
		if (project.getXliffFolder() != null) {
			folderText.setText(project.getXliffFolder());
		}

		Button xliffBrowse = new Button(top, SWT.PUSH);
		xliffBrowse.setText(Messages.getString("GenerateXliffDialog.3"));
		xliffBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				DirectoryDialog fd = new DirectoryDialog(shell, SWT.SAVE);
				if (folderText.getText() != null && !folderText.getText().isEmpty()) {
					File f = new File(folderText.getText());
					fd.setFilterPath(f.getAbsolutePath());
				}
				String folder = fd.open();
				if (folder != null) {
					folderText.setText(folder);
				}
			}
		});

		Label ditavalLabel = new Label(top, SWT.NONE);
		ditavalLabel.setText(Messages.getString("GenerateXliffDialog.5"));

		ditavalText = new Text(top, SWT.BORDER);
		ditavalText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button ditavalBrowse = new Button(top, SWT.PUSH);
		ditavalBrowse.setText(Messages.getString("GenerateXliffDialog.6"));
		ditavalBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				if (ditavalText.getText() != null && !ditavalText.getText().isEmpty()) {
					File f = new File(folderText.getText());
					fd.setFilterPath(f.getParent());
					fd.setFileName(f.getName());
				}
				fd.setFilterNames(new String[] { Messages.getString("GenerateXliffDialog.8"),
						Messages.getString("GenerateXliffDialog.9") });
				fd.setFilterExtensions(new String[] { "*.ditaval", "*.*" });
				String file = fd.open();
				if (file != null) {
					ditavalText.setText(file);
				}
			}
		});

		Composite panels = new Composite(shell, SWT.NONE);
		panels.setLayout(new GridLayout(2, false));
		panels.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group optionsGroup = new Group(panels, SWT.NONE);
		optionsGroup.setText(Messages.getString("GenerateXliffDialog.12"));
		optionsGroup.setLayout(new GridLayout());
		GridData leftData = new GridData(GridData.FILL_VERTICAL);
		leftData.verticalAlignment = SWT.TOP;
		optionsGroup.setLayoutData(leftData);

		useICE = new Button(optionsGroup, SWT.CHECK);
		useICE.setText(Messages.getString("GenerateXliffDialog.13"));
		useICE.setSelection(true);

		useTM = new Button(optionsGroup, SWT.CHECK);
		useTM.setText(Messages.getString("GenerateXliffDialog.14"));
		useTM.setSelection(true);

		generateCount = new Button(optionsGroup, SWT.CHECK);
		generateCount.setText(Messages.getString("GenerateXliffDialog.15"));
		generateCount.setSelection(true);

		xliff20 = new Button(optionsGroup, SWT.CHECK);
		xliff20.setText(Messages.getString("GenerateXliffDialog.1"));
		xliff20.setSelection(false);

		Group targetGroup = new Group(panels, SWT.NONE);
		targetGroup.setText(Messages.getString("GenerateXliffDialog.16"));
		targetGroup.setLayout(new GridLayout());
		GridData targetData = new GridData(GridData.FILL_BOTH);
		targetData.verticalAlignment = SWT.TOP;
		targetGroup.setLayoutData(targetData);

		try {
			List<Language> languages = project.getLanguages();
			targets = new Button[languages.size()];
			for (int i = 0; i < languages.size(); i++) {
				targets[i] = new Button(targetGroup, SWT.CHECK);
				targets[i].setSelection(true);
				targets[i].setText(LanguageUtils.getLanguage(languages.get(i).getCode()).getDescription());
				targets[i].setData("language", languages.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("GenerateXliffDialog.4"));
			box.open();
			shell.close();
		}

		logger = System.getProperty("file.separator").equals("\\") ? new LogPanel(shell, SWT.BORDER)
				: new LogTable(shell, SWT.NONE);

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button cancel = new Button(bottom, SWT.PUSH | SWT.CANCEL);
		cancel.setText(Messages.getString("GenerateXliffDialog.19"));
		cancel.setEnabled(false);
		cancel.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cancelled = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				cancelled = true;
			}
		});

		Button generateXliff = new Button(bottom, SWT.PUSH);
		generateXliff.setText(Messages.getString("GenerateXliffDialog.20"));
		generateXliff.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (folderText.getText() == null || folderText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("GenerateXliffDialog.22"));
					box.open();
					return;
				}
				File f = new File(folderText.getText());
				if (!f.exists()) {
					f.mkdirs();
				}
				List<Language> tgtLangs = new Vector<>();
				for (int i = 0; i < targets.length; i++) {
					if (targets[i].getSelection()) {
						tgtLangs.add((Language) targets[i].getData("language"));
					}
				}
				if (tgtLangs.isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("GenerateXliffDialog.24"));
					box.open();
					return;
				}
				cancel.setEnabled(true);
				generateXliff.setEnabled(false);
				closeListener = new Listener() {

					@Override
					public void handleEvent(Event ev) {
						cancelled = true;
						ev.doit = false;
					}
				};
				shell.addListener(SWT.Close, closeListener);
				String xliffFolder = folderText.getText();
				String ditaval = ditavalText.getText();
				project.setXliffFolder(xliffFolder);
				boolean useice = useICE.getSelection();
				boolean usetm = useTM.getSelection();
				boolean count = generateCount.getSelection();
				boolean useXliff20 = xliff20.getSelection();
				thread = new Thread() {

					@Override
					public void run() {
						try {
							MainView.getController().generateXliff(project, xliffFolder, tgtLangs, useice, usetm, count,
									ditaval, useXliff20, alogger);
						} catch (Exception e) {
							alogger.displayError(e.getMessage());
							e.printStackTrace();
						}
					}

				};
				thread.start();
			}
		});
		shell.pack();
		loadPreferences();
		optionsGroup.layout(true);
		targetGroup.layout(true);
	}

	private void loadPreferences() {
		try {
			Preferences pref = Preferences.getInstance();
			folderText.setText(pref.get("GenerateXliffDialog", "folderText." + projectId, ""));
			ditavalText.setText(pref.get("GenerateXliffDialog", "ditavalText." + projectId, ""));
			useICE.setSelection(pref.get("GenerateXliffDialog", "useICE", "yes").equalsIgnoreCase("yes"));
			useTM.setSelection(pref.get("GenerateXliffDialog", "useTM", "yes").equalsIgnoreCase("yes"));
			generateCount.setSelection(pref.get("GenerateXliffDialog", "generateCount", "no").equalsIgnoreCase("yes"));
			xliff20.setSelection(pref.get("GenerateXliffDialog", "xliff20", "no").equalsIgnoreCase("yes"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void savePreferences() {
		try {
			Preferences pref = Preferences.getInstance();
			pref.save("GenerateXliffDialog", "folderText." + projectId, folderText.getText());
			pref.save("GenerateXliffDialog", "ditavalText." + projectId, ditavalText.getText());
			pref.save("GenerateXliffDialog", "useICE", useICE.getSelection() ? "yes" : "no");
			pref.save("GenerateXliffDialog", "useTM", useTM.getSelection() ? "yes" : "no");
			pref.save("GenerateXliffDialog", "generateCount", generateCount.getSelection() ? "yes" : "no");
			pref.save("GenerateXliffDialog", "xliff20", xliff20.getSelection() ? "yes" : "no");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void show() {
		Locator.setLocation(shell, "GenerateXliffDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	public void log(String message) {
		logger.log(message);
	}

	@Override
	public void setStage(String stage) {
		logger.setStage(stage);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void logError(String error) {
		logger.logError(error);
	}

	@Override
	public List<String> getErrors() {
		return logger.getErrors();
	}

	@Override
	public void displayError(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				if (string != null) {
					box.setMessage(string);
				} else {
					box.setMessage(Messages.getString("GenerateXliffDialog.26"));
				}
				box.open();
				shell.close();
			}
		});
	}

	@Override
	public void displaySuccess(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MainView.getProjectsView().loadProjects();
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				box.setMessage(string);
				box.open();
				List<String> errors = alogger.getErrors();
				if (errors != null) {
					try {
						HTMLViewer viewer = new HTMLViewer(parentShell);
						StringBuilder sb = new StringBuilder();
						sb.append("<pre>\n");
						Iterator<String> it = errors.iterator();
						while (it.hasNext()) {
							sb.append(it.next() + "\n");
						}
						sb.append("</pre>");
						viewer.setContent(sb.toString());
						viewer.show();
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox box2 = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						box2.setMessage(Messages.getString("GenerateXliffDialog.31"));
						box2.open();
					}
				}
				savePreferences();
				shell.close();
			}
		});
	}

}
