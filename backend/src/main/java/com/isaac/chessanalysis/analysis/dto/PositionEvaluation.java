package com.isaac.chessanalysis.analysis.dto;

import com.isaac.chessanalysis.chess.engine.StockfishResult;
import com.isaac.chessanalysis.analysis.MoveClassification;

public class PositionEvaluation {

    private final int ply; // Numero do meio-lance
    private final String fen; // Posicao analisada
    private final StockfishResult result; // Resultado do Stockfish
    private final StockfishResult beforeResult;
    private final String playedMove;
    private final Integer centipawnLoss;
    private final MoveClassification classification;

    public PositionEvaluation(int ply, String fen, StockfishResult result) {
        this(ply, fen, result, null, null, null, null);
    }

    public PositionEvaluation(int ply, String fen, StockfishResult result, StockfishResult beforeResult,
            String playedMove, Integer centipawnLoss, MoveClassification classification) {
        this.ply = ply;
        this.fen = fen;
        this.result = result;
        this.beforeResult = beforeResult;
        this.playedMove = playedMove;
        this.centipawnLoss = centipawnLoss;
        this.classification = classification;
    }

    public int getPly() {
        return ply;
    }

    public String getFen() {
        return fen;
    }

    public StockfishResult getResult() {
        return result;
    }

    public StockfishResult getBeforeResult() {
        return beforeResult;
    }

    public String getPlayedMove() {
        return playedMove;
    }

    public Integer getCentipawnLoss() {
        return centipawnLoss;
    }

    public MoveClassification getClassification() {
        return classification;
    }
}
