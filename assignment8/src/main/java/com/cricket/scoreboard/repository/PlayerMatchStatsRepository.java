package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.Match;
import com.cricket.scoreboard.entity.Player;
import com.cricket.scoreboard.entity.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {
    List<PlayerMatchStats> findByMatchAndInningsNumber(Match match, Integer inningsNumber);
    List<PlayerMatchStats> findByMatchIdAndInningsNumber(Long matchId, Integer inningsNumber);
    Optional<PlayerMatchStats> findByMatchAndPlayerAndInningsNumber(Match match, Player player, Integer inningsNumber);
    List<PlayerMatchStats> findByMatch(Match match);
}
