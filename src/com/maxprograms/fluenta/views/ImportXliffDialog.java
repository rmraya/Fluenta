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
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

import com.maxprograms.converters.ILogger;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.Preferences;
import com.maxprograms.widgets.AsyncLogger;
import com.maxprograms.widgets.LogPanel;
import com.maxprograms.widgets.LogTable;
import com.maxprograms.widgets.LoggerComposite;

public class ImportXliffDialog extends Dialog implements ILogger {

	protected Shell shell;
	protected Shell parentShell;
	private Display display;
	protected Text folderText;
	protected Text xliffText;
	protected String xliffDocument;
	protected String targetFolder;
	private LoggerComposite logger;
	protected Button update;
	protected AsyncLogger alogger;
	protected boolean cancelled;
	protected Listener closeListener;
	protected Thread thread;
	protected Button unapproved;
	protected Button ignoreTagErrors;
	protected Button cleanAttributes;
	private long projectId;

	public ImportXliffDialog(Shell parent, int style, Project project) {
		super(parent, style);
		parentShell = parent;

		projectId = project.getId();

		alogger = new AsyncLogger(this);

		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.setText(Messages.getString("ImportXliffDialog.0"));
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "ImportXliffDialog");
				if (thread != null) {
					thread = null;
				}
				System.gc();
			}
		});
		display = shell.getDisplay();

		Composite top = new Composite(shell, SWT.NONE);
		GridLayout topLayout = new GridLayout(3, false);
		topLayout.marginHeight = 0;
		top.setLayout(topLayout);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label xliffLabel = new Label(top, SWT.NONE);
		xliffLabel.setText(Messages.getString("ImportXliffDialog.2"));

		xliffText = new Text(top, SWT.BORDER);
		GridData fill = new GridData(GridData.FILL_HORIZONTAL);
		fill.widthHint = 250;
		xliffText.setLayoutData(fill);

		Button browseXliff = new Button(top, SWT.PUSH);
		browseXliff.setText(Messages.getString("ImportXliffDialog.3"));
		browseXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.xlf", "*.*" });
				fd.setFilterNames(new String[] { Messages.getString("ImportXliffDialog.6"),
						Messages.getString("ImportXliffDialog.7") });
				String file = fd.open();
				if (file != null) {
					xliffText.setText(file);
				}
			}
		});

		Label folderLabel = new Label(top, SWT.NONE);
		folderLabel.setText(Messages.getString("ImportXliffDialog.8"));

		folderText = new Text(top, SWT.BORDER);
		folderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button browseFolder = new Button(top, SWT.PUSH);
		browseFolder.setText(Messages.getString("ImportXliffDialog.9"));
		browseFolder.addSelectionListener(new SelectionAdapter() {
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

		update = new Button(shell, SWT.CHECK);
		update.setSelection(true);
		update.setText(Messages.getString("ImportXliffDialog.11"));

		unapproved = new Button(shell, SWT.CHECK);
		unapproved.setText(Messages.getString("ImportXliffDialog.1"));

		ignoreTagErrors = new Button(shell, SWT.CHECK);
		ignoreTagErrors.setText(Messages.getString("ImportXliffDialog.4"));

		cleanAttributes = new Button(shell, SWT.CHECK);
		cleanAttributes.setText(Messages.getString("ImportXliffDialog.5"));

		logger = System.getProperty("file.separator").equals("\\") ? new LogPanel(shell, SWT.BORDER)
				: new LogTable(shell, SWT.NONE);

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button cancel = new Button(bottom, SWT.PUSH | SWT.CANCEL);
		cancel.setText(Messages.getString("ImportXliffDialog.13"));
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

		Button importXliff = new Button(bottom, SWT.PUSH);
		importXliff.setText(Messages.getString("ImportXliffDialog.14"));
		importXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (xliffText.getText() == null && xliffText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ImportXliffDialog.16"));
					box.open();
					return;
				}
				if (folderText.getText() == null && folderText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ImportXliffDialog.18"));
					box.open();
					return;
				}
				File xliff = new File(xliffText.getText());
				if (!xliff.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ImportXliffDialog.19"));
					box.open();
					return;
				}
				File output = new File(folderText.getText());
				if (!output.exists()) {
					output.mkdirs();
				}
				if (!output.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ImportXliffDialog.20"));
					box.open();
					return;
				}

				xliffDocument = xliff.getAbsolutePath();
				targetFolder = output.getAbsolutePath();

				boolean updateTM = update.getSelection();
				boolean acceptUnapproved = unapproved.getSelection();
				boolean ignoreTags = ignoreTagErrors.getSelection();
				boolean clean = cleanAttributes.getSelection();

				cancel.setEnabled(true);
				importXliff.setEnabled(false);
				closeListener = new Listener() {

					@Override
					public void handleEvent(Event ev) {
						cancelled = true;
						ev.doit = false;
					}
				};
				shell.addListener(SWT.Close, closeListener);

				thread = new Thread() {

					@Override
					public void run() {
						try {
							MainView.getController().importXliff(project, xliffDocument, targetFolder, updateTM,
									acceptUnapproved, ignoreTags, clean, alogger);
						} catch (NumberFormatException | ClassNotFoundException | IOException | SAXException
								| ParserConfigurationException | SQLException | URISyntaxException e) {
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
	}

	private void loadPreferences() {
		try {
			Preferences pref = Preferences.getInstance();
			folderText.setText(pref.get("ImportXliffDialog", "folderText." + projectId, ""));
			update.setSelection(pref.get("ImportXliffDialog", "update", "yes").equalsIgnoreCase("yes"));
			unapproved.setSelection(pref.get("ImportXliffDialog", "unapproved", "yes").equalsIgnoreCase("yes"));
			ignoreTagErrors
					.setSelection(pref.get("ImportXliffDialog", "ignoreTagErrors", "no").equalsIgnoreCase("yes"));
			cleanAttributes
					.setSelection(pref.get("ImportXliffDialog", "cleanAttributes", "yes").equalsIgnoreCase("yes"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void savePreferences() {
		try {
			Preferences pref = Preferences.getInstance();
			pref.save("ImportXliffDialog", "folderText." + projectId, folderText.getText());
			pref.save("ImportXliffDialog", "update", update.getSelection() ? "yes" : "no");
			pref.save("ImportXliffDialog", "unapproved", unapproved.getSelection() ? "yes" : "no");
			pref.save("ImportXliffDialog", "ignoreTagErrors", ignoreTagErrors.getSelection() ? "yes" : "no");
			pref.save("ImportXliffDialog", "cleanAttributes", cleanAttributes.getSelection() ? "yes" : "no");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void show() {
		Locator.setLocation(shell, "ImportXliffDialog");
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
				box.setMessage(string);
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

	public void displayReport(String string, String report) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(string);
				box.open();
				Program.launch(report);
				shell.close();
			}
		});
	}

}
