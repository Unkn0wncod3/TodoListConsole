package com.todo;

import com.todo.model.Task;
import com.todo.service.TaskService;
import com.todo.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        TaskService taskService = new TaskService();
        FileStorage fileStorage = new FileStorage();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nToDo Liste - Optionen:");
            System.out.println("1. Aufgabe hinzufügen");
            System.out.println("2. Aufgaben anzeigen");
            System.out.println("3. Aufgabe aktualisieren");
            System.out.println("4. Aufgabe löschen");
            System.out.println("5. Speichern & Beenden");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Titel: ");
                    String title = scanner.nextLine();
                    System.out.print("Beschreibung: ");
                    String description = scanner.nextLine();
                    System.out.print("Kategorie: ");
                    String category = scanner.nextLine();
                    System.out.print("Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ");
                    int priority = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Fälligkeitsdatum (YYYY-MM-DD): ");
                    LocalDate dueDate = LocalDate.parse(scanner.nextLine());

                    Task task = new Task(title, description, category, priority, dueDate);
                    taskService.addTask(task);
                    break;

                case 2:
                    taskService.getAllTasks().forEach(System.out::println);
                    break;

                case 3:
                    System.out.print("Aufgaben-Index zum Aktualisieren: ");
                    int updateIndex = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Neue Daten eingeben...");
                    // Wiederholung der Eingabelogik
                    break;

                case 4:
                    System.out.print("Aufgaben-Index zum Löschen: ");
                    int deleteIndex = scanner.nextInt();
                    taskService.deleteTask(deleteIndex);
                    break;

                case 5:
                    fileStorage.saveTasks(taskService.getAllTasks());
                    System.out.println("Gespeichert. Beenden...");
                    return;

                default:
                    System.out.println("Ungültige Auswahl!");
            }
        }
    }
}
