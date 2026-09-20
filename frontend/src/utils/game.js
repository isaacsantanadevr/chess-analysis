import { Chess } from "chess.js";

export function parsePgnMoves(pgn) {
  if (!pgn.trim()) return [];

  try {
    const game = new Chess();
    game.loadPgn(pgn);

    return game.history({ verbose: true }).map((move) => ({
      san: move.san,
      from: move.from,
      to: move.to,
      uci: `${move.from}${move.to}${move.promotion ?? ""}`,
    }));
  } catch {
    return [];
  }
}
