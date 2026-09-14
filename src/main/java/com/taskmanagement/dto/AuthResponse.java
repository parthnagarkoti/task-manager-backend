package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Returned by both /api/auth/login and /api/auth/register.
 * The client stores this token and sends it as:
 *   Authorization: Bearer <token>
 * on every subsequent request.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private String name;
}
