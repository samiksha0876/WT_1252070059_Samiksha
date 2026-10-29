package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.LeaderboardDTO;
import com.cricket.scoreboard.entity.Player;
import com.cricket.scoreboard.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Player> getPlayersByTeam(Long teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + id));
    }

    @Transactional(readOnly = true)
    public LeaderboardDTO getLeaderboard() {
        List<Player> orangeList = playerRepository.findTopRunScorers();
        List<Player> purpleList = playerRepository.findTopWicketTakers();
        List<Player> srList = playerRepository.findTopStrikeRates();
        List<Player> econList = playerRepository.findBestEconomyBowlers();

        return LeaderboardDTO.builder()
                .orangeCap(orangeList.stream().limit(8).map(this::toLeaderboardRow).collect(Collectors.toList()))
                .purpleCap(purpleList.stream().limit(8).map(this::toLeaderboardRow).collect(Collectors.toList()))
                .topStrikeRates(srList.stream().limit(8).map(this::toLeaderboardRow).collect(Collectors.toList()))
                .bestEconomy(econList.stream().limit(8).map(this::toLeaderboardRow).collect(Collectors.toList()))
                .build();
    }

    private LeaderboardDTO.PlayerLeaderboardRow toLeaderboardRow(Player p) {
        return LeaderboardDTO.PlayerLeaderboardRow.builder()
                .playerId(p.getId())
                .playerName(p.getName())
                .teamShort(p.getTeam() != null ? p.getTeam().getShortName() : "")
                .teamFlag(p.getTeam() != null ? p.getTeam().getFlagEmoji() : "")
                .role(p.getRole().name())
                .matches(p.getMatchesPlayed())
                .runs(p.getTotalRuns())
                .ballsFaced(p.getBallsFaced())
                .fours(p.getFours())
                .sixes(p.getSixes())
                .highestScore(p.getHighestScore())
                .strikeRate(p.getBattingStrikeRate())
                .average(p.getBattingAverage())
                .wickets(p.getWicketsTaken())
                .ballsBowled(p.getBallsBowled())
                .runsConceded(p.getRunsConceded())
                .economy(p.getBowlingEconomy())
                .build();
    }
}
