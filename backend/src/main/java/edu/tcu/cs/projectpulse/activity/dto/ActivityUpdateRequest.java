package edu.tcu.cs.projectpulse.activity.dto;

import edu.tcu.cs.projectpulse.activity.ActivityCategory;
import edu.tcu.cs.projectpulse.activity.ActivityStatus;
import jakarta.validation.constraints.NotNull;

public record ActivityUpdateRequest(
        String activityName,        // nullable — service falls back to existing value
        @NotNull ActivityCategory category,
        String description,         // nullable — service falls back to existing value
        Double plannedHours,        // nullable — service falls back to existing value
        Double actualHours,
        @NotNull ActivityStatus status
) {}
