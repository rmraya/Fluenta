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

package com.maxprograms.fluenta.controllers;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class Messages {

    private static Properties props;

    private Messages() {
        // do not instantiate this class
    }

    public static String getString(String key) {
        try {
            if (props == null) {
                Locale locale = Locale.getDefault();
                String extension = "en".equals(locale.getLanguage()) ? ".properties"
                        : "_" + locale.getLanguage() + ".properties";
                if (Messages.class.getResource("controllers" + extension) == null) {
                    extension = ".properties";
                }
                try (InputStream is = Messages.class.getResourceAsStream("controllers" + extension)) {
                    try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        props = new Properties();
                        props.load(reader);
                    }
                }
            }
            return props.getProperty(key, '!' + key + '!');
        } catch (IOException | NullPointerException e) {
            return '!' + key + '!';
        }
    }
}