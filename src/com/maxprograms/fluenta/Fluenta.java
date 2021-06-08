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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;

import com.maxprograms.fluenta.views.resources.ResourceManager;
import com.maxprograms.utils.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Fluenta {

	private static File lock;
	private static FileChannel channel;
	private static FileLock flock;
	private static FileOutputStream lockStream;
	private static Display display;
	private static ResourceManager resourceManager;
	private static String lang = "en"; //$NON-NLS-1$

	protected static final Logger LOGGER = System.getLogger(Fluenta.class.getName());

	public static void main(String[] args) {

		try {
			checkConfigurations();
			System.setProperty("user.dir", Preferences.getPreferencesDir().getAbsolutePath());
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Error setting working dir", e);
		}

		if (args.length > 0) {
			CLI.main(args);
			return;
		}

		try {
			Preferences prefs = Preferences.getInstance(Constants.PREFERENCES);
			lang = prefs.get("Fluenta", "uiLanguage", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!lang.equals("")) { //$NON-NLS-1$
				Locale.setDefault(new Locale(lang));
			} else {
				Locale locale = Locale.getDefault();
				if (locale.getLanguage().startsWith(new Locale("de").getLanguage())) { //$NON-NLS-1$
					Locale.setDefault(new Locale("de")); //$NON-NLS-1$
					lang = "de"; //$NON-NLS-1$
				}
				if (locale.getLanguage().startsWith(new Locale("ja").getLanguage())) { //$NON-NLS-1$
					Locale.setDefault(new Locale("ja")); //$NON-NLS-1$
					lang = "ja"; //$NON-NLS-1$
				}
				if (locale.getLanguage().startsWith(new Locale("es").getLanguage())) { //$NON-NLS-1$
					Locale.setDefault(new Locale("es")); //$NON-NLS-1$
					lang = "es"; //$NON-NLS-1$
				}
			}
		} catch (IOException e1) {
			LOGGER.log(Level.WARNING, "Error setting locale", e1);
		}

		Display.setAppName(Messages.getString("Fluenta.0")); //$NON-NLS-1$
		Display.setAppVersion(Constants.VERSION);
		display = Display.getDefault();

		resourceManager = new ResourceManager(display);

		try {

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
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "Fluenta_error.log"); //$NON-NLS-1$
				try (PrintStream stream = new PrintStream(log)) {
					e.printStackTrace(stream);
				}
				Program.launch(log.getAbsolutePath());
			} catch (Exception e2) {
				LOGGER.log(Level.ERROR, "Error writing to log file", e2);
			}
		} catch (Exception e) {
			try {
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "Fluenta_error.log"); //$NON-NLS-1$
				try (PrintStream stream = new PrintStream(log)) {
					e.printStackTrace(stream);
				}
				Program.launch(log.getAbsolutePath());
			} catch (Exception e2) {
				LOGGER.log(Level.ERROR, "Error writing to log file", e2);
			}
		}
	}

	private static void lock() throws IOException {
		lock = new File(Preferences.getPreferencesDir(), "lock"); //$NON-NLS-1$
		lockStream = new FileOutputStream(lock);
		Date d = new Date(System.currentTimeMillis());
		lockStream.write(d.toString().getBytes("UTF-8")); //$NON-NLS-1$
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
		File old = new File(Preferences.getPreferencesDir(), "lock"); //$NON-NLS-1$
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) { //$NON-NLS-1$
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						Shell shell = new Shell(display);
						shell.setImage(resourceManager.getIcon());
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
						box.setText(Messages.getString("Fluenta.7")); //$NON-NLS-1$
						box.setMessage(Messages.getString("Fluenta.8")); //$NON-NLS-1$
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

	public static String getLanguage() {
		return lang;
	}

	public static String getCatalogFile() throws IOException {
		File preferencesFolder = Preferences.getPreferencesDir();
		File catalogFolder = new File(preferencesFolder, "catalog"); //$NON-NLS-1$
		if (!catalogFolder.exists()) {
			copyFolder(new File("catalog"), catalogFolder); //$NON-NLS-1$
		}
		File catalog = new File(catalogFolder, "catalog.xml"); //$NON-NLS-1$
		return catalog.getAbsolutePath();
	}

	public static void checkConfigurations() throws IOException {
		File preferencesFolder = Preferences.getPreferencesDir();
		File catalogFolder = new File(preferencesFolder, "catalog"); //$NON-NLS-1$
		if (!catalogFolder.exists()) {
			copyFolder(new File("catalog"), catalogFolder); //$NON-NLS-1$
		}
		File filtersFolder = new File(preferencesFolder, "xmlfilter"); //$NON-NLS-1$
		if (!filtersFolder.exists()) {
			copyFolder(new File("xmlfilter"), filtersFolder); //$NON-NLS-1$
		}
		File srxFolder = new File(preferencesFolder, "srx"); //$NON-NLS-1$
		if (!srxFolder.exists()) {
			copyFolder(new File("srx"), srxFolder); //$NON-NLS-1$
		}
		File docsFolder = new File(preferencesFolder, "docs"); //$NON-NLS-1$
		if (!docsFolder.exists()) {
			copyFolder(new File("docs"), docsFolder); //$NON-NLS-1$
		}
	}

	public static File getFiltersFolder() throws IOException {
		File filtersFolder = new File(Preferences.getPreferencesDir(), "xmlfilter"); //$NON-NLS-1$
		if (!filtersFolder.exists()) {
			copyFolder(new File("xmlfilter"), filtersFolder); //$NON-NLS-1$
		}
		return filtersFolder;
	}
	
	private static void copyFolder(File sourceFolder, File targetFolder) throws IOException {
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
		File[] list = sourceFolder.listFiles();
		for (int i = 0; i < list.length; i++) {
			File f = list[i];
			if (f.isDirectory()) {
				copyFolder(f, new File(targetFolder, f.getName()));
			} else {
				copyFile(f, new File(targetFolder, f.getName()));
			}
		}
	}

	private static void copyFile(File source, File target) throws IOException {
		if (!target.getParentFile().exists()) {
			target.getParentFile().mkdirs();
		}
		Files.copy(source.toPath(), target.toPath());
	}
}
