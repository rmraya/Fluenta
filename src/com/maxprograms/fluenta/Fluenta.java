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
		Locale.setDefault(new Locale("en")); 
		Display.setAppName(Messages.getString("Fluenta.0")); 
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
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "Fluenta_error.log"); 
				try (PrintStream stream = new PrintStream(log)) {
					e.printStackTrace(stream);
				}
				Program.launch(log.getAbsolutePath());
			} catch (Exception e2) {
				LOGGER.log(Level.ERROR, "Error writing to log file", e2); 
			}
		} catch (Exception e) {
			try {
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "Fluenta_error.log"); 
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
		lock = new File(Preferences.getPreferencesDir(), "lock"); 
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
		File old = new File(Preferences.getPreferencesDir(), "lock"); 
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) { 
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						Shell shell = new Shell(display);
						shell.setImage(resourceManager.getIcon());
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
						box.setText(Messages.getString("Fluenta.7")); 
						box.setMessage(Messages.getString("Fluenta.8")); 
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

	public static String getCatalogFile() throws IOException {
		File preferencesFolder = Preferences.getPreferencesDir();
		File catalogFolder = new File(preferencesFolder, "catalog"); 
		if (!catalogFolder.exists()) {
			copyFolder(new File("catalog"), catalogFolder); 
		}
		File catalog = new File(catalogFolder, "catalog.xml"); 
		return catalog.getAbsolutePath();
	}

	public static void checkConfigurations() throws IOException {
		File preferencesFolder = Preferences.getPreferencesDir();
		File catalogFolder = new File(preferencesFolder, "catalog"); 
		if (!catalogFolder.exists()) {
			copyFolder(new File("catalog"), catalogFolder); 
		}
		File filtersFolder = new File(preferencesFolder, "xmlfilter"); 
		if (!filtersFolder.exists()) {
			copyFolder(new File("xmlfilter"), filtersFolder); 
		}
		File srxFolder = new File(preferencesFolder, "srx"); 
		if (!srxFolder.exists()) {
			copyFolder(new File("srx"), srxFolder); 
		}
		File docsFolder = new File(preferencesFolder, "docs"); 
		if (!docsFolder.exists()) {
			copyFolder(new File("docs"), docsFolder); 
		}
	}

	public static File getFiltersFolder() throws IOException {
		File filtersFolder = new File(Preferences.getPreferencesDir(), "xmlfilter"); 
		if (!filtersFolder.exists()) {
			copyFolder(new File("xmlfilter"), filtersFolder); 
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
