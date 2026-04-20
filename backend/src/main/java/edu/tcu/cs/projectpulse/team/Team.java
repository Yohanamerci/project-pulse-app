package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.user.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    @ManyToMany
    @JoinTable(
            name = "team_students",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "team_instructors",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private List<User> instructors = new ArrayList<>();

    public Team() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public Rubric getRubric() { return rubric; }
    public void setRubric(Rubric rubric) { this.rubric = rubric; }

    public List<User> getStudents() { return students; }
    public void setStudents(List<User> students) { this.students = students; }

    public List<User> getInstructors() { return instructors; }
    public void setInstructors(List<User> instructors) { this.instructors = instructors; }
}
