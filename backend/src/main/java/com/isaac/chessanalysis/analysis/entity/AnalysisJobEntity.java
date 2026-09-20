package com.isaac.chessanalysis.analysis.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.isaac.chessanalysis.game.entity.GameEntity;
import com.isaac.chessanalysis.chess.engine.StockfishResult;
import com.isaac.chessanalysis.chess.engine.ScoreType;
import jakarta.persistence.*;

// Classe responsavel por representar e salvar a execucao de analise de uma partida
@Entity
@Table(name = "analysis_jobs")
public class AnalysisJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Partida que sera analisada
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(nullable = false)
    private int depth; // Definicao do nivel de profundidade do stockfish

    // Situacao atual da analise
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AnalysisJobStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt; // Criacao da analise
    @Column(name = "started_at")
    private Instant startedAt; // Momento de execucao da analise
    @Column(name = "finished_at")
    private Instant finishedAt; // Fim da analise
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // Posicao inicial -> ANTES do primero lance
    @Column(name = "initial_best_mov", columnDefinition = "text")
    private String initialBestMov;
    @Column(name = "initial_evaluation")
    private Double initialEvaluation;
    @Column(name = "initial_variation", columnDefinition = "text")
    private String initialVariation;

    @Enumerated(EnumType.STRING)
    @Column(name = "initial_score_type", length = 8)
    private ScoreType initialScoreType;
    @Column(name = "initial_centipawns")
    private Integer initialCentipawns;
    @Column(name = "initial_mate")
    private Integer initialMate;
    @Column(name = "initial_mate_winner", length = 5)
    private String initialMateWinner;

    // Analise da execucao especifica
    @OneToMany(mappedBy = "job")
    private List<MoveAnalysisEntity> moveAnalyses = new ArrayList<>();

    protected AnalysisJobEntity() {}

    public AnalysisJobEntity(GameEntity game, int depth) {
        this.game = game;
        this.depth = depth;
        this.status = AnalysisJobStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // Define situacao da analise como iniciada
    public void start() {
        status = AnalysisJobStatus.PROCESSING;
        startedAt = Instant.now();
    }

    // Finalisa a analise -> Sem erro
    public void complete(StockfishResult initialResult) {
        initialScoreType = initialResult.getScoreType();
        initialCentipawns = initialResult.getCentipawns();
        initialMate = initialResult.getMate();
        initialMateWinner = initialResult.getMateWinner();
        initialBestMov = initialResult.getBestMov();
        initialEvaluation = initialResult.getEvaluation();
        initialVariation = String.join(" ", initialResult.getVariation());
        status = AnalysisJobStatus.COMPLETED; 
        finishedAt = Instant.now();
    }

    // Finaliza a analise -> Ouve errro
    public void fail(String message) {
        status = AnalysisJobStatus.FAILED;
        errorMessage = message;
        finishedAt = Instant.now();
    }

    // Getters e Setters abaixo  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public AnalysisJobStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisJobStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getInitialBestMov() {
        return initialBestMov;
    }

    public void setInitialBestMov(String initialBestMov) {
        this.initialBestMov = initialBestMov;
    }

    public Double getInitialEvaluation() {
        return initialEvaluation;
    }

    public void setInitialEvaluation(Double initialEvaluation) {
        this.initialEvaluation = initialEvaluation;
    }

    public String getInitialVariation() {
        return initialVariation;
    }

    public void setInitialVariation(String initialVariation) {
        this.initialVariation = initialVariation;
    }

    public List<MoveAnalysisEntity> getMoveAnalyses() {
        return moveAnalyses;
    }

    public void setMoveAnalyses(List<MoveAnalysisEntity> moveAnalyses) {
        this.moveAnalyses = moveAnalyses;
    }

    

    public ScoreType getInitialScoreType() {
        return initialScoreType;
    }

    public Integer getInitialCentipawns() {
        return initialCentipawns;
    }

    public Integer getInitialMate() {
        return initialMate;
    }

    public String getInitialMateWinner() {
        return initialMateWinner;
    }
}
