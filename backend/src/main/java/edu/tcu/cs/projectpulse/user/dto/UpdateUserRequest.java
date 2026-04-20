package edu.tcu.cs.projectpulse.user.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        Boolean enabled
) {}
