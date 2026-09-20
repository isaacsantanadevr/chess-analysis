import { describeEvaluation, getMateWinner } from "./evaluation.js";

export const CLASSIFICATIONS = {
  BEST: { label: "Melhor", description: "Foi a melhor opção encontrada pelo Stockfish." },
  GOOD: { label: "Bom", description: "O lance manteve aproximadamente a qualidade da posição." },
  INACCURACY: { label: "Imprecisão", description: "Havia uma opção um pouco melhor." },
  MISTAKE: { label: "Erro", description: "O lance piorou significativamente a posição de quem jogou." },
  BLUNDER: { label: "Erro grave", description: "O lance alterou fortemente a avaliação da posição." },
};

export function explainMove(review) {
  if (!Number.isInteger(review?.ply) || review.ply <= 0) return "";
  const player = review.ply % 2 === 1 ? "WHITE" : "BLACK";
  const opponent = player === "WHITE" ? "BLACK" : "WHITE";
  const before = review.beforeResult;
  const after = review.result;
  const beforeWinner = getMateWinner(before);
  const afterWinner = getMateWinner(after);
  const beforeKnown = beforeWinner || (before?.scoreType === "CP" && Number.isInteger(before.centipawns));
  const afterKnown = afterWinner || (after?.scoreType === "CP" && Number.isInteger(after.centipawns));

  if (afterWinner && after.mate === 0) return `${describeEvaluation(after)}.`;
  if (beforeKnown && afterWinner === opponent && beforeWinner !== opponent) {
    return "Esse lance permitiu um mate forçado para o adversário.";
  }
  if (afterKnown && beforeWinner === player && afterWinner !== player) {
    return "Esse lance perdeu uma sequência de mate que estava disponível.";
  }
  if (afterWinner) {
    return beforeWinner === afterWinner
      ? `O mate forçado para as ${afterWinner === "WHITE" ? "brancas" : "pretas"} continua disponível.`
      : `${describeEvaluation(after)}.`;
  }
  return CLASSIFICATIONS[review.classification]?.description ?? "";
}
