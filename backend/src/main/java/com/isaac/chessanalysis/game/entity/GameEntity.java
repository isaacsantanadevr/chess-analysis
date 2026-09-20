package com.isaac.chessanalysis.game.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.isaac.chessanalysis.analysis.entity.AnalysisJobEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String pgn;

    @Column(name = "initial_fen", nullable = false, columnDefinition = "text")
    private String initialFen;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    @OrderBy("ply ASC")
    private List<MoveEntity> moves = new ArrayList<>();

    @OneToMany(mappedBy = "game")
    private List<AnalysisJobEntity> analysisJobs = new ArrayList<>();

    protected GameEntity() {}

    public GameEntity(String pgn, String initialFen) {
        this.pgn = pgn;
        this.initialFen = initialFen;
        this.createdAt = Instant.now();
    }

    public void addMove(int ply, String uci, String fen) {
        moves.add(new MoveEntity(this, ply, uci, fen));
    }

    public Long getId() {
        return id;
    }

    public String getPgn() {
        return pgn;
    }

    public String getInitialFen() {
        return initialFen;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<MoveEntity> getMoves() {
        return moves;
    }

    public List<AnalysisJobEntity> getAnalysisJobs() {
        return analysisJobs;
    }

    
}
