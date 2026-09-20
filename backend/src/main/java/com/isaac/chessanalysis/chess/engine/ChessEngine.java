package com.isaac.chessanalysis.chess.engine;

import java.io.IOException;

public interface ChessEngine {
    boolean isReady();

    StockfishResult analyzePosition(String fen, int depth) throws IOException;

    StockfishResult analyzeGame(String pgn, int depth) throws IOException;

}