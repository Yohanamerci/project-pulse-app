package edu.tcu.cs.projectpulse.auth.dto;

public record LoginResponse(
        Long userId,
        String username,
        String role,
        String token
) {}
