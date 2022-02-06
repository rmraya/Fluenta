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
import java.util.StringTokenizer;
import java.util.TreeSet;
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
				home = new File(System.getProperty("user.dir")); //$NON-NLS-1$
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
				MessageFormat mf = new MessageFormat(Messages.getString("FileUtils.1")); //$NON-NLS-1$
				throw new IOException(mf.format(new Object[] { filename }));
			}
		}
		Vector<String> homelist;
		Vector<String> filelist;

		homelist = getPathList(home);
		filelist = getPathList(file);
		return matchPathLists(homelist, filelist);
	}

	private static Vector<String> getPathList(File file) throws IOException {
		Vector<String> list = new Vector<>();
		File r;
		r = file.getCanonicalFile();
		while (r != null) {
			list.add(r.getName());
			r = r.getParentFile();
		}
		return list;
	}

	private static String matchPathLists(Vector<String> r, Vector<String> f) {
		int i;
		int j;
		String s = ""; //$NON-NLS-1$
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
			s += ".." + File.separator; //$NON-NLS-1$
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

	public static String findTreeRoot(TreeSet<String> set) {
		String result = ""; //$NON-NLS-1$
		MTree<String> tree = filesTree(set);
		MTree.Node<String> root = tree.getRoot();
		while (root.size() == 1) {
			result = result + root.getData();
			root = root.getChild(0);
		}
		return result;
	}

	private static MTree<String> filesTree(TreeSet<String> files) {
		MTree<String> result = new MTree<String>(""); //$NON-NLS-1$
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String s = it.next();
			StringTokenizer st = new StringTokenizer(s, "/\\:", true); //$NON-NLS-1$
			MTree.Node<String> current = result.getRoot();
			while (st.hasMoreTokens()) {
				String name = st.nextToken();
				MTree.Node<String> level1 = current.getChild(name);
				if (level1 != null) {
					current = level1;
				} else {
					current.addChild(new MTree.Node<String>(name));
					current = current.getChild(name);
				}
			}
		}
		return result;
	}
}
