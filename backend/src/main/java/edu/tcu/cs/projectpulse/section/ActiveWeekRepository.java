package edu.tcu.cs.projectpulse.section;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {

    Optional<ActiveWeek> findBySectionIdAndActive(Long sectionId, boolean active);

    List<ActiveWeek> findBySectionIdOrderByWeekNumber(Long sectionId);

    Optional<ActiveWeek> findBySectionIdAndWeekNumber(Long sectionId, Integer weekNumber);

    boolean existsBySectionIdAndStartDateAndWeekNumberNot(Long sectionId, LocalDate startDate, Integer weekNumber);
}
