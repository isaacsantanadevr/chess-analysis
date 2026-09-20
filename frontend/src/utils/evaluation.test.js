import { test } from "node:test";
import assert from "node:assert/strict";
import { describeEvaluation, formatEvaluation, evalToWhitePercent, formatMove } from "./evaluation.js";

test("centipawns mantem sinal e precisao", () => {
  assert.equal(formatEvaluation({ scoreType: "CP", centipawns: 125 }), "+1.25");
  assert.equal(formatEvaluation({ scoreType: "CP", centipawns: -1 }), "-0.01");
  assert.equal(formatEvaluation({ scoreType: "CP", centipawns: 0 }), "+0.00");
  assert.equal(formatEvaluation(null), "—");
  assert.equal(evalToWhitePercent(null), 50);
  assert.ok(evalToWhitePercent({ centipawns: 100 }) > 50);
  assert.ok(evalToWhitePercent({ centipawns: -100 }) < 50);
});

test("mate nao vira peoes e mate zero preserva o vencedor", () => {
  for (const [winner, side, percent] of [["WHITE", "brancas", 100], ["BLACK", "pretas", 0]]) {
    for (const distance of [0, 3]) {
      const result = { scoreType: "MATE", mate: winner === "WHITE" ? distance : -distance, mateWinner: winner };
      assert.equal(formatEvaluation(result), distance === 0 ? `Xeque-mate · Vitória das ${side}` : `Mate em ${distance} para as ${side}`);
      assert.equal(evalToWhitePercent(result), percent);
    }
  }
});

test("melhor lance usa a posicao correspondente e trata ausencia de lances", () => {
  const fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
  assert.equal(formatMove(fen, "e2e4"), "e4");
  assert.equal(formatMove(fen, "g1f3"), "Nf3");
  assert.equal(formatMove(fen, "0000"), "Sem lance legal");
  assert.equal(formatMove(fen, "(none)"), "Sem lance legal");
  assert.equal(formatMove(fen, null), "—");
});


test("traduz intensidade e cor sem inverter a perspectiva das brancas", () => {
  for (const [value, description] of [[0, "Posição equilibrada"], [49, "Posição equilibrada"], [50, "Pequena vantagem"], [149, "Pequena vantagem"], [150, "Vantagem"], [299, "Vantagem"], [300, "Grande vantagem"], [877, "Grande vantagem"]]) {
    for (const sign of [1, -1]) {
      const expected = value < 50 ? description : `${description} para as ${sign > 0 ? "brancas" : "pretas"}`;
      assert.equal(describeEvaluation({ scoreType: "CP", centipawns: value * sign }), expected);
    }
  }
  assert.equal(describeEvaluation(null), "Aguardando análise");
  assert.equal(describeEvaluation({ scoreType: "MATE", mate: 0 }), "Avaliação indisponível");
  assert.equal(describeEvaluation({ scoreType: "MATE", mate: -1 }), "Mate em 1 para as pretas");
});
