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

package com.maxprograms.utils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Vector;

import com.maxprograms.converters.MTree;

public class FileUtils {

	public static String getAbsolutePath(String homeFile, String relative) throws IOException {
		File home = new File(homeFile);
		// If home is a file, get the parent
		File result;
		if (!home.isDirectory()) {
			home = home.getParentFile();
		}
		result = new File(home, relative);
		return result.getCanonicalPath();
	}

	public static String getRelativePath(String homeFile, String filename) throws IOException {
		File home = new File(homeFile);
		// If home is a file, get the parent
		if (!home.isDirectory()) {
			if (home.getParent() != null) {
				home = new File(home.getParent());
			} else {
				home = new File(System.getProperty("user.dir")); 
			}

		}
		File file = new File(filename);
		if (!file.isAbsolute()) {
			return filename;
		}
		// Check for relative path
		if (!home.isAbsolute()) {
			File f = new File(home.getAbsolutePath());
			home = f;
			if (!home.isAbsolute()) {
				MessageFormat mf = new MessageFormat(Messages.getString("FileUtils.1")); 
				throw new IOException(mf.format(new Object[] { filename }));
			}
		}
		List<String> homelist;
		List<String> filelist;

		homelist = getPathList(home);
		filelist = getPathList(file);
		return matchPathLists(homelist, filelist);
	}

	private static List<String> getPathList(File file) throws IOException {
		List<String> list = new Vector<>();
		File r;
		r = file.getCanonicalFile();
		while (r != null) {
			list.add(r.getName());
			r = r.getParentFile();
		}
		return list;
	}

	private static String matchPathLists(List<String> r, List<String> f) {
		int i;
		int j;
		String s = ""; 
		// start at the beginning of the lists
		// iterate while both lists are equal
		i = r.size() - 1;
		j = f.size() - 1;

		// first eliminate common root
		while (i >= 0 && j >= 0 && r.get(i).equals(f.get(j))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for (; i >= 0; i--) {
			s += ".." + File.separator; 
		}

		// for each level in the file path, add the path
		for (; j >= 1; j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		if (j >= 0 && j < f.size()) {
			s += f.get(j);
		}
		return s;
	}

	public static String findTreeRoot(SortedSet<String> set) {
		String result = ""; 
		MTree<String> tree = filesTree(set);
		MTree.Node<String> root = tree.getRoot();
		while (root.size() == 1) {
			result = result + root.getData();
			root = root.getChild(0);
		}
		return result;
	}

	private static MTree<String> filesTree(SortedSet<String> files) {
		MTree<String> result = new MTree<>(""); 
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String s = it.next();
			StringTokenizer st = new StringTokenizer(s, "/\\:", true); 
			MTree.Node<String> current = result.getRoot();
			while (st.hasMoreTokens()) {
				String name = st.nextToken();
				MTree.Node<String> level1 = current.getChild(name);
				if (level1 != null) {
					current = level1;
				} else {
					current.addChild(new MTree.Node<>(name));
					current = current.getChild(name);
				}
			}
		}
		return result;
	}
}
