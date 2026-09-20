import { describeEvaluation, formatEvaluation, formatMove } from "../utils/evaluation.js";
import { CLASSIFICATIONS, explainMove } from "../utils/moveReview.js";
import GameSummary from "./GameSummary.jsx";

function EvaluationDescription({ result }) {
  return (
    <>
      <p className="evaluation-description">{describeEvaluation(result)}</p>
      {result?.scoreType === "CP" && Number.isInteger(result.centipawns) && (
        <span className="evaluation-number">{formatEvaluation(result)}</span>
      )}
    </>
  );
}

export default function AnalysisSidebar({ result, review, beforeFen, summary }) {
  const bestMove = formatMove(review?.fen, result?.bestMov);
  const playedMove = formatMove(beforeFen, review?.playedMove);
  const alternative = review?.beforeResult?.bestMov;
  const hasAlternative = alternative && !["0000", "(none)"].includes(alternative);
  const classification = CLASSIFICATIONS[review?.classification];
  const explanation = explainMove(review);

  return (
    <aside className="glass-panel analysis-panel">
      {summary && <GameSummary summary={summary} />}
      <div className="section-heading">
        <h2>Análise da posição</h2>
        <p>Valores positivos favorecem as brancas; negativos, as pretas</p>
      </div>
      <div className="metrics-list">
        <div className="metric-card">
          <span className="metric-label">Situação no tabuleiro</span>
          <EvaluationDescription result={result} />
        </div>
        {result?.bestMov && (
          <div className="metric-card">
            <span className="metric-label">Melhor lance nesta posição</span>
            <strong>{bestMove}</strong>
          </div>
        )}
      </div>

      <section className="sidebar-section">
        <h3>Revisão do lance</h3>
        <div className="detail-card move-review-card">
          {review?.ply > 0 ? (
            <>
              {classification && (
                <span className={`classification-badge ${review.classification.toLowerCase()}`}>
                  {classification.label}
                </span>
              )}
              <div className="review-move">
                <span className="metric-label">As {review.ply % 2 === 1 ? "brancas" : "pretas"} jogaram</span>
                <strong>{playedMove}</strong>
              </div>
              {review.classification === "BEST" ? (
                <p>Foi o melhor lance encontrado.</p>
              ) : hasAlternative && (
                <div className="review-move">
                  <span className="metric-label">O melhor lance era</span>
                  <strong>{formatMove(beforeFen, alternative)}</strong>
                </div>
              )}
              <div className="review-evaluations">
                <div>
                  <h4>Antes do lance</h4>
                  <EvaluationDescription result={review.beforeResult} />
                </div>
                <div>
                  <h4>Depois do lance</h4>
                  <EvaluationDescription result={result} />
                </div>
              </div>
              {explanation && <p className="review-explanation">{explanation}</p>}
            </>
          ) : (
            <p>{review ? "Posição inicial. Avance para revisar o primeiro lance." : "Analise uma partida para revisar os lances."}</p>
          )}
        </div>
      </section>
    </aside>
  );
}
