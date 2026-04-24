package edu.tcu.cs.projectpulse.section.dto;

import edu.tcu.cs.projectpulse.section.Section;

import java.util.List;

public record SectionDto(
        Long id,
        String name,
        String semester,
        Integer year,
        int teamCount,
        int activeWeekCount,
        List<String> teamNames      // UC-2: search results display includes team names
) {
    public static SectionDto from(Section s) {
        List<String> names = s.getTeams() == null
                ? List.of()
                : s.getTeams().stream().map(t -> t.getName()).toList();
        return new SectionDto(
                s.getId(),
                s.getName(),
                s.getSemester().name(),
                s.getYear(),
                names.size(),
                s.getActiveWeeks() == null ? 0 : s.getActiveWeeks().size(),
                names
        );
    }
}
