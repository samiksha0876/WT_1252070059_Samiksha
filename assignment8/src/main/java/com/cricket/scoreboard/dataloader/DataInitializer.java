package com.cricket.scoreboard.dataloader;

import com.cricket.scoreboard.entity.*;
import com.cricket.scoreboard.enums.*;
import com.cricket.scoreboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerMatchStatsRepository statsRepository;
    private final BallEventRepository ballRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (teamRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data load.");
            return;
        }

        log.info("Seeding Cricket Tournament Data: Teams, Players, Live Matches, and Scorecards...");

        // 1. Teams
        Team ind = createTeam("India", "IND", "IN", "🇮🇳", "#0077b6");
        Team aus = createTeam("Australia", "AUS", "AU", "🇦🇺", "#e0a96d");
        Team eng = createTeam("England", "ENG", "GB", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#c1121f");
        Team sa  = createTeam("South Africa", "SA", "ZA", "🇿🇦", "#2a9d8f");
        Team pak = createTeam("Pakistan", "PAK", "PK", "🇵🇰", "#1b4332");
        Team nz  = createTeam("New Zealand", "NZ", "NZ", "🇳🇿", "#14213d");

        // 2. India Players
        Player rohit = createPlayer("Rohit Sharma (C)", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 45, 8, 385, 240, 36, 22, 92, 160.42, 48.12, 0, 0, 0, 0.0);
        Player kohli = createPlayer("Virat Kohli", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", 18, 8, 296, 210, 28, 12, 76, 140.95, 42.28, 0, 0, 0, 0.0);
        Player pant  = createPlayer("Rishabh Pant (WK)", ind, PlayerRole.WICKET_KEEPER, "Left-hand bat", "None", 17, 8, 214, 155, 20, 10, 52, 138.06, 35.66, 0, 0, 0, 0.0);
        Player sky   = createPlayer("Suryakumar Yadav", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 63, 8, 280, 172, 24, 18, 53, 162.79, 40.0, 0, 0, 0, 0.0);
        Player dube  = createPlayer("Shivam Dube", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Right-arm medium", 25, 8, 145, 105, 12, 9, 34, 138.09, 29.0, 2, 36, 42, 7.0);
        Player hardik= createPlayer("Hardik Pandya", ind, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm fast-medium", 33, 8, 185, 118, 14, 12, 50, 156.77, 37.0, 11, 144, 178, 7.41);
        Player jadeja= createPlayer("Ravindra Jadeja", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 8, 8, 85, 65, 8, 3, 22, 130.76, 21.25, 7, 132, 162, 7.36);
        Player axar  = createPlayer("Axar Patel", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 20, 8, 92, 72, 7, 4, 47, 127.77, 23.0, 9, 120, 145, 7.25);
        Player kuldeep=createPlayer("Kuldeep Yadav", ind, PlayerRole.BOWLER, "Left-hand bat", "Left-arm wrist spin", 23, 7, 12, 15, 1, 0, 8, 80.0, 6.0, 13, 138, 156, 6.78);
        Player arshdeep=createPlayer("Arshdeep Singh", ind, PlayerRole.BOWLER, "Left-hand bat", "Left-arm fast-medium", 2, 8, 10, 14, 1, 0, 6, 71.42, 5.0, 17, 180, 215, 7.16);
        Player bumrah= createPlayer("Jasprit Bumrah", ind, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 93, 8, 8, 12, 1, 0, 5, 66.66, 4.0, 15, 178, 124, 4.17);

        // 3. Australia Players
        Player head  = createPlayer("Travis Head", aus, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", 62, 7, 348, 210, 38, 18, 89, 165.71, 49.71, 1, 18, 25, 8.33);
        Player warner= createPlayer("David Warner", aus, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm legbreak", 31, 7, 178, 132, 19, 7, 56, 134.84, 25.42, 0, 0, 0, 0.0);
        Player marsh = createPlayer("Mitchell Marsh (C)", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm medium", 8, 7, 188, 140, 16, 9, 44, 134.28, 31.33, 3, 42, 58, 8.28);
        Player maxwell=createPlayer("Glenn Maxwell", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm offbreak", 32, 7, 162, 108, 15, 11, 59, 150.0, 27.0, 6, 96, 122, 7.62);
        Player stoinis=createPlayer("Marcus Stoinis", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm fast-medium", 17, 7, 210, 135, 18, 12, 67, 155.55, 42.0, 10, 114, 148, 7.78);
        Player david = createPlayer("Tim David", aus, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 85, 7, 130, 82, 9, 9, 37, 158.53, 26.0, 0, 0, 0, 0.0);
        Player wade  = createPlayer("Matthew Wade (WK)", aus, PlayerRole.WICKET_KEEPER, "Left-hand bat", "None", 13, 7, 95, 70, 8, 4, 28, 135.71, 23.75, 0, 0, 0, 0.0);
        Player cummins=createPlayer("Pat Cummins", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm fast", 30, 7, 68, 45, 5, 3, 28, 151.11, 17.0, 11, 156, 188, 7.23);
        Player starc = createPlayer("Mitchell Starc", aus, PlayerRole.BOWLER, "Left-hand bat", "Left-arm fast", 56, 7, 25, 20, 2, 1, 14, 125.0, 8.33, 12, 150, 195, 7.8);
        Player zampa = createPlayer("Adam Zampa", aus, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 88, 7, 6, 8, 0, 0, 4, 75.0, 3.0, 14, 168, 184, 6.57);
        Player hazelwood=createPlayer("Josh Hazlewood", aus, PlayerRole.BOWLER, "Left-hand bat", "Right-arm fast-medium", 38, 7, 4, 6, 0, 0, 2, 66.66, 2.0, 8, 156, 172, 6.61);

        // 4. England Players
        Player buttler=createPlayer("Jos Buttler (C/WK)", eng, PlayerRole.WICKET_KEEPER, "Right-hand bat", "None", 63, 7, 285, 185, 30, 14, 83, 154.05, 47.5, 0, 0, 0, 0.0);
        Player salt   =createPlayer("Phil Salt", eng, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 42, 7, 248, 148, 25, 15, 87, 167.56, 41.33, 0, 0, 0, 0.0);
        Player brook  =createPlayer("Harry Brook", eng, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", 88, 7, 215, 142, 19, 10, 53, 151.4, 43.0, 0, 0, 0, 0.0);
        Player livingstone=createPlayer("Liam Livingstone", eng, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm legbreak", 23, 7, 120, 78, 10, 8, 33, 153.84, 24.0, 4, 66, 82, 7.45);
        Player archer =createPlayer("Jofra Archer", eng, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 22, 7, 18, 14, 2, 1, 12, 128.57, 9.0, 12, 156, 178, 6.84);
        Player rashid =createPlayer("Adil Rashid", eng, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 95, 7, 14, 16, 1, 0, 8, 87.5, 4.66, 11, 168, 180, 6.42);

        // 5. South Africa Players
        Player qdk    =createPlayer("Quinton de Kock (WK)", sa, PlayerRole.WICKET_KEEPER, "Left-hand bat", "None", 12, 7, 260, 180, 26, 12, 74, 144.44, 37.14, 0, 0, 0, 0.0);
        Player markram=createPlayer("Aiden Markram (C)", sa, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 4, 7, 195, 145, 18, 8, 46, 134.48, 32.5, 2, 48, 56, 7.0);
        Player klaasen=createPlayer("Heinrich Klaasen", sa, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 45, 7, 235, 150, 20, 16, 52, 156.66, 39.16, 0, 0, 0, 0.0);
        Player rabada =createPlayer("Kagiso Rabada", sa, PlayerRole.BOWLER, "Left-hand bat", "Right-arm fast", 25, 7, 16, 12, 2, 1, 8, 133.33, 8.0, 13, 158, 182, 6.91);
        Player nortje =createPlayer("Anrich Nortje", sa, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 20, 7, 8, 10, 0, 0, 4, 80.0, 2.66, 15, 162, 175, 6.48);

        // ==========================================
        // MATCH 1: India vs Australia (THRILLING LIVE CHASE!)
        // ==========================================
        Match match1 = Match.builder()
                .title("ICC T20 World Cup - Super 8 Clash")
                .series("ICC Men's T20 World Cup 2026")
                .venue("Kensington Oval, Bridgetown, Barbados")
                .matchType(MatchType.T20)
                .status(MatchStatus.LIVE)
                .matchDate(LocalDateTime.now().minusHours(2))
                .team1(ind)
                .team2(aus)
                .tossWinner(aus)
                .tossDecision(TossDecision.BOWL)
                .battingTeam(aus)
                .bowlingTeam(ind)
                .currentInningsNumber(2)
                .totalOvers(20)
                .targetRuns(206)
                .striker(head)
                .nonStriker(stoinis)
                .currentBowler(bumrah)
                .currentOverBalls("1, 4, 0, W, 6, 2")
                .battingTeamWinProbability(34.5)
                .build();
        match1 = matchRepository.save(match1);

        // 1st Innings: India 205/5 (20.0)
        Innings indInnings = Innings.builder()
                .match(match1)
                .inningsNumber(1)
                .battingTeam(ind)
                .bowlingTeam(aus)
                .totalRuns(205)
                .wickets(5)
                .totalBalls(120)
                .wides(6)
                .noBalls(1)
                .byes(2)
                .legByes(4)
                .penaltyRuns(0)
                .isCompleted(true)
                .build();
        inningsRepository.save(indInnings);

        // India Batting stats in 1st Innings
        createBattingStat(match1, 1, rohit, 1, 92, 41, 7, 8, true, DismissalType.BOWLED, "Mitchell Starc", null);
        createBattingStat(match1, 1, kohli, 2, 0, 5, 0, 0, true, DismissalType.CAUGHT, "Josh Hazlewood", "Tim David");
        createBattingStat(match1, 1, pant, 3, 15, 14, 1, 1, true, DismissalType.CAUGHT, "Marcus Stoinis", "Josh Hazlewood");
        createBattingStat(match1, 1, sky, 4, 31, 16, 3, 2, true, DismissalType.CAUGHT, "Mitchell Starc", "Matthew Wade");
        createBattingStat(match1, 1, dube, 5, 28, 22, 2, 1, true, DismissalType.CAUGHT, "Marcus Stoinis", "David Warner");
        createBattingStat(match1, 1, hardik, 6, 27, 17, 1, 2, false, DismissalType.NOT_OUT, null, null);
        createBattingStat(match1, 1, jadeja, 7, 9, 5, 0, 1, false, DismissalType.NOT_OUT, null, null);

        // Australia Bowling stats in 1st Innings
        createBowlingStat(match1, 1, starc, 24, 0, 45, 2, 8);
        createBowlingStat(match1, 1, hazelwood, 24, 0, 25, 1, 12);
        createBowlingStat(match1, 1, cummins, 24, 0, 48, 0, 6);
        createBowlingStat(match1, 1, zampa, 24, 0, 41, 0, 7);
        createBowlingStat(match1, 1, stoinis, 24, 0, 46, 2, 5);

        // 2nd Innings: Australia 156/4 in 16.3 overs (Target 206)
        Innings ausInnings = Innings.builder()
                .match(match1)
                .inningsNumber(2)
                .battingTeam(aus)
                .bowlingTeam(ind)
                .totalRuns(156)
                .wickets(4)
                .totalBalls(99)
                .wides(4)
                .noBalls(0)
                .byes(1)
                .legByes(3)
                .penaltyRuns(0)
                .isCompleted(false)
                .build();
        inningsRepository.save(ausInnings);

        // Australia Batting in 2nd Innings
        createBattingStat(match1, 2, warner, 1, 6, 6, 1, 0, true, DismissalType.CAUGHT, "Arshdeep Singh", "Suryakumar Yadav");
        createBattingStat(match1, 2, head, 2, 76, 43, 9, 4, false, DismissalType.NOT_OUT, null, null);
        createBattingStat(match1, 2, marsh, 3, 37, 28, 3, 2, true, DismissalType.CAUGHT, "Kuldeep Yadav", "Axar Patel");
        createBattingStat(match1, 2, maxwell, 4, 20, 12, 2, 1, true, DismissalType.BOWLED, "Kuldeep Yadav", null);
        createBattingStat(match1, 2, stoinis, 5, 12, 10, 1, 0, false, DismissalType.NOT_OUT, null, null);

        // India Bowling in 2nd Innings
        createBowlingStat(match1, 2, arshdeep, 24, 0, 37, 2, 9);
        createBowlingStat(match1, 2, bumrah, 21, 0, 16, 1, 12);
        createBowlingStat(match1, 2, axar, 18, 0, 31, 0, 5);
        createBowlingStat(match1, 2, kuldeep, 24, 0, 24, 2, 11);
        createBowlingStat(match1, 2, hardik, 12, 0, 28, 0, 3);

        // Seed recent balls & commentary for Match 1
        createBall(match1, 2, 15, 6, bumrah, stoinis, head, 1, 0, ExtraType.NONE, false, DismissalType.NOT_OUT, null,
                "Bumrah to Stoinis, 1 run, yorker on middle and leg, squeezed out towards mid-wicket to retain strike.", "156/4", "15.6");
        createBall(match1, 2, 15, 5, bumrah, maxwell, stoinis, 0, 0, ExtraType.NONE, true, DismissalType.BOWLED, maxwell,
                "Bumrah to Maxwell, OUT! CLEAN BOWLED! 142kph reverse-swinging yorker! Maxwell backed away for the sweep, completely beaten for pace and timber is rattled!", "155/4", "15.5");
        createBall(match1, 2, 15, 4, bumrah, head, maxwell, 1, 0, ExtraType.NONE, false, DismissalType.NOT_OUT, null,
                "Bumrah to Head, 1 run, back of a length off-cutter, worked softly into the cover region.", "155/3", "15.4");
        createBall(match1, 2, 15, 3, bumrah, head, maxwell, 4, 0, ExtraType.NONE, false, DismissalType.NOT_OUT, null,
                "Bumrah to Head, FOUR! Sliced over backward point! Width offered and Head flashes hard, beating third man!", "154/3", "15.3");
        createBall(match1, 2, 15, 2, bumrah, head, maxwell, 0, 0, ExtraType.NONE, false, DismissalType.NOT_OUT, null,
                "Bumrah to Head, no run, blistering bouncer! Whistles past the helmet grille as Head ducks underneath.", "150/3", "15.2");
        createBall(match1, 2, 15, 1, bumrah, maxwell, head, 1, 0, ExtraType.NONE, false, DismissalType.NOT_OUT, null,
                "Bumrah to Maxwell, 1 run, full on the pads, nudged fine to fine leg.", "150/3", "15.1");

        // Seed over progression data balls for match 1 (innings 1 & 2)
        seedMatchProgressionBalls(match1, ind, aus, bumrah, starc, head, rohit);

        // ==========================================
        // MATCH 2: England vs South Africa (LIVE)
        // ==========================================
        Match match2 = Match.builder()
                .title("T20 World Cup - Super 8 Group 2")
                .series("ICC Men's T20 World Cup 2026")
                .venue("Daren Sammy Cricket Ground, Gros Islet, St Lucia")
                .matchType(MatchType.T20)
                .status(MatchStatus.LIVE)
                .matchDate(LocalDateTime.now().minusHours(1))
                .team1(sa)
                .team2(eng)
                .tossWinner(eng)
                .tossDecision(TossDecision.BOWL)
                .battingTeam(eng)
                .bowlingTeam(sa)
                .currentInningsNumber(2)
                .totalOvers(20)
                .targetRuns(164)
                .striker(brook)
                .nonStriker(livingstone)
                .currentBowler(rabada)
                .currentOverBalls("0, 1, 6, 1, 4")
                .battingTeamWinProbability(58.0)
                .build();
        match2 = matchRepository.save(match2);

        Innings saInn = Innings.builder()
                .match(match2)
                .inningsNumber(1)
                .battingTeam(sa)
                .bowlingTeam(eng)
                .totalRuns(163)
                .wickets(6)
                .totalBalls(120)
                .wides(5)
                .noBalls(0)
                .byes(1)
                .legByes(2)
                .isCompleted(true)
                .build();
        inningsRepository.save(saInn);

        Innings engInn = Innings.builder()
                .match(match2)
                .inningsNumber(2)
                .battingTeam(eng)
                .bowlingTeam(sa)
                .totalRuns(138)
                .wickets(4)
                .totalBalls(105) // 17.3 overs
                .wides(3)
                .noBalls(1)
                .byes(0)
                .legByes(2)
                .isCompleted(false)
                .build();
        inningsRepository.save(engInn);

        createBattingStat(match2, 2, buttler, 1, 49, 36, 5, 2, true, DismissalType.CAUGHT, "Keshav Maharaj", "Heinrich Klaasen");
        createBattingStat(match2, 2, salt, 2, 24, 15, 3, 1, true, DismissalType.CAUGHT, "Kagiso Rabada", "Reeza Hendricks");
        createBattingStat(match2, 2, brook, 3, 38, 26, 4, 1, false, DismissalType.NOT_OUT, null, null);
        createBattingStat(match2, 2, livingstone, 4, 21, 14, 2, 1, false, DismissalType.NOT_OUT, null, null);

        createBowlingStat(match2, 2, rabada, 21, 0, 29, 2, 8);
        createBowlingStat(match2, 2, nortje, 24, 0, 35, 1, 9);

        // ==========================================
        // MATCH 3: Pakistan vs New Zealand (COMPLETED)
        // ==========================================
        Match match3 = Match.builder()
                .title("Bilateral T20 Series - 3rd T20I")
                .series("NZ vs PAK T20 Series 2026")
                .venue("Eden Park, Auckland")
                .matchType(MatchType.T20)
                .status(MatchStatus.COMPLETED)
                .matchDate(LocalDateTime.now().minusDays(1))
                .team1(nz)
                .team2(pak)
                .tossWinner(pak)
                .tossDecision(TossDecision.BOWL)
                .battingTeam(pak)
                .bowlingTeam(nz)
                .currentInningsNumber(2)
                .totalOvers(20)
                .targetRuns(195)
                .winnerTeamName("New Zealand")
                .resultMessage("New Zealand won by 21 runs")
                .battingTeamWinProbability(0.0)
                .build();
        match3 = matchRepository.save(match3);

        Innings nzInn = Innings.builder()
                .match(match3)
                .inningsNumber(1)
                .battingTeam(nz)
                .bowlingTeam(pak)
                .totalRuns(194)
                .wickets(4)
                .totalBalls(120)
                .wides(4)
                .noBalls(1)
                .isCompleted(true)
                .build();
        inningsRepository.save(nzInn);

        Innings pakInn = Innings.builder()
                .match(match3)
                .inningsNumber(2)
                .battingTeam(pak)
                .bowlingTeam(nz)
                .totalRuns(173)
                .wickets(8)
                .totalBalls(120)
                .wides(6)
                .noBalls(0)
                .isCompleted(true)
                .build();
        inningsRepository.save(pakInn);

        // ==========================================
        // MATCH 4: India vs England (UPCOMING SEMI-FINAL)
        // ==========================================
        Match match4 = Match.builder()
                .title("ICC T20 World Cup - Semi Final 2")
                .series("ICC Men's T20 World Cup 2026")
                .venue("Providence Stadium, Georgetown, Guyana")
                .matchType(MatchType.T20)
                .status(MatchStatus.UPCOMING)
                .matchDate(LocalDateTime.now().plusDays(2))
                .team1(ind)
                .team2(eng)
                .battingTeam(ind)
                .bowlingTeam(eng)
                .currentInningsNumber(1)
                .totalOvers(20)
                .battingTeamWinProbability(55.0)
                .build();
        matchRepository.save(match4);

        log.info("Seeding completed successfully! 6 Teams, 28+ Players, 4 Matches configured.");
    }

    private Team createTeam(String name, String shortName, String countryCode, String flag, String color) {
        Team t = Team.builder()
                .name(name)
                .shortName(shortName)
                .countryCode(countryCode)
                .flagEmoji(flag)
                .primaryColor(color)
                .build();
        return teamRepository.save(t);
    }

    private Player createPlayer(String name, Team team, PlayerRole role, String batStyle, String bowlStyle,
                                 int jersey, int mat, int runs, int balls, int fours, int sixes, int hs,
                                 double sr, double avg, int wkts, int bBowled, int rc, double econ) {
        Player p = Player.builder()
                .name(name)
                .team(team)
                .role(role)
                .battingStyle(batStyle)
                .bowlingStyle(bowlStyle)
                .jerseyNumber(jersey)
                .matchesPlayed(mat)
                .totalRuns(runs)
                .ballsFaced(balls)
                .fours(fours)
                .sixes(sixes)
                .highestScore(hs)
                .battingStrikeRate(sr)
                .battingAverage(avg)
                .wicketsTaken(wkts)
                .ballsBowled(bBowled)
                .runsConceded(rc)
                .bowlingEconomy(econ)
                .isPlayingXI(true)
                .build();
        return playerRepository.save(p);
    }

    private void createBattingStat(Match match, int innNum, Player p, int order, int runs, int balls,
                                   int fours, int sixes, boolean isOut, DismissalType dt, String bowler, String fielder) {
        PlayerMatchStats s = PlayerMatchStats.builder()
                .match(match)
                .inningsNumber(innNum)
                .player(p)
                .battingOrder(order)
                .hasBatted(true)
                .runsScored(runs)
                .ballsFaced(balls)
                .fours(fours)
                .sixes(sixes)
                .isOut(isOut)
                .dismissalType(dt)
                .bowlerName(bowler)
                .fielderName(fielder)
                .build();
        statsRepository.save(s);
    }

    private void createBowlingStat(Match match, int innNum, Player p, int ballsBowled, int maidens,
                                   int runsConceded, int wickets, int dots) {
        PlayerMatchStats s = PlayerMatchStats.builder()
                .match(match)
                .inningsNumber(innNum)
                .player(p)
                .hasBowled(true)
                .oversBowledBalls(ballsBowled)
                .maidenOvers(maidens)
                .runsConceded(runsConceded)
                .wicketsTaken(wickets)
                .dotsConceded(dots)
                .build();
        statsRepository.save(s);
    }

    private void createBall(Match match, int innNum, int over, int ball, Player bowler, Player striker, Player nonStriker,
                            int runs, int extras, ExtraType et, boolean isWkt, DismissalType dt, Player dismissed,
                            String commentary, String scoreAtBall, String ballDisplay) {
        BallEvent b = BallEvent.builder()
                .match(match)
                .inningsNumber(innNum)
                .overNumber(over)
                .ballNumberInOver(ball)
                .bowler(bowler)
                .batsman(striker)
                .nonStriker(nonStriker)
                .runsOffBat(runs)
                .extrasRuns(extras)
                .extraType(et)
                .isWicket(isWkt)
                .dismissalType(dt)
                .dismissedPlayer(dismissed)
                .commentary(commentary)
                .scoreAtBall(scoreAtBall)
                .ballDisplay(ballDisplay)
                .timestamp(LocalDateTime.now().minusMinutes(over * 3L + ball))
                .build();
        ballRepository.save(b);
    }

    private void seedMatchProgressionBalls(Match match, Team ind, Team aus, Player bumrah, Player starc, Player head, Player rohit) {
        // Seed summary balls for each over 1..15 for Innings 1 and Innings 2 so progression graph renders beautifully!
        int[] inn1RunsPerOver = {8, 12, 15, 10, 14, 11, 7, 9, 13, 11, 8, 16, 12, 14, 18, 10, 8, 12, 14, 14};
        for (int ov = 0; ov < inn1RunsPerOver.length; ov++) {
            createBall(match, 1, ov, 6, starc, rohit, null, inn1RunsPerOver[ov], 0, ExtraType.NONE,
                    ov == 1 || ov == 7 || ov == 13 || ov == 16, DismissalType.CAUGHT, null,
                    "Over " + (ov + 1) + " ends. Runs in over: " + inn1RunsPerOver[ov], (ov * 10) + "/" + (ov % 3), (ov + 1) + ".0");
        }

        int[] inn2RunsPerOver = {10, 14, 8, 12, 16, 9, 7, 11, 8, 10, 14, 9, 8, 11, 9};
        for (int ov = 0; ov < inn2RunsPerOver.length; ov++) {
            createBall(match, 2, ov, 6, bumrah, head, null, inn2RunsPerOver[ov], 0, ExtraType.NONE,
                    ov == 0 || ov == 8 || ov == 14, DismissalType.CAUGHT, null,
                    "Over " + (ov + 1) + " ends. Runs in over: " + inn2RunsPerOver[ov], (ov * 10) + "/" + (ov % 3), (ov + 1) + ".0");
        }
    }
}
