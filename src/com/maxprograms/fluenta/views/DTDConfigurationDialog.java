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

import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.Locator;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLOutputter;

public class DTDConfigurationDialog extends Dialog {

    Shell shell;

    private Display display;

    Table table;

    private String config;
    private Document doc;

    public DTDConfigurationDialog(Shell parent, String configFile) {
        super(parent, SWT.NONE);

        config = configFile;
        shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.RESIZE);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		display = shell.getDisplay();
        shell.setText(Messages.getString("DTDConfigurationDialog.0"));  //$NON-NLS-1$
        shell.setLayout(new GridLayout());
        shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "DTDConfigurationDialog"); //$NON-NLS-1$
			}
		});
		
        Label fileName = new Label(shell, SWT.NONE);
        MessageFormat mf = new MessageFormat(Messages.getString("DTDConfigurationDialog.2")); //$NON-NLS-1$
        Object[] args = {configFile};
        fileName.setText(mf.format(args)); 

        table = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.READ_ONLY | SWT.FULL_SELECTION);
        GridData tableData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        tableData.heightHint = 150;
        table.setLayoutData(tableData);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(Messages.getString("DTDConfigurationDialog.3"));  //$NON-NLS-1$
        column1.setWidth(100);

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Messages.getString("DTDConfigurationDialog.4"));  //$NON-NLS-1$
        column2.setWidth(100);
        
        TableColumn column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Messages.getString("DTDConfigurationDialog.5"));  //$NON-NLS-1$
        column3.setWidth(100);
        
        TableColumn column4 = new TableColumn(table, SWT.NONE);
        column4.setText(Messages.getString("DTDConfigurationDialog.6"));  //$NON-NLS-1$
        column4.setWidth(200);
        TableColumn column5 = new TableColumn(table, SWT.NONE);
        column5.setText(Messages.getString("DTDConfigurationDialog.7"));  //$NON-NLS-1$
        column5.setWidth(100);
        
        fillTable();
        
        table.addMouseListener(new MouseListener() {
			
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
				edit();	
			}
		});

        // 
        // Buttons
        //

        Composite bottom = new Composite(shell, SWT.NONE);
        bottom.setLayout(new GridLayout(4, false));
        bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button add = new Button(bottom, SWT.PUSH);
        add.setText(Messages.getString("DTDConfigurationDialog.9"));  //$NON-NLS-1$
        add.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent arg0) {
                ElementConfigurationDialog eConfig = new ElementConfigurationDialog(shell);
                eConfig.setHard_break("yes");   //$NON-NLS-1$
                eConfig.show();
                if (!eConfig.wasCancelled()) {
                    if (!eConfig.getElement().equals("")){    //$NON-NLS-1$
                        TableItem item = new TableItem(table, SWT.NONE);
                        String[] array = new String[5];
                        array[0] = eConfig.getElement();
                        array[1] = eConfig.getHard_break();
                        array[2] = eConfig.getCtype();
                        array[3] = eConfig.getAttributes();
                        array[4] = eConfig.getKeep_format();
                        item.setText(array);
                        table.redraw();
                        saveTable();
                        fillTable();
                    }
                }
            }
        });

        Button edit = new Button(bottom, SWT.PUSH);
        edit.setText(Messages.getString("DTDConfigurationDialog.12"));  //$NON-NLS-1$
        edit.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent arg0) {
               edit();
            }
        });

        Button remove = new Button(bottom, SWT.PUSH);
        remove.setText(Messages.getString("DTDConfigurationDialog.13"));  //$NON-NLS-1$
        remove.addSelectionListener(new SelectionAdapter(){

            @Override
			public void widgetSelected(SelectionEvent arg0) {
                TableItem[] selection = table.getSelection();
                if ( selection == null || selection.length == 0) {
                    return;
                }
                int index = table.getSelectionIndex();
                MessageBox box = new MessageBox(shell,SWT.ICON_QUESTION|SWT.YES|SWT.NO);
                MessageFormat mf1 = new MessageFormat(Messages.getString("DTDConfigurationDialog.14"));  //$NON-NLS-1$
                Object[] args1 = {selection[0].getText(0)};
                box.setMessage(mf1.format(args1));    
                if ( box.open() == SWT.YES) {
                    table.remove(index);
                    table.redraw();
                    saveTable();
                    fillTable();
                }
            }
        });
        shell.pack();
        
    }

    protected void edit() {
    	TableItem[] selection = table.getSelection();
    	if ( selection == null || selection.length == 0) {
    		return;
    	}
    	int index = table.getSelectionIndex();
    	ElementConfigurationDialog eConfig = new ElementConfigurationDialog(shell);
    	eConfig.setElement(selection[0].getText(0));
    	eConfig.setHard_break(selection[0].getText(1));
    	eConfig.setCtype(selection[0].getText(2));
    	eConfig.setAttributes(selection[0].getText(3));
    	eConfig.setKeep_format(selection[0].getText(4));
    	eConfig.show();
    	if (!eConfig.wasCancelled()) {
    		table.remove(index);
    		TableItem item = new TableItem(table, SWT.NONE, index);
    		String[] array = new String[5];
    		array[0] = eConfig.getElement();
    		array[1] = eConfig.getHard_break();
    		array[2] = eConfig.getCtype();
    		array[3] = eConfig.getAttributes();
    		array[4] = eConfig.getKeep_format();
    		item.setText(array);
    		table.redraw();
    		saveTable();
    		fillTable();
    	}
	}

    void fillTable() {
        table.removeAll();
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
            doc = builder.build(config);
            Element root = doc.getRootElement();
            TreeSet<Element> tree = new TreeSet<>(new Comparator<Element>() {

				@Override
				public int compare(Element o1, Element o2) {
					return o1.getText().compareTo(o2.getText());
				}
            	
            });
            tree.addAll(root.getChildren());
            Iterator<Element> i = tree.iterator();
            while (i.hasNext()) {
                Element e = i.next();
                String type = e.getAttributeValue("hard-break", "no");   //$NON-NLS-1$//$NON-NLS-2$
                if ( type.equals("yes")) {  //$NON-NLS-1$
                	type = "segment";  //$NON-NLS-1$
                } else if (type.equals("no")) {  //$NON-NLS-1$
                	type = "inline";  //$NON-NLS-1$
                }
                String[] array = new String[5];
                array[0] = e.getText();
                array[1] = type;  
                array[2] = e.getAttributeValue("ctype", "");     //$NON-NLS-1$//$NON-NLS-2$
                array[3] = e.getAttributeValue("attributes", "");     //$NON-NLS-1$//$NON-NLS-2$
                array[4] = e.getAttributeValue("keep-format", "");     //$NON-NLS-1$//$NON-NLS-2$
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(array);
            }

        } catch (Exception e) {
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            box.setMessage(e.getMessage());
            box.open();
        }
    }

    void saveTable() {
        Element root = doc.getRootElement();
        List<XMLNode> content = new Vector<>();
        content.add(new TextNode("\n")); //$NON-NLS-1$
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++) {
            Element e = new Element("tag");   //$NON-NLS-1$
            e.setText(items[i].getText(0));
            e.setAttribute("hard-break", items[i].getText(1));   //$NON-NLS-1$
            String ctype = items[i].getText(2);
            if (!ctype.equals("")) {   //$NON-NLS-1$
                e.setAttribute("ctype", ctype);   //$NON-NLS-1$
            }
            String attributes = items[i].getText(3); 
            if (!attributes.equals("")) {   //$NON-NLS-1$
                e.setAttribute("attributes",attributes);   //$NON-NLS-1$
            }
            String keep = items[i].getText(4);
            if (!keep.equals("")) {   //$NON-NLS-1$
                e.setAttribute("keep-format", keep);   //$NON-NLS-1$
            }
            content.add(new TextNode("\n  ")); //$NON-NLS-1$
            content.add(e);
        }
        content.add(new TextNode("\n")); //$NON-NLS-1$
        root.setContent(content);
        XMLOutputter outputter = new XMLOutputter();
        outputter.preserveSpace(true);
        try {
        	FileOutputStream out = new FileOutputStream(config);
            outputter.output(doc, out);
            out.close();
        } catch (Exception e) {
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            box.setMessage(e.getMessage());
            box.open();
        }
    }

    public void show() {
    	Locator.setLocation(shell, "DTDConfigurationDialog"); //$NON-NLS-1$
    	shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

}
