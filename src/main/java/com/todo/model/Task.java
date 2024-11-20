package com.todo.model;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private String title;
    private String description;
    private String category;
    private int priority;
    private LocalDate dueDate;
    private String recurrenceType;
    private boolean completed;

    public Task(String title, String description, String category, int priority, LocalDate dueDate,
            String recurrenceType, boolean completed) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.recurrenceType = recurrenceType;
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public LocalDate calculateNextDueDate() {
        if (recurrenceType == null)
            return dueDate;

        switch (recurrenceType.toLowerCase()) {
            case "daily":
                return dueDate.plusDays(1);
            case "weekly":
                return dueDate.plusWeeks(1);
            case "monthly":
                return dueDate.plusMonths(1);
            default:
                return dueDate.plusDays(1);
        }
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Task task = (Task) obj;
        return priority == task.priority &&
                title.equals(task.title) &&
                description.equals(task.description) &&
                category.equals(task.category) &&
                dueDate.equals(task.dueDate) &&
                (recurrenceType == null ? task.recurrenceType == null : recurrenceType.equals(task.recurrenceType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, category, priority, dueDate, recurrenceType);
    }

    @Override
    public String toString() {
        String recurrenceInfo = (recurrenceType != null) ? " (Wiederkehrend: " + recurrenceType + ")" : "";
        String status = completed ? "Erledigt" : "Offen";
        return String.format("Title: %s, Description: %s, Category: %s, Priority: %d, DueDate: %s%s, Status: %s",
                title, description, category, priority, dueDate, recurrenceInfo, status);
    }
}
