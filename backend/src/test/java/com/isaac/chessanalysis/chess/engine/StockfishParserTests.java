package com.isaac.chessanalysis.chess.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class StockfishParserTests {
    private final StockfishParser parser = new StockfishParser();

    private StockfishResult read(String output, String side) throws IOException {
        String fen = "8/8/8/8/8/8/4K3/7k " + side + " - - 0 1";
        return parser.readResult(new BufferedReader(new StringReader(output)), fen);
    }

    @ParameterizedTest
    @CsvSource({"w,125,125", "b,125,-125", "w,-33,-33", "b,-33,33", "b,0,0", "w,1,1"})
    void normalizesCentipawns(String side, int score, int expected) throws Exception {
        StockfishResult sr = read("info depth 10 score cp " + score + " pv e2e4 e7e5\nbestmove e2e4\n", side);
        assertThat(sr.getScoreType()).isEqualTo(ScoreType.CP);
        assertThat(sr.getCentipawns()).isEqualTo(expected);
        assertThat(sr.getEvaluation()).isEqualTo(expected / 100.0);
        assertThat(sr.getMate()).isNull();
        assertThat(sr.getMateWinner()).isNull();
        assertThat(sr.getVariation()).containsExactly("e2e4", "e7e5");
    }

    @ParameterizedTest
    @CsvSource({"w,3,3,WHITE", "b,3,-3,BLACK", "w,-2,-2,BLACK", "b,-2,2,WHITE", "w,0,0,BLACK", "b,0,0,WHITE"})
    void normalizesMateIncludingCheckmate(String side, int score, int expected, String winner) throws Exception {
        StockfishResult sr = read("info depth 0 score mate " + score + "\nbestmove (none)\n", side);
        assertThat(sr.getScoreType()).isEqualTo(ScoreType.MATE);
        assertThat(sr.getMate()).isEqualTo(expected);
        assertThat(sr.getMateWinner()).isEqualTo(winner);
        assertThat(sr.getCentipawns()).isNull();
        assertThat(sr.getEvaluation()).isNull();
        assertThat(sr.getVariation()).isEmpty();
    }

    @Test
    void keepsLastExactPrincipalScoreDespiteTrailingInfo() throws Exception {
        StockfishResult sr = read("""
            info depth 9 score cp 30 pv e2e4
            info depth 10 multipv 1 score cp 45 pv d2d4 d7d5
            info depth 10 multipv 2 score cp 12 pv g1f3
            info depth 11 score cp 70 lowerbound pv e2e4
            info depth 11 score cp 35 upperbound pv e2e4
            info string score cp 9999
            info nodes 1000 nps 25000
            bestmove d2d4 ponder d7d5
            """, "w");
        assertThat(sr.getCentipawns()).isEqualTo(45);
        assertThat(sr.getBestMov()).isEqualTo("d2d4");
        assertThat(sr.getVariation()).containsExactly("d2d4", "d7d5");
    }

    @Test
    void rejectsMissingOrInvalidScoresInsteadOfReturningZero() {
        assertThatThrownBy(() -> read("info nodes 12\nbestmove e2e4\n", "w")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> read("info score cp 25 lowerbound\nbestmove e2e4\n", "w")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> read("info score cp abc\nbestmove e2e4\n", "w")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> read("info score cp 25\n", "w")).isInstanceOf(IOException.class);
    }
}
