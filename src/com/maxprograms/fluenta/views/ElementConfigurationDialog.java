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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;

public class ElementConfigurationDialog extends Dialog {

    Shell shell;

    private Display display;

    Text eText;
    String hardBreak;
    String element;
    Combo hCombo;
    Combo cCombo;
    Combo kCombo;
    boolean cancelled = true;
    protected String ctype;
    protected String keepFormat;
    Text aText;
    protected String attributes;

    public ElementConfigurationDialog(Shell parent) {
        super(parent, SWT.NONE);
        shell = new Shell(parent, SWT.DIALOG_TRIM);
        shell.setImage(Fluenta.getResourceManager().getIcon());
        display = shell.getDisplay();
        shell.setText("Element Configuration");
        shell.setLayout(new GridLayout());
        shell.addListener(SWT.Close, new Listener() {

            @Override
            public void handleEvent(Event arg0) {
                Locator.remember(shell, "ElementConfigurationDialog");
            }
        });

        Composite top = new Composite(shell, SWT.NONE);
        top.setLayout(new GridLayout(2, true));
        top.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL));

        Label eLabel = new Label(top, SWT.NONE);
        eLabel.setText("Element Name");

        eText = new Text(top, SWT.BORDER);
        eText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL));
        eText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                element = eText.getText();
            }
        });

        Label hLabel = new Label(top, SWT.NONE);
        hLabel.setText("Element Type");

        hCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] values = { "segment", "inline", "ignore" };
        hCombo.setItems(values);
        hCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL));
        hCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                hardBreak = hCombo.getText();
                if (hCombo.getText().equals("inline")) {
                    aText.setText("");
                } else {
                    cCombo.setText("");
                }
                if (hCombo.getText().equals("ignore")) {
                    aText.setText("");
                    kCombo.setText("");
                }
            }
        });

        Label cLabel = new Label(top, SWT.NONE);
        cLabel.setText("Inline Type");

        cCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] cValues = { "", "image", "pb", "lb", "x-bold", "x-entry", "x-font", "x-italic", "x-link",
                "x-underlined", "x-other" };
        cCombo.setItems(cValues);
        cCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL));
        cCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                ctype = cCombo.getText();
            }
        });

        Label aLabel = new Label(top, SWT.NONE);
        aLabel.setText("Translatable Attributes");

        aText = new Text(top, SWT.BORDER);
        aText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        aText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                attributes = aText.getText();
                if (aText.getText().trim().length() > 0) {
                    hCombo.setText("segment");
                    cCombo.setText("");
                }
            }
        });

        Label kLabel = new Label(top, SWT.NONE);
        kLabel.setText("Keep White Space");

        kCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] val = { "", "yes", "no" };
        kCombo.setItems(val);
        kCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL));
        kCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                keepFormat = kCombo.getText();
            }
        });

        Composite bottom = new Composite(shell, SWT.NONE);
        bottom.setLayout(new GridLayout(2, false));
        bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filler = new Label(bottom, SWT.NONE);
        filler.setText("");
        filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button save = new Button(bottom, SWT.PUSH);
        save.setText("Save Configuration");
        save.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                element = eText.getText();
                hardBreak = hCombo.getText();
                ctype = cCombo.getText();
                attributes = aText.getText();
                keepFormat = kCombo.getText();
                cancelled = false;
                shell.close();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // do nothing
            }
        });

        shell.pack();
    }

    public String getCtype() {
        return ctype;
    }

    public void setCtype(String ctype) {
        cCombo.setText(ctype);
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        eText.setText(element);
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        aText.setText(attributes);
    }

    public String getHardBreak() {
        return hardBreak;
    }

    public void setHardBreak(String value) {
        if (value.equals("yes")) {
            hCombo.setText("segment");
        } else if (value.equals("no")) {
            hCombo.setText("inline");
        } else {
            hCombo.setText(value);
        }
    }

    public String getKeep_format() {
        return keepFormat;
    }

    public void setKeepFormat(String value) {
        kCombo.setText(value);
    }

    public void show() {
        Locator.setLocation(shell, "ElementConfigurationDialog");
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public boolean wasCancelled() {
        return cancelled;
    }
}
