package com.todo.model;

import java.time.LocalDate;

public class Task {
    private String title;
    private String description;
    private String category;
    private int priority; // 1 = hoch, 2 = mittel, 3 = niedrig
    private LocalDate dueDate;

    // Konstruktor, Getter und Setter
    public Task(String title, String description, String category, int priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return String.format("Title: %s, Description: %s, Category: %s, Priority: %d, DueDate: %s",
                title, description, category, priority, dueDate);
    }

    // Getter und Setter...
}
