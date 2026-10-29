package com.cricket.scoreboard.repository;

import com.cricket.scoreboard.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNameIgnoreCase(String name);
    Optional<Team> findByShortNameIgnoreCase(String shortName);
}
