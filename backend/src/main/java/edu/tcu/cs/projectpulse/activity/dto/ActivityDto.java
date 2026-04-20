package edu.tcu.cs.projectpulse.activity.dto;

import edu.tcu.cs.projectpulse.activity.Activity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivityDto(
        Long id,
        String category,
        String description,
        BigDecimal hours,
        LocalDateTime submittedAt,
        Long studentId,
        String studentName,
        Long teamId,
        Long weekId,
        Integer weekNumber
) {
    public static ActivityDto from(Activity a) {
        return new ActivityDto(
                a.getId(),
                a.getCategory() != null ? a.getCategory().name() : null,
                a.getDescription(),
                a.getHours(),
                a.getSubmittedAt(),
                a.getStudent() != null ? a.getStudent().getId() : null,
                a.getStudent() != null ? a.getStudent().getFullName() : null,
                a.getTeam() != null ? a.getTeam().getId() : null,
                a.getWeek() != null ? a.getWeek().getId() : null,
                a.getWeek() != null ? a.getWeek().getWeekNumber() : null
        );
    }
}
