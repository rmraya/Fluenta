/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
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
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.utils.Preferences;

public class CLI {

	private static File lock;
	private static FileOutputStream lockStream;
	private static FileChannel channel;
	private static FileLock flock;

	protected static Logger logger = System.getLogger(CLI.class.getName());

	public static void main(String[] args) {
		boolean generateXLIFF = false;
		String genXliffFile = null;
		boolean importXLIFF = false;
		String xliffFile = null;
		boolean importTmx = false;
		boolean exportTmx = false;
		String memId = null;
		String tmxFile = null;
		boolean verbose = false;

		if (args.length == 0) {
			logger.log(Level.WARNING, Messages.getString("CLI.0"));
			System.exit(3);
		}

		try {
			checkLock();
			lock();
		} catch (IOException e1) {
			logger.log(Level.ERROR, Messages.getString("CLI.1"), e1);
			System.exit(3);
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-version")) {
				JSONObject json = new JSONObject();
				json.put("version", Constants.VERSION);
				json.put("build", Constants.BUILD);
				System.out.println(json.toString(2));
				System.exit(0);
			}
			if (args[i].equals("-about")) {
				JSONObject json = new JSONObject();
				json.put("version", Constants.VERSION);
				json.put("build", Constants.BUILD);
				json.put("java", System.getProperty("java.version"));
				json.put("vendor", System.getProperty("java.vendor"));
				json.put("swordfish", com.maxprograms.swordfish.Constants.VERSION + "-"
						+ com.maxprograms.swordfish.Constants.BUILD);
				json.put("openxliff", com.maxprograms.converters.Constants.VERSION + "-"
						+ com.maxprograms.converters.Constants.BUILD);
				json.put("bcp47j", com.maxprograms.languages.Constants.VERSION + "-"
						+ com.maxprograms.languages.Constants.BUILD);
				json.put("xmljava", com.maxprograms.xml.Constants.VERSION + "-" + com.maxprograms.xml.Constants.BUILD);
				System.out.println(json.toString(2));
				System.exit(0);
			}
			if (args[i].equals("-generateXLIFF") && (i + 1) < args.length) {
				generateXLIFF = true;
				genXliffFile = args[i + 1];
			}
			if (args[i].equals("-importXLIFF") && (i + 1) < args.length) {
				importXLIFF = true;
				xliffFile = args[i + 1];
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
			if (args[i].equals("-verbose")) {
				verbose = true;
			}
		}
		if (generateXLIFF) {
			try {
				API.generateXLIFF(genXliffFile, verbose);
			} catch (IOException | SAXException | ParserConfigurationException | URISyntaxException | SQLException
					| JSONException | ParseException ioe) {
				logger.log(Level.ERROR, Messages.getString("CLI.4"), ioe);
				System.exit(3);
			}
		}
		if (importXLIFF) {
			try {
				API.importXLIFF(xliffFile, verbose);
			} catch (IOException | NumberFormatException | SAXException | ParserConfigurationException | SQLException
					| URISyntaxException | JSONException | ParseException ioe) {
				logger.log(Level.ERROR, Messages.getString("CLI.5"), ioe);
				System.exit(3);
			}
		}
		if (importTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (NumberFormatException ex) {
				logger.log(Level.ERROR, Messages.getString("CLI.9"));
				System.exit(3);
			}
			if (tmxFile == null) {
				logger.log(Level.ERROR, Messages.getString("CLI.10"));
				System.exit(3);
			}
			File f = new File(tmxFile);
			if (!f.exists()) {
				logger.log(Level.ERROR, Messages.getString("CLI.11"));
				System.exit(3);
			}
			try {
				int imported = API.importMemory(id, tmxFile);
				MessageFormat mf = new MessageFormat(Messages.getString("CLI.15"));
				System.out.println(mf.format(new String[] { "" + imported }));
			} catch (IOException | JSONException | SQLException | SAXException | ParserConfigurationException
					| ParseException | URISyntaxException e) {
				logger.log(Level.ERROR, e.getMessage());
				System.exit(3);
			}
		}
		if (exportTmx) {
			long id = 0;
			try {
				id = Long.parseLong(memId);
			} catch (NumberFormatException ex) {
				logger.log(Level.ERROR, Messages.getString("CLI.12"));
				System.exit(3);
			}
			if (tmxFile == null) {
				logger.log(Level.ERROR, Messages.getString("CLI.13"));
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
			logger.log(Level.ERROR, Messages.getString("CLI.14"), e);
		}
	}

	private static void checkLock() throws IOException {
		File old = new File(Preferences.getInstance().getPreferencesFolder().getParentFile(), "lock");
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) {
				try (FileChannel oldchannel = file.getChannel()) {
					FileLock newlock = oldchannel.tryLock();
					if (newlock == null) {
						logger.log(Level.ERROR, Messages.getString("CLI.1"));
						System.exit(1);
					} else {
						newlock.release();
						newlock.close();
					}
				}
			}
			Files.delete(old.toPath());
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
		Files.delete(lock.toPath());
	}
}
