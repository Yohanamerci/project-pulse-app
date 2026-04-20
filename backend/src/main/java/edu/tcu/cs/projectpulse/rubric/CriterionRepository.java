package edu.tcu.cs.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CriterionRepository extends JpaRepository<Criterion, Long> {

    List<Criterion> findByRubricIdOrderByDisplayOrder(Long rubricId);
}
