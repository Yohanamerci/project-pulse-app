package edu.tcu.cs.projectpulse.rubric.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriterionRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(1) Integer maxScore,
        Integer displayOrder
) {}
