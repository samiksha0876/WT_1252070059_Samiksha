package com.cricket.scoreboard.dto;

import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.enums.MatchType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSummaryDTO {
    private Long id;
    private String title;
    private String series;
    private String venue;
    private MatchType matchType;
    private MatchStatus status;
    private LocalDateTime matchDate;

    // Team 1
    private Long team1Id;
    private String team1Name;
    private String team1ShortName;
    private String team1Flag;
    private String team1Score; // e.g. "186/5 (20.0)"

    // Team 2
    private Long team2Id;
    private String team2Name;
    private String team2ShortName;
    private String team2Flag;
    private String team2Score; // e.g. "145/3 (15.2)"

    private String currentBattingTeamShort;
    private String currentScore;
    private String currentOvers;
    private Double currentRunRate;
    private Double requiredRunRate;
    private String resultMessage;
    private String statusText;
}
