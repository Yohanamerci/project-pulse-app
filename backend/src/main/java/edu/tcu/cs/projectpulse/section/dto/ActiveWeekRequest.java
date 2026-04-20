package edu.tcu.cs.projectpulse.section.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActiveWeekRequest(
        @NotNull Integer weekNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
