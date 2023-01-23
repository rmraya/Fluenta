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

 package com.maxprograms.widgets;

import org.eclipse.swt.widgets.Composite;

import com.maxprograms.converters.ILogger;

public abstract class LoggerComposite extends Composite implements ILogger {

    protected LoggerComposite(Composite parent, int style) {
        super(parent, style);
    }
}
