package com.isaac.chessanalysis.chess.engine;

import java.util.List;

public class StockfishResult {
    private final String bestMov;
    private final ScoreType scoreType;
    private final Integer centipawns;
    private final Integer mate;
    private final String mateWinner;
    private final List<String> variation;

    public StockfishResult(String bestMov, int centipawns, List<String> variation) {
        this.bestMov = bestMov;
        this.scoreType = ScoreType.CP;
        this.centipawns = centipawns;
        this.mate = null;
        this.mateWinner = null;
        this.variation = variation;
    }

    public StockfishResult(String bestMov, int mate, String mateWinner, List<String> variation) {
        if (!"WHITE".equals(mateWinner) && !"BLACK".equals(mateWinner)) {
            throw new IllegalArgumentException("Invalid mate winner");
        }
        if ((mate > 0 && !"WHITE".equals(mateWinner)) || (mate < 0 && !"BLACK".equals(mateWinner))) {
            throw new IllegalArgumentException("Mate score must use White's perspective");
        }
        this.bestMov = bestMov;
        this.scoreType = ScoreType.MATE;
        this.centipawns = null;
        this.mate = mate;
        this.mateWinner = mateWinner; // Necessario para distinguir o vencedor em mate 0
        this.variation = variation;
    }

    public String getBestMov() {
        return bestMov;
    }

    public Double getEvaluation() {
        return centipawns == null ? null : centipawns / 100.0;
    }

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

    public List<String> getVariation() {
        return variation;
    }
    
}
