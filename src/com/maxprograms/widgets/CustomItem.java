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

package com.maxprograms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

public class CustomItem extends Composite {

	protected Label text;
	protected Label image;
	protected CustomItem instance;
	protected boolean dropDown;
	protected Label dropLabel;
	private Image normalImage;
	private Image disabledImage;
	protected boolean enabled;
	
	Color defaultForeground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	Color defaultBackground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	Color defaultTextForeground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	Color hoverForeground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	Color hoverBackground = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
	Color disabledText = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
	
	public CustomItem(CustomBar parent, int style) {
		super(parent, style);
		defaultForeground = parent.getForeground();
		defaultBackground = parent.getBackground();
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 2;
		layout.marginHeight = 3;
		layout.horizontalSpacing=1;
		setLayout(layout);
		
		if ((style & SWT.DROP_DOWN) > 0) {
			dropDown = true;
		}
		enabled = true;
		instance = this;
		
		setForeground(defaultForeground);
		setBackground(defaultBackground);
		
		image = new Label(this, SWT.NONE);
		image.setBackground(defaultBackground);
		
		text = new Label(this, SWT.PUSH);
		text.setForeground(defaultTextForeground);
		text.setBackground(defaultBackground);
		
		if ((style & SWT.DROP_DOWN) > 0) {
			dropDown = true;
			layout.numColumns = 3;
			dropLabel = new Label(this, SWT.NONE);
			dropLabel.setBackground(defaultBackground);
			dropLabel.setForeground(defaultForeground);
			dropLabel.setText("\u25BE"); 
		}
		
		MouseTrackListener mouseListener = new MouseTrackListener() {

			@Override
			public void mouseEnter(MouseEvent arg0) {
				instance.setBackground(hoverBackground);
				instance.setForeground(hoverForeground);
				image.setBackground(hoverBackground);
				image.setForeground(hoverForeground);
				if (enabled) {
					text.setForeground(hoverForeground);
				}
				text.setBackground(hoverBackground);
				if (dropDown) {
					dropLabel.setBackground(hoverBackground);
					dropLabel.setForeground(hoverForeground);
				}
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				
				setForeground(defaultForeground);
				setBackground(defaultBackground);
				
				image.setBackground(defaultBackground);
				text.setBackground(defaultBackground);
				image.setForeground(defaultTextForeground);
				if (enabled) {
					text.setForeground(defaultTextForeground);
				}
				if (dropDown) {
					dropLabel.setBackground(defaultBackground);
					dropLabel.setForeground(defaultTextForeground);
				}
			}

			@Override
			public void mouseHover(MouseEvent arg0) {
				image.setBackground(hoverBackground);
				text.setBackground(hoverBackground);
				if (dropDown) {
					dropLabel.setBackground(hoverBackground);
				}
			}
			
		};
		addMouseTrackListener(mouseListener);
		text.addMouseTrackListener(mouseListener);
		image.addMouseTrackListener(mouseListener);
		if (dropDown) {
			dropLabel.addMouseTrackListener(mouseListener);
		}
	}
	
	public void setText(String string) {
		text.setText(string);		
	}
	
	public void setImage(Image normalImage) {
		this.normalImage = normalImage;
		image.setImage(normalImage);
	}
	
	public void setDisabledImage(Image disabledImage) {
		this.disabledImage = disabledImage;
	}
	
	@Override
	public void addMouseListener(MouseListener listener) {
		image.addMouseListener(listener);
		text.addMouseListener(listener);
		if (dropDown) {
			dropLabel.addMouseListener(listener);
		}
	}
	
	@Override
	public void setMenu(final Menu menu) {
		addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				Rectangle rect = instance.getBounds();
				Point p = instance.getParent().toDisplay(rect.x, rect.y);
				menu.setLocation(p.x, p.y + rect.height + 6);
				menu.setVisible(true);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing				
			}
			
		});
	}
	
	@Override
	public void setToolTipText(String s) {
		if (image != null) {
			image.setToolTipText(s);
		}
		if (text != null) {
			text.setToolTipText(s);
		}
		if (dropDown) {
			dropLabel.setToolTipText(s);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.enabled = enabled;
		if (enabled) {
			if (normalImage != null) {
				image.setImage(normalImage);
			}
			text.setForeground(defaultTextForeground);
		} else {
			if (disabledImage != null) {
				image.setImage(disabledImage);
			}
			text.setForeground(disabledText);
		}
		layout(true);
	}
}
