package com.isaac.chessanalysis.analysis;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isaac.chessanalysis.analysis.dto.PositionEvaluation;
import com.isaac.chessanalysis.analysis.entity.AnalysisJobEntity;
import com.isaac.chessanalysis.analysis.entity.MoveAnalysisEntity;
import com.isaac.chessanalysis.analysis.repository.AnalysisJobRepository;
import com.isaac.chessanalysis.analysis.repository.MoveAnalysisRepository;
import com.isaac.chessanalysis.chess.pgn.PgnService.ParsedGame;
import com.isaac.chessanalysis.game.entity.GameEntity;
import com.isaac.chessanalysis.game.repository.GameRepository;
import com.isaac.chessanalysis.game.repository.MoveRepository;

@Service
public class AnalysisPersistenceService {
    private final GameRepository games;
    private final MoveRepository moves;
    private final AnalysisJobRepository jobs;
    private final MoveAnalysisRepository analyses;

    public AnalysisPersistenceService(GameRepository games, MoveRepository moves,
            AnalysisJobRepository jobs, MoveAnalysisRepository analyses) {
        this.games = games;
        this.moves = moves;
        this.jobs = jobs;
        this.analyses = analyses;
    }

    // Vai salvar a partida e fazer uma nova solicitacao de analise
    @Transactional
    public Long create(String pgn, int depth, ParsedGame parsed) {
        GameEntity game = new GameEntity(pgn, parsed.initialFen());
        for (int i = 0; i < parsed.moves().size(); i++) { // Converter os mov. PGN -> Entidades persistentes
            var move = parsed.moves().get(i);
            game.addMove(i + 1, move.uci(), move.fen());
        }
        games.save(game);
        return jobs.save(new AnalysisJobEntity(game, depth)).getId();
    }

    // Marca a analise como iniciada
    @Transactional
    public void start(Long jobId) {
        jobs.findById(jobId).orElseThrow().start();
    }

    // Salva resultados e finalizada analise
    @Transactional
    public void complete(Long jobId, List<PositionEvaluation> evaluations) {
        AnalysisJobEntity job = jobs.findById(jobId).orElseThrow();
        var gameMoves = moves.findByGameIdOrderByPlyAsc(job.getGame().getId()); // Para ter os movimentos na ordem correta
        if (evaluations.size() != gameMoves.size() + 1) { // Avalia-se a posicao + avaliacao apos o movimento
            throw new IllegalArgumentException("Analysis does not cover every game position");
        }
        for (var move : gameMoves) { // Associacao: Movimento - Resultado da pos
            analyses.save(new MoveAnalysisEntity(move, job, evaluations.get(move.getPly())));
        }
        job.complete(evaluations.get(0).getResult());
    }

    @Transactional // Indica falha na analise
    public void fail(Long jobId, Exception failure) {
        jobs.findById(jobId).orElseThrow().fail(failure.toString());
    }

}
