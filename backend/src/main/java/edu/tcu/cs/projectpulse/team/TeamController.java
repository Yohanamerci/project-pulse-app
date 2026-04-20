package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.shared.Result;
import edu.tcu.cs.projectpulse.team.dto.TeamDto;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.user.User;
import edu.tcu.cs.projectpulse.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    public TeamController(TeamService teamService, UserRepository userRepository) {
        this.teamService = teamService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public Result<List<TeamDto>> getAllTeams() {
        return Result.success(teamService.findAll());
    }

    @GetMapping("/{id}")
    public Result<TeamDto> getTeamById(@PathVariable Long id) {
        return Result.success(teamService.findById(id));
    }

    @GetMapping("/section/{sectionId}")
    public Result<List<TeamDto>> getTeamsBySection(@PathVariable Long sectionId) {
        return Result.success(teamService.findBySection(sectionId));
    }

    @GetMapping("/my-team")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<TeamDto> getMyTeam(@AuthenticationPrincipal Jwt jwt) {
        User student = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow();
        return Result.success(teamService.findByStudent(student.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDto> createTeam(@Valid @RequestBody TeamRequest req) {
        return Result.success("Team created", teamService.create(req));
    }

    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDto> addStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return Result.success("Student added to team", teamService.addStudent(id, studentId));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDto> removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return Result.success("Student removed from team", teamService.removeStudent(id, studentId));
    }

    @PutMapping("/{id}/instructor/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDto> assignInstructor(@PathVariable Long id, @PathVariable Long instructorId) {
        return Result.success("Instructor assigned", teamService.assignInstructor(id, instructorId));
    }

    @PutMapping("/{id}/rubric/{rubricId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDto> assignRubric(@PathVariable Long id, @PathVariable Long rubricId) {
        return Result.success("Rubric assigned to team", teamService.assignRubric(id, rubricId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTeam(@PathVariable Long id) {
        teamService.delete(id);
        return Result.success("Team deleted", null);
    }
}
