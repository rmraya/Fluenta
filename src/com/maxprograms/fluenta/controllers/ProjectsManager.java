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
package com.maxprograms.fluenta.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.fluenta.models.Project;
import com.maxprograms.utils.FileUtils;

public class ProjectsManager {

    private File projectsFile;
    private JSONObject projects;

    public ProjectsManager(File home) throws IOException, JSONException {
        if (!home.exists()) {
            Files.createDirectories(home.toPath());
        }
        projectsFile = new File(home, "projects.json");
        if (!projectsFile.exists()) {
            projects = new JSONObject();
            projects.put("projects", new JSONArray());
            saveProjects();
        }
        projects = FileUtils.readJSON(projectsFile);
    }

    private synchronized void saveProjects() throws IOException {
        try (FileOutputStream out = new FileOutputStream(projectsFile)) {
            out.write(projects.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    List<Project> getProjects() throws JSONException, ParseException, IOException {
        List<Project> result = new Vector<>();
        JSONArray array = projects.getJSONArray("projects");
        for (int i = 0; i < array.length(); i++) {
            result.add(new Project(array.getJSONObject(i)));
        }
        return result;
    }

    public Project getProject(long id) throws JSONException, ParseException, IOException {
        JSONArray array = projects.getJSONArray("projects");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (id == json.getLong("id")) {
                return new Project(json);
            }
        }
        throw new IOException("Project does not exist");
    }

    public void update(Project project) throws IOException, JSONException {
        remove(project.getId());
        add(project);
    }

    public void remove(long id) throws IOException {
        JSONArray array = projects.getJSONArray("projects");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (id == json.getLong("id")) {
                array.remove(i);
                saveProjects();
                return;
            }
        }
        throw new IOException("Project does not exist");
    }

    public void add(Project project) throws IOException {
        JSONArray array = projects.getJSONArray("projects");
        array.put(project.toJSON());
        saveProjects();
    }
}
