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

package com.maxprograms.fluenta.views.resources;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {
	private Display display;
	private Image splash;
	private Image icon;
	private Image add;
	private Image edit;
	private Image info;
	private Image key;
	private Image left;
	private Image remove;
	private Image right;
	private Cursor arrowCursor;
	private Cursor waitCursor;
	private boolean isWindows;

	public ResourceManager(Display display) {
		this.display = display;
		isWindows = File.separatorChar == '\\';
	}

	public Image getSplash() {
		if (splash == null) {
			splash = new Image(display, ResourceManager.class.getResourceAsStream("splash.png"));
		}
		return splash;
	}

	public Image getIcon() {
		if (icon == null) {
			icon = new Image(display, ResourceManager.class.getResourceAsStream("icon.png"));
		}
		return icon;
	}

	public Image getAdd() {
		if (add == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				add = new Image(display, ResourceManager.class.getResourceAsStream("add_white.png"));
			} else {
				add = new Image(display, ResourceManager.class.getResourceAsStream("add.png"));
			}
		}
		return add;
	}

	public Image getEdit() {
		if (edit == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				edit = new Image(display, ResourceManager.class.getResourceAsStream("edit_white.png"));
			} else {
				edit = new Image(display, ResourceManager.class.getResourceAsStream("edit.png"));
			}
		}
		return edit;
	}

	public Image getInfo() {
		if (info == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				info = new Image(display, ResourceManager.class.getResourceAsStream("info_white.png"));
			} else {
				info = new Image(display, ResourceManager.class.getResourceAsStream("info.png"));
			}
		}
		return info;
	}

	public Image getKey() {
		if (key == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				key = new Image(display, ResourceManager.class.getResourceAsStream("key_white.png"));
			} else {
				key = new Image(display, ResourceManager.class.getResourceAsStream("key.png"));
			}
		}
		return key;
	}

	public Image getLeft() {
		if (left == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				left = new Image(display, ResourceManager.class.getResourceAsStream("left_white.png"));
			} else {
				left = new Image(display, ResourceManager.class.getResourceAsStream("left.png"));
			}
		}
		return left;
	}

	public Image getRemove() {
		if (remove == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				remove = new Image(display, ResourceManager.class.getResourceAsStream("remove_white.png"));
			} else {
				remove = new Image(display, ResourceManager.class.getResourceAsStream("remove.png"));
			}
		}
		return remove;
	}

	public Image getRight() {
		if (right == null) {
			if (Display.isSystemDarkTheme() && !isWindows) {
				right = new Image(display, ResourceManager.class.getResourceAsStream("right_white.png"));
			} else {
				right = new Image(display, ResourceManager.class.getResourceAsStream("right.png"));
			}
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
