package edu.tcu.cs.projectpulse.evaluation.dto;

import edu.tcu.cs.projectpulse.evaluation.EvaluationScore;

public record ScoreDto(
        Long criterionId,
        String criterionName,
        Integer score,
        Integer maxScore
) {
    public static ScoreDto from(EvaluationScore es) {
        return new ScoreDto(
                es.getCriterion().getId(),
                es.getCriterion().getName(),
                es.getScore(),
                es.getCriterion().getMaxScore()
        );
    }
}
