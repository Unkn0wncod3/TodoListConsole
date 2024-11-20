package com.todo.storage;

import com.todo.model.Task;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.todo.logging.Logger;

public class FileStorage {
    private static final String FILE_PATH = "todo-data.txt";

    public void saveTasks(List<Task> tasks) throws IOException {
        List<Task> existingTasks = new ArrayList<>();

        if (new File(FILE_PATH).exists()) {
            try {
                existingTasks = loadTasks();
                Logger.log("FileStorage: Vorhandene Aufgaben erfolgreich geladen. Anzahl: " + existingTasks.size());
            } catch (IOException e) {
                Logger.log("FileStorage: Fehler beim Laden der bestehenden Aufgaben: " + e.getMessage());
            }
        }

        List<Task> mergedTasks = new ArrayList<>(existingTasks);
        mergedTasks.addAll(tasks);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Task task : mergedTasks) {
                writer.write(String.format("%s;%s;%s;%d;%s;%s%n",
                        task.getTitle(),
                        task.getDescription(),
                        task.getCategory(),
                        task.getPriority(),
                        task.getDueDate(),
                        task.getRecurrenceType() != null ? task.getRecurrenceType() : ""));
            }
            Logger.log("FileStorage: Aufgaben erfolgreich gespeichert. Gesamtanzahl: " + mergedTasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Speichern der Aufgaben: " + e.getMessage());
            throw e;
        }
    }

    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    String title = parts[0];
                    String description = parts[1];
                    String category = parts[2];
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate dueDate = LocalDate.parse(parts[4]);
                    String recurrenceType = parts.length > 5 && !parts[5].isBlank() ? parts[5].toLowerCase() : null;

                    tasks.add(new Task(title, description, category, priority, dueDate, recurrenceType));
                }
            }
            Logger.log("FileStorage: Aufgaben erfolgreich geladen. Anzahl: " + tasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Laden der Aufgaben: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Logger.log("FileStorage: Fehler beim Verarbeiten der Aufgaben: " + e.getMessage());
            throw new IOException("Fehler beim Verarbeiten der Datei", e);
        }
        return tasks;
    }
}
