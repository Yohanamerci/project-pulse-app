package edu.tcu.cs.projectpulse.evaluation;

import edu.tcu.cs.projectpulse.evaluation.dto.*;
import edu.tcu.cs.projectpulse.shared.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<EvaluationDto> submitEvaluation(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody EvaluationRequest req) {
        return Result.success("Evaluation submitted",
                evaluationService.submit(jwt.getSubject(), req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<EvaluationDto>> getMyEvaluations(@AuthenticationPrincipal Jwt jwt) {
        return Result.success(evaluationService.findMyEvaluations(jwt.getSubject()));
    }

    @GetMapping("/my-scores")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<MyScoreDto>> getMyScores(@AuthenticationPrincipal Jwt jwt) {
        return Result.success(evaluationService.findMyScores(jwt.getSubject()));
    }

    @GetMapping("/team/{teamId}/week/{weekId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<List<EvaluationDto>> getEvaluationsByTeamAndWeek(@PathVariable Long teamId,
                                                                   @PathVariable Long weekId) {
        return Result.success(evaluationService.findByTeamAndWeek(teamId, weekId));
    }

    @GetMapping("/grade/team/{teamId}/week/{weekId}/student/{studentId}")
    public Result<GradeDto> calculateGrade(@PathVariable Long teamId,
                                           @PathVariable Long weekId,
                                           @PathVariable Long studentId) {
        return Result.success(evaluationService.calculateGrade(teamId, weekId, studentId));
    }

    @GetMapping("/grade/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<List<GradeDto>> getGradesByStudent(@PathVariable Long studentId) {
        return Result.success(evaluationService.findGradesByStudent(studentId));
    }
}
