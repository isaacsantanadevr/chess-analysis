package com.isaac.chessanalysis.analysis;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.isaac.chessanalysis.chess.engine.StockfishResult;

import static org.assertj.core.api.Assertions.*;

class MoveClassificationTests {
    private final MoveClassificationService service = new MoveClassificationService();

    @ParameterizedTest
    @CsvSource({"w,100,75,25", "b,75,100,25", "w,-100,-180,80", "b,-180,-100,80",
        "w,0,50,0", "b,50,0,0", "w,25,25,0", "b,25,25,0"})
    void calculatesLossForThePlayerWhoMoved(String side, int before, int after, int expected) {
        var ev = service.classify(1, "after", "a2a3", "before " + side,
                new StockfishResult("d2d4", before, List.of()), new StockfishResult("e7e5", after, List.of()));
        assertThat(ev.getCentipawnLoss()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"0,GOOD", "49,GOOD", "50,INACCURACY", "99,INACCURACY", "100,MISTAKE", "199,MISTAKE", "200,BLUNDER", "950,BLUNDER"})
    void classifiesThresholdsForBothColors(int loss, MoveClassification expected) {
        for (String side : List.of("w", "b")) {
            int after = side.equals("w") ? -loss : loss;
            var ev = service.classify(1, "after", "a2a3", "before " + side,
                    new StockfishResult("d2d4", 0, List.of()), new StockfishResult("e7e5", after, List.of()));
            assertThat(ev.getClassification()).isEqualTo(expected);
            assertThat(ev.getCentipawnLoss()).isEqualTo(loss);
        }
    }

    @Test
    void bestUsesTheChoiceBeforeTheMoveEvenIfSearchScoresChange() {
        var before = new StockfishResult("a7a8q", 100, List.of());
        var after = new StockfishResult("h8h7", 60, List.of());
        var ev = service.classify(1, "after", "a7a8q", "before w", before, after);
        assertThat(ev.getClassification()).isEqualTo(MoveClassification.BEST);
        assertThat(ev.getCentipawnLoss()).isEqualTo(40);
        assertThat(service.classify(1, "after", "h8h7", "before w", before, after).getClassification())
            .isEqualTo(MoveClassification.GOOD);
    }

    @Test
    void mateTransitionsNeverCreateCentipawnLoss() {
        for (String side : List.of("w", "b")) {
            String player = side.equals("w") ? "WHITE" : "BLACK";
            String opponent = side.equals("w") ? "BLACK" : "WHITE";
            var cp = new StockfishResult("best", 80, List.of());
            var winning = new StockfishResult("best", player.equals("WHITE") ? 3 : -3, player, List.of());
            var losing = new StockfishResult("best", opponent.equals("WHITE") ? 2 : -2, opponent, List.of());
            var checkmate = new StockfishResult("(none)", 0, player, List.of());
            StockfishResult[][] pairs = {{winning, cp}, {cp, losing}, {winning, losing},
                    {winning, winning}, {losing, losing}, {losing, cp}, {cp, winning}, {winning, checkmate}};
            for (int i = 0; i < pairs.length; i++) {
                var ev = service.classify(1, "after", "played", "before " + side, pairs[i][0], pairs[i][1]);
                assertThat(ev.getCentipawnLoss()).isNull();
                assertThat(ev.getClassification()).isEqualTo(i < 3 ? MoveClassification.BLUNDER : MoveClassification.GOOD);
            }
            assertThat(service.classify(1, "after", "best", "before " + side, winning, checkmate).getClassification())
                .isEqualTo(MoveClassification.BEST);
        }
    }
}
