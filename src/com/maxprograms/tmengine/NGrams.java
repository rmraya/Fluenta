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

package com.maxprograms.tmengine;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


public class NGrams {

	private static final int NGRAMSIZE = 3;
	public static final String SEPARATORS = " \r\n\f\t\u2028\u2029,.;\":<>¿?¡!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; //$NON-NLS-1$
	// allow hyphen in terms
	public static final String TERM_SEPARATORS = " \u00A0\r\n\f\t\u2028\u2029,.;\":<>¿?¡!()[]{}=+/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; //$NON-NLS-1$

    public static int[] getNGrams(String source, boolean quality) {
		String src = source.toLowerCase();
		// src = normalise(src);
		Vector<String> words = buildWordList(src);
		Hashtable<String, String> table = new Hashtable<>();		
		
		if (quality) {
			Iterator<String> it = words.iterator();
			while (it.hasNext()) {
				String word = it.next();
				char[] array = word.toCharArray();
				int length = word.length();
				int ngrams = length / NGRAMSIZE;
				if ( ngrams * NGRAMSIZE < length ) {
					ngrams++;
				}			
				for (int i = 0; i < ngrams; i++) {
					String gram = ""; //$NON-NLS-1$
					for (int j = 0; j < NGRAMSIZE; j++) {
						if ( i*NGRAMSIZE + j < length) {
							char c = array[i*NGRAMSIZE + j];
							gram = gram + c;
						}
					}
					table.put("" + gram.hashCode(), ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} else {
			int length = words.size();
			for (int i=0 ; i<length ; i++) {
				table.put("" + words.get(i).hashCode(), ""); //$NON-NLS-1$ //$NON-NLS-2$			    
			}
		}
		Enumeration<String> keys = table.keys();
		int[] result = new int[table.size()];
		int idx = 0;
		while (keys.hasMoreElements()) {			
			result[idx++] = Integer.parseInt(keys.nextElement());
		}
		return result;
	}

    private static Vector<String> buildWordList(String src) {
        Vector<String> result = new Vector<>();
        StringTokenizer tokenizer = new StringTokenizer(src,SEPARATORS);
        while (tokenizer.hasMoreElements()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }
    
}