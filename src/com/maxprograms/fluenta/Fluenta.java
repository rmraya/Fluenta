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

package com.maxprograms.fluenta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.fluenta.views.resources.ResourceManager;
import com.maxprograms.utils.Preferences;

public class Fluenta {

	private static File lock;
	private static FileChannel channel;
	private static FileLock flock;
	private static FileOutputStream lockStream;
	private static Display display;
	private static ResourceManager resourceManager;

	protected static Logger logger = System.getLogger(Fluenta.class.getName());

	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				CLI.main(args);
				return;
			}
			Display.setAppName("Fluenta");
			Display.setAppVersion(Constants.VERSION);
			display = Display.getDefault();
			resourceManager = new ResourceManager(display);
			checkLock();
			lock();
			MainView main = new MainView(display);
			main.show();
			if (!display.isDisposed()) {
				display.dispose();
			}
			unlock();
		} catch (Error e) {
			try {
				File log = new File(Preferences.getInstance().getPreferencesFolder().getParentFile(),
						"Fluenta_error.log");
				try (PrintStream stream = new PrintStream(log)) {
					e.printStackTrace(stream);
				}
				Program.launch(log.getAbsolutePath());
			} catch (IOException e2) {
				logger.log(Level.ERROR, "Error writing to log file", e2);
			}
		} catch (Exception e) {
			try {
				File log = new File(Preferences.getInstance().getPreferencesFolder().getParentFile(),
						"Fluenta_error.log");
				try (PrintStream stream = new PrintStream(log)) {
					e.printStackTrace(stream);
				}
				Program.launch(log.getAbsolutePath());
			} catch (Exception e2) {
				logger.log(Level.ERROR, "Error writing to log file", e2);
			}
		}
	}

	private static void lock() throws IOException {
		lock = new File(Preferences.getInstance().getPreferencesFolder(), "lock");
		lockStream = new FileOutputStream(lock);
		Date d = new Date(System.currentTimeMillis());
		lockStream.write(d.toString().getBytes(StandardCharsets.UTF_8));
		channel = lockStream.getChannel();
		flock = channel.lock();
	}

	private static void unlock() throws IOException {
		flock.release();
		channel.close();
		lockStream.close();
		Files.delete(Paths.get(lock.toURI()));
	}

	private static void checkLock() throws IOException {
		File old = new File(Preferences.getInstance().getPreferencesFolder(), "lock");
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) {
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						Shell shell = new Shell(display);
						shell.setImage(resourceManager.getIcon());
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
						box.setText("Fluenta");
						box.setMessage("An instance of this application is already running");
						box.open();
						display.dispose();
						System.exit(1);
					} else {
						newlock.release();
						newlock.close();
					}
				}
			}
			Files.delete(Paths.get(old.toURI()));
		}
	}

	public static ResourceManager getResourceManager() {
		return resourceManager;
	}
}
