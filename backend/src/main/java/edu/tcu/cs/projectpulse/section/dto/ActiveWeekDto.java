package edu.tcu.cs.projectpulse.section.dto;

import edu.tcu.cs.projectpulse.section.ActiveWeek;

import java.time.LocalDate;

public record ActiveWeekDto(
        Long id,
        Integer weekNumber,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        Long sectionId
) {
    public static ActiveWeekDto from(ActiveWeek aw) {
        return new ActiveWeekDto(
                aw.getId(),
                aw.getWeekNumber(),
                aw.getStartDate(),
                aw.getEndDate(),
                aw.isActive(),
                aw.getSection() != null ? aw.getSection().getId() : null
        );
    }
}
