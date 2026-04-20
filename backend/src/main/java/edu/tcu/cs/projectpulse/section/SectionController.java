package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.section.dto.*;
import edu.tcu.cs.projectpulse.shared.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public Result<List<SectionDto>> getAllSections() {
        return Result.success(sectionService.findAll());
    }

    @GetMapping("/{id}")
    public Result<SectionDto> getSectionById(@PathVariable Long id) {
        return Result.success(sectionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SectionDto> createSection(@Valid @RequestBody SectionRequest req) {
        return Result.success("Section created", sectionService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SectionDto> updateSection(@PathVariable Long id,
                                            @Valid @RequestBody SectionRequest req) {
        return Result.success("Section updated", sectionService.update(id, req));
    }

    @PostMapping("/{id}/active-week")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ActiveWeekDto> setActiveWeek(@PathVariable Long id,
                                               @Valid @RequestBody ActiveWeekRequest req) {
        return Result.success("Active week set", sectionService.setActiveWeek(id, req));
    }

    @GetMapping("/{id}/active-week")
    public Result<ActiveWeekDto> getCurrentActiveWeek(@PathVariable Long id) {
        return Result.success(sectionService.getCurrentActiveWeek(id));
    }

    @GetMapping("/{id}/active-weeks")
    public Result<List<ActiveWeekDto>> getActiveWeeks(@PathVariable Long id) {
        return Result.success(sectionService.getActiveWeeks(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSection(@PathVariable Long id) {
        sectionService.delete(id);
        return Result.success("Section deleted", null);
    }
}
