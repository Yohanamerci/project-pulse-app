package edu.tcu.cs.projectpulse.evaluation.dto;

import edu.tcu.cs.projectpulse.evaluation.EvaluationScore;

public record AnonymousScoreDto(
        Long criterionId,
        String criterionName,
        Integer score,
        Integer maxScore,
        String publicComment
) {
    public static AnonymousScoreDto from(EvaluationScore es) {
        return new AnonymousScoreDto(
                es.getCriterion().getId(),
                es.getCriterion().getName(),
                es.getScore(),
                es.getCriterion().getMaxScore(),
                es.getPublicComment()
        );
    }
}
