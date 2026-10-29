package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.Match;
import com.cricket.scoreboard.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus status);

    @Query("SELECT m FROM Match m ORDER BY CASE WHEN m.status = 'LIVE' THEN 1 WHEN m.status = 'INNINGS_BREAK' THEN 2 WHEN m.status = 'UPCOMING' THEN 3 ELSE 4 END, m.id DESC")
    List<Match> findAllOrderedForDashboard();
}
