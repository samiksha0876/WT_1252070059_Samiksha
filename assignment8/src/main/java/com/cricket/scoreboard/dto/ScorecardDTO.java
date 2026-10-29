package com.cricket.scoreboard.dto;

import com.cricket.scoreboard.enums.DismissalType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScorecardDTO {
    private Long matchId;
    private String title;
    private String venue;
    private String tossInfo;
    private String resultMessage;
    private InningsScorecardDTO firstInnings;
    private InningsScorecardDTO secondInnings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InningsScorecardDTO {
        private Integer inningsNumber;
        private String teamName;
        private String teamShort;
        private String teamFlag;
        private Integer totalRuns;
        private Integer wickets;
        private String overs;
        private Double runRate;
        private Integer extras;
        private String extrasBreakdown; // e.g. "b 2, lb 4, w 5, nb 1"
        private List<BattingCardRowDTO> batting;
        private List<BowlingCardRowDTO> bowling;
        private List<FallOfWicketDTO> fallOfWickets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BattingCardRowDTO {
        private Long playerId;
        private String playerName;
        private String role;
        private DismissalType dismissalType;
        private String dismissalInfo; // e.g. "c Maxwell b Starc" or "not out"
        private Integer runs;
        private Integer balls;
        private Integer fours;
        private Integer sixes;
        private Double strikeRate;
        private Boolean isOut;
        private Boolean isCurrentlyBatting;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BowlingCardRowDTO {
        private Long playerId;
        private String bowlerName;
        private String overs;
        private Integer maidens;
        private Integer runsConceded;
        private Integer wickets;
        private Double economy;
        private Integer dotBalls;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FallOfWicketDTO {
        private Integer wicketNumber;
        private Integer score;
        private String playerOutName;
        private String over;
    }
}
