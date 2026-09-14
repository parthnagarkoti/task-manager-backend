package com.taskmanagement.enums;

/**
 * Represents the lifecycle states of a Task.
 * Stored as a STRING in the database (not a number) so it's
 * human-readable in SQL queries and safe to reorder.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    CANCELLED
}
