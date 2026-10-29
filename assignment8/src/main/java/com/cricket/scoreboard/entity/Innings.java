package com.cricket.scoreboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "innings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Innings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnore
    private Match match;

    @Column(nullable = false)
    private Integer inningsNumber; // 1 or 2

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batting_team_id", nullable = false)
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    private Team bowlingTeam;

    @Builder.Default
    private Integer totalRuns = 0;

    @Builder.Default
    private Integer wickets = 0;

    @Builder.Default
    private Integer totalBalls = 0; // Legal deliveries completed

    @Builder.Default
    private Integer wides = 0;

    @Builder.Default
    private Integer noBalls = 0;

    @Builder.Default
    private Integer byes = 0;

    @Builder.Default
    private Integer legByes = 0;

    @Builder.Default
    private Integer penaltyRuns = 0;

    @Builder.Default
    private Boolean isCompleted = false;

    public String getFormattedOvers() {
        int completedOvers = totalBalls / 6;
        int ballsInCurrentOver = totalBalls % 6;
        return completedOvers + "." + ballsInCurrentOver;
    }

    public Double getCurrentRunRate() {
        if (totalBalls == null || totalBalls == 0) {
            return 0.0;
        }
        double overs = (totalBalls / 6) + ((totalBalls % 6) / 6.0);
        if (overs == 0) return 0.0;
        BigDecimal bd = BigDecimal.valueOf(totalRuns / overs);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Integer getTotalExtras() {
        return (wides != null ? wides : 0) +
               (noBalls != null ? noBalls : 0) +
               (byes != null ? byes : 0) +
               (legByes != null ? legByes : 0) +
               (penaltyRuns != null ? penaltyRuns : 0);
    }
}
