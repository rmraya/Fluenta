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
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
import org.json.JSONException;
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

	Logger logger = System.getLogger(ImportXliffDialog.class.getName());

	private MainView mainView;	
	protected Shell shell;
	protected Shell parentShell;
	private Display display;
	protected Text folderText;
	protected Text xliffText;
	protected String xliffDocument;
	protected String targetFolder;
	private LoggerComposite loggerPanel;
	protected Button update;
	protected AsyncLogger aLogger;
	protected boolean cancelled;
	protected Listener closeListener;
	protected Thread thread;
	protected Button unapproved;
	protected Button ignoreTagErrors;
	protected Button cleanAttributes;
	private long projectId;

	public ImportXliffDialog(Shell parent, int style, Project project, MainView mainView) {
		super(parent, style);
		this.mainView = mainView;
		parentShell = parent;

		projectId = project.getId();

		aLogger = new AsyncLogger(this);

		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.setText("Import XLIFF");
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
		xliffLabel.setText("XLIFF File");

		xliffText = new Text(top, SWT.BORDER);
		GridData fill = new GridData(GridData.FILL_HORIZONTAL);
		fill.widthHint = 250;
		xliffText.setLayoutData(fill);

		Button browseXliff = new Button(top, SWT.PUSH);
		browseXliff.setText("Browse...");
		browseXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.xlf", "*.*" });
				fd.setFilterNames(new String[] { "XLIFF Files [*.xlf]", "All Files [*.*]" });
				String file = fd.open();
				if (file != null) {
					xliffText.setText(file);
				}
			}
		});

		Label folderLabel = new Label(top, SWT.NONE);
		folderLabel.setText("Output Folder");

		folderText = new Text(top, SWT.BORDER);
		folderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button browseFolder = new Button(top, SWT.PUSH);
		browseFolder.setText("Browse...");
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
		update.setText("Update Memory");

		unapproved = new Button(shell, SWT.CHECK);
		unapproved.setText("Accept Unapproved Translations");

		ignoreTagErrors = new Button(shell, SWT.CHECK);
		ignoreTagErrors.setText("Ignore Inline Tag Errors");

		cleanAttributes = new Button(shell, SWT.CHECK);
		cleanAttributes.setText("Clean Default DITA Attributes in Translated Files");

		loggerPanel = System.getProperty("file.separator").equals("\\") ? new LogPanel(shell, SWT.BORDER)
				: new LogTable(shell, SWT.NONE);

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button cancel = new Button(bottom, SWT.PUSH | SWT.CANCEL);
		cancel.setText("Cancel");
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
		importXliff.setText("Import XLIFF");
		importXliff.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (xliffText.getText() == null && xliffText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select XLIFF file");
					box.open();
					return;
				}
				if (folderText.getText() == null && folderText.getText().isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					box.setMessage("Select output folder");
					box.open();
					return;
				}
				File xliff = new File(xliffText.getText());
				if (!xliff.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage("Selected XLIFF file does not exist");
					box.open();
					return;
				}
				File output = new File(folderText.getText());
				if (!output.exists()) {
					output.mkdirs();
				}
				if (!output.exists()) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage("Selected output folder does not exist");
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
							mainView.getController().importXliff(project, xliffDocument, targetFolder, updateTM,
									acceptUnapproved, ignoreTags, clean, aLogger);
						} catch (NumberFormatException | ClassNotFoundException | IOException | SAXException
								| ParserConfigurationException | SQLException | URISyntaxException | JSONException
								| ParseException e) {
							logger.log(Level.ERROR, e);
							aLogger.displayError(e.getMessage());
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
			Preferences preferences = Preferences.getInstance();
			folderText.setText(preferences.get("ImportXliffDialog", "folderText." + projectId, ""));
			update.setSelection(preferences.get("ImportXliffDialog", "update", "yes").equalsIgnoreCase("yes"));
			unapproved.setSelection(preferences.get("ImportXliffDialog", "unapproved", "yes").equalsIgnoreCase("yes"));
			ignoreTagErrors
					.setSelection(
							preferences.get("ImportXliffDialog", "ignoreTagErrors", "no").equalsIgnoreCase("yes"));
			cleanAttributes
					.setSelection(
							preferences.get("ImportXliffDialog", "cleanAttributes", "yes").equalsIgnoreCase("yes"));
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
		}
	}

	protected void savePreferences() {
		try {
			Preferences preferences = Preferences.getInstance();
			preferences.save("ImportXliffDialog", "folderText." + projectId, folderText.getText());
			preferences.save("ImportXliffDialog", "update", update.getSelection() ? "yes" : "no");
			preferences.save("ImportXliffDialog", "unapproved", unapproved.getSelection() ? "yes" : "no");
			preferences.save("ImportXliffDialog", "ignoreTagErrors", ignoreTagErrors.getSelection() ? "yes" : "no");
			preferences.save("ImportXliffDialog", "cleanAttributes", cleanAttributes.getSelection() ? "yes" : "no");
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
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
		loggerPanel.log(message);
	}

	@Override
	public void setStage(String stage) {
		loggerPanel.setStage(stage);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void logError(String error) {
		loggerPanel.logError(error);
	}

	@Override
	public List<String> getErrors() {
		return loggerPanel.getErrors();
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
				mainView.getProjectsView().loadProjects();
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				box.setMessage(string);
				box.open();
				List<String> errors = aLogger.getErrors();
				if (errors != null) {
					try {
						HTMLViewer viewer = new HTMLViewer(parentShell);
						StringBuilder sb = new StringBuilder();
						if (!viewer.isLinux()) {
							sb.append("<pre>\n");
						}
						
						Iterator<String> it = errors.iterator();
						while (it.hasNext()) {
							sb.append(it.next() + "\n");
						}
						if (!viewer.isLinux()) {
							sb.append("</pre>\n");
						}
						
						viewer.setContent(sb.toString());
						viewer.show();
					} catch (SWTException e) {
						logger.log(Level.ERROR, e);
						MessageBox box2 = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						box2.setMessage("Error creating error log");
						box2.open();
					}
				}
				savePreferences();
				shell.close();
			}
		});
	}

	public void displayReport(String title, String report) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				box.setMessage(title);
				box.open();
				Program.launch(report);
				shell.close();
			}
		});
	}

}
