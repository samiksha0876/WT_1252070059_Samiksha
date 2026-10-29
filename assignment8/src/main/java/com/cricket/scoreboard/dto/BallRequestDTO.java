package com.cricket.scoreboard.dto;

import com.cricket.scoreboard.enums.DismissalType;
import com.cricket.scoreboard.enums.ExtraType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallRequestDTO {
    private Long matchId;
    private Integer runsOffBat;
    private ExtraType extraType;
    private Integer extrasRuns;
    private Boolean isWicket;
    private DismissalType dismissalType;
    private Long dismissedPlayerId;
    private String customCommentary;
}
