package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.section.dto.*;
import edu.tcu.cs.projectpulse.system.NotificationService;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final NotificationService notificationService;

    public SectionService(SectionRepository sectionRepository,
                          ActiveWeekRepository activeWeekRepository,
                          NotificationService notificationService) {
        this.sectionRepository = sectionRepository;
        this.activeWeekRepository = activeWeekRepository;
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

    public ActiveWeekDto setActiveWeek(Long sectionId, ActiveWeekRequest req) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        int weekNum = req.weekNumber();
        Semester semester = section.getSemester();
        if (semester == Semester.FALL) {
            if (weekNum < 5 || weekNum > 15) {
                throw new IllegalStateException("Week number for FALL semester must be between 5 and 15.");
            }
        } else {
            if (weekNum < 1 || weekNum > 15) {
                throw new IllegalStateException("Week number for SPRING semester must be between 1 and 15.");
            }
        }

        // Deactivate all current active weeks for this section
        List<ActiveWeek> allWeeks = activeWeekRepository.findBySectionIdOrderByWeekNumber(sectionId);
        for (ActiveWeek w : allWeeks) {
            if (w.isActive()) {
                w.setActive(false);
                activeWeekRepository.save(w);
            }
        }

        // Find existing week for this section+weekNumber or create new
        ActiveWeek activeWeek = activeWeekRepository
                .findBySectionIdAndWeekNumber(sectionId, weekNum)
                .orElse(new ActiveWeek());

        activeWeek.setSection(section);
        activeWeek.setWeekNumber(weekNum);
        activeWeek.setStartDate(req.startDate());
        activeWeek.setEndDate(req.endDate());
        activeWeek.setActive(true);

        ActiveWeekDto result = ActiveWeekDto.from(activeWeekRepository.save(activeWeek));

        // Notify all students in this section (async — never blocks the response)
        List<String> studentEmails = section.getTeams().stream()
                .flatMap(team -> team.getStudents().stream())
                .map(User::getEmail)
                .distinct()
                .toList();
        notificationService.notifyActiveWeekOpened(
                section.getName(), weekNum, req.startDate(), req.endDate(), studentEmails);

        return result;
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
