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

package com.maxprograms.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONException;
import org.json.JSONObject;

public class FileUtils {

	private FileUtils() {
		// do not instantiate this class
	}

	public static synchronized JSONObject readJSON(File file) throws IOException, JSONException {
		StringBuilder sb = new StringBuilder();
		try (FileReader input = new FileReader(file, StandardCharsets.UTF_8)) {
			try (BufferedReader reader = new BufferedReader(input)) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!sb.isEmpty()) {
						sb.append('\n');
					}
					sb.append(line);
				}
			}
		}
		return new JSONObject(sb.toString());
	}

	public static void copyFolder(File sourceFolder, File targetFolder) throws IOException {
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
