package com.cricket.scoreboard.dto;

import com.cricket.scoreboard.enums.MatchStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveScoreUpdateDTO {
    private Long matchId;
    private String title;
    private String venue;
    private MatchStatus status;
    private String statusMessage;
    private Integer currentInnings;
    private Integer totalOvers;

    // Batting team details
    private Long battingTeamId;
    private String battingTeamName;
    private String battingTeamShort;
    private String battingTeamFlag;
    private Integer runs;
    private Integer wickets;
    private Integer legalBalls;
    private String overs; // e.g. "16.4"
    private Double currentRunRate;

    // Target & Required Run Rate (2nd innings)
    private Integer targetRuns;
    private Integer runsNeeded;
    private Integer ballsRemaining;
    private Double requiredRunRate;

    // Bowling team details
    private Long bowlingTeamId;
    private String bowlingTeamName;
    private String bowlingTeamShort;
    private String bowlingTeamFlag;

    // 1st Innings score if in 2nd innings
    private String firstInningsScore;

    // Active Batters
    private BatsmanLiveDTO striker;
    private BatsmanLiveDTO nonStriker;

    // Current Bowler
    private BowlerLiveDTO currentBowler;

    // Current over deliveries (e.g. ["1", "4", "0", "W", "6", "2"])
    private List<String> currentOverBalls;

    // Win probability
    private Double battingTeamWinProbability;
    private Double bowlingTeamWinProbability;

    // Latest ball event commentary
    private String lastCommentary;
    private String lastEvent; // e.g. "FOUR", "SIX", "WICKET", "1 RUN"

    private String resultMessage;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatsmanLiveDTO {
        private Long playerId;
        private String name;
        private Integer runs;
        private Integer balls;
        private Integer fours;
        private Integer sixes;
        private Double strikeRate;
        private Boolean isStriker;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BowlerLiveDTO {
        private Long playerId;
        private String name;
        private String overs;
        private Integer maidens;
        private Integer runsConceded;
        private Integer wickets;
        private Double economy;
    }
}
