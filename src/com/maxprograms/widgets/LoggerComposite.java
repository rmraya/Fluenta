package com.maxprograms.widgets;

import org.eclipse.swt.widgets.Composite;

import com.maxprograms.converters.ILogger;

public abstract class LoggerComposite extends Composite implements ILogger {

    public LoggerComposite(Composite parent, int style) {
        super(parent, style);
    }
}
