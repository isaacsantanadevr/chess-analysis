import { test } from "node:test";
import assert from "node:assert/strict";
import { getGameSummary } from "./gameSummary.js";

const empty = { BEST: 0, GOOD: 0, INACCURACY: 0, MISTAKE: 0, BLUNDER: 0, total: 0 };

test("análise ausente, vazia ou só com posição inicial não conta lances", () => {
  for (const positions of [undefined, null, [], [{ ply: 0, classification: null }]]) {
    assert.deepEqual(getGameSummary(positions), { white: empty, black: empty });
  }
});

test("conta todas as classificações por ply e preserva o lance extra das brancas", () => {
  const categories = ["BEST", "GOOD", "INACCURACY", "MISTAKE", "BLUNDER"];
  const positions = Array.from({ length: 11 }, (_, index) => ({
    ply: index + 1,
    classification: categories[Math.floor(index / 2) % categories.length],
  }));
  // A ordem do array não determina o jogador.
  positions.reverse();
  const snapshot = structuredClone(positions);
  assert.deepEqual(getGameSummary(positions), {
    white: { BEST: 2, GOOD: 1, INACCURACY: 1, MISTAKE: 1, BLUNDER: 1, total: 6 },
    black: { BEST: 1, GOOD: 1, INACCURACY: 1, MISTAKE: 1, BLUNDER: 1, total: 5 },
  });
  assert.deepEqual(positions, snapshot);
});

test("partida com plies pares tem totais iguais e contagens independentes", () => {
  assert.deepEqual(getGameSummary([
    { ply: 0, classification: "BEST" },
    { ply: 1, classification: "BEST" },
    { ply: 2, classification: "BLUNDER" },
  ]), {
    white: { ...empty, BEST: 1, total: 1 },
    black: { ...empty, BLUNDER: 1, total: 1 },
  });
});

test("lances sem classificação conhecida entram apenas no total", () => {
  assert.deepEqual(getGameSummary([
    null,
    { ply: -1, classification: "BEST" },
    { ply: 1.5, classification: "BEST" },
    { classification: "BEST" },
    { ply: 1, classification: null },
    { ply: 2, classification: "UNKNOWN" },
  ]), {
    white: { ...empty, total: 1 },
    black: { ...empty, total: 1 },
  });
});
