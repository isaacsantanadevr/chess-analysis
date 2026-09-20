import { describeEvaluation, evalToWhitePercent, formatEvaluation } from "../utils/evaluation.js";

export default function EvaluationBar({ result }) {
  const whitePercent = evalToWhitePercent(result);

  return (
    <div className="evaluation-column" aria-label={describeEvaluation(result)} title={describeEvaluation(result)}>
      <div className="evaluation-track">
        <div className="evaluation-white" style={{ height: `${whitePercent}%` }} />
      </div>
      <span className="evaluation-label">{result?.scoreType === "MATE" ? "Mate" : formatEvaluation(result)}</span>
    </div>
  );
}
