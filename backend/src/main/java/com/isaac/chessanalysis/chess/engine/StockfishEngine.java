package com.isaac.chessanalysis.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class StockfishEngine implements ChessEngine {

    private final Process process; // Processo do stockfish (SF)
    private final BufferedWriter w; // Enviar comandos para SF
    private final BufferedReader r; // Ler respostas SF

    public StockfishEngine(@Value("${chess.engine.stockfish.path}") String stockfishPath) throws IOException {

        // Prepara execução SF
        ProcessBuilder pb = new ProcessBuilder(stockfishPath);

        this.process = pb.start(); // Inicia processo

        // Java para Stockfish
        this.w = new BufferedWriter(new OutputStreamWriter(
                process.getOutputStream(),
                StandardCharsets.UTF_8));

        // Stockfish para Java
        this.r = new BufferedReader(new InputStreamReader(
                process.getInputStream(),
                StandardCharsets.UTF_8));

        sendCommand("uci");
        readAnsw("uciok");

        sendCommand("isready");
        readAnsw("readyok");

        // sendCommand("position startpos");
        // sendCommand("go depth 10");
        // StockfishResult result = readResult();

        // System.out.println("Melhor movimento: " + result.getBestMov());
        // System.out.println("Avaliação: " + result.getEvaluation());
        // System.out.println("PV: " + result.getVariation());
    }

    private StockfishResult readResult(String fen) throws IOException {
        StockfishParser parser = new StockfishParser();
        return parser.readResult(r, fen);
    }

    @Override
    public boolean isReady() {
        return process.isAlive(); // Verifica se processo esta vivo
    }

    @PreDestroy
    public void shutdown() {
        process.destroy(); // Encerra SF
    }

    private void sendCommand(String command) throws IOException {
        w.write(command); // Comando no buffer
        w.newLine();
        w.flush(); // Envia para o SF
    }

    private void readAnsw(String stop) throws IOException {
        String line;

        while ((line = r.readLine()) != null) {
            System.out.println(line); // Resposta SF
            if (line.startsWith(stop)) {
                break; // Para ao chegar na resposta
            }
        }
    }

    //Metodo para analisar posicao
    @Override 
    public StockfishResult analyzePosition(String fen, int depth) throws IOException {
        sendCommand("position fen " + fen); //Define posicao em FEN
        sendCommand("go depth " + depth); //Pede analise + Profundidade
        return readResult(fen); //Resultado na perspectiva das brancas
    }

    @Override
    public StockfishResult analyzeGame(String pgn, int depth) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'analyzeGame'");
    }

}