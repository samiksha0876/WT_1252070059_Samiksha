package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.Player;
import com.cricket.scoreboard.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeam(Team team);

    List<Player> findByTeamId(Long teamId);

    // Top Run Scorers (Orange Cap contenders)
    @Query("SELECT p FROM Player p ORDER BY p.totalRuns DESC")
    List<Player> findTopRunScorers();

    // Top Wicket Takers (Purple Cap contenders)
    @Query("SELECT p FROM Player p ORDER BY p.wicketsTaken DESC")
    List<Player> findTopWicketTakers();

    // Highest Strike Rate (min 30 balls faced)
    @Query("SELECT p FROM Player p WHERE p.ballsFaced >= 20 ORDER BY p.battingStrikeRate DESC")
    List<Player> findTopStrikeRates();

    // Best Economy Rate (min 30 balls bowled)
    @Query("SELECT p FROM Player p WHERE p.ballsBowled >= 24 AND p.bowlingEconomy > 0 ORDER BY p.bowlingEconomy ASC")
    List<Player> findBestEconomyBowlers();
}
