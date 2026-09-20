package com.isaac.chessanalysis.game.repository;

import com.isaac.chessanalysis.game.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
}
