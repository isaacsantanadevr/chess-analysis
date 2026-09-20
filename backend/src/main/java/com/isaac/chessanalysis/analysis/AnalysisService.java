package com.isaac.chessanalysis.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.isaac.chessanalysis.chess.pgn.PgnService;
import com.isaac.chessanalysis.analysis.dto.PositionEvaluation;
import com.isaac.chessanalysis.chess.engine.ChessEngine;
import com.isaac.chessanalysis.chess.engine.StockfishResult;

@Service 
public class AnalysisService {
    private final ChessEngine ce;
    private final PgnService ps;
    private final AnalysisPersistenceService persistence;
    private final MoveClassificationService classification;

    public AnalysisService(ChessEngine ce, PgnService ps, AnalysisPersistenceService persistence,
            MoveClassificationService classification) {
        this.ce = ce;
        this.ps = ps;
        this.persistence = persistence;
        this.classification = classification;
    }

    //Analisa uma posicao
    public StockfishResult analyzePosition(String fen, int depth) throws IOException{
        return  ce.analyzePosition(fen, depth);
    }

    public List<PositionEvaluation> analyzeGame(String pgn, int depth) throws Exception{
        
        var parsed = ps.extractGame(pgn);
        List<String> pos = parsed.positions(); //Converte o PGN nas posicoes
        Long jobId = persistence.create(pgn, depth, parsed);
        List<PositionEvaluation> evs = new ArrayList<>(); //Armazena a analise de cada posicao

        try {
            persistence.start(jobId);
            for(int i = 0; i < pos.size(); i++){
                String fen = pos.get(i);

                StockfishResult sr = ce.analyzePosition(fen, depth); //Analisa a posicao
                if (i == 0) {
                    evs.add(new PositionEvaluation(i, fen, sr));
                } else {
                    StockfishResult before = evs.get(i - 1).getResult();
                    String playedMove = parsed.moves().get(i - 1).uci();
                    evs.add(classification.classify(i, fen, playedMove, pos.get(i - 1), before, sr));
                }
            }

            persistence.complete(jobId, evs);
        } catch (Exception failure) {
            try {
                persistence.fail(jobId, failure);
            } catch (Exception persistenceFailure) {
                failure.addSuppressed(persistenceFailure);
            }
            throw failure;
        }

        return evs;
    }
}
