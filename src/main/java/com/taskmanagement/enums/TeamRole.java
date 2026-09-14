package com.taskmanagement.enums;

/**
 * Role of a User within a Team.
 * OWNER  — created the team, full control
 * MEMBER — can create and update tasks
 * VIEWER — read-only access
 */
public enum TeamRole {
    OWNER,
    MEMBER,
    VIEWER
}
