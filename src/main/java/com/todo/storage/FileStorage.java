package com.todo.storage;

import com.todo.model.Task;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.todo.logging.Logger;

public class FileStorage {
    private static final String FILE_PATH = "todo-data.txt";
    private static final String ARCHIVE_FILE_PATH = "archive-data.txt";

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
        return loadTasksFromFile(FILE_PATH);
    }

    private String formatTask(Task task) {
        List<String> tags = task.getTags();
        if (tags == null || tags.isEmpty()) {
            tags = List.of("#default");
        }
        return String.format("%s;%s;%s;%d;%s;%s;%b;%s",
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.getRecurrenceType() != null ? task.getRecurrenceType() : "",
                task.isCompleted(),
                String.join(",", tags));
    }

    public void archiveTask(Task task) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVE_FILE_PATH, true))) {
            writer.write(formatTask(task));
            writer.newLine();
            Logger.log("FileStorage: Aufgabe archiviert: " + task);
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Archivieren der Aufgabe: " + e.getMessage());
            throw e;
        }
    }

    public List<Task> loadArchivedTasks() throws IOException {
        return loadTasksFromFile(ARCHIVE_FILE_PATH);
    }

    public void saveAllArchivedTasks(List<Task> archivedTasks) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVE_FILE_PATH))) {
            for (Task task : archivedTasks) {
                writer.write(formatTask(task));
                writer.newLine();
            }
            Logger.log("FileStorage: Archivierte Aufgaben erfolgreich aktualisiert. Gesamtanzahl: "
                    + archivedTasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Aktualisieren archivierter Aufgaben: " + e.getMessage());
            throw e;
        }
    }

    private List<Task> loadTasksFromFile(String filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 8) {
                    String title = parts[0];
                    String description = parts[1];
                    String category = parts[2];
                    int priority = Integer.parseInt(parts[3]);
                    LocalDate dueDate = LocalDate.parse(parts[4]);
                    String recurrenceType = parts[5].isBlank() ? null : parts[5];
                    boolean completed = Boolean.parseBoolean(parts[6]);
                    List<String> tags = parts[7].isBlank() ? new ArrayList<>() : Arrays.asList(parts[7].split(","));

                    tasks.add(
                            new Task(title, description, category, priority, dueDate, recurrenceType, completed, tags));
                }
            }
            Logger.log("FileStorage: Aufgaben aus Datei " + filePath + " erfolgreich geladen. Anzahl: " + tasks.size());
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Laden der Aufgaben aus Datei " + filePath + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Logger.log(
                    "FileStorage: Fehler beim Verarbeiten der Aufgaben aus Datei " + filePath + ": " + e.getMessage());
            throw new IOException("Fehler beim Verarbeiten der Datei " + filePath, e);
        }
        return tasks;
    }

    public void exportTasksToCSV(List<Task> tasks, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Title,Description,Category,Priority,DueDate,RecurrenceType,Completed,Tags");
            writer.newLine();

            for (Task task : tasks) {
                writer.write(formatTaskAsCSV(task));
                writer.newLine();
            }

            Logger.log("FileStorage: Aufgaben erfolgreich nach CSV exportiert: " + filePath);
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Exportieren der Aufgaben nach CSV: " + e.getMessage());
            throw e;
        }
    }

    private String formatTaskAsCSV(Task task) {
        return String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",%b,\"%s\"",
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.getRecurrenceType() != null ? task.getRecurrenceType() : "",
                task.isCompleted(),
                String.join(",", task.getTags()));
    }

    public List<Task> importTasksFromCSV(String filePath) throws IOException {
        List<Task> importedTasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = parseCSVLine(line);
                if (parts.length >= 7) {
                    try {
                        String title = parts[0];
                        String description = parts[1];
                        String category = parts[2];
                        int priority = Integer.parseInt(parts[3].trim());
                        LocalDate dueDate = LocalDate.parse(parts[4].trim());
                        String recurrenceType = parts[5].isBlank() ? null : parts[5];
                        boolean completed = Boolean.parseBoolean(parts[6].trim());
                        List<String> tags = parts.length > 7 && !parts[7].trim().isEmpty()
                                ? Arrays.asList(parts[7].split(","))
                                : new ArrayList<>();

                        importedTasks.add(new Task(title, description, category, priority, dueDate, recurrenceType,
                                completed, tags));
                    } catch (Exception e) {
                        throw new IOException("Fehler beim Parsing von Priorität in Zeile: " + line, e);
                    }
                } else {
                    throw new IOException("Ungültige Anzahl von Spalten in Zeile: " + line);
                }
            }
        } catch (IOException e) {
            Logger.log("FileStorage: Fehler beim Importieren aus CSV: " + e.getMessage());
            throw e;
        }
        Logger.log("FileStorage: Aufgaben erfolgreich aus CSV importiert. Anzahl: " + importedTasks.size());
        return importedTasks;
    }

    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean insideQuotes = false;

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                insideQuotes = !insideQuotes;
            } else if (ch == ',' && !insideQuotes) {
                tokens.add(currentToken.toString().trim());
                currentToken.setLength(0);
            } else {
                currentToken.append(ch);
            }
        }
        tokens.add(currentToken.toString().trim());
        return tokens.toArray(new String[0]);
    }

}
