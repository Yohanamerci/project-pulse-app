package edu.tcu.cs.projectpulse.activity.dto;

import edu.tcu.cs.projectpulse.activity.ActivityCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ActivityRequest(
        @NotNull Long teamId,
        @NotNull Long weekId,
        @NotNull ActivityCategory category,
        @NotBlank String description,
        @NotNull @DecimalMin("0.5") BigDecimal hours
) {}
