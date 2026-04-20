package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.rubric.dto.*;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RubricService {

    private final RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;
    private final SectionRepository sectionRepository;

    public RubricService(RubricRepository rubricRepository,
                         CriterionRepository criterionRepository,
                         SectionRepository sectionRepository) {
        this.rubricRepository = rubricRepository;
        this.criterionRepository = criterionRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public List<RubricDto> findAll() {
        return rubricRepository.findAll().stream().map(RubricDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RubricDto findById(Long id) {
        Rubric rubric = rubricRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rubric not found with id: " + id));
        return RubricDto.from(rubric);
    }

    public RubricDto create(RubricRequest req) {
        Rubric rubric = new Rubric();
        rubric.setName(req.name());
        rubric.setDescription(req.description());

        if (req.sectionId() != null) {
            Section section = sectionRepository.findById(req.sectionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Section not found with id: " + req.sectionId()));
            rubric.setSection(section);
        }

        if (req.criteria() != null) {
            for (CriterionRequest cr : req.criteria()) {
                Criterion criterion = new Criterion();
                criterion.setName(cr.name());
                criterion.setDescription(cr.description());
                criterion.setMaxScore(cr.maxScore());
                criterion.setDisplayOrder(cr.displayOrder() != null ? cr.displayOrder() : 0);
                criterion.setRubric(rubric);
                rubric.getCriteria().add(criterion);
            }
        }

        return RubricDto.from(rubricRepository.save(rubric));
    }

    public CriterionDto addCriterion(Long rubricId, CriterionRequest req) {
        Rubric rubric = rubricRepository.findById(rubricId)
                .orElseThrow(() -> new EntityNotFoundException("Rubric not found with id: " + rubricId));
        Criterion criterion = new Criterion();
        criterion.setName(req.name());
        criterion.setDescription(req.description());
        criterion.setMaxScore(req.maxScore());
        criterion.setDisplayOrder(req.displayOrder() != null ? req.displayOrder() : 0);
        criterion.setRubric(rubric);
        return CriterionDto.from(criterionRepository.save(criterion));
    }

    public CriterionDto updateCriterion(Long criterionId, CriterionRequest req) {
        Criterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Criterion not found with id: " + criterionId));
        criterion.setName(req.name());
        criterion.setDescription(req.description());
        criterion.setMaxScore(req.maxScore());
        if (req.displayOrder() != null) {
            criterion.setDisplayOrder(req.displayOrder());
        }
        return CriterionDto.from(criterionRepository.save(criterion));
    }

    public void deleteCriterion(Long criterionId) {
        if (!criterionRepository.existsById(criterionId)) {
            throw new EntityNotFoundException("Criterion not found with id: " + criterionId);
        }
        criterionRepository.deleteById(criterionId);
    }

    public void delete(Long rubricId) {
        if (!rubricRepository.existsById(rubricId)) {
            throw new EntityNotFoundException("Rubric not found with id: " + rubricId);
        }
        rubricRepository.deleteById(rubricId);
    }
}
