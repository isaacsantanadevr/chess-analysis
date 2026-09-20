package com.isaac.chessanalysis.analysis.entity;

import com.isaac.chessanalysis.game.entity.MoveEntity;
import com.isaac.chessanalysis.chess.engine.StockfishResult;
import com.isaac.chessanalysis.chess.engine.ScoreType;
import com.isaac.chessanalysis.analysis.MoveClassification;
import com.isaac.chessanalysis.analysis.dto.PositionEvaluation;
import jakarta.persistence.*;

@Entity
@Table(name = "move_analyses", uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "move_id"}))
public class MoveAnalysisEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "move_id", nullable = false)
    private MoveEntity move;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private AnalysisJobEntity job;

    @Column(name = "best_mov", nullable = false, columnDefinition = "text")
    private String bestMov;
    @Column
    private Double evaluation;
    @Column(nullable = false, columnDefinition = "text")
    private String variation;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", length = 8)
    private ScoreType scoreType;
    private Integer centipawns;
    private Integer mate;
    @Column(name = "mate_winner", length = 5)
    private String mateWinner;

    // Avaliacao antes do lance, tambem na perspectiva das brancas
    @Enumerated(EnumType.STRING)
    @Column(name = "before_score_type", length = 8)
    private ScoreType beforeScoreType;
    @Column(name = "before_centipawns")
    private Integer beforeCentipawns;
    @Column(name = "before_mate")
    private Integer beforeMate;
    @Column(name = "before_mate_winner", length = 5)
    private String beforeMateWinner;
    @Column(name = "best_move_before", columnDefinition = "text")
    private String bestMoveBefore;
    @Column(name = "centipawn_loss")
    private Integer centipawnLoss;
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private MoveClassification classification;

    protected MoveAnalysisEntity() {}

    public MoveAnalysisEntity(MoveEntity move, AnalysisJobEntity job, PositionEvaluation ev) {
        if (!move.getGame().getId().equals(job.getGame().getId())) {
            throw new IllegalArgumentException("Move and analysis job must belong to the same game");
        }
        StockfishResult result = ev.getResult();
        StockfishResult before = ev.getBeforeResult();
        this.scoreType = result.getScoreType();
        this.centipawns = result.getCentipawns();
        this.mate = result.getMate();
        this.mateWinner = result.getMateWinner();
        this.beforeScoreType = before.getScoreType();
        this.beforeCentipawns = before.getCentipawns();
        this.beforeMate = before.getMate();
        this.beforeMateWinner = before.getMateWinner();
        this.bestMoveBefore = before.getBestMov();
        this.centipawnLoss = ev.getCentipawnLoss();
        this.classification = ev.getClassification();
        this.move = move;
        this.job = job;
        this.bestMov = result.getBestMov();
        this.evaluation = result.getEvaluation();
        this.variation = String.join(" ", result.getVariation());
    }

    public Long getId() { return id; }
    public MoveEntity getMove() { return move; }
    public AnalysisJobEntity getJob() { return job; }
    public String getBestMov() { return bestMov; }
    public Double getEvaluation() { return evaluation; }
    public String getVariation() { return variation; }

    public ScoreType getScoreType() {
        return scoreType;
    }

    public Integer getCentipawns() {
        return centipawns;
    }

    public Integer getMate() {
        return mate;
    }

    public String getMateWinner() {
        return mateWinner;
    }

    public ScoreType getBeforeScoreType() {
        return beforeScoreType;
    }

    public Integer getBeforeCentipawns() {
        return beforeCentipawns;
    }

    public Integer getBeforeMate() {
        return beforeMate;
    }

    public String getBeforeMateWinner() {
        return beforeMateWinner;
    }

    public String getBestMoveBefore() {
        return bestMoveBefore;
    }

    public Integer getCentipawnLoss() {
        return centipawnLoss;
    }

    public MoveClassification getClassification() {
        return classification;
    }
}
