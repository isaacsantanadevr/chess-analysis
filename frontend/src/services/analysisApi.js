const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function analyzeGame(pgn, depth) {
  const response = await fetch(`${API_BASE_URL}/api/v1/analysis/game`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ pgn, depth }),
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `Erro HTTP ${response.status}`);
  }

  const data = await response.json();

  if (!Array.isArray(data)) {
    throw new Error("Resposta inesperada do backend.");
  }

  return data;
}
