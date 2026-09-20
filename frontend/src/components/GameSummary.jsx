import { CLASSIFICATIONS } from "../utils/moveReview.js";

export default function GameSummary({ summary }) {
  return (
    <section className="game-summary" aria-labelledby="game-summary-title">
      <div className="section-heading">
        <h2 id="game-summary-title">Resumo da partida</h2>
        <p>Classificação dos lances por jogador</p>
      </div>
      <div className="detail-card game-summary-card">
        <table>
          <thead>
            <tr>
              <th scope="col">Classificação</th>
              <th scope="col">Brancas</th>
              <th scope="col">Pretas</th>
            </tr>
          </thead>
          <tbody>
            {Object.entries(CLASSIFICATIONS).map(([classification, { label }]) => (
              <tr key={classification}>
                <th scope="row">
                  <span className={`classification-badge ${classification.toLowerCase()}`}>
                    {label}
                  </span>
                </th>
                <td>{summary.white[classification]}</td>
                <td>{summary.black[classification]}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th scope="row">Total de lances</th>
              <td>{summary.white.total}</td>
              <td>{summary.black.total}</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </section>
  );
}
