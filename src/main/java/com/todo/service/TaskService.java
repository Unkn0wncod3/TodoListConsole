package com.todo.service;

import com.todo.model.Task;
import com.todo.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.todo.logging.Logger;

public class TaskService {
    private List<Task> tasks = new ArrayList<>();
    private List<Task> archivedTasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
        Logger.log("TaskService: Aufgabe hinzugefügt: " + task);
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public void updateTask(int index, Task updatedTask) {
        if (index >= 0 && index < tasks.size()) {
            Task oldTask = tasks.get(index);
            tasks.set(index, updatedTask);
            Logger.log("TaskService: Aufgabe aktualisiert: ALT: " + oldTask + " NEU: " + updatedTask);
        }
    }

    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            Task removedTask = tasks.remove(index);
            Logger.log("TaskService: Aufgabe gelöscht: " + removedTask);
        }
    }

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
        Logger.log("TaskService: Aufgabenliste neu gesetzt. Anzahl Aufgaben: " + tasks.size());
    }

    public List<Task> searchByTitle(String title) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(title.toLowerCase())) {
                result.add(task);
            }
        }
        return result;
    }

    public List<Task> searchByDescription(String description) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().toLowerCase().contains(description.toLowerCase())) {
                result.add(task);
            }
        }
        return result;
    }

    public List<Task> filterByCategory(String category) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getCategory().equalsIgnoreCase(category)) {
                result.add(task);
            }
        }
        return result;
    }

    public List<Task> filterByPriority(int priority) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getPriority() == priority) {
                result.add(task);
            }
        }
        return result;
    }

    public long countCompletedTasksForCurrentWeek() {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        return tasks.stream()
                .filter(task -> task.isCompleted() &&
                        (task.getDueDate().isAfter(startOfWeek.minusDays(1)) &&
                                task.getDueDate().isBefore(endOfWeek.plusDays(1))))
                .count();
    }

    public long countCompletedTasksForCurrentMonth() {
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        return tasks.stream()
                .filter(task -> task.isCompleted() &&
                        (task.getDueDate().isAfter(startOfMonth.minusDays(1)) &&
                                task.getDueDate().isBefore(endOfMonth.plusDays(1))))
                .count();
    }

    public long countTotalCompletedTasks() {
        return tasks.stream().filter(Task::isCompleted).count();
    }

    public long countTotalPendingTasks() {
        return tasks.stream().filter(task -> !task.isCompleted()).count();
    }

    public Map<String, Long> countTasksByCategory() {
        return tasks.stream().collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
    }

    public Map<Integer, Long> countTasksByPriority() {
        return tasks.stream().collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    public Map<LocalDate, Long> countTasksByDueDate() {
        return tasks.stream().collect(Collectors.groupingBy(Task::getDueDate, Collectors.counting()));
    }

    public List<Task> filterByCompletionStatus(boolean isCompleted) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == isCompleted)
                .collect(Collectors.toList());
    }

    public List<Task> filterByRecurrenceType(String recurrenceType) {
        return tasks.stream()
                .filter(task -> recurrenceType.equalsIgnoreCase(task.getRecurrenceType()))
                .collect(Collectors.toList());
    }

    public Map<String, Long> countRecurringTasksByType() {
        return tasks.stream()
                .filter(task -> task.getRecurrenceType() != null)
                .collect(Collectors.groupingBy(Task::getRecurrenceType, Collectors.counting()));
    }

    public void updateRecurringTasks() {
        for (Task task : tasks) {
            if (!task.isCompleted() && task.getRecurrenceType() != null) {
                LocalDate nextDueDate = task.calculateNextDueDate();
                if (nextDueDate.isAfter(LocalDate.now())) {
                    task.setDueDate(nextDueDate);
                    Logger.log("TaskService: Fälligkeitsdatum aktualisiert für Aufgabe: " + task);
                }
            }
        }
    }

    public void generateRecurringTasks() {
        List<Task> newTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted() && task.getRecurrenceType() != null) {
                LocalDate nextDueDate = task.calculateNextDueDate();
                if (nextDueDate.isAfter(task.getDueDate())) {
                    Task newTask = new Task(
                            task.getTitle(),
                            task.getDescription(),
                            task.getCategory(),
                            task.getPriority(),
                            nextDueDate,
                            task.getRecurrenceType(),
                            false, null);
                    newTasks.add(newTask);
                }
            }
        }
        tasks.addAll(newTasks);
        Logger.log("TaskService: Wiederkehrende Aufgaben generiert. Neue Aufgaben: " + newTasks.size());
    }

    public long countOverdueTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted() && task.getDueDate().isBefore(LocalDate.now()))
                .count();
    }

    public List<Task> filterByTag(String tag) {
        return tasks.stream()
                .filter(task -> task.getTags() != null && task.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public void addTagToTask(int index, String tag) {
        if (index >= 0 && index < tasks.size()) {
            Task task = tasks.get(index);
            if (task.getTags() == null) {
                task.setTags(new ArrayList<>());
            }
            task.getTags().add(tag);
            Logger.log("Tag hinzugefügt: " + tag + " für Aufgabe: " + task);
        } else {
            throw new IndexOutOfBoundsException("Ungültiger Aufgaben-Index: " + index);
        }
    }

    public void removeTagFromTask(int index, String tag) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).removeTag(tag);
        }
    }

    public Task archiveTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            Task taskToArchive = tasks.remove(index);
            archivedTasks.add(taskToArchive);
            Logger.log("TaskService: Aufgabe archiviert: " + taskToArchive);
            return taskToArchive;
        }
        return null;
    }

    public List<Task> getArchivedTasks() {
        return archivedTasks;
    }

    public void setArchivedTasks(List<Task> archivedTasks) {
        this.archivedTasks.clear();
        this.archivedTasks.addAll(archivedTasks);
        Logger.log("TaskService: Archivierte Aufgaben gesetzt. Anzahl: " + archivedTasks.size());
    }

    public Task restoreTask(int index, FileStorage fileStorage) throws IOException {
        if (index >= 0 && index < archivedTasks.size()) {
            Task taskToRestore = archivedTasks.remove(index);
            tasks.add(taskToRestore);
            fileStorage.saveAllArchivedTasks(archivedTasks);
            Logger.log("TaskService: Aufgabe wiederhergestellt: " + taskToRestore);
            return taskToRestore;
        }
        return null;
    }

}
