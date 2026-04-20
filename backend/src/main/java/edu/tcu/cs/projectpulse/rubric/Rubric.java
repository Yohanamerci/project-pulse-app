package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.section.Section;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rubrics")
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Criterion> criteria = new ArrayList<>();

    public Rubric() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public List<Criterion> getCriteria() { return criteria; }
    public void setCriteria(List<Criterion> criteria) { this.criteria = criteria; }
}
