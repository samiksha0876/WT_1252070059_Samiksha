package com.cricket.scoreboard.entity;

import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.enums.MatchType;
import com.cricket.scoreboard.enums.TossDecision;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g. "ICC T20 World Cup Super 8 - Match 42"

    private String series; // e.g. "ICC Men's T20 World Cup 2026"
    private String venue; // e.g. "Kensington Oval, Bridgetown, Barbados"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MatchType matchType = MatchType.T20;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MatchStatus status = MatchStatus.LIVE;

    @Builder.Default
    private LocalDateTime matchDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "toss_winner_id")
    private Team tossWinner;

    @Enumerated(EnumType.STRING)
    private TossDecision tossDecision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batting_team_id")
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowling_team_id")
    private Team bowlingTeam;

    @Builder.Default
    private Integer currentInningsNumber = 1;

    @Builder.Default
    private Integer totalOvers = 20;

    private Integer targetRuns; // Set in 2nd innings

    private String resultMessage; // e.g. "India won by 24 runs"
    private String winnerTeamName;

    // Currently active players on the pitch
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "striker_id")
    private Player striker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "non_striker_id")
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_bowler_id")
    private Player currentBowler;

    // Recent ball outcomes in the current over (e.g. "1, 0, 4, W, 6, 2")
    private String currentOverBalls;

    // Win probability for batting team (0.0 to 100.0)
    @Builder.Default
    private Double battingTeamWinProbability = 50.0;
}
