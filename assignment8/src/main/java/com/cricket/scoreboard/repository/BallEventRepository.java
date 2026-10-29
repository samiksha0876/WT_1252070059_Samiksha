package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.BallEvent;
import com.cricket.scoreboard.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BallEventRepository extends JpaRepository<BallEvent, Long> {
    List<BallEvent> findByMatchOrderByTimestampDesc(Match match);
    List<BallEvent> findByMatchIdOrderByTimestampDesc(Long matchId);
    List<BallEvent> findByMatchAndInningsNumberOrderByTimestampAsc(Match match, Integer inningsNumber);
    List<BallEvent> findByMatchIdAndInningsNumberOrderByTimestampAsc(Long matchId, Integer inningsNumber);

    // Get recent N balls for a match
    @Query("SELECT b FROM BallEvent b WHERE b.match.id = :matchId ORDER BY b.id DESC")
    List<BallEvent> findRecentBalls(Long matchId);
}
