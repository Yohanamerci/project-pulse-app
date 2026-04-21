package edu.tcu.cs.projectpulse.evaluation;

import edu.tcu.cs.projectpulse.evaluation.dto.*;
import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.rubric.CriterionRepository;
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
public class EvaluationService {

    private final PeerEvaluationRepository peerEvaluationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final CriterionRepository criterionRepository;

    public EvaluationService(PeerEvaluationRepository peerEvaluationRepository,
                              UserRepository userRepository,
                              TeamRepository teamRepository,
                              ActiveWeekRepository activeWeekRepository,
                              CriterionRepository criterionRepository) {
        this.peerEvaluationRepository = peerEvaluationRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.criterionRepository = criterionRepository;
    }

    public EvaluationDto submit(String username, EvaluationRequest req) {
        User evaluator = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        ActiveWeek week = activeWeekRepository.findById(req.weekId())
                .orElseThrow(() -> new EntityNotFoundException("Week not found with id: " + req.weekId()));

        if (!week.isActive()) {
            throw new IllegalStateException("Submissions are only allowed during the active week.");
        }

        if (evaluator.getId().equals(req.evaluateeId())) {
            throw new IllegalArgumentException("Cannot evaluate yourself");
        }

        if (peerEvaluationRepository.existsByEvaluatorIdAndEvaluateeIdAndWeekId(
                evaluator.getId(), req.evaluateeId(), req.weekId())) {
            throw new IllegalStateException("Evaluation already submitted and cannot be edited.");
        }

        User evaluatee = userRepository.findById(req.evaluateeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + req.evaluateeId()));

        Team team = teamRepository.findById(req.teamId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Team not found with id: " + req.teamId()));

        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluatee(evaluatee);
        evaluation.setTeam(team);
        evaluation.setWeek(week);

        for (ScoreRequest sr : req.scores()) {
            Criterion criterion = criterionRepository.findById(sr.criterionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Criterion not found with id: " + sr.criterionId()));
            EvaluationScore es = new EvaluationScore();
            es.setCriterion(criterion);
            es.setScore(sr.score());
            es.setPublicComment(sr.publicComment());
            es.setPrivateComment(sr.privateComment());
            es.setEvaluation(evaluation);
            evaluation.getScores().add(es);
        }

        return EvaluationDto.from(peerEvaluationRepository.save(evaluation));
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> findMyEvaluations(String username) {
        User evaluator = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return peerEvaluationRepository.findByEvaluatorId(evaluator.getId())
                .stream().map(EvaluationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MyScoreDto> findMyScores(String username) {
        User evaluatee = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return peerEvaluationRepository.findByEvaluateeId(evaluatee.getId())
                .stream().map(MyScoreDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> findByTeamAndWeek(Long teamId, Long weekId) {
        return peerEvaluationRepository.findByTeamIdAndWeekId(teamId, weekId)
                .stream().map(EvaluationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public GradeDto calculateGrade(Long teamId, Long weekId, Long studentId) {
        User evaluatee = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + studentId));

        ActiveWeek week = activeWeekRepository.findById(weekId)
                .orElseThrow(() -> new EntityNotFoundException("Week not found with id: " + weekId));

        List<PeerEvaluation> evals = peerEvaluationRepository
                .findByEvaluateeIdAndWeekId(studentId, weekId);

        List<EvaluationDto> evalDtos = evals.stream().map(EvaluationDto::from).toList();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));

        int maxPossibleScore = 0;
        if (team.getRubric() != null && team.getRubric().getCriteria() != null) {
            maxPossibleScore = team.getRubric().getCriteria().stream()
                    .mapToInt(c -> c.getMaxScore() != null ? c.getMaxScore() : 0)
                    .sum();
        }

        double averageScore = evalDtos.isEmpty()
                ? 0.0
                : evalDtos.stream().mapToInt(EvaluationDto::totalScore).sum()
                  / (double) evalDtos.size();

        return new GradeDto(
                evaluatee.getId(),
                evaluatee.getFullName(),
                week.getId(),
                week.getWeekNumber(),
                averageScore,
                maxPossibleScore,
                evalDtos
        );
    }
}
