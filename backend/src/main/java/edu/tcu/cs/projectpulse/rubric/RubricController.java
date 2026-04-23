package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.rubric.dto.*;
import edu.tcu.cs.projectpulse.shared.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<List<RubricDto>> getAllRubrics() {
        return Result.success(rubricService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<RubricDto> getRubricById(@PathVariable Long id) {
        return Result.success(rubricService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RubricDto> createRubric(@Valid @RequestBody RubricRequest req) {
        return Result.success("Rubric created", rubricService.create(req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRubric(@PathVariable Long id) {
        rubricService.delete(id);
        return Result.success("Rubric deleted", null);
    }

    @PostMapping("/{id}/criteria")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<CriterionDto> addCriterion(@PathVariable Long id,
                                             @Valid @RequestBody CriterionRequest req) {
        return Result.success("Criterion added", rubricService.addCriterion(id, req));
    }

    @PutMapping("/criteria/{criterionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<CriterionDto> updateCriterion(@PathVariable Long criterionId,
                                                @Valid @RequestBody CriterionRequest req) {
        return Result.success("Criterion updated", rubricService.updateCriterion(criterionId, req));
    }

    @DeleteMapping("/criteria/{criterionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<Void> deleteCriterion(@PathVariable Long criterionId) {
        rubricService.deleteCriterion(criterionId);
        return Result.success("Criterion deleted", null);
    }
}
