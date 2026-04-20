package edu.tcu.cs.projectpulse.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findBySectionId(Long sectionId);

    List<Team> findByStudentsId(Long studentId);

    List<Team> findByInstructorsId(Long instructorId);
}
