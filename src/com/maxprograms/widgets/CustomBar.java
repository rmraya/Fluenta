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

package com.maxprograms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CustomBar extends Composite {

	private GridLayout barLayout;
	private int count;
	private Image imageSeparator;

	Color defaultForeground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	Color defaultBackground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	Color separatorForeground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

	public CustomBar(Composite parent, int style) {
		super(parent, style);
		barLayout = new GridLayout();
		barLayout.marginWidth = 1;
		barLayout.marginHeight = 0;
		barLayout.horizontalSpacing = 1;
		setLayout(barLayout);

		setForeground(defaultForeground);
		setBackground(defaultBackground);
	}

	public CustomItem addItem(int style) {
		count++;
		setItemCount(count);
		return new CustomItem(this, style);
	}

	public void addSeparator() {
		count++;
		Label separator = new Label(this, SWT.NONE);
		separator.setText("|"); //$NON-NLS-1$
		separator.setForeground(separatorForeground);
		separator.setBackground(defaultBackground);
		setItemCount(count);
	}

	public void addImageSeparator() {
		count++;
		Label separator = new Label(this, SWT.NONE);
		separator.setImage(getImageSeparator());
		separator.setBackground(defaultBackground);
		setItemCount(count);
	}

	private Image getImageSeparator() {
		if (imageSeparator == null) {
			imageSeparator = new Image(getDisplay(), CustomBar.class.getResourceAsStream("separator.png")); //$NON-NLS-1$
		}
		return imageSeparator;
	}

	public void addFiller() {
		count++;
		setItemCount(count);
		Label separator = new Label(this, SWT.NONE);
		separator.setBackground(defaultBackground);
		separator.setText(" "); //$NON-NLS-1$
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void setItemCount(int value) {
		count = value;
		barLayout.numColumns = count;
		layout();
	}

	public Composite addWidget() {
		count++;
		setItemCount(count);
		return this;
	}

	@Override
	public Color getForeground() {
		return defaultForeground;
	}

	@Override
	public Color getBackground() {
		return defaultBackground;
	}

}
