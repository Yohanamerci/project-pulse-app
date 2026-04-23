package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.section.dto.*;
import edu.tcu.cs.projectpulse.system.NotificationService;
import edu.tcu.cs.projectpulse.team.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    public SectionService(SectionRepository sectionRepository,
                          ActiveWeekRepository activeWeekRepository,
                          TeamRepository teamRepository,
                          NotificationService notificationService) {
        this.sectionRepository = sectionRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<SectionDto> findAll() {
        return sectionRepository.findAll().stream().map(SectionDto::from).toList();
    }

    @Transactional(readOnly = true)
    public SectionDto findById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        return SectionDto.from(section);
    }

    public SectionDto create(SectionRequest req) {
        Section section = new Section();
        section.setName(req.name());
        section.setSemester(req.semester());
        section.setYear(req.year());
        return SectionDto.from(sectionRepository.save(section));
    }

    public SectionDto update(Long id, SectionRequest req) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        section.setName(req.name());
        section.setSemester(req.semester());
        section.setYear(req.year());
        return SectionDto.from(sectionRepository.save(section));
    }

    /** Save (create or update) a week's dates without changing its active status. */
    public ActiveWeekDto saveWeek(Long sectionId, ActiveWeekRequest req) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        int weekNum = req.weekNumber();
        validateWeekRange(section.getSemester(), weekNum);

        // Guard: no two weeks in the same section may share the same start date
        if (activeWeekRepository.existsBySectionIdAndStartDateAndWeekNumberNot(
                sectionId, req.startDate(), weekNum)) {
            throw new IllegalStateException(
                    "Another week in this section already starts on " + req.startDate() +
                    ". Each week must have a unique start date.");
        }

        // Upsert: find existing week or create new (do NOT change active flag)
        ActiveWeek week = activeWeekRepository
                .findBySectionIdAndWeekNumber(sectionId, weekNum)
                .orElse(new ActiveWeek());

        week.setSection(section);
        week.setWeekNumber(weekNum);
        week.setStartDate(req.startDate());
        week.setEndDate(req.endDate());
        // active flag is intentionally left unchanged (false for new rows, preserved for existing)

        return ActiveWeekDto.from(activeWeekRepository.save(week));
    }

    /** Activate an existing week (deactivates all others, sends student notifications). */
    public ActiveWeekDto activateWeek(Long sectionId, Integer weekNumber) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        ActiveWeek target = activeWeekRepository
                .findBySectionIdAndWeekNumber(sectionId, weekNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Week " + weekNumber + " not found for this section. Save it first."));

        // Collect notification data via a single JPQL query — avoids lazy-loading
        // the full Team→students object graph inside the write transaction
        String sectionName = section.getName();
        List<String> studentEmails = teamRepository.findStudentEmailsBySectionId(sectionId);
        LocalDate startDate = target.getStartDate();
        LocalDate endDate   = target.getEndDate();

        // Deactivate all currently active weeks for this section
        List<ActiveWeek> allWeeks = activeWeekRepository.findBySectionIdOrderByWeekNumber(sectionId);
        for (ActiveWeek w : allWeeks) {
            if (w.isActive()) {
                w.setActive(false);
                activeWeekRepository.save(w);
            }
        }

        target.setActive(true);
        ActiveWeekDto result = ActiveWeekDto.from(activeWeekRepository.save(target));

        // Notify students — runs async (@EnableAsync); any failure must never 500 the request
        try {
            notificationService.notifyActiveWeekOpened(
                    sectionName, weekNumber, startDate, endDate, studentEmails);
        } catch (Exception ignored) {
            // notification is best-effort; log already happens inside notifyActiveWeekOpened
        }

        return result;
    }

    private void validateWeekRange(Semester semester, int weekNum) {
        if (semester == Semester.FALL) {
            if (weekNum < 5 || weekNum > 15)
                throw new IllegalStateException("Week number for FALL semester must be between 5 and 15.");
        } else {
            if (weekNum < 1 || weekNum > 15)
                throw new IllegalStateException("Week number for SPRING semester must be between 1 and 15.");
        }
    }

    @Transactional(readOnly = true)
    public ActiveWeekDto getCurrentActiveWeek(Long sectionId) {
        ActiveWeek week = activeWeekRepository.findBySectionIdAndActive(sectionId, true)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active week found for section with id: " + sectionId));
        return ActiveWeekDto.from(week);
    }

    @Transactional(readOnly = true)
    public List<ActiveWeekDto> getActiveWeeks(Long sectionId) {
        return activeWeekRepository.findBySectionIdOrderByWeekNumber(sectionId)
                .stream().map(ActiveWeekDto::from).toList();
    }

    public void delete(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        if (!section.getTeams().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete a section that still has teams. Remove all teams first.");
        }
        sectionRepository.deleteById(id);
    }
}
