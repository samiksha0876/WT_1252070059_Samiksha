package com.cricket.scoreboard.controller;

import com.cricket.scoreboard.dto.BallRequestDTO;
import com.cricket.scoreboard.entity.BallEvent;
import com.cricket.scoreboard.service.ScoringService;
import com.cricket.scoreboard.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;
    private final SimulationService simulationService;

    @PostMapping("/ball")
    public ResponseEntity<?> recordCustomBall(@RequestBody BallRequestDTO request) {
        BallEvent event = scoringService.recordBall(request);
        if (event == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not record delivery. Match may be completed or uninitialized."));
        }
        return ResponseEntity.ok(event);
    }

    @PostMapping("/simulate-ball/{matchId}")
    public ResponseEntity<?> simulateSingleBall(@PathVariable Long matchId) {
        boolean simulated = simulationService.simulateNextBall(matchId);
        return ResponseEntity.ok(Map.of(
                "matchId", matchId,
                "simulated", simulated,
                "message", simulated ? "Delivery simulated successfully" : "No more balls or match finished"
        ));
    }

    @PostMapping("/start-simulation/{matchId}")
    public ResponseEntity<?> startSimulation(
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "2500") int intervalMs) {
        boolean started = simulationService.startSimulation(matchId, intervalMs);
        return ResponseEntity.ok(Map.of(
                "matchId", matchId,
                "isSimulating", started,
                "intervalMs", intervalMs,
                "message", started ? "Live simulation started" : "Failed to start simulation"
        ));
    }

    @PostMapping("/stop-simulation/{matchId}")
    public ResponseEntity<?> stopSimulation(@PathVariable Long matchId) {
        boolean stopped = simulationService.stopSimulation(matchId);
        return ResponseEntity.ok(Map.of(
                "matchId", matchId,
                "stopped", stopped,
                "message", stopped ? "Simulation paused" : "Simulation was not running"
        ));
    }

    @GetMapping("/simulation-status/{matchId}")
    public ResponseEntity<?> getSimulationStatus(@PathVariable Long matchId) {
        boolean isRunning = simulationService.isSimulating(matchId);
        return ResponseEntity.ok(Map.of(
                "matchId", matchId,
                "isSimulating", isRunning
        ));
    }
}
