package com.isaac.chessanalysis.analysis.dto;

public class PositionAnalysisRequest {
    private String fen;
    private int depth;

    public String getFen() {
        return fen;
    }
    public void setFen(String fen) {
        this.fen = fen;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
}
    