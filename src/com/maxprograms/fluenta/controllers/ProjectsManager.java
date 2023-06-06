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
package com.maxprograms.fluenta.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.fluenta.models.Project;
import com.maxprograms.utils.FileUtils;

public class ProjectsManager {

    private File projectsFile;
    private List<Project> projects;

    public ProjectsManager(File home) throws IOException, JSONException, ParseException {
        if (!home.exists()) {
            Files.createDirectories(home.toPath());
        }
        projectsFile = new File(home, "projects.json");
        if (!projectsFile.exists()) {
            JSONObject json = new JSONObject();
            json.put("version", Project.VERSION);
            json.put("projects", new JSONArray());
            try (FileOutputStream out = new FileOutputStream(projectsFile)) {
                out.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        }
        projects = new Vector<>();
        JSONObject json = FileUtils.readJSON(projectsFile);
        if (!json.has("version") || json.getInt("version") != Project.VERSION) {
            json = upgradeProjects(json);
        }
        JSONArray array = json.getJSONArray("projects");
        for (int i = 0; i < array.length(); i++) {
            projects.add(new Project(array.getJSONObject(i)));
        }
        if (!json.has("version") || json.getInt("version") != Project.VERSION) {
            saveProjects();
        }
    }

    private JSONObject upgradeProjects(JSONObject json) throws JSONException, ParseException {
        JSONArray projectsArray = json.getJSONArray("projects");
        DateFormat df = DateFormat.getDateTimeInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        DateFormat ldf = DateFormat.getDateInstance(DateFormat.LONG);
        for (int i = 0; i < projectsArray.length(); i++) {
            JSONObject project = projectsArray.getJSONObject(i);
            Date creationDate = df.parse(project.getString("creationDate"));
            project.put("creationDate", sdf.format(creationDate));
            Date lastUpdate = df.parse(project.getString("lastUpdate"));
            project.put("lastUpdate", sdf.format(lastUpdate));
            JSONArray history = project.getJSONArray("history");
            for (int j = 0; j < history.length(); j++) {
                JSONObject entry = history.getJSONObject(j);
                Date date = ldf.parse(entry.getString("date"));
                entry.put("date", sdf.format(date));
            }
        }
        return json;
    }

    private synchronized void saveProjects() throws IOException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 0; i < projects.size(); i++) {
            array.put(projects.get(i).toJSON());
        }
        json.put("projects", array);
        json.put("version", Project.VERSION);
        try (FileOutputStream out = new FileOutputStream(projectsFile)) {
            out.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    List<Project> getProjects() throws JSONException {
        return projects;
    }

    public Project getProject(long id) throws JSONException, IOException {
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            if (id == project.getId()) {
                return project;
            }
        }
        throw new IOException("Project does not exist");
    }

    public void update(Project project) throws IOException, JSONException {
        remove(project.getId());
        add(project);
    }

    public void remove(long id) throws IOException {
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            if (id == project.getId()) {
                projects.remove(project);
                saveProjects();
                return;
            }
        }
        throw new IOException("Project does not exist");
    }

    public void add(Project project) throws IOException {
        projects.add(project);
        saveProjects();
    }
}
