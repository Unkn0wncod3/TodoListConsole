package com.todo;

import com.todo.model.Task;
import com.todo.service.TaskService;
import com.todo.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        TaskService taskService = new TaskService();
        FileStorage fileStorage = new FileStorage();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=====================================");
                System.out.println("           2ToDo Liste");
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
                scanner.nextLine(); // Eingabepuffer leeren

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
                        scanner.nextLine(); // Eingabepuffer leeren
                        System.out.print("Fälligkeitsdatum (YYYY-MM-DD): ");
                        LocalDate dueDate = LocalDate.parse(scanner.nextLine());

                        Task task = new Task(title, description, category, priority, dueDate);
                        taskService.addTask(task);

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
                        break;

                    case 3:
                        System.out.print("Aufgaben-Index zum Aktualisieren: ");
                        int updateIndex = scanner.nextInt() - 1;
                        scanner.nextLine(); // Eingabepuffer leeren

                        if (updateIndex >= 0 && updateIndex < taskService.getAllTasks().size()) {
                            System.out.println("Aktuelle Aufgabe: ");
                            System.out.println(taskService.getAllTasks().get(updateIndex));
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
                            System.out.print("Neues Fälligkeitsdatum (YYYY-MM-DD): ");
                            LocalDate newDueDate = LocalDate.parse(scanner.nextLine());

                            Task updatedTask = new Task(newTitle, newDescription, newCategory, newPriority, newDueDate);
                            taskService.updateTask(updateIndex, updatedTask);

                            System.out.println("Aufgabe erfolgreich aktualisiert!");
                        } else {
                            System.out.println("Ungültiger Index.");
                        }
                        break;

                    case 4:
                        System.out.print("Aufgaben-Index zum Löschen: ");
                        int deleteIndex = scanner.nextInt() - 1;

                        if (deleteIndex >= 0 && deleteIndex < taskService.getAllTasks().size()) {
                            taskService.deleteTask(deleteIndex);
                            System.out.println("Aufgabe erfolgreich gelöscht!");
                        } else {
                            System.out.println("Ungültiger Index.");
                        }
                        break;

                    case 5:
                        fileStorage.saveTasks(taskService.getAllTasks());
                        System.out.println("Daten erfolgreich gespeichert!");
                        break;
                    case 6:
                        List<Task> loadedTasks = fileStorage.loadTasks();
                        taskService.setTasks(loadedTasks);
                        System.out.println("Daten erfolgreich geladen!");
                        break;
                    case 7:
                        fileStorage.saveTasks(taskService.getAllTasks());
                        System.out.println("Daten erfolgreich gespeichert!");

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
