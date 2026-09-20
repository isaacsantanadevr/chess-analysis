import { useMemo, useState } from "react";
import { Chess } from "chess.js";
import Header from "./components/Header.jsx";
import ChessBoardPanel from "./components/ChessBoardPanel.jsx";
import AnalysisSidebar from "./components/AnalysisSidebar.jsx";
import { analyzeGame } from "./services/analysisApi.js";
import { parsePgnMoves } from "./utils/game.js";
import { getGameSummary } from "./utils/gameSummary.js";
import "./App.css";

const DEPTH = 10;
const START_FEN = new Chess().fen();

export default function App() {
  const [pgn, setPgn] = useState("");
  const [positions, setPositions] = useState([]);
  const [currentPly, setCurrentPly] = useState(0);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState("");

  const moves = useMemo(() => parsePgnMoves(pgn), [pgn]);
  const current = positions[currentPly] ?? null;
  const summary = useMemo(() => getGameSummary(positions), [positions]);
  const lastPly = positions.at(-1)?.ply ?? 0;
  const analysisComplete = status === "online" && positions.length > 0;
  const currentFen = current?.fen ?? START_FEN;
  const currentResult = current?.result ?? null;
  const currentMove = current?.playedMove
    ? { from: current.playedMove.slice(0, 2), to: current.playedMove.slice(2, 4) }
    : currentPly > 0 ? moves[currentPly - 1] ?? null : null;
  const previousPosition = currentPly > 0 ? positions[currentPly - 1] ?? null : null;

  async function handleAnalyze() {
    if (!pgn.trim()) {
      setError("Cole um PGN antes de iniciar a análise.");
      return;
    }

    setStatus("loading");
    setError("");

    try {
      const result = await analyzeGame(pgn, DEPTH);
      setPositions(result);
      setCurrentPly(0);
      setStatus("online");
    } catch (err) {
      setStatus("offline");
      setError(err.message || "Não foi possível analisar a partida.");
    }
  }

  return (
    <div className="app-shell">
      <div className="ambient ambient-blue" aria-hidden="true" />
      <div className="ambient ambient-lavender" aria-hidden="true" />

      <Header status={status} depth={DEPTH} />

      <main className="workspace">
        <ChessBoardPanel
          fen={currentFen}
          result={currentResult}
          currentPly={current?.ply ?? 0}
          totalPlies={lastPly}
          lastMove={currentMove}
          pgn={pgn}
          onPgnChange={setPgn}
          onAnalyze={handleAnalyze}
          onPrevious={() => setCurrentPly((ply) => Math.max(0, ply - 1))}
          onNext={() =>
            setCurrentPly((ply) => Math.min(Math.max(positions.length - 1, 0), ply + 1))
          }
          canPrevious={currentPly > 0}
          canNext={positions.length > 0 && currentPly < positions.length - 1}
          loading={status === "loading"}
          error={error}
        />

        <AnalysisSidebar
          summary={analysisComplete ? summary : null}
          result={currentResult}
          review={current}
          beforeFen={previousPosition?.fen}
        />
      </main>
    </div>
  );
}
