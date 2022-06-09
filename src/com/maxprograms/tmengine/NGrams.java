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

package com.maxprograms.tmengine;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


public class NGrams {

	private static final int NGRAMSIZE = 3;
	public static final String SEPARATORS = " \r\n\f\t\u2028\u2029,.;\":<>¿?¡!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; 
	// allow hyphen in terms
	public static final String TERM_SEPARATORS = " \u00A0\r\n\f\t\u2028\u2029,.;\":<>¿?¡!()[]{}=+/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; 

    public static int[] getNGrams(String source, boolean quality) {
		String src = source.toLowerCase();
		// src = normalise(src);
		List<String> words = buildWordList(src);
		Map<String, String> table = new Hashtable<>();		
		
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
					String gram = ""; 
					for (int j = 0; j < NGRAMSIZE; j++) {
						if ( i*NGRAMSIZE + j < length) {
							char c = array[i*NGRAMSIZE + j];
							gram = gram + c;
						}
					}
					table.put("" + gram.hashCode(), "");  
				}
			}
		} else {
			int length = words.size();
			for (int i=0 ; i<length ; i++) {
				table.put("" + words.get(i).hashCode(), "");  			    
			}
		}
		Set<String> keySet = table.keySet();
		Iterator<String> kt = keySet.iterator();
		int[] result = new int[table.size()];
		int idx = 0;
		while (kt.hasNext()) {			
			result[idx++] = Integer.parseInt(kt.next());
		}
		return result;
	}

    private static List<String> buildWordList(String src) {
        List<String> result = new Vector<>();
        StringTokenizer tokenizer = new StringTokenizer(src,SEPARATORS);
        while (tokenizer.hasMoreElements()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }
    
}