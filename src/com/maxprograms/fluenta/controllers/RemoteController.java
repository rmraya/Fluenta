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
import java.util.Vector;

import com.maxprograms.tmengine.ILogger;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.fluenta.models.Project;
import com.maxprograms.languages.Language;
import com.maxprograms.tmengine.IDatabase;

public class RemoteController implements IController {

	public RemoteController(String user, String password, String server) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<Project> getProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createProject(Project p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector<Memory> getMemories() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateProject(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createMemory(Memory m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDatabase getTMEngine(long memoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateXliff(Project project, String xliffFolder, Vector<Language> tgtLangs, boolean useICE, boolean useTM, boolean generateCount, 
			String ditavalFile, boolean useXliff20, ILogger logger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importXliff(Project project, String xliffDocument, String targetFolder, boolean updateTM, boolean accptUnapproved, boolean ignoreTagErrors, boolean cleanAttributes, ILogger alogger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importTMX(Memory memory, String tmxFile, ILogger logger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMemory(Memory memory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Memory getMemory(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProject(Project project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMemory(long id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exportTMX(Memory memory, String file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Project getProject(long id) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
