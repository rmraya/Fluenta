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
import java.io.RandomAccessFile;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import com.maxprograms.utils.Preferences;

import org.xml.sax.SAXException;

public class CLI {

	private static File lock;
	private static FileOutputStream lockStream;
	private static FileChannel channel;
	private static FileLock flock;

	protected static final Logger LOGGER = System.getLogger(CLI.class.getName());

	public static void main(String[] args) {
		boolean addProject = false;
		String addFile = null;
		boolean generateXLIFF = false;
		String genXliffFile = null;
		boolean importXLIFF = false;
		String xliffFile = null;
		boolean addMemory = false;
		String addMemFile = null;
		boolean importTmx = false;
		boolean exportTmx = false;
		String memId = null;
		String tmxFile = null;
		boolean getProjects = false;
		boolean getMemories = false;
		boolean verbose = false;
		boolean removeProject = false;
		String projectId = "";

		if (args.length == 0) {
			LOGGER.log(Level.WARNING, Messages.getString("CLI.0"));
			System.exit(3);
		}

		try {
			checkLock();
			lock();
		} catch (IOException e1) {
			LOGGER.log(Level.ERROR, "Error locking process", e1);
			System.exit(3);
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-add") && (i + 1) < args.length) {
				addProject = true;
				addFile = args[i + 1];
			}
			if (args[i].equals("-del") && (i + 1) < args.length) {
				removeProject = true;
				projectId = args[i + 1];
			}
			if (args[i].equals("-getProjects")) {
				getProjects = true;
			}
			if (args[i].equals("-generateXLIFF") && (i + 1) < args.length) {
				generateXLIFF = true;
				genXliffFile = args[i + 1];
			}
			if (args[i].equals("-importXLIFF") && (i + 1) < args.length) {
				importXLIFF = true;
				xliffFile = args[i + 1];
			}
			if (args[i].equals("-addMem") && (i + 1) < args.length) {
				addMemory = true;
				addMemFile = args[i + 1];
			}
			if (args[i].equals("-importTmx") && (i + 1) < args.length) {
				importTmx = true;
				memId = args[i + 1];
			}
			if (args[i].equals("-exportTmx") && (i + 1) < args.length) {
				exportTmx = true;
				memId = args[i + 1];
			}
			if (args[i].equals("-tmx") && (i + 1) < args.length) {
				tmxFile = args[i + 1];
			}
			if (args[i].equals("-getMemories")) {
				getMemories = true;
			}
			if (args[i].equals("-verbose")) {
				verbose = true;
			}
		}
		if (addProject) {
			try {
				API.addProject(addFile);
			} catch (IOException | ClassNotFoundException | SQLException | SAXException
					| ParserConfigurationException e) {
				LOGGER.log(Level.ERROR, "Error adding project", e);
				System.exit(3);
			}
		}
		if (removeProject) {
			try {
				long number = Long.parseLong(projectId);
				API.removeProject(number);
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Error removing project", e);
				System.exit(3);
			}
		}
		if (generateXLIFF) {
			try {
				API.generateXLIFF(genXliffFile, verbose);
			} catch (IOException | ClassNotFoundException | SAXException | ParserConfigurationException
					| URISyntaxException | SQLException ioe) {
				LOGGER.log(Level.ERROR, "Error generating XLIFF", ioe);
				System.exit(3);
			}
		}
		if (importXLIFF) {
			try {
				API.importXLIFF(xliffFile, verbose);
			} catch (IOException | NumberFormatException | ClassNotFoundException | SAXException
					| ParserConfigurationException | SQLException | URISyntaxException ioe) {
				LOGGER.log(Level.ERROR, "Error importing XLIFF", ioe);
				System.exit(3);
			}
		}
		if (getProjects) {
			try {
				System.out.println(API.getProjects());
			} catch (IOException e) {
				LOGGER.log(Level.ERROR, "Error getting projects", e);
				System.exit(3);
			}
		}
		if (addMemory) {
			try {
				API.addMemory(addMemFile);
			} catch (IOException e) {
				LOGGER.log(Level.ERROR, "Error adding memory", e);
				System.exit(3);
			}
		}
		if (getMemories) {
			try {
				System.out.println(API.getMemories());
			} catch (IOException e) {
				LOGGER.log(Level.ERROR, "Error getting memories", e);
				System.exit(3);
			}
		}
		if (importTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (Exception ex) {
				LOGGER.log(Level.ERROR, Messages.getString("CLI.13"));
				System.exit(3);
			}
			if (tmxFile == null) {
				LOGGER.log(Level.ERROR, Messages.getString("CLI.14"));
				System.exit(3);
			}
			File f = new File(tmxFile);
			if (!f.exists()) {
				LOGGER.log(Level.ERROR, Messages.getString("CLI.15"));
				System.exit(3);
			}
			try {
				API.importMemory(id, tmxFile, verbose);
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, e.getMessage());
				System.exit(3);
			}
		}
		if (exportTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (Exception ex) {
				LOGGER.log(Level.ERROR, Messages.getString("CLI.16"));
				System.exit(3);
			}
			if (tmxFile == null) {
				LOGGER.log(Level.ERROR, Messages.getString("CLI.17"));
				System.exit(3);
			}
			try {
				API.exportMemory(id, tmxFile);
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, e.getMessage());
				System.exit(3);
			}
		}
		try {
			unlock();
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Error unlocking process", e);
		}
	}

	private static void checkLock() throws IOException {
		File old = new File(Preferences.getPreferencesDir().getParentFile(), "lock");
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) {
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						LOGGER.log(Level.ERROR, Messages.getString("CLI.8"));
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
}
