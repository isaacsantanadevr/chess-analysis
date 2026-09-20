package com.isaac.chessanalysis.chess.pgn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

@Service 
public class PgnService {

    public List<String> extractPos(String pgn) throws Exception{
        return extractGame(pgn).positions();
    }

    public ParsedGame extractGame(String pgn) throws Exception{
        
        PgnHolder ph = new PgnHolder(); //Carrega o PGN recebido como texto
        ph.loadPgn(pgn); //Carrega PGN

        Game game = ph.getGames().get(0); //Parse dos moves da partida
        MoveList moves = game.getHalfMoves();

        Board b = new Board();

        List<ParsedMove> parsedMoves = new ArrayList<>();

        String initialFen = b.getFen(); //Posicao inicial

        for(Move move : moves){ //Faz os movimentos e salva a nova posicao
            b.doMove(move);
            parsedMoves.add(new ParsedMove(move.toString(), b.getFen()));
        }

        return new ParsedGame(initialFen, parsedMoves);
    }

    public record ParsedMove(String uci, String fen) {}

    public record ParsedGame(String initialFen, List<ParsedMove> moves) {
        public List<String> positions() {
            List<String> positions = new ArrayList<>();
            positions.add(initialFen);
            for (ParsedMove move : moves) {
                positions.add(move.fen());
            }
            return positions;
        }
    }

}
