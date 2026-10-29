package com.cricket.scoreboard.controller;

import com.cricket.scoreboard.service.ExternalFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final ExternalFeedService externalFeedService;

    @GetMapping("/external/{matchId}")
    public ResponseEntity<Map<String, Object>> getExternalLiveFeed(@PathVariable Long matchId) {
        return ResponseEntity.ok(externalFeedService.fetchExternalLiveMatchFeed(matchId));
    }

    @PostMapping("/ingest/{matchId}")
    public ResponseEntity<Map<String, Object>> ingestFeed(
            @PathVariable Long matchId,
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(externalFeedService.ingestExternalFeed(matchId, payload));
    }
}
