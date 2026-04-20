package edu.tcu.cs.projectpulse.section.dto;

import edu.tcu.cs.projectpulse.section.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(
        @NotBlank String name,
        @NotNull Semester semester,
        @NotNull Integer year
) {}
