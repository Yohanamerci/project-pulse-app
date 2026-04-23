package edu.tcu.cs.projectpulse.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findBySectionId(Long sectionId);

    List<Team> findByStudentsId(Long studentId);

    List<Team> findByInstructorsId(Long instructorId);

    /** Fetch student emails for all teams in a section without loading full entity graphs. */
    @Query("SELECT DISTINCT u.email FROM Team t JOIN t.students u WHERE t.section.id = :sectionId")
    List<String> findStudentEmailsBySectionId(@Param("sectionId") Long sectionId);
}
