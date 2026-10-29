package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.BallRequestDTO;
import com.cricket.scoreboard.dto.LiveScoreUpdateDTO;
import com.cricket.scoreboard.entity.*;
import com.cricket.scoreboard.enums.DismissalType;
import com.cricket.scoreboard.enums.ExtraType;
import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerMatchStatsRepository playerMatchStatsRepository;
    private final BallEventRepository ballEventRepository;
    private final PlayerRepository playerRepository;
    private final WebSocketBroadcastService broadcastService;
    private final MatchService matchService;

    @Transactional
    public BallEvent recordBall(BallRequestDTO request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + request.getMatchId()));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            log.warn("Cannot record ball: Match {} is already completed.", match.getId());
            return null;
        }

        int innNum = match.getCurrentInningsNumber();
        Innings innings = inningsRepository.findByMatchAndInningsNumber(match, innNum)
                .orElseThrow(() -> new IllegalStateException("Innings not initialized for match " + match.getId()));

        int runsOffBat = request.getRunsOffBat() != null ? request.getRunsOffBat() : 0;
        int extrasRuns = request.getExtrasRuns() != null ? request.getExtrasRuns() : 0;
        ExtraType extraType = request.getExtraType() != null ? request.getExtraType() : ExtraType.NONE;
        boolean isWicket = Boolean.TRUE.equals(request.getIsWicket());
        DismissalType dismissalType = request.getDismissalType() != null ? request.getDismissalType() :
                (isWicket ? DismissalType.BOWLED : DismissalType.NOT_OUT);

        boolean isLegalDelivery = (extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL);

        Player striker = match.getStriker();
        Player nonStriker = match.getNonStriker();
        Player bowler = match.getCurrentBowler();

        if (striker == null || bowler == null) {
            log.error("Striker or Bowler is null for match {}", match.getId());
            return null;
        }

        // 1. Fetch or create PlayerMatchStats for Striker & Bowler
        PlayerMatchStats strikerStats = getOrCreateStats(match, striker, innNum, true);
        PlayerMatchStats bowlerStats = getOrCreateStats(match, bowler, innNum, false);

        // 2. Update Batting Stats
        if (extraType != ExtraType.WIDE) {
            strikerStats.setBallsFaced(strikerStats.getBallsFaced() + 1);
        }
        strikerStats.setRunsScored(strikerStats.getRunsScored() + runsOffBat);
        if (runsOffBat == 4) {
            strikerStats.setFours(strikerStats.getFours() + 1);
        } else if (runsOffBat == 6) {
            strikerStats.setSixes(strikerStats.getSixes() + 1);
        }

        // 3. Update Bowler Stats
        if (isLegalDelivery) {
            bowlerStats.setOversBowledBalls(bowlerStats.getOversBowledBalls() + 1);
        }
        int bowlerConceded = runsOffBat + (extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL ? extrasRuns : 0);
        bowlerStats.setRunsConceded(bowlerStats.getRunsConceded() + bowlerConceded);
        if (runsOffBat == 0 && extrasRuns == 0) {
            bowlerStats.setDotsConceded(bowlerStats.getDotsConceded() + 1);
        }

        // 4. Update Innings totals
        int totalBallRuns = runsOffBat + extrasRuns;
        innings.setTotalRuns(innings.getTotalRuns() + totalBallRuns);

        if (extraType == ExtraType.WIDE) {
            innings.setWides(innings.getWides() + extrasRuns);
        } else if (extraType == ExtraType.NO_BALL) {
            innings.setNoBalls(innings.getNoBalls() + extrasRuns);
        } else if (extraType == ExtraType.BYE) {
            innings.setByes(innings.getByes() + extrasRuns);
        } else if (extraType == ExtraType.LEG_BYE) {
            innings.setLegByes(innings.getLegByes() + extrasRuns);
        }

        int currentOverNumber = innings.getTotalBalls() / 6;
        int currentBallInOver = (innings.getTotalBalls() % 6) + (isLegalDelivery ? 1 : 0);

        if (isLegalDelivery) {
            innings.setTotalBalls(innings.getTotalBalls() + 1);
        }

        // 5. Handle Wicket
        Player dismissedPlayer = null;
        if (isWicket) {
            dismissedPlayer = striker; // Default striker is out
            innings.setWickets(innings.getWickets() + 1);

            strikerStats.setIsOut(true);
            strikerStats.setDismissalType(dismissalType);
            strikerStats.setBowlerName(bowler.getName());

            if (dismissalType != DismissalType.RUN_OUT) {
                bowlerStats.setWicketsTaken(bowlerStats.getWicketsTaken() + 1);
            }

            // Bring in next batsman
            Player nextBatter = getNextBatsman(match, innings.getBattingTeam(), innNum);
            match.setStriker(nextBatter);
        }

        // 6. Generate ball tag string for current over display
        String ballBadge = getBallBadge(runsOffBat, extrasRuns, extraType, isWicket);
        String currentOverStr = match.getCurrentOverBalls();
        if (currentOverStr == null || currentOverStr.isEmpty()) {
            currentOverStr = ballBadge;
        } else {
            currentOverStr = currentOverStr + ", " + ballBadge;
        }
        match.setCurrentOverBalls(currentOverStr);

        // 7. Generate Commentary
        String commentary = request.getCustomCommentary();
        if (commentary == null || commentary.isEmpty()) {
            commentary = generateCommentaryText(bowler, striker, runsOffBat, extrasRuns, extraType, isWicket, dismissalType);
        }

        // 8. Create and save BallEvent
        String scoreDisplay = innings.getTotalRuns() + "/" + innings.getWickets();
        String ballDisplay = currentOverNumber + "." + (isLegalDelivery ? currentBallInOver : innings.getTotalBalls() % 6);

        BallEvent ballEvent = BallEvent.builder()
                .match(match)
                .inningsNumber(innNum)
                .overNumber(currentOverNumber)
                .ballNumberInOver(currentBallInOver)
                .batsman(striker)
                .nonStriker(nonStriker)
                .bowler(bowler)
                .runsOffBat(runsOffBat)
                .extrasRuns(extrasRuns)
                .extraType(extraType)
                .isWicket(isWicket)
                .dismissalType(dismissalType)
                .dismissedPlayer(dismissedPlayer)
                .commentary(commentary)
                .scoreAtBall(scoreDisplay)
                .ballDisplay(ballDisplay)
                .timestamp(LocalDateTime.now())
                .build();

        ballEventRepository.save(ballEvent);
        playerMatchStatsRepository.save(strikerStats);
        playerMatchStatsRepository.save(bowlerStats);

        // Update player career aggregations
        updatePlayerCareerStats(striker, runsOffBat, extraType != ExtraType.WIDE);
        updateBowlerCareerStats(bowler, bowlerConceded, isLegalDelivery, isWicket && dismissalType != DismissalType.RUN_OUT);

        // 9. Strike rotation on odd runs (if not out or after next batsman is in)
        if (runsOffBat % 2 != 0) {
            rotateStrike(match);
        }

        // 10. Check if over is completed (6 legal deliveries)
        if (isLegalDelivery && innings.getTotalBalls() % 6 == 0) {
            // Over finished!
            // Bowler maidens check
            if (bowlerStats.getRunsConceded() == 0) {
                bowlerStats.setMaidenOvers(bowlerStats.getMaidenOvers() + 1);
            }

            // Rotate strike at end of over
            rotateStrike(match);

            // Change bowler
            rotateBowler(match, innings.getBowlingTeam(), innNum, bowler);

            // Reset current over deliveries list
            match.setCurrentOverBalls("");
        }

        // 11. Check Innings / Match Completion conditions
        checkMatchProgression(match, innings, innNum);

        // Calculate dynamic win probability
        updateWinProbability(match, innings);

        // Save states
        inningsRepository.save(innings);
        matchRepository.save(match);

        // 12. Broadcast updates in real time via WebSockets
        LiveScoreUpdateDTO liveUpdate = matchService.getLiveScoreUpdate(match.getId());
        broadcastService.broadcastLiveScore(match.getId(), liveUpdate);
        broadcastService.broadcastBallEvent(match.getId(), ballEvent);
        broadcastService.broadcastMatchesList(matchService.getAllMatches(null));

        return ballEvent;
    }

    private void rotateStrike(Match match) {
        Player temp = match.getStriker();
        match.setStriker(match.getNonStriker());
        match.setNonStriker(temp);
    }

    private void rotateBowler(Match match, Team bowlingTeam, int innNum, Player currentBowler) {
        List<Player> teamPlayers = playerRepository.findByTeam(bowlingTeam);
        // Find bowlers / all-rounders other than the current bowler
        List<Player> availableBowlers = teamPlayers.stream()
                .filter(p -> !p.getId().equals(currentBowler.getId()))
                .filter(p -> p.getRole().name().contains("BOWL") || p.getRole().name().contains("ALL"))
                .toList();

        if (!availableBowlers.isEmpty()) {
            // Pick a bowler who hasn't exceeded 4 overs (for T20)
            for (Player b : availableBowlers) {
                PlayerMatchStats stats = playerMatchStatsRepository
                        .findByMatchAndPlayerAndInningsNumber(match, b, innNum).orElse(null);
                int ballsBowled = stats != null ? stats.getOversBowledBalls() : 0;
                if (ballsBowled < (match.getTotalOvers() / 5) * 6) { // max 4 overs in T20
                    match.setCurrentBowler(b);
                    getOrCreateStats(match, b, innNum, false);
                    return;
                }
            }
            // Fallback pick any other available bowler
            match.setCurrentBowler(availableBowlers.get(0));
            getOrCreateStats(match, availableBowlers.get(0), innNum, false);
        }
    }

    private Player getNextBatsman(Match match, Team battingTeam, int innNum) {
        List<Player> squad = playerRepository.findByTeam(battingTeam);
        List<PlayerMatchStats> existingBatters = playerMatchStatsRepository.findByMatchAndInningsNumber(match, innNum);
        Set<Long> battedPlayerIds = new HashSet<>();
        for (PlayerMatchStats pms : existingBatters) {
            if (Boolean.TRUE.equals(pms.getHasBatted())) {
                battedPlayerIds.add(pms.getPlayer().getId());
            }
        }

        for (Player p : squad) {
            if (!battedPlayerIds.contains(p.getId())) {
                PlayerMatchStats nextStats = getOrCreateStats(match, p, innNum, true);
                nextStats.setBattingOrder(existingBatters.size() + 1);
                playerMatchStatsRepository.save(nextStats);
                return p;
            }
        }
        return null; // All out
    }

    private void checkMatchProgression(Match match, Innings innings, int innNum) {
        int maxLegalBalls = match.getTotalOvers() * 6;
        boolean allOut = innings.getWickets() >= 10;
        boolean oversFinished = innings.getTotalBalls() >= maxLegalBalls;

        if (innNum == 1) {
            if (allOut || oversFinished) {
                // First innings complete
                innings.setIsCompleted(true);
                match.setTargetRuns(innings.getTotalRuns() + 1);
                match.setCurrentInningsNumber(2);
                match.setStatus(MatchStatus.INNINGS_BREAK);

                // Swap batting & bowling teams
                Team prevBatting = match.getBattingTeam();
                Team prevBowling = match.getBowlingTeam();
                match.setBattingTeam(prevBowling);
                match.setBowlingTeam(prevBatting);
                match.setCurrentOverBalls("");

                // Initialize 2nd Innings
                Innings inn2 = Innings.builder()
                        .match(match)
                        .inningsNumber(2)
                        .battingTeam(prevBowling)
                        .bowlingTeam(prevBatting)
                        .totalRuns(0)
                        .wickets(0)
                        .totalBalls(0)
                        .isCompleted(false)
                        .build();
                inningsRepository.save(inn2);

                // Set openers for team 2
                List<Player> team2Players = playerRepository.findByTeam(prevBowling);
                if (team2Players.size() >= 2) {
                    match.setStriker(team2Players.get(0));
                    match.setNonStriker(team2Players.get(1));
                    getOrCreateStats(match, team2Players.get(0), 2, true);
                    getOrCreateStats(match, team2Players.get(1), 2, true);
                }

                // Set opening bowler from team 1
                List<Player> team1Players = playerRepository.findByTeam(prevBatting);
                Player openingBowler = team1Players.stream()
                        .filter(p -> p.getRole().name().contains("BOWL") || p.getRole().name().contains("ALL"))
                        .findFirst().orElse(team1Players.get(team1Players.size() - 1));
                match.setCurrentBowler(openingBowler);
                getOrCreateStats(match, openingBowler, 2, false);

                match.setStatus(MatchStatus.LIVE);
                broadcastService.broadcastTickerMessage("Innings Break: " + prevBatting.getName() + " set a target of " + match.getTargetRuns());
            }
        } else if (innNum == 2) {
            Integer target = match.getTargetRuns();
            if (target != null && innings.getTotalRuns() >= target) {
                // Chasing team won!
                innings.setIsCompleted(true);
                match.setStatus(MatchStatus.COMPLETED);
                int wicketsInHand = 10 - innings.getWickets();
                match.setWinnerTeamName(innings.getBattingTeam().getName());
                match.setResultMessage(innings.getBattingTeam().getName() + " won by " + wicketsInHand + " wickets");
                broadcastService.broadcastTickerMessage(match.getResultMessage());
            } else if (allOut || oversFinished) {
                // Defending team won or tied
                innings.setIsCompleted(true);
                match.setStatus(MatchStatus.COMPLETED);
                if (target != null) {
                    if (innings.getTotalRuns().equals(target - 1)) {
                        match.setResultMessage("Match Tied! (Scores level)");
                    } else {
                        int margin = (target - 1) - innings.getTotalRuns();
                        match.setWinnerTeamName(innings.getBowlingTeam().getName());
                        match.setResultMessage(innings.getBowlingTeam().getName() + " won by " + margin + " runs");
                    }
                    broadcastService.broadcastTickerMessage(match.getResultMessage());
                }
            }
        }
    }

    private void updateWinProbability(Match match, Innings innings) {
        if (match.getStatus() == MatchStatus.COMPLETED) {
            if (match.getWinnerTeamName() != null && match.getBattingTeam() != null &&
                    match.getWinnerTeamName().equals(match.getBattingTeam().getName())) {
                match.setBattingTeamWinProbability(100.0);
            } else {
                match.setBattingTeamWinProbability(0.0);
            }
            return;
        }

        if (match.getCurrentInningsNumber() == 1) {
            // First innings probability based on current run rate vs par (8.5 rpo)
            double crr = innings.getCurrentRunRate();
            double prob = 50.0 + (crr - 8.5) * 5.0 - (innings.getWickets() * 4.0);
            prob = Math.max(10.0, Math.min(90.0, prob));
            match.setBattingTeamWinProbability(Math.round(prob * 10.0) / 10.0);
        } else {
            // Second innings chasing probability
            if (match.getTargetRuns() != null) {
                int needed = match.getTargetRuns() - innings.getTotalRuns();
                int ballsLeft = (match.getTotalOvers() * 6) - innings.getTotalBalls();
                if (needed <= 0) {
                    match.setBattingTeamWinProbability(100.0);
                } else if (ballsLeft <= 0 || innings.getWickets() >= 10) {
                    match.setBattingTeamWinProbability(0.0);
                } else {
                    double rrr = (needed * 6.0) / ballsLeft;
                    double crr = innings.getCurrentRunRate();
                    double base = 50.0 - (rrr - crr) * 6.0 - (innings.getWickets() * 4.5);
                    base = Math.max(1.0, Math.min(99.0, base));
                    match.setBattingTeamWinProbability(Math.round(base * 10.0) / 10.0);
                }
            }
        }
    }

    private PlayerMatchStats getOrCreateStats(Match match, Player player, int innNum, boolean isBatting) {
        return playerMatchStatsRepository.findByMatchAndPlayerAndInningsNumber(match, player, innNum)
                .orElseGet(() -> {
                    PlayerMatchStats pms = PlayerMatchStats.builder()
                            .match(match)
                            .player(player)
                            .inningsNumber(innNum)
                            .hasBatted(isBatting)
                            .hasBowled(!isBatting)
                            .build();
                    return playerMatchStatsRepository.save(pms);
                });
    }

    private void updatePlayerCareerStats(Player player, int runs, boolean facedLegalBall) {
        player.setTotalRuns(player.getTotalRuns() + runs);
        if (facedLegalBall) {
            player.setBallsFaced(player.getBallsFaced() + 1);
        }
        if (runs == 4) player.setFours(player.getFours() + 1);
        if (runs == 6) player.setSixes(player.getSixes() + 1);
        if (player.getBallsFaced() > 0) {
            player.setBattingStrikeRate(Math.round(((player.getTotalRuns() * 100.0) / player.getBallsFaced()) * 100.0) / 100.0);
        }
        playerRepository.save(player);
    }

    private void updateBowlerCareerStats(Player bowler, int runsConceded, boolean isLegalBall, boolean tookWicket) {
        bowler.setRunsConceded(bowler.getRunsConceded() + runsConceded);
        if (isLegalBall) {
            bowler.setBallsBowled(bowler.getBallsBowled() + 1);
        }
        if (tookWicket) {
            bowler.setWicketsTaken(bowler.getWicketsTaken() + 1);
        }
        if (bowler.getBallsBowled() > 0) {
            double overs = bowler.getBallsBowled() / 6.0;
            bowler.setBowlingEconomy(Math.round((bowler.getRunsConceded() / overs) * 100.0) / 100.0);
        }
        playerRepository.save(bowler);
    }

    private String getBallBadge(int runs, int extras, ExtraType extraType, boolean isWicket) {
        if (isWicket) return "W";
        if (extraType == ExtraType.WIDE) return "Wd";
        if (extraType == ExtraType.NO_BALL) return "Nb";
        if (runs == 6) return "6";
        if (runs == 4) return "4";
        return String.valueOf(runs);
    }

    private String generateCommentaryText(Player bowler, Player striker, int runs, int extras,
                                         ExtraType extraType, boolean isWicket, DismissalType dismissalType) {
        String bName = bowler.getName();
        String sName = striker.getName();

        if (isWicket) {
            switch (dismissalType) {
                case BOWLED -> {
                    return String.format("%s to %s, OUT! CLEAN BOWLED! Absolute ripper of a delivery! Castle is disturbed as the timber flies!", bName, sName);
                }
                case CAUGHT -> {
                    return String.format("%s to %s, OUT! CAUGHT! Sliced high into the air and taken safely in the deep! Massive breakthrough!", bName, sName);
                }
                case LBW -> {
                    return String.format("%s to %s, OUT! LBW! Trapped right in front! Loud shout from the bowler and the umpire raises the finger!", bName, sName);
                }
                case RUN_OUT -> {
                    return String.format("%s to %s, OUT! RUN OUT! Direct hit at the non-striker's end! Desperate dive falls short!", bName, sName);
                }
                default -> {
                    return String.format("%s to %s, OUT! Wicket falls! Brilliant cricket from %s!", bName, sName, bName);
                }
            }
        }

        if (extraType == ExtraType.WIDE) {
            return String.format("%s to %s, Wide! Slipped down the leg side, umpire stretches arms.", bName, sName);
        }
        if (extraType == ExtraType.NO_BALL) {
            return String.format("%s to %s, NO BALL! Overstepped by inches! Free hit coming up next!", bName, sName);
        }

        switch (runs) {
            case 6 -> {
                String[] sixPhrases = {
                        "SIX! Dispatched into orbit! Right out of the meat of the bat over deep mid-wicket!",
                        "SIX! Maximum! Stepped out and clubbed it with authority over long-on! What a hit!",
                        "SIX! Stand and deliver! Clean bat swing sent soaring into the upper tier!"
                };
                return String.format("%s to %s, %s", bName, sName, sixPhrases[new Random().nextInt(sixPhrases.length)]);
            }
            case 4 -> {
                String[] fourPhrases = {
                        "FOUR! Creamed through the covers! Pure elegance and exquisite timing!",
                        "FOUR! Slapped through point! Lightning fast outfield does the rest!",
                        "FOUR! Pulled away behind square! Rocked back in a flash and nailed it!"
                };
                return String.format("%s to %s, %s", bName, sName, fourPhrases[new Random().nextInt(fourPhrases.length)]);
            }
            case 2 -> {
                return String.format("%s to %s, worked into the vacant gap at deep midwicket, great hustle between wickets for a brisk brace.", bName, sName);
            }
            case 3 -> {
                return String.format("%s to %s, guided fine past third man, the fielder chases down in the deep as they run three.", bName, sName);
            }
            case 1 -> {
                return String.format("%s to %s, pushed gently to long-off for an easy single to rotate the strike.", bName, sName);
            }
            default -> {
                String[] dotPhrases = {
                        "no run, defended solidly off the front foot back to the bowler.",
                        "no run, beaten outside off stump with extra bounce and zip!",
                        "no run, dabbed straight to backward point, fielder stays alert."
                };
                return String.format("%s to %s, %s", bName, sName, dotPhrases[new Random().nextInt(dotPhrases.length)]);
            }
        }
    }
}
