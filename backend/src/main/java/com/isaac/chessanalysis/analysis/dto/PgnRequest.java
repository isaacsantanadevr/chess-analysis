    package com.isaac.chessanalysis.analysis.dto;

public class PgnRequest {

    private String pgn;
    private int depth;

    public String getPgn(){
        return pgn;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setPgn(String pgn){
        this.pgn = pgn;
    }

}
