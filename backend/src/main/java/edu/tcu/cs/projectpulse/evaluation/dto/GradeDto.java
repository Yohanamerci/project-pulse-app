package edu.tcu.cs.projectpulse.evaluation.dto;

import java.util.List;

public record GradeDto(
        Long studentId,
        String studentName,
        Long weekId,
        Integer weekNumber,
        double averageScore,
        int maxPossibleScore,
        List<EvaluationDto> evaluationsReceived
) {}
