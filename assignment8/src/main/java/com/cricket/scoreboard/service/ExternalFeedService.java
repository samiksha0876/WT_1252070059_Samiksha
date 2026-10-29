package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.LiveScoreUpdateDTO;
import com.cricket.scoreboard.entity.*;
import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.repository.InningsRepository;
import com.cricket.scoreboard.repository.MatchRepository;
import com.cricket.scoreboard.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalFeedService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerRepository playerRepository;
    private final MatchService matchService;
    private final WebSocketBroadcastService broadcastService;

    /**
     * Simulates fetching external cricket match feeds from an external sports provider API
     */
    public Map<String, Object> fetchExternalLiveMatchFeed(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            return Map.of("status", "error", "message", "Match not found");
        }

        LiveScoreUpdateDTO live = matchService.getLiveScoreUpdate(matchId);

        Map<String, Object> feed = new HashMap<>();
        feed.put("provider", "GlobalCricketFeedAPI-v2");
        feed.put("source", "https://api.cricketfeeds.example.com/v2/live/" + matchId);
        feed.put("timestamp", System.currentTimeMillis());
        feed.put("matchId", match.getId());
        feed.put("series", match.getSeries());
        feed.put("title", match.getTitle());
        feed.put("venue", match.getVenue());
        feed.put("matchState", match.getStatus().name());
        feed.put("currentScore", live.getRuns() + "/" + live.getWickets());
        feed.put("overs", live.getOvers());
        feed.put("crr", live.getCurrentRunRate());
        feed.put("target", live.getTargetRuns());
        feed.put("rrr", live.getRequiredRunRate());
        feed.put("battingTeam", live.getBattingTeamShort());
        feed.put("bowlingTeam", live.getBowlingTeamShort());
        feed.put("striker", live.getStriker() != null ? live.getStriker().getName() + " (" + live.getStriker().getRuns() + ")" : "-");
        feed.put("bowler", live.getCurrentBowler() != null ? live.getCurrentBowler().getName() + " (" + live.getCurrentBowler().getWickets() + "/" + live.getCurrentBowler().getRunsConceded() + ")" : "-");
        feed.put("winProbability", live.getBattingTeamWinProbability());

        return feed;
    }

    /**
     * Processes and ingests external live match payload into the system
     */
    @Transactional
    public Map<String, Object> ingestExternalFeed(Long matchId, Map<String, Object> externalPayload) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        try {
            if (externalPayload.containsKey("status")) {
                String statusStr = String.valueOf(externalPayload.get("status"));
                try {
                    match.setStatus(MatchStatus.valueOf(statusStr.toUpperCase()));
                } catch (Exception ignored) {}
            }

            if (externalPayload.containsKey("resultMessage")) {
                match.setResultMessage(String.valueOf(externalPayload.get("resultMessage")));
            }

            matchRepository.save(match);

            // Broadcast ingested sync
            LiveScoreUpdateDTO update = matchService.getLiveScoreUpdate(matchId);
            broadcastService.broadcastLiveScore(matchId, update);
            broadcastService.broadcastTickerMessage("External Feed Ingested: " + match.getTitle() + " updated.");

            return Map.of("status", "success", "message", "Feed successfully ingested and broadcasted");
        } catch (Exception e) {
            log.error("Failed to ingest external feed: {}", e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}
