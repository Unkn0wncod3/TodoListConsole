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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String PASSWORD_FILE = "password.txt";
    private static String password = "1";

    public static void main(String[] args) throws IOException {
        loadPassword();

        Logger.log("Programm gestartet.");
        TaskService taskService = new TaskService();
        FileStorage fileStorage = new FileStorage();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (Scanner scanner = new Scanner(System.in)) {
            if (!authenticateUser(scanner)) {
                return;
            }

            reloadTasks(fileStorage, taskService);

            List<Task> archivedTasks = fileStorage.loadArchivedTasks();
            taskService.setArchivedTasks(archivedTasks);
            Logger.log("Archivierte Aufgaben geladen: " + archivedTasks.size() + " Aufgaben.");
            System.out.println("Archivierte Aufgaben erfolgreich geladen!");

            while (true) {
                displayMenu();
                int choice = getValidatedChoice(scanner);

                System.out.println("-------------------------------------");

                switch (choice) {
                    case 1 -> addTask(scanner, taskService, fileStorage, dateFormatter);
                    case 2 -> displayTasks(taskService);
                    case 3 -> updateTask(scanner, taskService, dateFormatter);
                    case 4 -> deleteTask(scanner, taskService);
                    case 5 -> saveAllTasks(fileStorage, taskService);
                    case 6 -> reloadTasks(fileStorage, taskService);
                    case 7 -> changePassword(scanner);
                    case 8 -> filterTasks(scanner, taskService);
                    case 9 -> {
                        Logger.log("Programm beendet.");
                        System.out.println("Beenden...");
                        return;
                    }
                    case 10 -> displayStatistics(taskService);
                    case 11 -> markTaskAsCompleted(scanner, taskService);
                    case 12 -> archiveTask(scanner, taskService, fileStorage);
                    case 13 -> displayArchivedTasks(taskService);
                    case 14 -> restoreArchivedTask(scanner, taskService, fileStorage);

                    default -> System.out.println("Ungültige Auswahl. Bitte erneut versuchen!");
                }

                System.out.println("-------------------------------------");
            }
        }
    }

    private static void displayMenu() {
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
        System.out.println("10. Aufgabenstatistik anzeigen");
        System.out.println("11. Aufgabe als abgeschlossen markieren");
        System.out.println("12. Aufgabe archivieren");
        System.out.println("13. Archiv anzeigen");
        System.out.println("14. Archivierte Aufgabe wiederherstellen");

        System.out.println("=====================================");
    }

    private static int getValidatedChoice(Scanner scanner) {
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
        return choice;
    }

    private static void addTask(Scanner scanner, TaskService taskService, FileStorage fileStorage,
            DateTimeFormatter dateFormatter) throws IOException {
        System.out.println("Neue Aufgabe hinzufügen:");
        String title = validateInput(scanner, "Titel: ", input -> !input.trim().isEmpty(),
                "Titel darf nicht leer sein.");
        String description = validateInput(scanner, "Beschreibung: ", input -> !input.trim().isEmpty(),
                "Beschreibung darf nicht leer sein.");
        String category = validateInput(scanner, "Kategorie: ", input -> !input.trim().isEmpty(),
                "Kategorie darf nicht leer sein.");
        int priority = Integer.parseInt(validateInput(scanner, "Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ",
                input -> input.matches("[1-3]"), "Ungültige Priorität."));

        LocalDate dueDate = getValidatedDueDate(scanner, dateFormatter);

        String recurrenceType = validateInput(scanner,
                "Wiederholung (daily, weekly, monthly oder leer für keine Wiederholung): ",
                input -> input.isBlank() || input.equalsIgnoreCase("daily") || input.equalsIgnoreCase("weekly")
                        || input.equalsIgnoreCase("monthly"),
                "Ungültige Eingabe.");

        String statusInput = validateInput(scanner,
                "Neuer Status (true für abgeschlossen, false für offen, leer für false): ",
                input -> input.isBlank() || input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false"),
                "Ungültiger Status. Bitte true, false oder leer eingeben.");
        boolean completed = statusInput.isBlank() ? false : Boolean.parseBoolean(statusInput);

        Task task = new Task(title, description, category, priority, dueDate,
                recurrenceType.isBlank() ? null : recurrenceType.toLowerCase(), completed, new ArrayList<>());
        taskService.addTask(task);
        fileStorage.saveTask(task);
        Logger.log("Aufgabe hinzugefügt: " + task);
        System.out.println("Aufgabe erfolgreich hinzugefügt!");
    }

    private static void displayTasks(TaskService taskService) {
        System.out.println("Alle Aufgaben:");
        System.out.println("=====================================");
        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("Keine Aufgaben vorhanden.");
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.println((i + 1) + ". " + task);
                System.out.println("-------------------------------------");
            }
        }
        Logger.log("Aufgaben angezeigt: " + tasks.size() + " Aufgaben");
    }

    private static void updateTask(Scanner scanner, TaskService taskService, DateTimeFormatter dateFormatter) {
        System.out.print("Aufgaben-Index zum Aktualisieren: ");
        int updateIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        List<Task> tasks = taskService.getAllTasks();

        if (updateIndex >= 0 && updateIndex < tasks.size()) {
            Task oldTask = tasks.get(updateIndex);
            System.out.println("Aktuelle Aufgabe: " + oldTask);

            boolean continueEditing = true;
            while (continueEditing) {
                System.out.println("\nWelche Eigenschaft möchten Sie ändern?");
                System.out.println("1. Titel");
                System.out.println("2. Beschreibung");
                System.out.println("3. Kategorie");
                System.out.println("4. Priorität");
                System.out.println("5. Fälligkeitsdatum");
                System.out.println("6. Wiederholung");
                System.out.println("7. Status");
                System.out.println("8. Tag hinzufügen");
                System.out.println("9. Tag entfernen");
                System.out.println("10. Fertig");
                System.out.print("Wählen Sie eine Option: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> {
                        String newTitle = validateInput(scanner, "Neuer Titel: ", input -> !input.trim().isEmpty(),
                                "Titel darf nicht leer sein.");
                        oldTask.setTitle(newTitle);
                        System.out.println("Titel erfolgreich aktualisiert.");
                    }
                    case 2 -> {
                        String newDescription = validateInput(scanner, "Neue Beschreibung: ",
                                input -> !input.trim().isEmpty(), "Beschreibung darf nicht leer sein.");
                        oldTask.setDescription(newDescription);
                        System.out.println("Beschreibung erfolgreich aktualisiert.");
                    }
                    case 3 -> {
                        String newCategory = validateInput(scanner, "Neue Kategorie: ",
                                input -> !input.trim().isEmpty(),
                                "Kategorie darf nicht leer sein.");
                        oldTask.setCategory(newCategory);
                        System.out.println("Kategorie erfolgreich aktualisiert.");
                    }
                    case 4 -> {
                        int newPriority = Integer.parseInt(validateInput(scanner,
                                "Neue Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ",
                                input -> input.matches("[1-3]"), "Ungültige Priorität."));
                        oldTask.setPriority(newPriority);
                        System.out.println("Priorität erfolgreich aktualisiert.");
                    }
                    case 5 -> {
                        LocalDate newDueDate = getValidatedDueDate(scanner, dateFormatter);
                        oldTask.setDueDate(newDueDate);
                        System.out.println("Fälligkeitsdatum erfolgreich aktualisiert.");
                    }
                    case 6 -> {
                        String newRecurrenceType = validateInput(scanner,
                                "Neue Wiederholung (daily, weekly, monthly oder leer für keine Wiederholung): ",
                                input -> input.isBlank() || input.equalsIgnoreCase("daily")
                                        || input.equalsIgnoreCase("weekly")
                                        || input.equalsIgnoreCase("monthly"),
                                "Ungültige Eingabe.");
                        oldTask.setRecurrenceType(newRecurrenceType.isBlank() ? null : newRecurrenceType.toLowerCase());
                        System.out.println("Wiederholung erfolgreich aktualisiert.");
                    }
                    case 7 -> {
                        String statusInputCompleted = validateInput(scanner,
                                "Neuer Status (true für abgeschlossen, false für offen, leer für false): ",
                                input -> input.isBlank() || input.equalsIgnoreCase("true")
                                        || input.equalsIgnoreCase("false"),
                                "Ungültiger Status. Bitte true, false oder leer eingeben.");
                        boolean newCompleted = statusInputCompleted.isBlank() ? false
                                : Boolean.parseBoolean(statusInputCompleted);
                        oldTask.setCompleted(newCompleted);
                        System.out.println("Status erfolgreich aktualisiert.");
                    }
                    case 8 -> {
                        System.out.print("Tag hinzufügen: ");
                        String newTag = scanner.nextLine();
                        oldTask.getTags().add(newTag);
                        System.out.println("Tag erfolgreich hinzugefügt.");
                    }
                    case 9 -> {
                        System.out.print("Tag entfernen: ");
                        String tagToRemove = scanner.nextLine();
                        if (oldTask.getTags().remove(tagToRemove)) {
                            System.out.println("Tag erfolgreich entfernt.");
                        } else {
                            System.out.println("Tag nicht gefunden.");
                        }
                    }
                    case 10 -> {
                        continueEditing = false;
                        System.out.println("Bearbeitung abgeschlossen.");
                    }
                    default -> System.out.println("Ungültige Auswahl.");
                }
            }

            taskService.updateTask(updateIndex, oldTask);
            Logger.log("Aufgabe aktualisiert: " + oldTask);
        } else {
            System.out.println("Ungültiger Index.");
        }
    }

    private static LocalDate getValidatedDueDate(Scanner scanner, DateTimeFormatter dateFormatter) {
        LocalDate dueDate = null;
        while (dueDate == null) {
            String dateInput = validateInput(scanner, "Fälligkeitsdatum (TT.MM.JJJJ) [Leer für morgen]: ",
                    input -> input.isBlank() || input.matches("\\d{2}\\.\\d{2}\\.\\d{4}"), "Ungültiges Datum.");
            if (dateInput.isBlank()) {
                dueDate = LocalDate.now().plusDays(1);
                System.out.println(
                        "Kein Datum eingegeben. Fälligkeitsdatum auf " + dueDate.format(dateFormatter) + " gesetzt.");
            } else {
                try {
                    dueDate = LocalDate.parse(dateInput, dateFormatter);
                } catch (DateTimeParseException e) {
                    System.out.println("Ungültiges Datum. Bitte erneut versuchen.");
                }
            }
        }
        return dueDate;
    }

    private static void deleteTask(Scanner scanner, TaskService taskService) {
        System.out.print("Aufgaben-Index zum Löschen: ");
        int deleteIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (deleteIndex >= 0 && deleteIndex < taskService.getAllTasks().size()) {
            Task deletedTask = taskService.getAllTasks().get(deleteIndex);
            taskService.deleteTask(deleteIndex);
            Logger.log("Aufgabe gelöscht: " + deletedTask);
            System.out.println("Aufgabe erfolgreich gelöscht!");
        } else {
            System.out.println("Ungültiger Index.");
        }
    }

    private static void saveAllTasks(FileStorage fileStorage, TaskService taskService) throws IOException {
        fileStorage.saveAllTasks(taskService.getAllTasks());
        Logger.log("Aufgaben gespeichert: " + taskService.getAllTasks().size() + " Aufgaben");
        System.out.println("Daten erfolgreich gespeichert!");
    }

    private static void reloadTasks(FileStorage fileStorage, TaskService taskService) throws IOException {
        List<Task> loadedTasks = fileStorage.loadTasks();
        taskService.setTasks(loadedTasks);
        Logger.log("Aufgaben geladen: " + loadedTasks.size() + " Aufgaben");
        System.out.println("Daten erfolgreich geladen!");
    }

    private static void filterTasks(Scanner scanner, TaskService taskService) {
        System.out.println("Suche und Filter - Wählen Sie ein Kriterium:");
        System.out.println("1. Nach Titel suchen");
        System.out.println("2. Nach Beschreibung suchen");
        System.out.println("3. Nach Kategorie filtern");
        System.out.println("4. Nach Priorität filtern");
        System.out.println("5. Nach Tag filtern");
        System.out.print("Wähle eine Option: ");
        int filterChoice = scanner.nextInt();
        scanner.nextLine();

        List<Task> filteredTasks = null;
        switch (filterChoice) {
            case 1 -> {
                System.out.print("Suchbegriff im Titel: ");
                String titleSearch = scanner.nextLine();
                filteredTasks = taskService.searchByTitle(titleSearch);
            }
            case 2 -> {
                System.out.print("Suchbegriff in der Beschreibung: ");
                String descriptionSearch = scanner.nextLine();
                filteredTasks = taskService.searchByDescription(descriptionSearch);
            }
            case 3 -> {
                System.out.print("Genaue Kategorie: ");
                String categoryFilter = scanner.nextLine();
                filteredTasks = taskService.filterByCategory(categoryFilter);
            }
            case 4 -> {
                System.out.print("Priorität (1 = hoch, 2 = mittel, 3 = niedrig): ");
                int priorityFilter = scanner.nextInt();
                filteredTasks = taskService.filterByPriority(priorityFilter);
            }
            case 5 -> {
                System.out.print("Tag eingeben: ");
                String tagFilter = scanner.nextLine();
                filteredTasks = taskService.filterByTag(tagFilter);
            }
            default -> System.out.println("Ungültige Auswahl.");
        }

        if (filteredTasks != null && !filteredTasks.isEmpty()) {
            System.out.println("Gefundene Aufgaben:");
            for (Task singleTask : filteredTasks) {
                System.out.println(singleTask);
            }
        } else {
            System.out.println("Keine Aufgaben gefunden.");
        }

        Logger.log("Aufgaben gefiltert: " + (filteredTasks != null ? filteredTasks.size() : 0) + " Ergebnisse");
    }

    private static void displayStatistics(TaskService taskService) {
        long completedTasksWeek = taskService.countCompletedTasksForCurrentWeek();
        long completedTasksMonth = taskService.countCompletedTasksForCurrentMonth();
        long totalCompletedTasks = taskService.countTotalCompletedTasks();
        long totalPendingTasks = taskService.countTotalPendingTasks();
        int totalTasks = taskService.getAllTasks().size();
        long overdueTasks = taskService.countOverdueTasks();

        System.out.println("\n=====================================");
        System.out.println("           Aufgabenstatistik");
        System.out.println("=====================================");
        System.out.println("Gesamtanzahl Aufgaben: " + totalTasks);
        System.out.println("Abgeschlossene Aufgaben insgesamt: " + totalCompletedTasks);
        System.out.println("Nicht abgeschlossene Aufgaben: " + totalPendingTasks);
        System.out.println("Überfällige Aufgaben: " + overdueTasks);
        System.out.println("-------------------------------------");
        System.out.println("Abgeschlossene Aufgaben diese Woche: " + completedTasksWeek);
        System.out.println("Abgeschlossene Aufgaben diesen Monat: " + completedTasksMonth);
        System.out.println("-------------------------------------");

        Map<String, Long> tasksByCategory = taskService.countTasksByCategory();
        System.out.println("Aufgaben nach Kategorie:");
        tasksByCategory.forEach((cat, count) -> System.out.println(" - " + cat + ": " + count));
        System.out.println("-------------------------------------");

        Map<Integer, Long> tasksByPriority = taskService.countTasksByPriority();
        System.out.println("Aufgaben nach Priorität:");
        tasksByPriority.forEach((prio, count) -> {
            String priorityLabel = switch (prio) {
                case 1 -> "Hoch";
                case 2 -> "Mittel";
                case 3 -> "Niedrig";
                default -> "Unbekannt";
            };
            System.out.println(" - " + priorityLabel + ": " + count);
        });
        System.out.println("-------------------------------------");

        Map<LocalDate, Long> tasksByDueDate = taskService.countTasksByDueDate();
        System.out.println("Aufgaben nach Fälligkeitsdatum:");
        tasksByDueDate.forEach((dd, count) -> System.out.println(" - " + dd + ": " + count));
        System.out.println("-------------------------------------");

        Map<String, Long> recurringTasksByType = taskService.countRecurringTasksByType();
        System.out.println("Wiederkehrende Aufgaben:");
        recurringTasksByType.forEach((type, count) -> System.out.println(" - " + type + ": " + count));
        System.out.println("=====================================");

        Logger.log("Statistik: Gesamt: " + totalTasks + ", Abgeschlossen: " + totalCompletedTasks + ", Offen: "
                + totalPendingTasks + ", Woche: " + completedTasksWeek + ", Monat: " + completedTasksMonth +
                ", Überfällig: " + overdueTasks);
    }

    private static void markTaskAsCompleted(Scanner scanner, TaskService taskService) {
        System.out.print("Aufgaben-Index zum Markieren als abgeschlossen: ");
        int completeIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (completeIndex >= 0 && completeIndex < taskService.getAllTasks().size()) {
            Task taskToComplete = taskService.getAllTasks().get(completeIndex);
            taskToComplete.setCompleted(true);
            System.out.println("Aufgabe erfolgreich als abgeschlossen markiert: " + taskToComplete.getTitle());
            Logger.log("Aufgabe abgeschlossen: " + taskToComplete);
        } else {
            System.out.println("Ungültiger Index.");
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

    private static void archiveTask(Scanner scanner, TaskService taskService, FileStorage fileStorage)
            throws IOException {
        System.out.print("Aufgaben-Index zum Archivieren: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        Task archivedTask = taskService.archiveTask(index);
        if (archivedTask != null) {
            fileStorage.archiveTask(archivedTask);
            System.out.println("Aufgabe erfolgreich archiviert.");
        } else {
            System.out.println("Ungültiger Index.");
        }
    }

    private static void displayArchivedTasks(TaskService taskService) {
        List<Task> archivedTasks = taskService.getArchivedTasks();
        if (archivedTasks.isEmpty()) {
            System.out.println("Keine archivierten Aufgaben vorhanden.");
        } else {
            System.out.println("Archivierte Aufgaben:");
            for (int i = 0; i < archivedTasks.size(); i++) {
                System.out.println((i + 1) + ". " + archivedTasks.get(i));
            }
        }
    }

    private static void restoreArchivedTask(Scanner scanner, TaskService taskService, FileStorage fileStorage)
            throws IOException {
        System.out.print("Archiv-Index zur Wiederherstellung: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        Task restoredTask = taskService.restoreTask(index, fileStorage);
        if (restoredTask != null) {
            System.out.println("Aufgabe erfolgreich wiederhergestellt: " + restoredTask.getTitle());
        } else {
            System.out.println("Ungültiger Index.");
        }
    }

}
