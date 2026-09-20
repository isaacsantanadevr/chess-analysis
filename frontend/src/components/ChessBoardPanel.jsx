import { Chessboard } from "react-chessboard";
import EvaluationBar from "./EvaluationBar.jsx";
import MoveNavigation from "./MoveNavigation.jsx";
import PgnInput from "./PgnInput.jsx";

export default function ChessBoardPanel({
  fen,
  result,
  currentPly,
  totalPlies,
  lastMove,
  pgn,
  onPgnChange,
  onAnalyze,
  onPrevious,
  onNext,
  canPrevious,
  canNext,
  loading,
  error,
}) {
  const squareStyles = lastMove
    ? {
        [lastMove.from]: {
          boxShadow: "inset 0 0 0 9999px var(--last-move-highlight)",
        },
        [lastMove.to]: {
          boxShadow: "inset 0 0 0 9999px var(--last-move-highlight)",
        },
      }
    : {};

  return (
    <section className="glass-panel board-panel">
      <div className="section-heading">
        <h2>Game review</h2>
        <p>Cole um PGN, analise e navegue lance a lance</p>
      </div>

      <div className="board-stage">
        <EvaluationBar result={result} />
        <div className="board-wrap">
          <Chessboard
            options={{
              position: fen,
              allowDragging: false,
              animationDurationInMs: 180,
              boardStyle: {
                borderRadius: "16px",
                boxShadow: "none",
              },
              lightSquareStyle: {
                backgroundColor: "var(--board-light)",
              },
              darkSquareStyle: {
                backgroundColor: "var(--board-dark)",
              },
              squareStyles,
            }}
          />
        </div>
      </div>

      <MoveNavigation
        currentPly={currentPly}
        totalPlies={totalPlies}
        onPrevious={onPrevious}
        onNext={onNext}
        canPrevious={canPrevious}
        canNext={canNext}
      />

      <PgnInput
        value={pgn}
        onChange={onPgnChange}
        onAnalyze={onAnalyze}
        loading={loading}
        error={error}
      />
    </section>
  );
}
