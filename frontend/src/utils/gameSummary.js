export function getGameSummary(positions) {
  const emptyCounts = () => ({
    BEST: 0,
    GOOD: 0,
    INACCURACY: 0,
    MISTAKE: 0,
    BLUNDER: 0,
    total: 0,
  });
  const summary = { white: emptyCounts(), black: emptyCounts() };

  for (const position of positions ?? []) {
    const { ply, classification } = position ?? {};
    if (!Number.isInteger(ply) || ply <= 0) continue;

    const counts = ply % 2 === 1 ? summary.white : summary.black;
    counts.total += 1;
    if (classification !== "total" && Object.hasOwn(counts, classification)) {
      counts[classification] += 1;
    }
  }

  return summary;
}
