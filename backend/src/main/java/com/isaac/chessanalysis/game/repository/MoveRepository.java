package com.isaac.chessanalysis.game.repository;

import com.isaac.chessanalysis.game.entity.MoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<MoveEntity, Long> {

    java.util.List<MoveEntity> findByGameIdOrderByPlyAsc(Long gameId);
}
