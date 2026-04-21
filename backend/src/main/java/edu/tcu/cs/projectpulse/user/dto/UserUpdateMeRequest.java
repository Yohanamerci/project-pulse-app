package edu.tcu.cs.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateMeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {}
