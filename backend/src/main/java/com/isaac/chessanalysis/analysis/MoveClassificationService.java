package com.isaac.chessanalysis.analysis;

import org.springframework.stereotype.Service;

import com.isaac.chessanalysis.analysis.dto.PositionEvaluation;
import com.isaac.chessanalysis.chess.engine.ScoreType;
import com.isaac.chessanalysis.chess.engine.StockfishResult;

@Service
public class MoveClassificationService {

    public PositionEvaluation classify(int ply, String fen, String playedMove, String beforeFen,
            StockfishResult before, StockfishResult after) {
        
        // De acordo com o FEN anterior, define quem fez o melhor lance
        boolean white = beforeFen.split("\\s+")[1].equals("w");
        Integer loss = null;
        MoveClassification classification;

        // Antes e depois se calcula
        if (before.getScoreType() == ScoreType.CP && after.getScoreType() == ScoreType.CP) {
            int difference = before.getCentipawns() - after.getCentipawns();
            loss = Math.max(0, white ? difference : -difference); // Perda para quem fez o lance
        }

        // Calculada a partir dos centipaws perdidos
        if (playedMove.equals(before.getBestMov())) { // Define a partir do movimento, quao preciso ele foi
            classification = MoveClassification.BEST; // Melhor movimento
        } else if (loss != null) { 
            if (loss < 50) {
                classification = MoveClassification.GOOD; // Bom
            } else if (loss < 100) {
                classification = MoveClassification.INACCURACY; // Imprecisao
            } else if (loss < 200) {
                classification = MoveClassification.MISTAKE; // Erro
            } else {
                classification = MoveClassification.BLUNDER; // Capivarada 
            }
        // Caso nao avalie de acordo com centipaws, trata mate
        } else {
            String player = white ? "WHITE" : "BLACK";

            // Perdeu o mate
            boolean lostMate = player.equals(before.getMateWinner()) && !player.equals(after.getMateWinner());

            // Entregou mate forcado
            boolean allowedMate = after.getScoreType() == ScoreType.MATE && !player.equals(after.getMateWinner())
                    && (before.getScoreType() != ScoreType.MATE || player.equals(before.getMateWinner()));
            classification = lostMate || allowedMate ? MoveClassification.BLUNDER : MoveClassification.GOOD;
        }

        // Apos a analise do lance, retorna
        return new PositionEvaluation(ply, fen, after, before, playedMove, loss, classification);
    }
}
