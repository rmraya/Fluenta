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

 package com.maxprograms.fluenta.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.tmengine.ILogger;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.languages.Language;
import com.maxprograms.tmengine.IDatabase;

public interface IController {

	public void close();

	public Vector<Project> getProjects() throws IOException;
	public void createProject(Project p) throws IOException;
	public Project getProject(long id) throws IOException;
	public void updateProject(Project project);
	public void removeProject(Project project) throws IOException; 

	public void generateXliff(Project project, String xliffFolder, Vector<Language> tgtLangs, boolean useICE, boolean useTM, boolean generateCount, String ditavalFile, boolean useXliff20, ILogger logger);
	public void importXliff(Project project, String xliffDocument, String targetFolder, boolean updateTM, boolean acceptUnapproved, boolean ignoreTags, boolean cleanAttributes, ILogger alogger);

	public Vector<Memory> getMemories() throws IOException;
	public void createMemory(Memory m) throws IOException;
	public void updateMemory(Memory memory);
	public Memory getMemory(long id) throws IOException;
	public IDatabase getTMEngine(long memoryId) throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException;
	public void importTMX(Memory memory, String tmxFile, ILogger logger);
	public void removeMemory(long id) throws IOException;
	public void exportTMX(Memory memory, String file) throws Exception;
}
