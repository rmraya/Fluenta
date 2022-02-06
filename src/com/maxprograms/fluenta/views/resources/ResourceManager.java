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

package com.maxprograms.fluenta.views.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {
	private Display display;
	private Image splash;
	private Image icon;
	private Image background;
	private Image add;
	private Image edit;
	private Image info;
	private Image key;
	private Image left;
	private Image remove;
	private Image right;
	private Cursor arrowCursor;
	private Cursor waitCursor;

	public ResourceManager(Display display) {
		this.display = display;
	}
	
	public Image getSplash() {
		if (splash == null) {
			splash = new Image(display, ResourceManager.class.getResourceAsStream("splash.png")); //$NON-NLS-1$
		}
		return splash;
	}

	public Image getIcon() {
		if (icon == null) {
			icon = new Image(display, ResourceManager.class.getResourceAsStream("icon.png")); //$NON-NLS-1$
		}
		return icon;
	}

	public Image getBackground() {
		if (background == null) {
			background = new Image(display, ResourceManager.class.getResourceAsStream("background.png")); //$NON-NLS-1$
		}
		return background;
	}

	public Image getAdd() {
		if (add == null) {
			add = new Image(display, ResourceManager.class.getResourceAsStream("add.png")); //$NON-NLS-1$
		}
		return add;
	}

	public Image getEdit() {
		if (edit == null) {
			edit = new Image(display, ResourceManager.class.getResourceAsStream("edit.png")); //$NON-NLS-1$
		}
		return edit;
	}
	
	public Image getInfo() {
		if (info == null) {
			info = new Image(display, ResourceManager.class.getResourceAsStream("info.png")); //$NON-NLS-1$
		}
		return info;
	}

	public Image getKey() {
		if (key == null) {
			key = new Image(display, ResourceManager.class.getResourceAsStream("key.png")); //$NON-NLS-1$
		}
		return key;
	}

	public Image getLeft() {
		if (left == null) {
			left = new Image(display, ResourceManager.class.getResourceAsStream("left.png")); //$NON-NLS-1$
		}
		return left;
	}

	public Image getRemove() {
		if (remove == null) {
			remove = new Image(display, ResourceManager.class.getResourceAsStream("remove.png")); //$NON-NLS-1$
		}
		return remove;
	}

	public Image getRight() {
		if (right == null) {
			right = new Image(display, ResourceManager.class.getResourceAsStream("right.png")); //$NON-NLS-1$
		}
		return right;
	}

	public Cursor getArrowCursor() {
		if (arrowCursor == null) {
			arrowCursor = new Cursor(display, SWT.CURSOR_ARROW);
		}
		return arrowCursor;
	}

	public Cursor getWaitCursor() {
		if (waitCursor == null) {
			waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		}
		return waitCursor;
	}


}
