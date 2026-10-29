package com.cricket.scoreboard.entity;

import com.cricket.scoreboard.enums.DismissalType;
import com.cricket.scoreboard.enums.ExtraType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ball_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BallEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnore
    private Match match;

    @Column(nullable = false)
    private Integer inningsNumber;

    @Column(nullable = false)
    private Integer overNumber; // 0-indexed or 1-indexed (e.g. 19 for 19.x)

    @Column(nullable = false)
    private Integer ballNumberInOver; // 1 to 6 (or beyond for no-balls/wides)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batsman_id")
    private Player batsman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "non_striker_id")
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowler_id")
    private Player bowler;

    @Builder.Default
    private Integer runsOffBat = 0;

    @Builder.Default
    private Integer extrasRuns = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExtraType extraType = ExtraType.NONE;

    @Builder.Default
    private Boolean isWicket = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DismissalType dismissalType = DismissalType.NOT_OUT;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dismissed_player_id")
    private Player dismissedPlayer;

    @Column(length = 1000)
    private String commentary;

    private String scoreAtBall; // e.g. "186/4"
    private String ballDisplay; // e.g. "18.4"

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public Integer getTotalBallRuns() {
        return (runsOffBat != null ? runsOffBat : 0) + (extrasRuns != null ? extrasRuns : 0);
    }
}
