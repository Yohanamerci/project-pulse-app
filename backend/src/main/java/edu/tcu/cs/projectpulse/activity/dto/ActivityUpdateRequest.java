package edu.tcu.cs.projectpulse.activity.dto;

import edu.tcu.cs.projectpulse.activity.ActivityCategory;
import edu.tcu.cs.projectpulse.activity.ActivityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivityUpdateRequest(
        @NotBlank String activityName,
        @NotNull ActivityCategory category,
        @NotBlank String description,
        @NotNull @DecimalMin("0.5") Double plannedHours,
        Double actualHours,
        @NotNull ActivityStatus status
) {}
