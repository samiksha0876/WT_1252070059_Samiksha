package com.cricket.scoreboard.entity;

import com.cricket.scoreboard.enums.DismissalType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "player_match_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlayerMatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnore
    private Match match;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer inningsNumber; // 1 or 2

    // Batting stats
    @Builder.Default
    private Integer battingOrder = 0;

    @Builder.Default
    private Boolean hasBatted = false;

    @Builder.Default
    private Integer runsScored = 0;

    @Builder.Default
    private Integer ballsFaced = 0;

    @Builder.Default
    private Integer fours = 0;

    @Builder.Default
    private Integer sixes = 0;

    @Builder.Default
    private Boolean isOut = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DismissalType dismissalType = DismissalType.NOT_OUT;

    private String bowlerName;
    private String fielderName;

    // Bowling stats
    @Builder.Default
    private Boolean hasBowled = false;

    @Builder.Default
    private Integer oversBowledBalls = 0; // Number of legal deliveries bowled

    @Builder.Default
    private Integer maidenOvers = 0;

    @Builder.Default
    private Integer runsConceded = 0;

    @Builder.Default
    private Integer wicketsTaken = 0;

    @Builder.Default
    private Integer dotsConceded = 0;

    public Double getBattingStrikeRate() {
        if (ballsFaced == null || ballsFaced == 0) return 0.0;
        BigDecimal bd = BigDecimal.valueOf((runsScored * 100.0) / ballsFaced);
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getFormattedOversBowled() {
        if (oversBowledBalls == null || oversBowledBalls == 0) return "0.0";
        int overs = oversBowledBalls / 6;
        int balls = oversBowledBalls % 6;
        return overs + "." + balls;
    }

    public Double getBowlingEconomy() {
        if (oversBowledBalls == null || oversBowledBalls == 0) return 0.0;
        double overs = (oversBowledBalls / 6) + ((oversBowledBalls % 6) / 6.0);
        if (overs == 0) return 0.0;
        BigDecimal bd = BigDecimal.valueOf(runsConceded / overs);
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
