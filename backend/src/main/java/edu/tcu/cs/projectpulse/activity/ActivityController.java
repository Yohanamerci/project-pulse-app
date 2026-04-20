package edu.tcu.cs.projectpulse.activity;

import edu.tcu.cs.projectpulse.activity.dto.ActivityDto;
import edu.tcu.cs.projectpulse.activity.dto.ActivityRequest;
import edu.tcu.cs.projectpulse.shared.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ActivityDto> submitActivity(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody ActivityRequest req) {
        return Result.success("Activity submitted", activityService.submit(jwt.getSubject(), req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<ActivityDto>> getMyActivities(@AuthenticationPrincipal Jwt jwt) {
        return Result.success(activityService.findMyActivities(jwt.getSubject()));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<List<ActivityDto>> getActivitiesByTeam(@PathVariable Long teamId) {
        return Result.success(activityService.findByTeam(teamId));
    }

    @GetMapping("/team/{teamId}/week/{weekId}")
    public Result<List<ActivityDto>> getActivitiesByTeamAndWeek(@PathVariable Long teamId,
                                                                @PathVariable Long weekId) {
        return Result.success(activityService.findByTeamAndWeek(teamId, weekId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ActivityDto>> getAllActivities() {
        return Result.success(activityService.findAll());
    }
}
