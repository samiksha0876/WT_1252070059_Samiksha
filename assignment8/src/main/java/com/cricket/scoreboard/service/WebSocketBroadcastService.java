package com.cricket.scoreboard.service;

import com.cricket.scoreboard.dto.LiveScoreUpdateDTO;
import com.cricket.scoreboard.dto.MatchSummaryDTO;
import com.cricket.scoreboard.entity.BallEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastLiveScore(Long matchId, LiveScoreUpdateDTO scoreUpdate) {
        try {
            messagingTemplate.convertAndSend("/topic/match/" + matchId, scoreUpdate);
        } catch (Exception e) {
            log.error("Failed to broadcast live score for match {}: {}", matchId, e.getMessage());
        }
    }

    public void broadcastMatchesList(List<MatchSummaryDTO> matches) {
        try {
            messagingTemplate.convertAndSend("/topic/matches", matches);
        } catch (Exception e) {
            log.error("Failed to broadcast matches list: {}", e.getMessage());
        }
    }

    public void broadcastBallEvent(Long matchId, BallEvent ballEvent) {
        try {
            messagingTemplate.convertAndSend("/topic/commentary/" + matchId, ballEvent);
        } catch (Exception e) {
            log.error("Failed to broadcast ball event for match {}: {}", matchId, e.getMessage());
        }
    }

    public void broadcastTickerMessage(String tickerText) {
        try {
            messagingTemplate.convertAndSend("/topic/ticker", tickerText);
        } catch (Exception e) {
            log.error("Failed to broadcast ticker: {}", e.getMessage());
        }
    }
}
