package com.isaac.chessanalysis.game.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "moves", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "ply"}))
public class MoveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(nullable = false)
    private int ply;

    @Column(nullable = false, length = 5)
    private String uci;

    @Column(nullable = false, columnDefinition = "text")
    private String fen;

    protected MoveEntity() {}

    public MoveEntity(GameEntity game, int ply, String uci, String fen) {
        this.game = game;
        this.ply = ply;
        this.uci = uci;
        this.fen = fen;
    }

    public Long getId() {
        return id;
    }

    public GameEntity getGame() {
        return game;
    }

    public int getPly() {
        return ply;
    }

    public String getUci() {
        return uci;
    }

    public String getFen() {
        return fen;
    }

    
}
