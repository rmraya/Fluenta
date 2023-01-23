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
import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.utils.FileUtils;

public class MemoriesManager {

    private File memoriesFile;
    private JSONObject memories;

    public MemoriesManager(File home) throws IOException, JSONException {
        if (!home.exists()) {
            Files.createDirectories(home.toPath());
        }
        memoriesFile = new File(home, "memories.json");
        if (!memoriesFile.exists()) {
            memories = new JSONObject();
            memories.put("memories", new JSONArray());
            saveMemories();
        }
        memories = FileUtils.readJSON(memoriesFile);
    }

    private synchronized void saveMemories() throws IOException {
        try (FileOutputStream out = new FileOutputStream(memoriesFile)) {
            out.write(memories.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    List<Memory> getMemories() throws JSONException, ParseException, IOException {
        List<Memory> result = new Vector<>();
        JSONArray array = memories.getJSONArray("memories");
        for (int i = 0; i < array.length(); i++) {
            result.add(new Memory(array.getJSONObject(i)));
        }
        return result;
    }

    public Memory getMemory(long id) throws JSONException, ParseException, IOException {
        JSONArray array = memories.getJSONArray("memories");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (id == json.getLong("id")) {
                return new Memory(json);
            }
        }
        throw new IOException("Memory does not exist");
    }

    public void update(Memory memory) throws IOException, JSONException, ParseException {
        remove(memory.getId());
        add(memory);
    }

    public void remove(long id) throws IOException {
        JSONArray array = memories.getJSONArray("memories");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (id == json.getLong("id")) {
                array.remove(i);
                saveMemories();
                return;
            }
        }
        throw new IOException("Memory does not exist");
    }

    public void add(Memory memory) throws IOException {
        JSONArray array = memories.getJSONArray("memories");
        array.put(memory.toJSON());
        saveMemories();
    }
}
