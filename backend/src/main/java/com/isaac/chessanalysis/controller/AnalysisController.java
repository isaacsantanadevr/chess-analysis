package com.isaac.chessanalysis.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isaac.chessanalysis.analysis.AnalysisService;
import com.isaac.chessanalysis.chess.pgn.PgnService;
import com.isaac.chessanalysis.analysis.dto.PgnRequest;
import com.isaac.chessanalysis.analysis.dto.PositionAnalysisRequest;
import com.isaac.chessanalysis.analysis.dto.PositionEvaluation;
import com.isaac.chessanalysis.chess.engine.StockfishResult;

@CrossOrigin(origins = "http://localhost:5173")
@RestController 
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService as;
    private final PgnService ps;

    public AnalysisController(AnalysisService as, PgnService ps) {
        this.as = as;
        this.ps = ps;
    }

    @PostMapping("/position")
    public ResponseEntity<StockfishResult> analyzePosition(@RequestBody PositionAnalysisRequest par) throws IOException{
        StockfishResult sr = as.analyzePosition(par.getFen(), par.getDepth());
        return ResponseEntity.ok(sr);
    }

    @PostMapping("/pgn/positions")
    public ResponseEntity<List<String>> extractPos(@RequestBody PgnRequest pr) throws Exception{
        List<String> pos = ps.extractPos(pr.getPgn());
        return ResponseEntity.ok(pos);
    }


    @PostMapping("/game")
    public ResponseEntity<List<PositionEvaluation>> analyzeGame(@RequestBody PgnRequest pr) throws Exception{
        List<PositionEvaluation> r = as.analyzeGame(pr.getPgn(), pr.getDepth());
        return ResponseEntity.ok(r);
    }

}
