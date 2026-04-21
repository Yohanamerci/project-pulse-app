package edu.tcu.cs.projectpulse.evaluation.dto;

import edu.tcu.cs.projectpulse.evaluation.PeerEvaluation;

import java.time.LocalDateTime;
import java.util.List;

public record MyScoreDto(
        Long id,
        Long weekId,
        Integer weekNumber,
        LocalDateTime submittedAt,
        List<AnonymousScoreDto> scores,
        int totalScore
) {
    public static MyScoreDto from(PeerEvaluation e) {
        List<AnonymousScoreDto> scoreDtos = e.getScores() == null
                ? List.of()
                : e.getScores().stream().map(AnonymousScoreDto::from).toList();
        int total = scoreDtos.stream().mapToInt(AnonymousScoreDto::score).sum();
        return new MyScoreDto(
                e.getId(),
                e.getWeek() != null ? e.getWeek().getId() : null,
                e.getWeek() != null ? e.getWeek().getWeekNumber() : null,
                e.getSubmittedAt(),
                scoreDtos,
                total
        );
    }
}
