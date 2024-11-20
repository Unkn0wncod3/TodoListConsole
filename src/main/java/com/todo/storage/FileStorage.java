package com.todo.storage;

import com.todo.model.Task;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.todo.logging.Logger;

public class FileStorage {
    private static final String FILE_PATH = "todo-data.txt";

    public void saveTask(Task task) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatTask(task));
            writer.newLine();
            Logger.log("FileStorage: Aufgabe erfolgreich hinzugefügt: " + task);
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Hinzufügen der Aufgabe: " + e.getMessage());
            throw e;
        }
    }

    public void saveAllTasks(List<Task> tasks) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Task task : tasks) {
                writer.write(formatTask(task));
                writer.newLine();
            }
            Logger.log("FileStorage: Alle Aufgaben erfolgreich gespeichert. Gesamtanzahl: " + tasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Speichern aller Aufgaben: " + e.getMessage());
            throw e;
        }
    }

    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 7) {
                    String title = parts[0];
                    String description = parts[1];
                    String category = parts[2];
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate dueDate = LocalDate.parse(parts[4]);
                    String recurrenceType = parts[5].isBlank() ? null : parts[5];
                    boolean completed = Boolean.parseBoolean(parts[6]);

                    tasks.add(new Task(title, description, category, priority, dueDate, recurrenceType, completed));
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

    private String formatTask(Task task) {
        return String.format("%s;%s;%s;%d;%s;%s;%b",
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.getRecurrenceType() != null ? task.getRecurrenceType() : "",
                task.isCompleted());
    }
}
