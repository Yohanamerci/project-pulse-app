package edu.tcu.cs.projectpulse.evaluation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EvaluationRequest(
        @NotNull Long evaluateeId,
        @NotNull Long teamId,
        @NotNull Long weekId,
        @NotEmpty List<ScoreRequest> scores
) {}
