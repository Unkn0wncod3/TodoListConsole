package com.todo;

import com.todo.logging.Logger;
import com.todo.model.Task;
import com.todo.service.TaskService;
import com.todo.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        TaskService taskService = new TaskService();
        FileStorage fileStorage = new FileStorage();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                Logger.log("Programm gestartet.");
                System.out.println("\n=====================================");
                System.out.println("           ToDo Liste");
                System.out.println("=====================================");
                System.out.println("1. Aufgabe hinzufügen");
                System.out.println("2. Aufgaben anzeigen");
                System.out.println("3. Aufgabe aktualisieren");
                System.out.println("4. Aufgabe löschen");
                System.out.println("5. Aufgaben speichern");
                System.out.println("6. Aufgaben laden");
                System.out.println("7. Beenden");
                System.out.println("=====================================");
                System.out.print("Wähle eine Option: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                System.out.println("-------------------------------------");

                switch (choice) {
                    case 1:
                        System.out.println("Neue Aufgabe hinzufügen:");
                        System.out.print("Titel: ");
                        String title = scanner.nextLine();
                        System.out.print("Beschreibung: ");
                        String description = scanner.nextLine();
                        System.out.print("Kategorie: ");
                        String category = scanner.nextLine();
                        System.out.print("Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ");
                        int priority = scanner.nextInt();
                        scanner.nextLine();

                        LocalDate dueDate = null;
                        while (dueDate == null) {
                            System.out.print("Fälligkeitsdatum (TT.MM.JJJJ) [Leer für morgen]: ");
                            String dateInput = scanner.nextLine();
                            if (dateInput.isBlank()) {
                                dueDate = LocalDate.now().plusDays(1);
                                System.out.println("Kein Datum eingegeben. Fälligkeitsdatum auf "
                                        + dueDate.format(dateFormatter) + " gesetzt.");
                            } else {
                                try {
                                    dueDate = LocalDate.parse(dateInput, dateFormatter);
                                } catch (DateTimeParseException e) {
                                    System.out.println("Ungültiges Datum. Bitte das Format TT.MM.JJJJ verwenden.");
                                }
                            }
                        }

                        Task task = new Task(title, description, category, priority, dueDate);
                        taskService.addTask(task);
                        Logger.log("Aufgabe hinzugefügt: " + task);
                        System.out.println("Aufgabe erfolgreich hinzugefügt!");
                        break;

                    case 2:
                        System.out.println("Alle Aufgaben:");
                        System.out.println("=====================================");
                        if (taskService.getAllTasks().isEmpty()) {
                            System.out.println("Keine Aufgaben vorhanden.");
                        } else {
                            for (int i = 0; i < taskService.getAllTasks().size(); i++) {
                                System.out.println((i + 1) + ". " + taskService.getAllTasks().get(i));
                                System.out.println("-------------------------------------");
                            }
                        }
                        Logger.log("Aufgaben angezeigt: " + taskService.getAllTasks().size() + " Aufgaben");
                        break;

                    case 3:
                        System.out.print("Aufgaben-Index zum Aktualisieren: ");
                        int updateIndex = scanner.nextInt() - 1;
                        scanner.nextLine();

                        if (updateIndex >= 0 && updateIndex < taskService.getAllTasks().size()) {
                            Task oldTask = taskService.getAllTasks().get(updateIndex);

                            System.out.println("Aktuelle Aufgabe: ");
                            System.out.println(oldTask);
                            System.out.println("Neue Daten eingeben:");
                            System.out.print("Neuer Titel: ");
                            String newTitle = scanner.nextLine();
                            System.out.print("Neue Beschreibung: ");
                            String newDescription = scanner.nextLine();
                            System.out.print("Neue Kategorie: ");
                            String newCategory = scanner.nextLine();
                            System.out.print("Neue Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ");
                            int newPriority = scanner.nextInt();
                            scanner.nextLine();

                            LocalDate newDueDate = null;
                            while (newDueDate == null) {
                                System.out.print("Neues Fälligkeitsdatum (TT.MM.JJJJ): ");
                                String dateInput = scanner.nextLine();
                                try {
                                    newDueDate = LocalDate.parse(dateInput, dateFormatter);
                                } catch (DateTimeParseException e) {
                                    System.out.println("Ungültiges Datum. Bitte das Format TT.MM.JJJJ verwenden.");
                                }
                            }

                            Task updatedTask = new Task(newTitle, newDescription, newCategory, newPriority, newDueDate);
                            taskService.updateTask(updateIndex, updatedTask);
                            Logger.log("Aufgabe aktualisiert: ALT: " + oldTask + " NEU: " + updatedTask);
                            System.out.println("Aufgabe erfolgreich aktualisiert!");
                        } else {
                            System.out.println("Ungültiger Index.");
                        }
                        break;

                    case 4:
                        System.out.print("Aufgaben-Index zum Löschen: ");
                        int deleteIndex = scanner.nextInt() - 1;

                        if (deleteIndex >= 0 && deleteIndex < taskService.getAllTasks().size()) {
                            Task deletedTask = taskService.getAllTasks().get(deleteIndex);
                            taskService.deleteTask(deleteIndex);
                            Logger.log("Aufgabe gelöscht: " + deletedTask);
                            System.out.println("Aufgabe erfolgreich gelöscht!");
                        } else {
                            System.out.println("Ungültiger Index.");
                        }
                        break;

                    case 5:
                        fileStorage.saveTasks(taskService.getAllTasks());
                        Logger.log("Aufgaben gespeichert: " + taskService.getAllTasks().size() + " Aufgaben");
                        System.out.println("Daten erfolgreich gespeichert!");
                        break;

                    case 6:
                        List<Task> loadedTasks = fileStorage.loadTasks();
                        taskService.setTasks(loadedTasks);
                        Logger.log("Aufgaben geladen: " + loadedTasks.size() + " Aufgaben");
                        System.out.println("Daten erfolgreich geladen!");
                        break;

                    case 7:
                        Logger.log("Programm beendet.");
                        System.out.println("Beenden...");
                        System.out.println("=====================================");
                        return;

                    default:
                        System.out.println("Ungültige Auswahl. Bitte erneut versuchen!");
                }
                System.out.println("-------------------------------------");
            }
        }
    }
}
