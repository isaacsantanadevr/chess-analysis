import { Chess } from "chess.js";

export function getMateWinner(result) {
  if (result?.scoreType !== "MATE" || !Number.isInteger(result.mate)) return null;
  if (["WHITE", "BLACK"].includes(result.mateWinner)) return result.mateWinner;
  return result.mate > 0 ? "WHITE" : result.mate < 0 ? "BLACK" : null;
}

export function describeEvaluation(result) {
  if (result?.scoreType === "MATE") {
    const winner = getMateWinner(result);
    if (!winner) return "Avaliação indisponível";
    const side = winner === "WHITE" ? "brancas" : "pretas";
    return result.mate === 0
      ? `Xeque-mate · Vitória das ${side}`
      : `Mate em ${Math.abs(result.mate)} para as ${side}`;
  }
  if (!Number.isInteger(result?.centipawns)) return "Aguardando análise";
  const magnitude = Math.abs(result.centipawns);
  if (magnitude < 50) return "Posição equilibrada";
  const side = result.centipawns > 0 ? "brancas" : "pretas";
  const advantage = magnitude < 150 ? "Pequena vantagem" : magnitude < 300 ? "Vantagem" : "Grande vantagem";
  return `${advantage} para as ${side}`;
}

export function formatEvaluation(result) {
  if (result?.scoreType === "MATE" && Number.isInteger(result.mate)) {
    return describeEvaluation(result);
  }
  if (!Number.isInteger(result?.centipawns)) return "—";
  const value = result.centipawns / 100;
  return `${value >= 0 ? "+" : ""}${value.toFixed(2)}`;
}

export function evalToWhitePercent(result) {
  if (result?.scoreType === "MATE") {
    return result.mateWinner === "WHITE" ? 100 : 0;
  }
  if (!Number.isInteger(result?.centipawns)) return 50;
  const probability = 1 / (1 + 10 ** (-result.centipawns / 400));
  return Math.min(95, Math.max(5, probability * 100));
}

export function formatMove(fen, uci) {
  if (!uci) return "—";
  if (uci === "0000" || uci === "(none)") return "Sem lance legal";
  if (!fen) return uci;

  try {
    const game = new Chess(fen);
    return game.move({ from: uci.slice(0, 2), to: uci.slice(2, 4), promotion: uci[4] }).san;
  } catch {
    return uci;
  }
}
