package com.isaac.chessanalysis.analysis;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.isaac.chessanalysis.PostgresTestConfiguration;
import com.isaac.chessanalysis.analysis.entity.AnalysisJobEntity;
import com.isaac.chessanalysis.analysis.entity.AnalysisJobStatus;
import com.isaac.chessanalysis.analysis.repository.AnalysisJobRepository;
import com.isaac.chessanalysis.analysis.repository.MoveAnalysisRepository;
import com.isaac.chessanalysis.chess.engine.ChessEngine;
import com.isaac.chessanalysis.chess.engine.StockfishResult;
import com.isaac.chessanalysis.chess.pgn.PgnService;
import com.isaac.chessanalysis.controller.AnalysisController;
import com.isaac.chessanalysis.game.entity.MoveEntity;
import com.isaac.chessanalysis.game.repository.GameRepository;
import com.isaac.chessanalysis.game.repository.MoveRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class AnalysisPersistenceTests {
    private static final String PGN = "1. e4 e5 2. Nf3 Nc6 *";
    private static final StockfishResult RESULT = new StockfishResult("e2e4", -25, List.of("e2e4", "e7e5"));

    @Autowired AnalysisService service;
    @Autowired MoveClassificationService classification;
    @Autowired javax.sql.DataSource dataSource;
    @Autowired AnalysisPersistenceService persistence;
    @Autowired PgnService pgn;
    @Autowired AnalysisController controller;
    @Autowired GameRepository games;
    @Autowired MoveRepository moves;
    @Autowired AnalysisJobRepository jobs;
    @Autowired MoveAnalysisRepository analyses;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean ChessEngine engine;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE move_analyses, analysis_jobs, moves, games RESTART IDENTITY CASCADE");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @SuppressWarnings("null")
    @Test
    void migratesAndPersistsGameWithoutChangingHttpResponse() throws Exception {
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("2");
        flyway.validate();
        var positions = pgn.extractPos(PGN);
        AtomicInteger index = new AtomicInteger();
        when(engine.analyzePosition(anyString(), eq(10))).thenAnswer(call -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat(jobs.findAll()).singleElement().satisfies(job -> {
                assertThat(job.getStatus()).isEqualTo(AnalysisJobStatus.PROCESSING);
                assertThat(job.getStartedAt()).isNotNull();
            });
            assertThat(call.getArgument(0, String.class)).isEqualTo(positions.get(index.getAndIncrement()));
            return RESULT;
        });

        mvc.perform(post("/api/v1/analysis/game").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pgn\":\"" + PGN + "\",\"depth\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0].length()").value(7))
            .andExpect(jsonPath("$[0].ply").value(0))
            .andExpect(jsonPath("$[0].fen").value(positions.get(0)))
            .andExpect(jsonPath("$[0].result.length()").value(7))
            .andExpect(jsonPath("$[0].result.bestMov").value("e2e4"))
            .andExpect(jsonPath("$[0].result.evaluation").value(-0.25))
            .andExpect(jsonPath("$[0].result.centipawns").value(-25))
            .andExpect(jsonPath("$[0].result.scoreType").value("CP"))
            .andExpect(jsonPath("$[0].classification").isEmpty())
            .andExpect(jsonPath("$[1].classification").value("BEST"))
            .andExpect(jsonPath("$[1].playedMove").value("e2e4"))
            .andExpect(jsonPath("$[1].beforeResult.centipawns").value(-25))
            .andExpect(jsonPath("$[1].centipawnLoss").value(0))
            .andExpect(jsonPath("$[0].result.variation[1]").value("e7e5"))
            .andExpect(jsonPath("$[4].ply").value(4))
            .andExpect(jsonPath("$[4].fen").value(positions.get(4)));

        assertThat(index).hasValue(5);
        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            var game = games.findAll().getFirst();
            assertThat(game.getPgn()).isEqualTo(PGN);
            assertThat(game.getInitialFen()).isEqualTo(positions.getFirst());
            assertThat(game.getMoves()).extracting(MoveEntity::getPly).containsExactly(1, 2, 3, 4);
            assertThat(game.getMoves()).extracting(MoveEntity::getUci)
                .containsExactly("e2e4", "e7e5", "g1f3", "b8c6");
            assertThat(game.getMoves()).extracting(MoveEntity::getFen).containsExactlyElementsOf(positions.subList(1, 5));
            assertThat(game.getAnalysisJobs()).hasSize(1);
            var job = game.getAnalysisJobs().getFirst();
            assertThat(job.getStatus()).isEqualTo(AnalysisJobStatus.COMPLETED);
            assertThat(job.getDepth()).isEqualTo(10);
            assertThat(job.getFinishedAt()).isNotNull();
            assertThat(job.getErrorMessage()).isNull();
            assertThat(job.getInitialBestMov()).isEqualTo(RESULT.getBestMov());
            assertThat(job.getInitialEvaluation()).isEqualTo(RESULT.getEvaluation());
            assertThat(job.getInitialVariation()).isEqualTo("e2e4 e7e5");
            assertThat(job.getMoveAnalyses()).hasSize(4);
            assertThat(analyses.findByJobIdOrderByMovePlyAsc(job.getId())).allSatisfy(analysis -> {
                assertThat(analysis.getMove().getGame().getId()).isEqualTo(game.getId());
                assertThat(analysis.getJob().getId()).isEqualTo(job.getId());
                assertThat(analysis.getCentipawns()).isEqualTo(-25);
                assertThat(analysis.getBeforeCentipawns()).isEqualTo(-25);
                assertThat(analysis.getCentipawnLoss()).isZero();
                assertThat(analysis.getClassification()).isNotNull();
                assertThat(analysis.getBestMoveBefore()).isEqualTo("e2e4");
                assertThat(analysis.getBestMov()).isEqualTo(RESULT.getBestMov());
                assertThat(analysis.getEvaluation()).isEqualTo(RESULT.getEvaluation());
                assertThat(analysis.getVariation()).isEqualTo("e2e4 e7e5");
            });
        });
    }

    @Test
    void recordsFailureAndRethrowsOriginalEngineException() throws Exception {
        IOException failure = new IOException("Engine unavailable");
        when(engine.analyzePosition(anyString(), anyInt())).thenReturn(RESULT).thenThrow(failure);
        assertThatThrownBy(() -> service.analyzeGame(PGN, 10)).isSameAs(failure);
        assertThat(jobs.findAll()).singleElement().satisfies(job -> {
            assertThat(job.getStatus()).isEqualTo(AnalysisJobStatus.FAILED);
            assertThat(job.getErrorMessage()).contains("Engine unavailable");
            assertThat(job.getFinishedAt()).isNotNull();
            assertThat(job.getInitialEvaluation()).isNull();
        });
        assertThat(games.count()).isEqualTo(1);
        assertThat(moves.count()).isEqualTo(4);
        assertThat(analyses.count()).isZero();
    }

    @Test
    void rollsBackResultsWhenPersistenceFailsAndMarksJobFailed() throws Exception {
        when(engine.analyzePosition(anyString(), anyInt())).thenReturn(RESULT, RESULT, RESULT, RESULT,
                new StockfishResult(null, 0, List.of()));
        assertThatThrownBy(() -> service.analyzeGame(PGN, 10)).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(analyses.count()).isZero();
        assertThat(jobs.findAll()).singleElement().satisfies(job -> {
            assertThat(job.getStatus()).isEqualTo(AnalysisJobStatus.FAILED);
            assertThat(job.getInitialEvaluation()).isNull();
        });
    }

    @Test
    void supportsMultipleJobsForOneGameAndEnforcesConstraints() throws Exception {
        Long firstId = persistence.create(PGN, 10, pgn.extractGame(PGN));
        assertThat(jobs.findById(firstId).orElseThrow().getStatus()).isEqualTo(AnalysisJobStatus.PENDING);
        var game = games.findAll().getFirst();
        var second = jobs.save(new AnalysisJobEntity(game, 12));
        assertThat(jobs.findByGameIdOrderByIdAsc(game.getId())).hasSize(2);
        var positions = pgn.extractPos(PGN);
        var evaluations = java.util.stream.IntStream.range(0, positions.size())
            .mapToObj(i -> i == 0 ? new com.isaac.chessanalysis.analysis.dto.PositionEvaluation(i, positions.get(i), RESULT)
                : classification.classify(i, positions.get(i), pgnMove(i), positions.get(i - 1), RESULT, RESULT)).toList();
        persistence.start(firstId);
        persistence.complete(firstId, evaluations);
        persistence.start(second.getId());
        persistence.complete(second.getId(), evaluations);
        assertThat(analyses.count()).isEqualTo(8);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO moves(game_id, ply, uci, fen) VALUES (?, 1, 'e2e4', 'fen')", game.getId()))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO move_analyses(move_id, job_id, best_mov, evaluation, variation) SELECT move_id, job_id, best_mov, evaluation, variation FROM move_analyses LIMIT 1"))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO move_analyses(move_id, job_id, best_mov, evaluation, variation) VALUES (-1, ?, 'e2e4', 0, '')", firstId))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void invalidPgnDoesNotCreateRowsOrCallEngine() {
        assertThatThrownBy(() -> service.analyzeGame("", 10)).isInstanceOf(Exception.class);
        assertThat(games.count()).isZero();
        assertThat(jobs.count()).isZero();
        verifyNoInteractions(engine);
    }

    @Test
    void otherEndpointsKeepTheirResponsesAndDoNotCreateGames() throws Exception {
        when(engine.analyzePosition("fen", 7)).thenReturn(RESULT);
        mvc.perform(post("/api/v1/analysis/position").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fen\":\"fen\",\"depth\":7}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.bestMov").value("e2e4"));
        mvc.perform(post("/api/v1/analysis/pgn/positions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pgn\":\"" + PGN + "\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5));
        assertThat(games.count()).isZero();
        assertThat(jobs.count()).isZero();
        verify(engine).analyzePosition("fen", 7);
        verifyNoMoreInteractions(engine);
    }
    private String pgnMove(int ply) {
        return List.of("e2e4", "e7e5", "g1f3", "b8c6").get(ply - 1);
    }

    @Test
    void persistsMateWithoutInventingCentipawns() throws Exception {
        StockfishResult before = new StockfishResult("d2d4", 3, "WHITE", List.of("d2d4"));
        StockfishResult after = new StockfishResult("e7e5", -2, "BLACK", List.of("e7e5"));
        when(engine.analyzePosition(anyString(), anyInt())).thenReturn(before, after);
        var result = service.analyzeGame("1. e4 *", 10);
        assertThat(result.get(1).getClassification()).isEqualTo(MoveClassification.BLUNDER);
        assertThat(result.get(1).getCentipawnLoss()).isNull();
        var saved = analyses.findAll().getFirst();
        assertThat(saved.getMate()).isEqualTo(-2);
        assertThat(saved.getMateWinner()).isEqualTo("BLACK");
        assertThat(saved.getCentipawns()).isNull();
        assertThat(saved.getEvaluation()).isNull();
        assertThat(saved.getBeforeMate()).isEqualTo(3);
        assertThat(saved.getBeforeMateWinner()).isEqualTo("WHITE");
        assertThat(saved.getClassification()).isEqualTo(MoveClassification.BLUNDER);
        assertThat(saved.getCentipawnLoss()).isNull();
        var job = jobs.findAll().getFirst();
        assertThat(job.getInitialMate()).isEqualTo(3);
        assertThat(job.getInitialMateWinner()).isEqualTo("WHITE");
        assertThat(job.getInitialCentipawns()).isNull();
        assertThat(job.getInitialEvaluation()).isNull();
    }

    @Test
    void migratesExistingResultsWithoutInventingLostMateInformation() {
        Flyway old = Flyway.configure().dataSource(dataSource).schemas("legacy_scores").target("1").load();
        old.migrate();
        jdbc.update("INSERT INTO legacy_scores.games(id, pgn, initial_fen, created_at) VALUES (1, 'pgn', 'fen', now())");
        jdbc.update("INSERT INTO legacy_scores.moves(id, game_id, ply, uci, fen) VALUES (1, 1, 1, 'e2e4', 'fen')");
        jdbc.update("INSERT INTO legacy_scores.analysis_jobs(id, game_id, depth, status, created_at, initial_evaluation) VALUES (1, 1, 10, 'COMPLETED', now(), 0.25)");
        jdbc.update("INSERT INTO legacy_scores.move_analyses(move_id, job_id, best_mov, evaluation, variation) VALUES (1, 1, 'e7e5', -0.5, '')");
        Flyway updated = Flyway.configure().dataSource(dataSource).schemas("legacy_scores").load();
        updated.migrate();
        updated.validate();
        var saved = jdbc.queryForMap("SELECT * FROM legacy_scores.move_analyses");
        assertThat(saved.get("evaluation")).isEqualTo(-0.5);
        assertThat(saved.get("score_type")).isNull();
        assertThat(saved.get("centipawns")).isNull();
        assertThat(saved.get("classification")).isNull();
        assertThat(jdbc.queryForObject("SELECT initial_evaluation FROM legacy_scores.analysis_jobs", Double.class)).isEqualTo(0.25);
    }
}
