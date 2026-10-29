package com.cricket.scoreboard.controller;

import com.cricket.scoreboard.dto.LiveScoreUpdateDTO;
import com.cricket.scoreboard.dto.MatchSummaryDTO;
import com.cricket.scoreboard.dto.OverSummaryDTO;
import com.cricket.scoreboard.dto.ScorecardDTO;
import com.cricket.scoreboard.entity.BallEvent;
import com.cricket.scoreboard.enums.MatchStatus;
import com.cricket.scoreboard.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchSummaryDTO>> getAllMatches(
            @RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(matchService.getAllMatches(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveScoreUpdateDTO> getLiveScoreUpdate(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getLiveScoreUpdate(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<MatchSummaryDTO> getMatchSummary(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchSummary(id));
    }

    @GetMapping("/{id}/scorecard")
    public ResponseEntity<ScorecardDTO> getScorecard(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getScorecard(id));
    }

    @GetMapping("/{id}/commentary")
    public ResponseEntity<List<BallEvent>> getMatchCommentary(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchCommentary(id));
    }

    @GetMapping("/{id}/progression")
    public ResponseEntity<OverSummaryDTO> getOverProgression(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getOverProgression(id));
    }
}
