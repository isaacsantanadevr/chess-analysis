package com.isaac.chessanalysis.analysis.repository;

import com.isaac.chessanalysis.analysis.entity.MoveAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveAnalysisRepository extends JpaRepository<MoveAnalysisEntity, Long> {

    java.util.List<MoveAnalysisEntity> findByJobIdOrderByMovePlyAsc(Long jobId);
}
