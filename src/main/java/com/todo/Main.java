package com.todo;

import com.todo.logging.Logger;
import com.todo.model.Task;
import com.todo.service.TaskService;
import com.todo.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String PASSWORD_FILE = "password.txt";
    private static String password = "1";

    public static void main(String[] args) throws IOException {
        loadPassword();

        Logger.log("Benutzer erfolgreich authentifiziert.");
        TaskService taskService = new TaskService();
        FileStorage fileStorage = new FileStorage();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (Scanner scanner = new Scanner(System.in)) {
            if (!authenticateUser(scanner)) {
                return;
            }

            // Preload Tasks
            List<Task> preLoadedTasks = fileStorage.loadTasks();
            taskService.setTasks(preLoadedTasks);
            Logger.log("Aufgaben geladen: " + preLoadedTasks.size() + " Aufgaben");
            System.out.println("Daten erfolgreich geladen!");

            while (true) {
                System.out.println("\n=====================================");
                System.out.println("           ToDo Liste");
                System.out.println("=====================================");
                System.out.println("1. Aufgabe hinzufügen");
                System.out.println("2. Aufgaben anzeigen");
                System.out.println("3. Aufgabe aktualisieren");
                System.out.println("4. Aufgabe löschen");
                System.out.println("5. Aufgaben speichern");
                System.out.println("6. Aufgaben laden");
                System.out.println("7. Passwort ändern");
                System.out.println("8. Aufgaben suchen und filtern");
                System.out.println("9. Beenden");
                System.out.println("=====================================");

                int choice = -1;
                while (choice == -1) {
                    System.out.print("Wähle eine Option: ");
                    String input = scanner.nextLine();

                    try {
                        choice = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        System.out.println("Ungültige Eingabe. Bitte geben Sie eine Zahl ein.");
                        Logger.log("Ungültige Eingabe: " + input);
                    }
                }

                System.out.println("-------------------------------------");

                switch (choice) {
                    case 1:
                        System.out.println("Neue Aufgabe hinzufügen:");
                        String title = validateInput(scanner, "Titel: ", input -> !input.trim().isEmpty(),
                                "Titel darf nicht leer sein.");
                        String description = validateInput(scanner, "Beschreibung: ", input -> !input.trim().isEmpty(),
                                "Beschreibung darf nicht leer sein.");
                        String category = validateInput(scanner, "Kategorie: ", input -> !input.trim().isEmpty(),
                                "Kategorie darf nicht leer sein.");

                        String priorityInput = validateInput(scanner, "Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ",
                                input -> input.matches("[1-3]"),
                                "Ungültige Priorität. Bitte geben Sie eine Zahl zwischen 1 und 3 ein.");
                        int priority = Integer.parseInt(priorityInput);

                        LocalDate dueDate = null;
                        while (dueDate == null) {
                            String dateInput = validateInput(scanner,
                                    "Fälligkeitsdatum (TT.MM.JJJJ) [Leer für morgen]: ",
                                    input -> input.isBlank() || input.matches("\\d{2}\\.\\d{2}\\.\\d{4}"),
                                    "Ungültiges Datum. Bitte das Format TT.MM.JJJJ verwenden oder leer lassen.");

                            if (dateInput.isBlank()) {
                                dueDate = LocalDate.now().plusDays(1);
                                System.out.println("Kein Datum eingegeben. Fälligkeitsdatum auf "
                                        + dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " gesetzt.");
                            } else {
                                try {
                                    dueDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
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
                        changePassword(scanner);
                        break;

                    case 8:
                        System.out.println("Suche und Filter - Wählen Sie ein Kriterium:");
                        System.out.println("1. Nach Titel suchen");
                        System.out.println("2. Nach Beschreibung suchen");
                        System.out.println("3. Nach Kategorie filtern");
                        System.out.println("4. Nach Priorität filtern");
                        System.out.print("Wähle eine Option: ");
                        int filterChoice = scanner.nextInt();
                        scanner.nextLine();

                        List<Task> filteredTasks = null;
                        switch (filterChoice) {
                            case 1:
                                System.out.print("Suchbegriff im Titel: ");
                                String titleSearch = scanner.nextLine();
                                filteredTasks = taskService.searchByTitle(titleSearch);
                                break;
                            case 2:
                                System.out.print("Suchbegriff in der Beschreibung: ");
                                String descriptionSearch = scanner.nextLine();
                                filteredTasks = taskService.searchByDescription(descriptionSearch);
                                break;
                            case 3:
                                System.out.print("Genaue Kategorie: ");
                                String categoryFilter = scanner.nextLine();
                                filteredTasks = taskService.filterByCategory(categoryFilter);
                                break;
                            case 4:
                                System.out.print("Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ");
                                int priorityFilter = scanner.nextInt();
                                filteredTasks = taskService.filterByPriority(priorityFilter);
                                break;
                            default:
                                System.out.println("Ungültige Auswahl.");
                        }

                        if (filteredTasks != null && !filteredTasks.isEmpty()) {
                            System.out.println("Gefundene Aufgaben:");
                            for (Task singleTask : filteredTasks) {
                                System.out.println(singleTask);
                            }
                        } else {
                            System.out.println("Keine Aufgaben gefunden.");
                        }

                        Logger.log("Aufgaben gefiltert: " + (filteredTasks != null ? filteredTasks.size() : 0)
                                + " Ergebnisse");
                        break;

                    case 9:
                        Logger.log("Programm beendet.");
                        System.out.println("Beenden...");
                        return;

                    default:
                        System.out.println("Ungültige Auswahl. Bitte erneut versuchen!");
                }
                System.out.println("-------------------------------------");
            }
        }
    }

    private static void loadPassword() {
        try {
            if (Files.exists(Paths.get(PASSWORD_FILE))) {
                password = new String(Files.readAllBytes(Paths.get(PASSWORD_FILE))).trim();
            } else {
                savePassword(password);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden des Passworts: " + e.getMessage());
            Logger.log("Fehler beim Laden des Passworts.");
        }
    }

    private static void savePassword(String newPassword) {
        try {
            Files.write(Paths.get(PASSWORD_FILE), newPassword.getBytes());
            Logger.log("Passwort geändert.");
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern des Passworts: " + e.getMessage());
            Logger.log("Fehler beim Speichern des Passworts.");
        }
    }

    private static boolean authenticateUser(Scanner scanner) {
        System.out.print("Passwort eingeben: ");
        if (scanner.hasNextLine()) {
            String userInput = scanner.nextLine();
            if (password.equals(userInput)) {
                Logger.log("Authentifizierung erfolgreich.");
                return true;
            }
        }
        Logger.log("Authentifizierung fehlgeschlagen. Falsches Passwort eingegeben.");
        System.out.println("Falsches Passwort. Programm wird beendet.");
        return false;
    }

    private static void changePassword(Scanner scanner) {
        System.out.print("Altes Passwort eingeben: ");
        String oldPassword = scanner.nextLine();

        if (!password.equals(oldPassword)) {
            System.out.println("Falsches Passwort. Passwortänderung abgebrochen.");
            Logger.log("Passwortänderung fehlgeschlagen: falsches Passwort.");
            return;
        }

        System.out.print("Neues Passwort eingeben: ");
        String newPassword = scanner.nextLine();
        System.out.print("Neues Passwort bestätigen: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwörter stimmen nicht überein. Passwortänderung abgebrochen.");
            Logger.log("Passwortänderung fehlgeschlagen: Bestätigung stimmt nicht überein.");
            return;
        }

        password = newPassword;
        savePassword(password);
        System.out.println("Passwort erfolgreich geändert.");
        Logger.log("Passwort erfolgreich geändert.");
    }

    private static String validateInput(Scanner scanner, String prompt, java.util.function.Predicate<String> validator,
            String errorMessage) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine();
            if (validator.test(input)) {
                return input;
            }
            System.out.println(errorMessage);
        }
    }

}
