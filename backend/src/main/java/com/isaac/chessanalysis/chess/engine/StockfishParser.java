package com.isaac.chessanalysis.chess.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockfishParser {

    public StockfishResult readResult(BufferedReader r, String fen) throws IOException {
        String line;
        String lastInfos = null;

        while ((line = r.readLine()) != null) {
            if (hasScore(line)) {
                lastInfos = line; // Ignora estatisticas sem score e limites de busca
            }
            if (line.startsWith("bestmove ")) {
                if (lastInfos == null) {
                    throw new IOException("Stockfish returned no exact score");
                }
                return parseResult(lastInfos, line, fen);
            }
        }
        throw new IOException("Stockfish ended before bestmove");
    }

    private boolean hasScore(String line) {
        if (!line.startsWith("info ") || line.startsWith("info string ")) {
            return false;
        }
        List<String> infos = Arrays.asList(line.trim().split("\\s+"));
        int pv = infos.indexOf("multipv");
        return infos.contains("score") && !infos.contains("lowerbound") && !infos.contains("upperbound")
                && (pv < 0 || (pv + 1 < infos.size() && infos.get(pv + 1).equals("1")));
    }

    private StockfishResult parseResult(String line, String bestMovLine, String fen) throws IOException {
        String[] fields = fen.trim().split("\\s+");
        if (fields.length < 2 || (!fields[1].equals("w") && !fields[1].equals("b"))) {
            throw new IOException("FEN has no valid side to move");
        }
        boolean white = fields[1].equals("w");
        List<String> infos = Arrays.asList(line.trim().split("\\s+"));
        int score = infos.indexOf("score");
        if (score < 0 || score + 2 >= infos.size()) {
            throw new IOException("Invalid Stockfish score");
        }

        int value;
        try {
            value = Integer.parseInt(infos.get(score + 2));
        } catch (NumberFormatException failure) {
            throw new IOException("Invalid Stockfish score", failure);
        }
        String bestMov = bestMovLine.trim().split("\\s+")[1];
        List<String> variation = new ArrayList<>();
        int pv = infos.indexOf("pv");
        if (pv >= 0) {
            variation.addAll(infos.subList(pv + 1, infos.size()));
        }

        int normalized = white ? value : -value; // Score UCI e relativo a quem joga
        if (infos.get(score + 1).equals("cp")) {
            return new StockfishResult(bestMov, normalized, variation);
        }
        if (infos.get(score + 1).equals("mate")) {
            boolean whiteWins = value > 0 ? white : !white;
            return new StockfishResult(bestMov, normalized, whiteWins ? "WHITE" : "BLACK", variation);
        }
        throw new IOException("Unknown Stockfish score type");
    }
}
