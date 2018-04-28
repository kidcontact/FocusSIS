package com.slensky.focussis.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.slensky.focussis.data.Course;
import com.slensky.focussis.data.CourseAssignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by slensky on 4/12/18.
 */

public class CourseAssignmentFileHandler {
    private static final String TAG = "AssignmentFileHandler";
    private static final String filename = "assignments.txt";
    private static final String filenameTemp = "assignments.txt.tmp";
    private static final JsonParser parser = new JsonParser();

    public static Map<String, List<CourseAssignment>> getSavedAssignments(Context context) throws IOException  {
        initializeFile(context);
        StringBuilder sb = readFile(context);

        JsonObject json = parser.parse(sb.toString()).getAsJsonObject();
        Map<String, List<CourseAssignment>> assignmentMap = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            List<CourseAssignment> courseAssignments = GsonSingleton.getInstance().fromJson(json.getAsJsonArray(entry.getKey()), new TypeToken<List<CourseAssignment>>(){}.getType());
            assignmentMap.put(entry.getKey(), courseAssignments);
        }

        return assignmentMap;
    }

    public static List<CourseAssignment> getSavedAssignmentsById(Context context, String courseId) throws IOException {
        initializeFile(context);
        StringBuilder sb = readFile(context);

        JsonObject json = parser.parse(sb.toString()).getAsJsonObject();
        if (json.has(courseId)) {
            return GsonSingleton.getInstance().fromJson(json.getAsJsonArray(courseId), new TypeToken<List<CourseAssignment>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public static void addSavedAssignment(Context context, String courseId, CourseAssignment assignment) throws IOException {
        initializeFile(context);
        StringBuilder sb = readFile(context);

        JsonObject json = parser.parse(sb.toString()).getAsJsonObject();
        if (!json.has(courseId)) {
            json.add(courseId, new JsonArray());
        }
        JsonArray arr = json.getAsJsonArray(courseId);
        arr.add(GsonSingleton.getInstance().toJsonTree(assignment, CourseAssignment.class));

        writeFile(context, json);
    }

    public static boolean removeSavedAssignment(Context context, String courseId, CourseAssignment assignment) throws IOException {
        initializeFile(context);
        StringBuilder sb = readFile(context);

        JsonObject json = parser.parse(sb.toString()).getAsJsonObject();
        if (!json.has(courseId)) {
            json.add(courseId, new JsonArray());
        }
        JsonArray arr = json.getAsJsonArray(courseId);
        List<CourseAssignment> assignments = GsonSingleton.getInstance().fromJson(arr, new TypeToken<List<CourseAssignment>>(){}.getType());

        if (assignments.remove(assignment)) {
            arr = GsonSingleton.getInstance().toJsonTree(assignments).getAsJsonArray();
            json.add(courseId, arr);

            writeFile(context, json);
            return true;
        }
        return false;
    }

    public static void clearSavedAssignmentsForCourse(Context context, String courseId) throws IOException {
        initializeFile(context);
        StringBuilder sb = readFile(context);

        JsonObject json = parser.parse(sb.toString()).getAsJsonObject();
        if (json.has(courseId)) {
            json.remove(courseId);
            writeFile(context, json);
        }
    }

    public static void clearAllSavedAssignments(Context context) throws IOException {
        initializeFile(context);
        context.deleteFile(filename);
    }

    private static void initializeFile(Context context) throws IOException {
        File f = context.getFileStreamPath(filename);
        File tmp = context.getFileStreamPath(filenameTemp);
        if (f.length() == 0 && tmp.length() > 0) {
            tmp.renameTo(f);
        }
        else if (f.length() == 0) {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write("{}".getBytes());
            outputStream.close();
        }
    }

    private static StringBuilder readFile(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        fis.close();
        return sb;
    }

    private static void writeFile(Context context, JsonObject json) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(filenameTemp, Context.MODE_PRIVATE);
        outputStream.write(json.toString().getBytes());
        outputStream.close();

        context.deleteFile(filename);

        File to = context.getFileStreamPath(filename);
        File from = context.getFileStreamPath(filenameTemp);
        from.renameTo(to);
    }

}
