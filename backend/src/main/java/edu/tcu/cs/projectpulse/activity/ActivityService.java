package edu.tcu.cs.projectpulse.activity;

import edu.tcu.cs.projectpulse.activity.dto.ActivityDto;
import edu.tcu.cs.projectpulse.activity.dto.ActivityRequest;
import edu.tcu.cs.projectpulse.activity.dto.ActivityUpdateRequest;
import edu.tcu.cs.projectpulse.section.ActiveWeek;
import edu.tcu.cs.projectpulse.section.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.team.TeamRepository;
import edu.tcu.cs.projectpulse.user.User;
import edu.tcu.cs.projectpulse.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ActiveWeekRepository activeWeekRepository;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           TeamRepository teamRepository,
                           ActiveWeekRepository activeWeekRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.activeWeekRepository = activeWeekRepository;
    }

    public ActivityDto submit(String username, ActivityRequest req) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        ActiveWeek week = activeWeekRepository.findById(req.weekId())
                .orElseThrow(() -> new EntityNotFoundException("Week not found with id: " + req.weekId()));

        if (!week.isActive()) {
            throw new IllegalStateException("Submissions are only allowed during the active week.");
        }

        Team team = teamRepository.findById(req.teamId())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + req.teamId()));

        Activity activity = new Activity();
        activity.setStudent(student);
        activity.setTeam(team);
        activity.setWeek(week);
        activity.setActivityName(req.activityName());
        activity.setCategory(req.category());
        activity.setDescription(req.description());
        activity.setPlannedHours(req.plannedHours());
        activity.setActualHours(req.actualHours());
        activity.setStatus(req.status());

        return ActivityDto.from(activityRepository.save(activity));
    }

    public ActivityDto updateActivity(Long id, String username, ActivityUpdateRequest req) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));

        if (!activity.getStudent().getUsername().equals(username)) {
            throw new IllegalStateException("You can only edit your own activities.");
        }

        // Apply provided fields; fall back to the persisted value when blank/zero
        // (guards against legacy seed data that never had activityName / plannedHours)
        String newName = req.activityName();
        activity.setActivityName((newName != null && !newName.isBlank())
                ? newName : activity.getActivityName());

        activity.setCategory(req.category());

        String newDesc = req.description();
        activity.setDescription((newDesc != null && !newDesc.isBlank())
                ? newDesc : activity.getDescription());

        Double newHours = req.plannedHours();
        activity.setPlannedHours((newHours != null && newHours >= 0.5)
                ? newHours : activity.getPlannedHours());

        activity.setActualHours(req.actualHours());
        activity.setStatus(req.status());

        return ActivityDto.from(activityRepository.save(activity));
    }

    public void deleteActivity(Long id, String username) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));

        if (!activity.getStudent().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own activities.");
        }

        activityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> findMyActivities(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return activityRepository.findByStudentId(student.getId())
                .stream().map(ActivityDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> findByTeam(Long teamId) {
        return activityRepository.findByTeamId(teamId)
                .stream().map(ActivityDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> findByTeamAndWeek(Long teamId, Long weekId) {
        return activityRepository.findByTeamIdAndWeekId(teamId, weekId)
                .stream().map(ActivityDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> findByStudent(Long studentId) {
        return activityRepository.findByStudentId(studentId)
                .stream().map(ActivityDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> findAll() {
        return activityRepository.findAll().stream().map(ActivityDto::from).toList();
    }
}
