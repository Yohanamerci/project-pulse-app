package edu.tcu.cs.projectpulse.section.dto;

import edu.tcu.cs.projectpulse.section.Section;

public record SectionDto(
        Long id,
        String name,
        String semester,
        Integer year,
        int teamCount,
        int activeWeekCount
) {
    public static SectionDto from(Section s) {
        return new SectionDto(
                s.getId(),
                s.getName(),
                s.getSemester().name(),
                s.getYear(),
                s.getTeams() == null ? 0 : s.getTeams().size(),
                s.getActiveWeeks() == null ? 0 : s.getActiveWeeks().size()
        );
    }
}
