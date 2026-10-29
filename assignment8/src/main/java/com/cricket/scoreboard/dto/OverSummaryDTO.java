package com.cricket.scoreboard.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverSummaryDTO {
    private Long matchId;
    private List<InningsOverProgressionDTO> innings1Progression;
    private List<InningsOverProgressionDTO> innings2Progression;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InningsOverProgressionDTO {
        private Integer overNumber; // 1, 2, 3...
        private Integer runsInOver;
        private Integer cumulativeRuns;
        private Integer wicketsInOver;
        private Integer cumulativeWickets;
        private Double runRate;
        private String bowlerName;
    }
}
