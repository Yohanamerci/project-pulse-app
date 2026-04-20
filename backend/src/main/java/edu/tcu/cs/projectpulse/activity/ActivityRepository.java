package edu.tcu.cs.projectpulse.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStudentId(Long studentId);

    List<Activity> findByTeamId(Long teamId);

    List<Activity> findByTeamIdAndWeekId(Long teamId, Long weekId);

    List<Activity> findByStudentIdAndWeekId(Long studentId, Long weekId);
}
