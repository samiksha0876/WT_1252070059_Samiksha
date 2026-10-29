package com.cricket.scoreboard.entity;

import com.cricket.scoreboard.enums.PlayerRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRole role;

    private String battingStyle; // e.g. "Right-hand bat"
    private String bowlingStyle; // e.g. "Right-arm fast"
    private Integer jerseyNumber;

    // Career / Tournament aggregated statistics
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Builder.Default
    private Integer totalRuns = 0;

    @Builder.Default
    private Integer ballsFaced = 0;

    @Builder.Default
    private Integer fours = 0;

    @Builder.Default
    private Integer sixes = 0;

    @Builder.Default
    private Integer highestScore = 0;

    @Builder.Default
    private Double battingStrikeRate = 0.0;

    @Builder.Default
    private Double battingAverage = 0.0;

    @Builder.Default
    private Integer wicketsTaken = 0;

    @Builder.Default
    private Integer ballsBowled = 0;

    @Builder.Default
    private Integer runsConceded = 0;

    @Builder.Default
    private Double bowlingEconomy = 0.0;

    @Builder.Default
    private Boolean isPlayingXI = true;
}
