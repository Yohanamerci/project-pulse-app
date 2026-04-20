package edu.tcu.cs.projectpulse.user.dto;

import edu.tcu.cs.projectpulse.user.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role
) {}
