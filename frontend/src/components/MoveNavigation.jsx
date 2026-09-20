export default function MoveNavigation({
  currentPly,
  totalPlies,
  onPrevious,
  onNext,
  canPrevious,
  canNext,
}) {
  const moveNumber = Math.ceil(currentPly / 2);
  const label = currentPly === 0
    ? "Posição inicial"
    : currentPly === totalPlies
      ? `Fim da partida · ${moveNumber} ${moveNumber === 1 ? "lance" : "lances"}`
      : `Lance ${moveNumber} · ${currentPly % 2 === 1 ? "Brancas" : "Pretas"}`;

  return (
    <div className="move-navigation">
      <button type="button" onClick={onPrevious} disabled={!canPrevious}>
        ← Anterior
      </button>
      <span aria-live="polite">{label}</span>
      <button type="button" onClick={onNext} disabled={!canNext}>
        Próximo →
      </button>
    </div>
  );
}
