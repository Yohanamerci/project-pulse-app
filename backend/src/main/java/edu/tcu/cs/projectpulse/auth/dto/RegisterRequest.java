package edu.tcu.cs.projectpulse.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String token,
        @NotBlank String firstName,
        String middleInitial,          // optional — single character
        @NotBlank String lastName,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank String confirmPassword
) {}
