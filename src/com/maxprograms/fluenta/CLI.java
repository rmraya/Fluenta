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
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.maxprograms.utils.Preferences;

public class CLI {

	private static File lock;
	private static FileOutputStream lockStream;
	private static FileChannel channel;
	private static FileLock flock;

	protected static Logger logger = System.getLogger(CLI.class.getName());

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
			logger.log(Level.WARNING, "Parameters are missing");
			System.exit(3);
		}

		try {
			checkLock();
			lock();
		} catch (IOException e1) {
			logger.log(Level.ERROR, "Error locking process", e1);
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
				logger.log(Level.ERROR, "Error adding project", e);
				System.exit(3);
			}
		}
		if (removeProject) {
			try {
				long number = Long.parseLong(projectId);
				API.removeProject(number);
			} catch (IOException | JSONException | ParseException e) {
				logger.log(Level.ERROR, "Error removing project", e);
				System.exit(3);
			}
		}
		if (generateXLIFF) {
			try {
				API.generateXLIFF(genXliffFile, verbose);
			} catch (IOException | ClassNotFoundException | SAXException | ParserConfigurationException
					| URISyntaxException | SQLException | JSONException | ParseException ioe) {
				logger.log(Level.ERROR, "Error generating XLIFF", ioe);
				System.exit(3);
			}
		}
		if (importXLIFF) {
			try {
				API.importXLIFF(xliffFile, verbose);
			} catch (IOException | NumberFormatException | ClassNotFoundException | SAXException
					| ParserConfigurationException | SQLException | URISyntaxException | JSONException
					| ParseException ioe) {
				logger.log(Level.ERROR, "Error importing XLIFF", ioe);
				System.exit(3);
			}
		}
		if (getProjects) {
			try {
				System.out.println(API.getProjects());
			} catch (IOException | JSONException | ParseException e) {
				logger.log(Level.ERROR, "Error getting projects", e);
				System.exit(3);
			}
		}
		if (addMemory) {
			try {
				API.addMemory(addMemFile);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Error adding memory", e);
				System.exit(3);
			}
		}
		if (getMemories) {
			try {
				System.out.println(API.getMemories());
			} catch (IOException | JSONException | ParseException e) {
				logger.log(Level.ERROR, "Error getting memories", e);
				System.exit(3);
			}
		}
		if (importTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (NumberFormatException ex) {
				logger.log(Level.ERROR, "Invalid memory id");
				System.exit(3);
			}
			if (tmxFile == null) {
				logger.log(Level.ERROR, "Missing TMX file");
				System.exit(3);
			}
			File f = new File(tmxFile);
			if (!f.exists()) {
				logger.log(Level.ERROR, "TMX file does not exist");
				System.exit(3);
			}
			try {
				API.importMemory(id, tmxFile);
			} catch (IOException | ClassNotFoundException | JSONException | SQLException | SAXException
					| ParserConfigurationException | ParseException e) {
				logger.log(Level.ERROR, e.getMessage());
				System.exit(3);
			}
		}
		if (exportTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (NumberFormatException ex) {
				logger.log(Level.ERROR, "Invalid memory id");
				System.exit(3);
			}
			if (tmxFile == null) {
				logger.log(Level.ERROR, "Missing TMX file");
				System.exit(3);
			}
			try {
				API.exportMemory(id, tmxFile);
			} catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage());
				System.exit(3);
			}
		}
		try {
			unlock();
		} catch (IOException e) {
			logger.log(Level.ERROR, "Error unlocking process", e);
		}
	}

	private static void checkLock() throws IOException {
		File old = new File(Preferences.getInstance().getPreferencesFolder().getParentFile(), "lock");
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) {
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						logger.log(Level.ERROR, "Error locking process");
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
}
