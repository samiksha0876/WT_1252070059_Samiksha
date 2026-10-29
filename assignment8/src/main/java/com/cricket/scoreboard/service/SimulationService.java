package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.BallRequestDTO;
import com.cricket.scoreboard.entity.Innings;
import com.cricket.scoreboard.entity.Match;
import com.cricket.scoreboard.entity.Player;
import com.cricket.scoreboard.enums.DismissalType;
import com.cricket.scoreboard.enums.ExtraType;
import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.repository.InningsRepository;
import com.cricket.scoreboard.repository.MatchRepository;
import com.cricket.scoreboard.repository.PlayerRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final ScoringService scoringService;
    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerRepository playerRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ConcurrentMap<Long, ScheduledFuture<?>> activeSimulations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public synchronized boolean startSimulation(Long matchId, int intervalMs) {
        if (activeSimulations.containsKey(matchId)) {
            log.info("Simulation for match {} is already active.", matchId);
            return true;
        }

        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null || match.getStatus() == MatchStatus.COMPLETED) {
            log.warn("Cannot start simulation for invalid or completed match: {}", matchId);
            return false;
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean hasMore = simulateNextBall(matchId);
                if (!hasMore) {
                    stopSimulation(matchId);
                }
            } catch (Exception e) {
                log.error("Error during simulation step for match {}: {}", matchId, e.getMessage());
                stopSimulation(matchId);
            }
        }, 500, intervalMs, TimeUnit.MILLISECONDS);

        activeSimulations.put(matchId, future);
        log.info("Started real-time live simulation for match {} every {}ms", matchId, intervalMs);
        return true;
    }

    public synchronized boolean stopSimulation(Long matchId) {
        ScheduledFuture<?> future = activeSimulations.remove(matchId);
        if (future != null) {
            future.cancel(false);
            log.info("Stopped simulation for match {}", matchId);
            return true;
        }
        return false;
    }

    public boolean isSimulating(Long matchId) {
        ScheduledFuture<?> future = activeSimulations.get(matchId);
        return future != null && !future.isDone() && !future.isCancelled();
    }

    public boolean simulateNextBall(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null || match.getStatus() == MatchStatus.COMPLETED) {
            return false;
        }

        int innNum = match.getCurrentInningsNumber();
        Innings inn = inningsRepository.findByMatchAndInningsNumber(match, innNum).orElse(null);
        if (inn == null || inn.getIsCompleted()) {
            return false;
        }

        int overNumber = inn.getTotalBalls() / 6;

        // Realistic cricket distribution depending on match phase
        // Phase 1: Powerplay (overs 0-5)
        // Phase 2: Middle overs (overs 6-14)
        // Phase 3: Death overs (overs 15-19)
        int roll = random.nextInt(100);
        BallRequestDTO request = BallRequestDTO.builder()
                .matchId(matchId)
                .runsOffBat(0)
                .extrasRuns(0)
                .extraType(ExtraType.NONE)
                .isWicket(false)
                .build();

        // Extra check (wide/no-ball: 5% chance)
        if (roll < 4) {
            // Wide
            request.setExtraType(ExtraType.WIDE);
            request.setExtrasRuns(1);
            scoringService.recordBall(request);
            return true;
        } else if (roll == 4) {
            // No ball
            request.setExtraType(ExtraType.NO_BALL);
            request.setExtrasRuns(1);
            request.setRunsOffBat(random.nextBoolean() ? 1 : 4);
            scoringService.recordBall(request);
            return true;
        }

        // Wicket check
        // Death overs and chasing high RRR increases wicket chances
        int wicketThreshold = overNumber >= 15 ? 12 : 6;
        if (roll < (5 + wicketThreshold)) {
            // Wicket!
            request.setIsWicket(true);
            DismissalType[] types = {DismissalType.CAUGHT, DismissalType.BOWLED, DismissalType.LBW, DismissalType.RUN_OUT};
            request.setDismissalType(types[random.nextInt(types.length)]);
            scoringService.recordBall(request);
            return true;
        }

        // Scoring distribution based on phase
        int runsRoll = random.nextInt(100);
        if (overNumber >= 15) {
            // Death overs: boundaries and dots
            if (runsRoll < 30) {
                request.setRunsOffBat(0); // Dot ball
            } else if (runsRoll < 55) {
                request.setRunsOffBat(1); // Single
            } else if (runsRoll < 70) {
                request.setRunsOffBat(2); // Two
            } else if (runsRoll < 88) {
                request.setRunsOffBat(4); // Four
            } else {
                request.setRunsOffBat(6); // Six
            }
        } else if (overNumber < 6) {
            // Powerplay
            if (runsRoll < 40) {
                request.setRunsOffBat(0);
            } else if (runsRoll < 65) {
                request.setRunsOffBat(1);
            } else if (runsRoll < 75) {
                request.setRunsOffBat(2);
            } else if (runsRoll < 93) {
                request.setRunsOffBat(4);
            } else {
                request.setRunsOffBat(6);
            }
        } else {
            // Middle overs: strike rotation
            if (runsRoll < 35) {
                request.setRunsOffBat(0);
            } else if (runsRoll < 75) {
                request.setRunsOffBat(1);
            } else if (runsRoll < 88) {
                request.setRunsOffBat(2);
            } else if (runsRoll < 97) {
                request.setRunsOffBat(4);
            } else {
                request.setRunsOffBat(6);
            }
        }

        scoringService.recordBall(request);
        return true;
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdownNow();
    }
}
