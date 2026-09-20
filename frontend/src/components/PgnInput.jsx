export default function PgnInput({ value, onChange, onAnalyze, loading, error }) {
  return (
    <div className="pgn-section">
      <label htmlFor="pgn-input">PGN da partida</label>
      <div className="pgn-row">
        <div className="pgn-field">
          <textarea
            id="pgn-input"
            value={value}
            onChange={(event) => onChange(event.target.value)}
            placeholder="1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 ..."
            spellCheck="false"
            disabled={loading}
          />
          <span>Aceita PGN completo com headers</span>
        </div>
        <button
          type="button"
          className="analyze-button"
          onClick={onAnalyze}
          disabled={loading}
        >
          {loading ? "Analisando…" : "Analisar"}
        </button>
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
    </div>
  );
}
