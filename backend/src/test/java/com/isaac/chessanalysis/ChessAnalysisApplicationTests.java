package com.isaac.chessanalysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import com.isaac.chessanalysis.chess.engine.ChessEngine;
import com.isaac.chessanalysis.chess.engine.ScoreType;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class ChessAnalysisApplicationTests {

    @Autowired ChessEngine engine;

    @Test
    void readsCheckmateForBothColorsFromStockfish() throws Exception {
        var white = engine.analyzePosition("7k/6Q1/6K1/8/8/8/8/8 b - - 0 1", 2);
        assertThat(white.getScoreType()).isEqualTo(ScoreType.MATE);
        assertThat(white.getMate()).isZero();
        assertThat(white.getMateWinner()).isEqualTo("WHITE");
        assertThat(white.getEvaluation()).isNull();

        var black = engine.analyzePosition("8/8/8/8/8/6k1/6q1/7K w - - 0 1", 2);
        assertThat(black.getScoreType()).isEqualTo(ScoreType.MATE);
        assertThat(black.getMate()).isZero();
        assertThat(black.getMateWinner()).isEqualTo("BLACK");
    }

    @Test
    void readsStalemateAsZeroCentipawns() throws Exception {
        var result = engine.analyzePosition("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", 2);
        assertThat(result.getScoreType()).isEqualTo(ScoreType.CP);
        assertThat(result.getCentipawns()).isZero();
        assertThat(result.getMate()).isNull();
    }

	@Test
	void contextLoads() {
	}

}
