package com.isaac.chessanalysis.analysis.repository;

import com.isaac.chessanalysis.analysis.entity.AnalysisJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJobEntity, Long> {

    java.util.List<AnalysisJobEntity> findByGameIdOrderByIdAsc(Long gameId);
}
