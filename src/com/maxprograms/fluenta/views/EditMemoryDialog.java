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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.languages.Language;
import com.maxprograms.utils.LanguageUtils;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.TextUtils;

public class EditMemoryDialog  extends Dialog {

	protected Shell shell;
	private Display display;
	protected Text descText;
	protected Combo sourceLanguages;
	protected StyledText descriptionText;
	protected Memory memory;

	public EditMemoryDialog(Shell parent, int style) {
		super(parent, style);
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setText(Messages.getString("EditMemoryDialog.0")); //$NON-NLS-1$
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "EditMemoryDialog"); //$NON-NLS-1$
			}
		});
		display = shell.getDisplay();
		
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label descLabel = new Label(top, SWT.NONE);
		descLabel.setText(Messages.getString("AddMemoryDialog.2")); //$NON-NLS-1$
		
		descText = new Text(top, SWT.BORDER);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.widthHint = 250;
		descText.setLayoutData(textData);
		
		Label sourceLabel = new Label(top, SWT.NONE);
		sourceLabel.setText(Messages.getString("AddMemoryDialog.3")); //$NON-NLS-1$
		
		sourceLanguages = new Combo(top, SWT.READ_ONLY|SWT.DROP_DOWN);
		sourceLanguages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLanguages.setItems(LanguageUtils.getLanguageNames());
			sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(), LanguageUtils.getLanguageName(ProjectPreferences.getDefaultSource().getCode())));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
			box.setMessage(Messages.getString("AddMemoryDialog.4")); //$NON-NLS-1$
			box.open();
			shell.close();
		}		
		
		Group descriptionGroup = new Group(shell, SWT.NONE);
		descriptionGroup.setText(Messages.getString("AddMemoryDialog.5")); //$NON-NLS-1$
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		descriptionGroup.setLayout(groupLayout);
		descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		descriptionText = new StyledText(descriptionGroup, SWT.BORDER|SWT.WRAP|SWT.V_SCROLL);
		GridData descData = new GridData(GridData.FILL_HORIZONTAL);
		descData.heightHint = descriptionText.getLineHeight() * 5;
		descriptionText.setLayoutData(descData);
		
		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button save = new Button(bottom, SWT.PUSH);
		save.setText(Messages.getString("EditMemoryDialog.1")); //$NON-NLS-1$
		save.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (descText.getText() == null || descText.getText().equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("EditMemoryDialog.2")); //$NON-NLS-1$
					box.open();
					return;
				}
				if (sourceLanguages.getText() == null || sourceLanguages.getText().equals("") ) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("EditMemoryDialog.3")); //$NON-NLS-1$
					box.open();
					return;
				}
				Language srcLang;
				try {
					srcLang = LanguageUtils.getLanguage(sourceLanguages.getText());
				} catch (SAXException | IOException | ParserConfigurationException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("EditMemoryDialog.4")); //$NON-NLS-1$
					box.open();
					return;
				}
				memory.setName(descText.getText());
				memory.setSrcLanguage(srcLang);
				memory.setDescription(descriptionText.getText());
				MainView.getController().updateMemory(memory);
				MainView.getMemoriesView().loadMemories();
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});
		
		shell.pack();
	}

	public void setMemory(Memory memory) {
		descText.setText(memory.getName());
		descriptionText.setText(memory.getDescription());
		sourceLanguages.select(TextUtils.geIndex(sourceLanguages.getItems(), LanguageUtils.getLanguageName(memory.getSrcLanguage().getCode())));
		this.memory = memory;
	}

	public void show() {
		Locator.setLocation(shell, "EditMemoryDialog"); //$NON-NLS-1$
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
