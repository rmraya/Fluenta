/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 * Maxprograms - initial API and implementation
 *******************************************************************************/

module fluenta {

    exports com.maxprograms.fluenta;
    exports com.maxprograms.fluenta.controllers;
    exports com.maxprograms.fluenta.models;

    opens com.maxprograms.fluenta.models to mapdb;

    requires java.base;
    requires transitive openxliff;
    requires transitive swordfish;
    requires transitive xmljava;
    requires java.sql;
    requires mapdb;
    requires jsoup;
    requires java.logging;
    requires org.xerial.sqlitejdbc;
    requires json;
}
