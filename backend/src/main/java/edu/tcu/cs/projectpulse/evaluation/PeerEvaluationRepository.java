package edu.tcu.cs.projectpulse.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {

    List<PeerEvaluation> findByEvaluatorId(Long evaluatorId);

    List<PeerEvaluation> findByEvaluateeId(Long evaluateeId);

    List<PeerEvaluation> findByTeamIdAndWeekId(Long teamId, Long weekId);

    boolean existsByEvaluatorIdAndEvaluateeIdAndWeekId(Long evaluatorId, Long evaluateeId, Long weekId);

    List<PeerEvaluation> findByEvaluateeIdAndWeekId(Long evaluateeId, Long weekId);

    java.util.Optional<PeerEvaluation> findByEvaluatorIdAndEvaluateeIdAndWeekId(
            Long evaluatorId, Long evaluateeId, Long weekId);

    void deleteByEvaluatorIdOrEvaluateeId(Long evaluatorId, Long evaluateeId);
}
