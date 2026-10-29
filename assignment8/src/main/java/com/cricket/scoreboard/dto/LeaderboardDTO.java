package com.cricket.scoreboard.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardDTO {
    private List<PlayerLeaderboardRow> orangeCap; // Top run scorers
    private List<PlayerLeaderboardRow> purpleCap; // Top wicket takers
    private List<PlayerLeaderboardRow> topStrikeRates;
    private List<PlayerLeaderboardRow> bestEconomy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerLeaderboardRow {
        private Long playerId;
        private String playerName;
        private String teamShort;
        private String teamFlag;
        private String role;
        private Integer matches;
        private Integer runs;
        private Integer ballsFaced;
        private Integer fours;
        private Integer sixes;
        private Integer highestScore;
        private Double strikeRate;
        private Double average;
        private Integer wickets;
        private Integer ballsBowled;
        private Integer runsConceded;
        private Double economy;
    }
}
