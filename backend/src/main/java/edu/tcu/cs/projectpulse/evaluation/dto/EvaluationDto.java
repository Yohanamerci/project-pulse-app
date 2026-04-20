package edu.tcu.cs.projectpulse.evaluation.dto;

import edu.tcu.cs.projectpulse.evaluation.PeerEvaluation;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluationDto(
        Long id,
        Long evaluatorId,
        String evaluatorName,
        Long evaluateeId,
        String evaluateeName,
        Long teamId,
        Long weekId,
        Integer weekNumber,
        LocalDateTime submittedAt,
        List<ScoreDto> scores,
        int totalScore
) {
    public static EvaluationDto from(PeerEvaluation e) {
        List<ScoreDto> scoreDtos = e.getScores() == null
                ? List.of()
                : e.getScores().stream().map(ScoreDto::from).toList();
        int total = scoreDtos.stream().mapToInt(ScoreDto::score).sum();
        return new EvaluationDto(
                e.getId(),
                e.getEvaluator() != null ? e.getEvaluator().getId() : null,
                e.getEvaluator() != null ? e.getEvaluator().getFullName() : null,
                e.getEvaluatee() != null ? e.getEvaluatee().getId() : null,
                e.getEvaluatee() != null ? e.getEvaluatee().getFullName() : null,
                e.getTeam() != null ? e.getTeam().getId() : null,
                e.getWeek() != null ? e.getWeek().getId() : null,
                e.getWeek() != null ? e.getWeek().getWeekNumber() : null,
                e.getSubmittedAt(),
                scoreDtos,
                total
        );
    }
}
