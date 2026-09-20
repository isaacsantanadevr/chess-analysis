import { test } from "node:test";
import assert from "node:assert/strict";
import { CLASSIFICATIONS, explainMove } from "./moveReview.js";

const cp = { scoreType: "CP", centipawns: 0 };
const mate = (mateWinner, distance = 1) => ({ scoreType: "MATE", mateWinner, mate: mateWinner === "WHITE" ? distance : -distance });

test("explica mate permitido e perdido para ambos os jogadores", () => {
  for (const [ply, player, opponent] of [[1, "WHITE", "BLACK"], [2, "BLACK", "WHITE"]]) {
    const review = { ply, classification: "BLUNDER" };
    assert.match(explainMove({ ...review, beforeResult: cp, result: mate(opponent) }), /permitiu um mate/);
    assert.match(explainMove({ ...review, beforeResult: mate(player), result: cp }), /perdeu uma sequência/);
    assert.match(explainMove({ ...review, beforeResult: mate(opponent), result: mate(opponent) }), /continua disponível/);
    assert.doesNotMatch(explainMove({ ...review, result: mate(opponent) }), /permitiu/);
    assert.doesNotMatch(explainMove({ ...review, beforeResult: mate(player) }), /perdeu/);
    assert.match(explainMove({ ...review, beforeResult: mate(player), result: mate(player, 0) }), /Xeque-mate/);
  }
});

test("mantém os enums e oferece rótulos e explicações em português", () => {
  assert.deepEqual(Object.values(CLASSIFICATIONS).map(({ label }) => label), ["Melhor", "Bom", "Imprecisão", "Erro", "Erro grave"]);
  for (const [classification, { description }] of Object.entries(CLASSIFICATIONS)) {
    assert.equal(explainMove({ ply: 1, classification, beforeResult: cp, result: cp }), description);
  }
  assert.equal(explainMove(null), "");
  assert.equal(explainMove({ ply: 0 }), "");
});
