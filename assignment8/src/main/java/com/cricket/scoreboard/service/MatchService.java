package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.LiveScoreUpdateDTO;
import com.cricket.scoreboard.dto.MatchSummaryDTO;
import com.cricket.scoreboard.dto.OverSummaryDTO;
import com.cricket.scoreboard.dto.ScorecardDTO;
import com.cricket.scoreboard.entity.*;
import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerMatchStatsRepository playerMatchStatsRepository;
    private final BallEventRepository ballEventRepository;
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<MatchSummaryDTO> getAllMatches(MatchStatus statusFilter) {
        List<Match> matches;
        if (statusFilter != null) {
            matches = matchRepository.findByStatus(statusFilter);
        } else {
            matches = matchRepository.findAllOrderedForDashboard();
        }

        return matches.stream().map(this::convertToSummaryDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchSummaryDTO getMatchSummary(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        return convertToSummaryDTO(match);
    }

    @Transactional(readOnly = true)
    public LiveScoreUpdateDTO getLiveScoreUpdate(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        int currentInnNum = match.getCurrentInningsNumber();
        Innings currentInnings = inningsRepository.findByMatchAndInningsNumber(match, currentInnNum)
                .orElse(null);

        Innings firstInnings = currentInnNum == 2 ?
                inningsRepository.findByMatchAndInningsNumber(match, 1).orElse(null) : null;

        String firstInningsScoreStr = null;
        if (firstInnings != null) {
            firstInningsScoreStr = firstInnings.getBattingTeam().getShortName() + " " +
                    firstInnings.getTotalRuns() + "/" + firstInnings.getWickets() + " (" +
                    firstInnings.getFormattedOvers() + " ov)";
        }

        Team battingTeam = match.getBattingTeam();
        Team bowlingTeam = match.getBowlingTeam();

        Integer runs = currentInnings != null ? currentInnings.getTotalRuns() : 0;
        Integer wickets = currentInnings != null ? currentInnings.getWickets() : 0;
        Integer balls = currentInnings != null ? currentInnings.getTotalBalls() : 0;
        String overs = currentInnings != null ? currentInnings.getFormattedOvers() : "0.0";
        Double crr = currentInnings != null ? currentInnings.getCurrentRunRate() : 0.0;

        Integer targetRuns = match.getTargetRuns();
        Integer runsNeeded = null;
        Integer ballsRemaining = null;
        Double rrr = null;

        if (currentInnNum == 2 && targetRuns != null) {
            runsNeeded = Math.max(0, targetRuns - runs);
            int totalMatchBalls = match.getTotalOvers() * 6;
            ballsRemaining = Math.max(0, totalMatchBalls - balls);
            if (ballsRemaining > 0 && runsNeeded > 0) {
                double rrrVal = (runsNeeded * 6.0) / ballsRemaining;
                rrr = BigDecimal.valueOf(rrrVal).setScale(2, RoundingMode.HALF_UP).doubleValue();
            } else if (runsNeeded <= 0) {
                rrr = 0.0;
            }
        }

        // Active Batters
        LiveScoreUpdateDTO.BatsmanLiveDTO strikerDto = null;
        if (match.getStriker() != null) {
            Player striker = match.getStriker();
            PlayerMatchStats stats = playerMatchStatsRepository
                    .findByMatchAndPlayerAndInningsNumber(match, striker, currentInnNum).orElse(null);
            strikerDto = LiveScoreUpdateDTO.BatsmanLiveDTO.builder()
                    .playerId(striker.getId())
                    .name(striker.getName())
                    .runs(stats != null ? stats.getRunsScored() : 0)
                    .balls(stats != null ? stats.getBallsFaced() : 0)
                    .fours(stats != null ? stats.getFours() : 0)
                    .sixes(stats != null ? stats.getSixes() : 0)
                    .strikeRate(stats != null ? stats.getBattingStrikeRate() : 0.0)
                    .isStriker(true)
                    .build();
        }

        LiveScoreUpdateDTO.BatsmanLiveDTO nonStrikerDto = null;
        if (match.getNonStriker() != null) {
            Player nonStriker = match.getNonStriker();
            PlayerMatchStats stats = playerMatchStatsRepository
                    .findByMatchAndPlayerAndInningsNumber(match, nonStriker, currentInnNum).orElse(null);
            nonStrikerDto = LiveScoreUpdateDTO.BatsmanLiveDTO.builder()
                    .playerId(nonStriker.getId())
                    .name(nonStriker.getName())
                    .runs(stats != null ? stats.getRunsScored() : 0)
                    .balls(stats != null ? stats.getBallsFaced() : 0)
                    .fours(stats != null ? stats.getFours() : 0)
                    .sixes(stats != null ? stats.getSixes() : 0)
                    .strikeRate(stats != null ? stats.getBattingStrikeRate() : 0.0)
                    .isStriker(false)
                    .build();
        }

        // Active Bowler
        LiveScoreUpdateDTO.BowlerLiveDTO bowlerDto = null;
        if (match.getCurrentBowler() != null) {
            Player bowler = match.getCurrentBowler();
            PlayerMatchStats stats = playerMatchStatsRepository
                    .findByMatchAndPlayerAndInningsNumber(match, bowler, currentInnNum).orElse(null);
            bowlerDto = LiveScoreUpdateDTO.BowlerLiveDTO.builder()
                    .playerId(bowler.getId())
                    .name(bowler.getName())
                    .overs(stats != null ? stats.getFormattedOversBowled() : "0.0")
                    .maidens(stats != null ? stats.getMaidenOvers() : 0)
                    .runsConceded(stats != null ? stats.getRunsConceded() : 0)
                    .wickets(stats != null ? stats.getWicketsTaken() : 0)
                    .economy(stats != null ? stats.getBowlingEconomy() : 0.0)
                    .build();
        }

        // Recent balls in the over
        List<String> currentOver = parseCurrentOverBalls(match.getCurrentOverBalls());

        // Latest commentary
        List<BallEvent> recentBalls = ballEventRepository.findRecentBalls(matchId);
        String lastCommentary = recentBalls.isEmpty() ? "Match underway." : recentBalls.get(0).getCommentary();
        String lastEvent = recentBalls.isEmpty() ? "" : formatBallBadge(recentBalls.get(0));

        double batWinProb = match.getBattingTeamWinProbability() != null ? match.getBattingTeamWinProbability() : 50.0;
        double bowlWinProb = BigDecimal.valueOf(100.0 - batWinProb).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return LiveScoreUpdateDTO.builder()
                .matchId(match.getId())
                .title(match.getTitle())
                .venue(match.getVenue())
                .status(match.getStatus())
                .statusMessage(getStatusMessage(match, currentInnings))
                .currentInnings(currentInnNum)
                .totalOvers(match.getTotalOvers())
                .battingTeamId(battingTeam != null ? battingTeam.getId() : null)
                .battingTeamName(battingTeam != null ? battingTeam.getName() : "")
                .battingTeamShort(battingTeam != null ? battingTeam.getShortName() : "")
                .battingTeamFlag(battingTeam != null ? battingTeam.getFlagEmoji() : "")
                .runs(runs)
                .wickets(wickets)
                .legalBalls(balls)
                .overs(overs)
                .currentRunRate(crr)
                .targetRuns(targetRuns)
                .runsNeeded(runsNeeded)
                .ballsRemaining(ballsRemaining)
                .requiredRunRate(rrr)
                .bowlingTeamId(bowlingTeam != null ? bowlingTeam.getId() : null)
                .bowlingTeamName(bowlingTeam != null ? bowlingTeam.getName() : "")
                .bowlingTeamShort(bowlingTeam != null ? bowlingTeam.getShortName() : "")
                .bowlingTeamFlag(bowlingTeam != null ? bowlingTeam.getFlagEmoji() : "")
                .firstInningsScore(firstInningsScoreStr)
                .striker(strikerDto)
                .nonStriker(nonStrikerDto)
                .currentBowler(bowlerDto)
                .currentOverBalls(currentOver)
                .battingTeamWinProbability(batWinProb)
                .bowlingTeamWinProbability(bowlWinProb)
                .lastCommentary(lastCommentary)
                .lastEvent(lastEvent)
                .resultMessage(match.getResultMessage())
                .build();
    }

    @Transactional(readOnly = true)
    public ScorecardDTO getScorecard(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        ScorecardDTO.InningsScorecardDTO inn1Card = buildInningsScorecard(match, 1);
        ScorecardDTO.InningsScorecardDTO inn2Card = buildInningsScorecard(match, 2);

        String tossStr = match.getTossWinner() != null ?
                match.getTossWinner().getName() + " won the toss and elected to " +
                        (match.getTossDecision() != null ? match.getTossDecision().name().toLowerCase() : "bat") : "Toss pending";

        return ScorecardDTO.builder()
                .matchId(match.getId())
                .title(match.getTitle())
                .venue(match.getVenue())
                .tossInfo(tossStr)
                .resultMessage(match.getResultMessage())
                .firstInnings(inn1Card)
                .secondInnings(inn2Card)
                .build();
    }

    private ScorecardDTO.InningsScorecardDTO buildInningsScorecard(Match match, int inningsNumber) {
        Innings innings = inningsRepository.findByMatchAndInningsNumber(match, inningsNumber).orElse(null);
        if (innings == null) return null;

        List<PlayerMatchStats> allStats = playerMatchStatsRepository.findByMatchAndInningsNumber(match, inningsNumber);

        // Batting rows (hasBatted or player belongs to batting team)
        List<ScorecardDTO.BattingCardRowDTO> battingRows = allStats.stream()
                .filter(s -> s.getPlayer().getTeam().getId().equals(innings.getBattingTeam().getId()))
                .filter(s -> Boolean.TRUE.equals(s.getHasBatted()))
                .sorted(Comparator.comparingInt(PlayerMatchStats::getBattingOrder))
                .map(s -> {
                    String dismissalInfo;
                    if (Boolean.TRUE.equals(s.getIsOut())) {
                        switch (s.getDismissalType()) {
                            case BOWLED -> dismissalInfo = "b " + s.getBowlerName();
                            case CAUGHT -> dismissalInfo = "c " + (s.getFielderName() != null ? s.getFielderName() : "sub") + " b " + s.getBowlerName();
                            case LBW -> dismissalInfo = "lbw b " + s.getBowlerName();
                            case RUN_OUT -> dismissalInfo = "run out (" + s.getFielderName() + ")";
                            case STUMPED -> dismissalInfo = "st " + s.getFielderName() + " b " + s.getBowlerName();
                            default -> dismissalInfo = "out";
                        }
                    } else {
                        dismissalInfo = "not out";
                    }

                    boolean isCurrent = match.getStatus() == MatchStatus.LIVE &&
                            match.getCurrentInningsNumber() == inningsNumber &&
                            ((match.getStriker() != null && match.getStriker().getId().equals(s.getPlayer().getId())) ||
                             (match.getNonStriker() != null && match.getNonStriker().getId().equals(s.getPlayer().getId())));

                    return ScorecardDTO.BattingCardRowDTO.builder()
                            .playerId(s.getPlayer().getId())
                            .playerName(s.getPlayer().getName())
                            .role(s.getPlayer().getRole().name())
                            .dismissalType(s.getDismissalType())
                            .dismissalInfo(dismissalInfo)
                            .runs(s.getRunsScored())
                            .balls(s.getBallsFaced())
                            .fours(s.getFours())
                            .sixes(s.getSixes())
                            .strikeRate(s.getBattingStrikeRate())
                            .isOut(s.getIsOut())
                            .isCurrentlyBatting(isCurrent)
                            .build();
                }).collect(Collectors.toList());

        // Bowling rows (hasBowled)
        List<ScorecardDTO.BowlingCardRowDTO> bowlingRows = allStats.stream()
                .filter(s -> Boolean.TRUE.equals(s.getHasBowled()))
                .sorted(Comparator.comparingInt(PlayerMatchStats::getWicketsTaken).reversed())
                .map(s -> ScorecardDTO.BowlingCardRowDTO.builder()
                        .playerId(s.getPlayer().getId())
                        .bowlerName(s.getPlayer().getName())
                        .overs(s.getFormattedOversBowled())
                        .maidens(s.getMaidenOvers())
                        .runsConceded(s.getRunsConceded())
                        .wickets(s.getWicketsTaken())
                        .economy(s.getBowlingEconomy())
                        .dotBalls(s.getDotsConceded())
                        .build())
                .collect(Collectors.toList());

        // Fall of wickets
        List<BallEvent> wicketBalls = ballEventRepository.findByMatchAndInningsNumberOrderByTimestampAsc(match, inningsNumber)
                .stream().filter(BallEvent::getIsWicket).toList();

        List<ScorecardDTO.FallOfWicketDTO> fowList = new ArrayList<>();
        int wktIndex = 1;
        for (BallEvent wb : wicketBalls) {
            fowList.add(ScorecardDTO.FallOfWicketDTO.builder()
                    .wicketNumber(wktIndex++)
                    .score(wb.getScoreAtBall() != null ? parseRuns(wb.getScoreAtBall()) : 0)
                    .playerOutName(wb.getDismissedPlayer() != null ? wb.getDismissedPlayer().getName() : "Batter")
                    .over(wb.getBallDisplay())
                    .build());
        }

        String extrasBreakdown = String.format("b %d, lb %d, w %d, nb %d",
                innings.getByes(), innings.getLegByes(), innings.getWides(), innings.getNoBalls());

        return ScorecardDTO.InningsScorecardDTO.builder()
                .inningsNumber(inningsNumber)
                .teamName(innings.getBattingTeam().getName())
                .teamShort(innings.getBattingTeam().getShortName())
                .teamFlag(innings.getBattingTeam().getFlagEmoji())
                .totalRuns(innings.getTotalRuns())
                .wickets(innings.getWickets())
                .overs(innings.getFormattedOvers())
                .runRate(innings.getCurrentRunRate())
                .extras(innings.getTotalExtras())
                .extrasBreakdown(extrasBreakdown)
                .batting(battingRows)
                .bowling(bowlingRows)
                .fallOfWickets(fowList)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BallEvent> getMatchCommentary(Long matchId) {
        return ballEventRepository.findByMatchIdOrderByTimestampDesc(matchId);
    }

    @Transactional(readOnly = true)
    public OverSummaryDTO getOverProgression(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        List<OverSummaryDTO.InningsOverProgressionDTO> inn1 = computeProgression(match, 1);
        List<OverSummaryDTO.InningsOverProgressionDTO> inn2 = computeProgression(match, 2);

        return OverSummaryDTO.builder()
                .matchId(matchId)
                .innings1Progression(inn1)
                .innings2Progression(inn2)
                .build();
    }

    private List<OverSummaryDTO.InningsOverProgressionDTO> computeProgression(Match match, int inningsNumber) {
        List<BallEvent> balls = ballEventRepository.findByMatchAndInningsNumberOrderByTimestampAsc(match, inningsNumber);
        if (balls.isEmpty()) return Collections.emptyList();

        Map<Integer, List<BallEvent>> ballsByOver = balls.stream()
                .collect(Collectors.groupingBy(BallEvent::getOverNumber, TreeMap::new, Collectors.toList()));

        List<OverSummaryDTO.InningsOverProgressionDTO> list = new ArrayList<>();
        int cumulativeRuns = 0;
        int cumulativeWickets = 0;

        for (Map.Entry<Integer, List<BallEvent>> entry : ballsByOver.entrySet()) {
            int overNum = entry.getKey() + 1; // 1-based display
            int runsInOver = entry.getValue().stream().mapToInt(BallEvent::getTotalBallRuns).sum();
            int wktsInOver = (int) entry.getValue().stream().filter(BallEvent::getIsWicket).count();

            cumulativeRuns += runsInOver;
            cumulativeWickets += wktsInOver;

            double rr = overNum > 0 ? (double) cumulativeRuns / overNum : 0.0;
            String bowlerName = entry.getValue().isEmpty() ? "" :
                    (entry.getValue().get(0).getBowler() != null ? entry.getValue().get(0).getBowler().getName() : "");

            list.add(OverSummaryDTO.InningsOverProgressionDTO.builder()
                    .overNumber(overNum)
                    .runsInOver(runsInOver)
                    .cumulativeRuns(cumulativeRuns)
                    .wicketsInOver(wktsInOver)
                    .cumulativeWickets(cumulativeWickets)
                    .runRate(BigDecimal.valueOf(rr).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .bowlerName(bowlerName)
                    .build());
        }

        return list;
    }

    private MatchSummaryDTO convertToSummaryDTO(Match match) {
        Innings inn1 = inningsRepository.findByMatchAndInningsNumber(match, 1).orElse(null);
        Innings inn2 = inningsRepository.findByMatchAndInningsNumber(match, 2).orElse(null);

        String team1Score = null;
        String team2Score = null;

        if (inn1 != null) {
            if (inn1.getBattingTeam().getId().equals(match.getTeam1().getId())) {
                team1Score = inn1.getTotalRuns() + "/" + inn1.getWickets() + " (" + inn1.getFormattedOvers() + ")";
            } else {
                team2Score = inn1.getTotalRuns() + "/" + inn1.getWickets() + " (" + inn1.getFormattedOvers() + ")";
            }
        }

        if (inn2 != null) {
            if (inn2.getBattingTeam().getId().equals(match.getTeam1().getId())) {
                team1Score = inn2.getTotalRuns() + "/" + inn2.getWickets() + " (" + inn2.getFormattedOvers() + ")";
            } else {
                team2Score = inn2.getTotalRuns() + "/" + inn2.getWickets() + " (" + inn2.getFormattedOvers() + ")";
            }
        }

        Innings currentInnings = match.getCurrentInningsNumber() == 2 ? inn2 : inn1;

        Double rrr = null;
        if (match.getCurrentInningsNumber() == 2 && inn2 != null && match.getTargetRuns() != null) {
            int runsNeeded = Math.max(0, match.getTargetRuns() - inn2.getTotalRuns());
            int ballsLeft = Math.max(0, (match.getTotalOvers() * 6) - inn2.getTotalBalls());
            if (ballsLeft > 0) {
                rrr = BigDecimal.valueOf((runsNeeded * 6.0) / ballsLeft).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
        }

        return MatchSummaryDTO.builder()
                .id(match.getId())
                .title(match.getTitle())
                .series(match.getSeries())
                .venue(match.getVenue())
                .matchType(match.getMatchType())
                .status(match.getStatus())
                .matchDate(match.getMatchDate())
                .team1Id(match.getTeam1().getId())
                .team1Name(match.getTeam1().getName())
                .team1ShortName(match.getTeam1().getShortName())
                .team1Flag(match.getTeam1().getFlagEmoji())
                .team1Score(team1Score != null ? team1Score : "Yet to bat")
                .team2Id(match.getTeam2().getId())
                .team2Name(match.getTeam2().getName())
                .team2ShortName(match.getTeam2().getShortName())
                .team2Flag(match.getTeam2().getFlagEmoji())
                .team2Score(team2Score != null ? team2Score : "Yet to bat")
                .currentBattingTeamShort(match.getBattingTeam() != null ? match.getBattingTeam().getShortName() : "")
                .currentScore(currentInnings != null ? currentInnings.getTotalRuns() + "/" + currentInnings.getWickets() : "")
                .currentOvers(currentInnings != null ? currentInnings.getFormattedOvers() : "")
                .currentRunRate(currentInnings != null ? currentInnings.getCurrentRunRate() : 0.0)
                .requiredRunRate(rrr)
                .resultMessage(match.getResultMessage())
                .statusText(getStatusMessage(match, currentInnings))
                .build();
    }

    private String getStatusMessage(Match match, Innings currentInnings) {
        if (match.getStatus() == MatchStatus.COMPLETED) {
            return match.getResultMessage() != null ? match.getResultMessage() : "Match Finished";
        }
        if (match.getStatus() == MatchStatus.INNINGS_BREAK) {
            return "Innings Break - Target: " + match.getTargetRuns();
        }
        if (match.getStatus() == MatchStatus.UPCOMING) {
            return "Starts soon • " + match.getVenue();
        }
        if (match.getCurrentInningsNumber() == 2 && match.getTargetRuns() != null && currentInnings != null) {
            int needed = Math.max(0, match.getTargetRuns() - currentInnings.getTotalRuns());
            int ballsLeft = Math.max(0, (match.getTotalOvers() * 6) - currentInnings.getTotalBalls());
            return match.getBattingTeam().getShortName() + " need " + needed + " runs in " + ballsLeft + " balls";
        }
        return match.getBattingTeam() != null ? match.getBattingTeam().getName() + " are batting" : "In Progress";
    }

    private List<String> parseCurrentOverBalls(String currentOverBalls) {
        if (currentOverBalls == null || currentOverBalls.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(currentOverBalls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String formatBallBadge(BallEvent ball) {
        if (Boolean.TRUE.equals(ball.getIsWicket())) return "WICKET";
        if (ball.getRunsOffBat() != null && ball.getRunsOffBat() == 6) return "SIX";
        if (ball.getRunsOffBat() != null && ball.getRunsOffBat() == 4) return "FOUR";
        if (ball.getExtrasRuns() != null && ball.getExtrasRuns() > 0) return ball.getExtraType().name();
        return ball.getRunsOffBat() + " RUN" + (ball.getRunsOffBat() == 1 ? "" : "S");
    }

    private int parseRuns(String scoreAtBall) {
        try {
            if (scoreAtBall.contains("/")) {
                return Integer.parseInt(scoreAtBall.split("/")[0].trim());
            }
            return Integer.parseInt(scoreAtBall.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
