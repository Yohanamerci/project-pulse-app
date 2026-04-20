package edu.tcu.cs.projectpulse.rubric.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RubricRequest(
        @NotBlank String name,
        String description,
        Long sectionId,
        List<CriterionRequest> criteria
) {}
