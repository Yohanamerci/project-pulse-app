package edu.tcu.cs.projectpulse.activity;

import edu.tcu.cs.projectpulse.section.ActiveWeek;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActivityCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal hours;

    @Column(name = "submitted_at")
    @CreationTimestamp
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private ActiveWeek week;

    public Activity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActivityCategory getCategory() { return category; }
    public void setCategory(ActivityCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public ActiveWeek getWeek() { return week; }
    public void setWeek(ActiveWeek week) { this.week = week; }
}
