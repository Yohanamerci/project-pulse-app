package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.team.Team;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    private Integer year;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActiveWeek> activeWeeks = new ArrayList<>();

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Rubric> rubrics = new ArrayList<>();

    public Section() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    public List<ActiveWeek> getActiveWeeks() { return activeWeeks; }
    public void setActiveWeeks(List<ActiveWeek> activeWeeks) { this.activeWeeks = activeWeeks; }

    public List<Rubric> getRubrics() { return rubrics; }
    public void setRubrics(List<Rubric> rubrics) { this.rubrics = rubrics; }
}
