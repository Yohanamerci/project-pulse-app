package edu.tcu.cs.projectpulse.team.dto;

import edu.tcu.cs.projectpulse.team.Team;

import java.util.List;

public record TeamDto(
        Long id,
        String name,
        Long sectionId,
        String sectionName,
        Long rubricId,
        String rubricName,
        List<TeamMemberDto> students,
        List<TeamMemberDto> instructors
) {
    public static TeamDto from(Team t) {
        return new TeamDto(
                t.getId(),
                t.getName(),
                t.getSection() != null ? t.getSection().getId() : null,
                t.getSection() != null ? t.getSection().getName() : null,
                t.getRubric() != null ? t.getRubric().getId() : null,
                t.getRubric() != null ? t.getRubric().getName() : null,
                t.getStudents() == null
                        ? List.of()
                        : t.getStudents().stream().map(TeamMemberDto::from).toList(),
                t.getInstructors() == null
                        ? List.of()
                        : t.getInstructors().stream().map(TeamMemberDto::from).toList()
        );
    }
}
