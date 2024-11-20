package com.todo.service;

import com.todo.model.Task;
import java.util.ArrayList;
import java.util.List;
import com.todo.logging.Logger;

public class TaskService {
    private List<Task> tasks = new ArrayList<>();

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
}
