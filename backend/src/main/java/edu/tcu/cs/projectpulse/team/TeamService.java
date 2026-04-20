package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.rubric.RubricRepository;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import edu.tcu.cs.projectpulse.team.dto.TeamDto;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.user.User;
import edu.tcu.cs.projectpulse.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;

    public TeamService(TeamRepository teamRepository,
                       UserRepository userRepository,
                       SectionRepository sectionRepository,
                       RubricRepository rubricRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamDto> findAll() {
        return teamRepository.findAll().stream().map(TeamDto::from).toList();
    }

    @Transactional(readOnly = true)
    public TeamDto findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        return TeamDto.from(team);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> findBySection(Long sectionId) {
        return teamRepository.findBySectionId(sectionId).stream().map(TeamDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TeamDto> findByInstructor(Long instructorId) {
        return teamRepository.findByInstructorsId(instructorId).stream().map(TeamDto::from).toList();
    }

    @Transactional(readOnly = true)
    public TeamDto findByStudent(Long studentId) {
        List<Team> teams = teamRepository.findByStudentsId(studentId);
        if (teams.isEmpty()) {
            throw new EntityNotFoundException("You are not assigned to a team");
        }
        return TeamDto.from(teams.get(0));
    }

    public TeamDto create(TeamRequest req) {
        Section section = sectionRepository.findById(req.sectionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Section not found with id: " + req.sectionId()));
        Team team = new Team();
        team.setName(req.name());
        team.setSection(section);

        if (req.rubricId() != null) {
            Rubric rubric = rubricRepository.findById(req.rubricId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Rubric not found with id: " + req.rubricId()));
            team.setRubric(rubric);
        }

        return TeamDto.from(teamRepository.save(team));
    }

    public TeamDto addStudent(Long teamId, Long studentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + studentId));
        team.getStudents().add(student);
        return TeamDto.from(teamRepository.save(team));
    }

    public TeamDto removeStudent(Long teamId, Long studentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));
        team.getStudents().removeIf(u -> u.getId().equals(studentId));
        return TeamDto.from(teamRepository.save(team));
    }

    public TeamDto assignInstructor(Long teamId, Long instructorId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + instructorId));
        boolean alreadyAssigned = team.getInstructors().stream()
                .anyMatch(u -> u.getId().equals(instructorId));
        if (!alreadyAssigned) {
            team.getInstructors().add(instructor);
        }
        return TeamDto.from(teamRepository.save(team));
    }

    public TeamDto assignRubric(Long teamId, Long rubricId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));
        Rubric rubric = rubricRepository.findById(rubricId)
                .orElseThrow(() -> new EntityNotFoundException("Rubric not found with id: " + rubricId));
        team.setRubric(rubric);
        return TeamDto.from(teamRepository.save(team));
    }

    public void delete(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        if (!team.getStudents().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete a team that still has students. Remove all members first.");
        }
        teamRepository.deleteById(id);
    }
}
