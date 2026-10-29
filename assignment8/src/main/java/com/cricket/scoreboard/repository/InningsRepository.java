package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.Innings;
import com.cricket.scoreboard.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InningsRepository extends JpaRepository<Innings, Long> {
    List<Innings> findByMatchOrderByInningsNumberAsc(Match match);
    Optional<Innings> findByMatchAndInningsNumber(Match match, Integer inningsNumber);
    Optional<Innings> findByMatchIdAndInningsNumber(Long matchId, Integer inningsNumber);
}
