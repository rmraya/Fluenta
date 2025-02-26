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

interface StatusEvent {
    date: string;
    build: number;
    language: string;
    type: string;
}

interface Project {

    id: number;
    title: string;
    description: string;
    map: string;
    languageStatus: any;
    srcLanguage: string;
    tgtLanguages: string[];
    history: StatusEvent[];
    creationDate: string;
    lastUpdate: string;
    memories: number[];
    status: string;
}