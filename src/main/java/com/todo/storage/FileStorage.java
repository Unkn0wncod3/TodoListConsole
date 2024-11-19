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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Task task : tasks) {
                writer.write(String.format("%s;%s;%s;%d;%s%n",
                        task.getTitle(),
                        task.getDescription(),
                        task.getCategory(),
                        task.getPriority(),
                        task.getDueDate()));
            }
            Logger.log("FileStorage: Aufgaben erfolgreich gespeichert. Anzahl: " + tasks.size());
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
                if (parts.length == 5) {
                    String title = parts[0];
                    String description = parts[1];
                    String category = parts[2];
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate dueDate = LocalDate.parse(parts[4]);
                    tasks.add(new Task(title, description, category, priority, dueDate));
                }
            }
            Logger.log("FileStorage: Aufgaben erfolgreich geladen. Anzahl: " + tasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Laden der Aufgaben: " + e.getMessage());
            throw e;
        }
        return tasks;
    }
}
