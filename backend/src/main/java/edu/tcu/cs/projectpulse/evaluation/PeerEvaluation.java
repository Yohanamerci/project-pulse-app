package edu.tcu.cs.projectpulse.evaluation;

import edu.tcu.cs.projectpulse.section.ActiveWeek;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peer_evaluations")
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submitted_at")
    @CreationTimestamp
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id")
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id")
    private User evaluatee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private ActiveWeek week;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationScore> scores = new ArrayList<>();

    public PeerEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getEvaluator() { return evaluator; }
    public void setEvaluator(User evaluator) { this.evaluator = evaluator; }

    public User getEvaluatee() { return evaluatee; }
    public void setEvaluatee(User evaluatee) { this.evaluatee = evaluatee; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public ActiveWeek getWeek() { return week; }
    public void setWeek(ActiveWeek week) { this.week = week; }

    public List<EvaluationScore> getScores() { return scores; }
    public void setScores(List<EvaluationScore> scores) { this.scores = scores; }
}
