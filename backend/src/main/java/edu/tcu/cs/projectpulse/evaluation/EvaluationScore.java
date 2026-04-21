package edu.tcu.cs.projectpulse.evaluation;

import edu.tcu.cs.projectpulse.rubric.Criterion;
import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_scores")
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer score;

    @Column(name = "public_comment", columnDefinition = "TEXT")
    private String publicComment;

    @Column(name = "private_comment", columnDefinition = "TEXT")
    private String privateComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private PeerEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id")
    private Criterion criterion;

    public EvaluationScore() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getPublicComment() { return publicComment; }
    public void setPublicComment(String publicComment) { this.publicComment = publicComment; }

    public String getPrivateComment() { return privateComment; }
    public void setPrivateComment(String privateComment) { this.privateComment = privateComment; }

    public PeerEvaluation getEvaluation() { return evaluation; }
    public void setEvaluation(PeerEvaluation evaluation) { this.evaluation = evaluation; }

    public Criterion getCriterion() { return criterion; }
    public void setCriterion(Criterion criterion) { this.criterion = criterion; }
}
