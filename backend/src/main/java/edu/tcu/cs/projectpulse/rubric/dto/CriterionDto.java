package edu.tcu.cs.projectpulse.rubric.dto;

import edu.tcu.cs.projectpulse.rubric.Criterion;

public record CriterionDto(
        Long id,
        String name,
        String description,
        Integer maxScore,
        Integer displayOrder
) {
    public static CriterionDto from(Criterion c) {
        return new CriterionDto(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getMaxScore(),
                c.getDisplayOrder()
        );
    }
}
