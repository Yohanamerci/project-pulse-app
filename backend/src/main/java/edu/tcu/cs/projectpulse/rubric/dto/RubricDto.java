package edu.tcu.cs.projectpulse.rubric.dto;

import edu.tcu.cs.projectpulse.rubric.Rubric;

import java.util.List;

public record RubricDto(
        Long id,
        String name,
        String description,
        Long sectionId,
        List<CriterionDto> criteria
) {
    public static RubricDto from(Rubric r) {
        return new RubricDto(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getSection() != null ? r.getSection().getId() : null,
                r.getCriteria() == null
                        ? List.of()
                        : r.getCriteria().stream().map(CriterionDto::from).toList()
        );
    }
}
