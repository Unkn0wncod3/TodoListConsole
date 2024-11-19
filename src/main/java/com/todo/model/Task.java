package com.todo.model;

import java.time.LocalDate;
import com.todo.logging.Logger;

public class Task {
    private String title;
    private String description;
    private String category;
    private int priority;
    private LocalDate dueDate;

    public Task(String title, String description, String category, int priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        String taskString = String.format("Title: %s, Description: %s, Category: %s, Priority: %d, DueDate: %s",
                title, description, category, priority, dueDate);
        Logger.log("Task: toString aufgerufen für: " + taskString);
        return taskString;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
