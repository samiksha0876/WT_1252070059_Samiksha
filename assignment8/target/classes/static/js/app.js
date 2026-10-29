/**
 * CricPulse - Real-Time Cricket Score Management System
 * WebSocket Client, UI Controller, Canvas Worm Chart & Sound Synthesizer
 */

class CricketApp {
    constructor() {
        this.activeMatchId = 1; // Default to India vs Australia
        this.stompClient = null;
        this.activeFilter = 'ALL';
        this.allMatches = [];
        this.isSoundEnabled = true;
        this.isSimulating = false;
        this.currentSubscription = null;
        this.commentarySubscription = null;
        this.lastProgressionData = null;

        // Theme management: default to light theme per user preference
        this.theme = localStorage.getItem('cricpulse_theme') || 'light';

        // Audio Context for synthesized sound alerts without external audio files
        this.audioCtx = null;

        this.init();
    }

    init() {
        this.setupTheme();
        this.setupAudio();
        this.setupEventListeners();
        this.connectWebSocket();
        this.loadMatches();
        this.loadMatchDetails(this.activeMatchId);
        this.loadCommentary(this.activeMatchId);
        this.loadLeaderboards();

        // Polling fallback to keep stats fresh if WS drops
        setInterval(() => {
            if (!this.stompClient || !this.stompClient.connected) {
                this.loadMatchDetails(this.activeMatchId);
                this.loadMatches();
            }
        }, 5000);
    }

    /* ==========================================================================
       Theme Management
       ========================================================================== */
    setupTheme() {
        document.documentElement.setAttribute('data-theme', this.theme);
        document.body.setAttribute('data-theme', this.theme);
        const themeBtn = document.getElementById('themeToggleBtn');
        if (themeBtn) {
            themeBtn.innerHTML = this.theme === 'dark' 
                ? '<i class="fa-solid fa-sun" style="color:#f59e0b;"></i>' 
                : '<i class="fa-solid fa-moon"></i>';
            themeBtn.title = `Switch to ${this.theme === 'dark' ? 'Light' : 'Dark'} Theme`;
        }
    }

    toggleTheme() {
        this.theme = this.theme === 'dark' ? 'light' : 'dark';
        localStorage.setItem('cricpulse_theme', this.theme);
        this.setupTheme();
        this.showToast(`Switched to ${this.theme.toUpperCase()} theme`, 'info');

        // Redraw worm chart if cached
        if (this.lastProgressionData) {
            this.drawProgressionWormChart(this.lastProgressionData);
        }
    }

    /* ==========================================================================
       Toast Notification System
       ========================================================================== */
    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        let icon = '<i class="fa-solid fa-circle-info" style="color:var(--accent-blue)"></i>';
        if (type === 'success') icon = '<i class="fa-solid fa-circle-check" style="color:var(--accent-green)"></i>';
        if (type === 'warning') icon = '<i class="fa-solid fa-triangle-exclamation" style="color:var(--accent-gold)"></i>';
        if (type === 'danger') icon = '<i class="fa-solid fa-circle-exclamation" style="color:var(--accent-red)"></i>';

        toast.innerHTML = `${icon} <span>${message}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('toast-out');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    /* ==========================================================================
       Sound Synthesizer (Web Audio API)
       ========================================================================== */
    setupAudio() {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (AudioContext) {
            this.audioCtx = new AudioContext();
        }
    }

    playCricketSound(type) {
        if (!this.isSoundEnabled || !this.audioCtx) return;
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }

        const now = this.audioCtx.currentTime;
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        if (type === 'FOUR') {
            // Cheerful double chime
            osc.frequency.setValueAtTime(523.25, now); // C5
            osc.frequency.setValueAtTime(659.25, now + 0.1); // E5
            gain.gain.setValueAtTime(0.3, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.35);
            osc.start(now);
            osc.stop(now + 0.35);
        } else if (type === 'SIX') {
            // Triumphant arpeggio
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(440, now); // A4
            osc.frequency.setValueAtTime(554.37, now + 0.08); // C#5
            osc.frequency.setValueAtTime(659.25, now + 0.16); // E5
            osc.frequency.setValueAtTime(880, now + 0.24); // A5
            gain.gain.setValueAtTime(0.4, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.55);
            osc.start(now);
            osc.stop(now + 0.55);
        } else if (type === 'WICKET') {
            // Dramatic descending sweep
            osc.type = 'sawtooth';
            osc.frequency.setValueAtTime(320, now);
            osc.frequency.exponentialRampToValueAtTime(70, now + 0.45);
            gain.gain.setValueAtTime(0.35, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
            osc.start(now);
            osc.stop(now + 0.45);
        }
    }

    /* ==========================================================================
       WebSocket Connection & Real-Time Subscriptions
       ========================================================================== */
    connectWebSocket() {
        const wsStatusEl = document.getElementById('wsStatus');
        const statusLabel = wsStatusEl.querySelector('.status-label');

        try {
            const socket = new SockJS('/ws-cricket');
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = null; // Suppress verbose logging

            this.stompClient.connect({}, () => {
                wsStatusEl.className = 'ws-status connected';
                statusLabel.textContent = 'LIVE WS';

                // 1. Subscribe to all matches list
                this.stompClient.subscribe('/topic/matches', (msg) => {
                    const matches = JSON.parse(msg.body);
                    this.renderMatchesList(matches);
                });

                // 2. Subscribe to general live ticker
                this.stompClient.subscribe('/topic/ticker', (msg) => {
                    const tickerEl = document.getElementById('liveTickerText');
                    tickerEl.textContent = msg.body;
                    tickerEl.classList.remove('pulse');
                    void tickerEl.offsetWidth; // Trigger reflow
                    tickerEl.classList.add('pulse');
                });

                // 3. Subscribe to active match score & commentary
                this.subscribeToActiveMatch(this.activeMatchId);

            }, (error) => {
                console.warn('WebSocket connection dropped:', error);
                wsStatusEl.className = 'ws-status disconnected';
                statusLabel.textContent = 'POLLING';
                // Try reconnect in 4 seconds
                setTimeout(() => this.connectWebSocket(), 4000);
            });
        } catch (e) {
            console.error('SockJS error:', e);
            wsStatusEl.className = 'ws-status disconnected';
            statusLabel.textContent = 'POLLING';
        }
    }

    subscribeToActiveMatch(matchId) {
        if (!this.stompClient || !this.stompClient.connected) return;

        if (this.currentSubscription) {
            this.currentSubscription.unsubscribe();
        }
        if (this.commentarySubscription) {
            this.commentarySubscription.unsubscribe();
        }

        // Live score updates for active match
        this.currentSubscription = this.stompClient.subscribe(`/topic/match/${matchId}`, (msg) => {
            const scoreUpdate = JSON.parse(msg.body);
            this.renderLiveScoreUpdate(scoreUpdate);
        });

        // Live commentary for active match
        this.commentarySubscription = this.stompClient.subscribe(`/topic/commentary/${matchId}`, (msg) => {
            const ballEvent = JSON.parse(msg.body);
            this.prependCommentaryBall(ballEvent);
            this.triggerBallAudio(ballEvent);
        });
    }

    triggerBallAudio(ball) {
        if (ball.isWicket) {
            this.playCricketSound('WICKET');
        } else if (ball.runsOffBat === 6) {
            this.playCricketSound('SIX');
        } else if (ball.runsOffBat === 4) {
            this.playCricketSound('FOUR');
        }
    }

    /* ==========================================================================
       Event Listeners Setup
       ========================================================================== */
    setupEventListeners() {
        // Sound toggle
        const soundBtn = document.getElementById('soundToggleBtn');
        soundBtn.addEventListener('click', () => {
            this.isSoundEnabled = !this.isSoundEnabled;
            soundBtn.innerHTML = this.isSoundEnabled ?
                '<i class="fa-solid fa-volume-high"></i>' :
                '<i class="fa-solid fa-volume-xmark"></i>';
            soundBtn.classList.toggle('active', this.isSoundEnabled);
            if (this.isSoundEnabled && this.audioCtx && this.audioCtx.state === 'suspended') {
                this.audioCtx.resume();
            }
            this.showToast(this.isSoundEnabled ? 'Audio sound effects enabled' : 'Audio sound effects muted', 'info');
        });

        // Theme toggle
        const themeBtn = document.getElementById('themeToggleBtn');
        if (themeBtn) {
            themeBtn.addEventListener('click', () => {
                this.toggleTheme();
            });
        }

        // Filter pills for match carousel
        const pills = document.querySelectorAll('#matchFilterPills .pill');
        pills.forEach(pill => {
            pill.addEventListener('click', () => {
                pills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                this.activeFilter = pill.dataset.filter;
                this.renderMatchesList(this.allMatches);
            });
        });

        // Insights tab switching
        const tabBtns = document.querySelectorAll('.tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                tabBtns.forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

                btn.classList.add('active');
                const targetPane = document.getElementById(btn.dataset.tab);
                if (targetPane) targetPane.classList.add('active');

                // Load tab-specific content on-demand
                if (btn.dataset.tab === 'tab-scorecard') {
                    this.loadScorecard(this.activeMatchId);
                } else if (btn.dataset.tab === 'tab-charts') {
                    this.loadProgressionChart(this.activeMatchId);
                } else if (btn.dataset.tab === 'tab-leaderboard') {
                    this.loadLeaderboards();
                } else if (btn.dataset.tab === 'tab-feed') {
                    this.loadExternalFeed(this.activeMatchId);
                }
            });
        });

        // Simulation controls
        const startSimBtn = document.getElementById('startSimBtn');
        const stopSimBtn = document.getElementById('stopSimBtn');
        const stepBallBtn = document.getElementById('stepBallBtn');
        const simIntervalSelect = document.getElementById('simInterval');

        startSimBtn.addEventListener('click', () => {
            const interval = parseInt(simIntervalSelect.value, 10) || 2500;
            fetch(`/api/scoring/start-simulation/${this.activeMatchId}?intervalMs=${interval}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    this.isSimulating = true;
                    startSimBtn.style.display = 'none';
                    stopSimBtn.style.display = 'inline-flex';
                    this.showToast(`Auto simulation started (${(interval / 1000).toFixed(1)}s / ball)`, 'success');
                });
        });

        stopSimBtn.addEventListener('click', () => {
            fetch(`/api/scoring/stop-simulation/${this.activeMatchId}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    this.isSimulating = false;
                    startSimBtn.style.display = 'inline-flex';
                    stopSimBtn.style.display = 'none';
                    this.showToast('Auto simulation paused', 'warning');
                });
        });

        stepBallBtn.addEventListener('click', () => {
            fetch(`/api/scoring/simulate-ball/${this.activeMatchId}`, { method: 'POST' })
                .then(() => {
                    this.showToast('Next ball delivery simulated', 'info');
                });
        });

        // Manual Umpire Scoring Keypad Handlers
        document.querySelectorAll('.key-run').forEach(btn => {
            btn.addEventListener('click', () => {
                const runs = parseInt(btn.dataset.runs, 10);
                this.recordCustomBall({ runsOffBat: runs });
                this.showToast(`Recorded ${runs === 0 ? 'Dot ball (0)' : runs + (runs === 4 ? ' (Boundary Four)' : runs === 6 ? ' (Maximum Six)' : ' run' + (runs > 1 ? 's' : ''))}`, 'info');
            });
        });

        document.querySelectorAll('.key-extra').forEach(btn => {
            btn.addEventListener('click', () => {
                const extraType = btn.dataset.extra;
                const extrasRuns = parseInt(btn.dataset.extrasRuns, 10);
                this.recordCustomBall({ extraType: extraType, extrasRuns: extrasRuns, runsOffBat: 0 });
                this.showToast(`Recorded Extra: ${extraType} (+${extrasRuns})`, 'warning');
            });
        });

        document.querySelectorAll('.key-wicket').forEach(btn => {
            btn.addEventListener('click', () => {
                const dismissal = btn.dataset.dismissal;
                this.recordCustomBall({ isWicket: true, dismissalType: dismissal, runsOffBat: 0 });
                this.showToast(`WICKET! Dismissal: ${dismissal}`, 'danger');
            });
        });

        // Custom commentary submit
        document.getElementById('submitCustomBallBtn').addEventListener('click', () => {
            const input = document.getElementById('customCommentaryInput');
            const customText = input.value.trim();
            if (customText) {
                this.recordCustomBall({ runsOffBat: 1, customCommentary: customText });
                input.value = '';
                this.showToast('Custom commentary delivery recorded', 'success');
            }
        });

        // External feed button
        document.getElementById('fetchExternalFeedBtn').addEventListener('click', () => {
            this.loadExternalFeed(this.activeMatchId);
            this.showToast('Fetching latest external feed payload...', 'info');
        });
    }

    recordCustomBall(payload) {
        payload.matchId = this.activeMatchId;
        fetch('/api/scoring/ball', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(r => r.json())
        .then(res => {
            // Instant feedback
        })
        .catch(err => console.error('Error recording ball:', err));
    }

    /* ==========================================================================
       REST API Data Fetchers
       ========================================================================== */
    loadMatches() {
        fetch('/api/matches')
            .then(r => r.json())
            .then(matches => {
                this.allMatches = matches;
                this.renderMatchesList(matches);
            })
            .catch(err => console.error('Error fetching matches:', err));
    }

    loadMatchDetails(matchId) {
        fetch(`/api/matches/${matchId}`)
            .then(r => r.json())
            .then(data => {
                this.renderLiveScoreUpdate(data);
                // Check if match is simulating
                fetch(`/api/scoring/simulation-status/${matchId}`)
                    .then(r => r.json())
                    .then(simStatus => {
                        this.isSimulating = simStatus.isSimulating;
                        const startBtn = document.getElementById('startSimBtn');
                        const stopBtn = document.getElementById('stopSimBtn');
                        if (this.isSimulating) {
                            startBtn.style.display = 'none';
                            stopBtn.style.display = 'inline-flex';
                        } else {
                            startBtn.style.display = 'inline-flex';
                            stopBtn.style.display = 'none';
                        }
                    });
            })
            .catch(err => console.error('Error fetching match details:', err));
    }

    loadCommentary(matchId) {
        fetch(`/api/matches/${matchId}/commentary`)
            .then(r => r.json())
            .then(balls => {
                this.renderCommentaryList(balls);
            })
            .catch(err => console.error('Error loading commentary:', err));
    }

    loadScorecard(matchId) {
        const container = document.getElementById('scorecardContainer');
        container.innerHTML = '<div class="empty-state">Loading detailed scorecard...</div>';

        fetch(`/api/matches/${matchId}/scorecard`)
            .then(r => r.json())
            .then(card => {
                this.renderScorecard(card);
            })
            .catch(err => {
                container.innerHTML = '<div class="empty-state">Failed to load scorecard</div>';
            });
    }

    loadProgressionChart(matchId) {
        fetch(`/api/matches/${matchId}/progression`)
            .then(r => r.json())
            .then(data => {
                this.drawProgressionWormChart(data);
            })
            .catch(err => console.error('Error loading chart:', err));
    }

    loadLeaderboards() {
        fetch('/api/players/leaderboard')
            .then(r => r.json())
            .then(data => {
                this.renderLeaderboards(data);
            })
            .catch(err => console.error('Error loading leaderboards:', err));
    }

    loadExternalFeed(matchId) {
        const pre = document.getElementById('externalFeedJson');
        pre.textContent = 'Fetching external feed from API /api/feed/external/' + matchId + '...';

        fetch(`/api/feed/external/${matchId}`)
            .then(r => r.json())
            .then(data => {
                pre.textContent = JSON.stringify(data, null, 2);
            })
            .catch(err => {
                pre.textContent = '// Failed to fetch external live feed';
            });
    }

    /* ==========================================================================
       UI Render Functions
       ========================================================================== */
    renderMatchesList(matches) {
        const carousel = document.getElementById('matchesCarousel');
        carousel.innerHTML = '';

        let filtered = matches;
        if (this.activeFilter !== 'ALL') {
            filtered = matches.filter(m => m.status === this.activeFilter);
        }

        if (filtered.length === 0) {
            carousel.innerHTML = '<div class="empty-state">No matches found for this filter.</div>';
            return;
        }

        filtered.forEach(m => {
            const card = document.createElement('div');
            card.className = `mini-match-card ${m.id === this.activeMatchId ? 'active-match' : ''}`;
            card.onclick = () => this.switchActiveMatch(m.id);

            let statusBadge = '';
            if (m.status === 'LIVE') {
                statusBadge = '<span class="badge-live-sm"><i class="fa-solid fa-circle"></i> LIVE</span>';
            } else if (m.status === 'COMPLETED') {
                statusBadge = '<span class="badge-completed-sm">COMPLETED</span>';
            } else {
                statusBadge = '<span class="badge-upcoming-sm">UPCOMING</span>';
            }

            card.innerHTML = `
                <div class="mini-card-top">
                    <span>${m.matchType || 'T20'} • ${m.series || 'World Cup'}</span>
                    ${statusBadge}
                </div>
                <div class="mini-teams-row">
                    <div class="mini-team-line">
                        <div class="mini-team-left">
                            <span>${m.team1Flag || ''}</span>
                            <span>${m.team1ShortName || m.team1Name}</span>
                        </div>
                        <span class="mini-team-score">${m.team1Score || 'Yet to bat'}</span>
                    </div>
                    <div class="mini-team-line">
                        <div class="mini-team-left">
                            <span>${m.team2Flag || ''}</span>
                            <span>${m.team2ShortName || m.team2Name}</span>
                        </div>
                        <span class="mini-team-score">${m.team2Score || 'Yet to bat'}</span>
                    </div>
                </div>
                <div class="mini-card-footer">
                    ${m.statusText || m.resultMessage || m.venue}
                </div>
            `;
            carousel.appendChild(card);
        });
    }

    switchActiveMatch(matchId) {
        if (this.activeMatchId === matchId) return;
        this.activeMatchId = matchId;

        // Update carousel active state
        document.querySelectorAll('.mini-match-card').forEach(c => c.classList.remove('active-match'));
        this.renderMatchesList(this.allMatches);

        // Update active match display
        document.getElementById('scorerActiveMatchTitle').textContent = 'Match #' + matchId;

        const m = this.allMatches.find(x => x.id === matchId);
        const matchTitle = m ? `${m.team1ShortName || m.team1Name} vs ${m.team2ShortName || m.team2Name}` : `Match #${matchId}`;
        this.showToast(`Switched to ${matchTitle}`, 'info');

        this.subscribeToActiveMatch(matchId);
        this.loadMatchDetails(matchId);
        this.loadCommentary(matchId);

        // Refresh currently active tab
        const activeTabBtn = document.querySelector('.tab-btn.active');
        if (activeTabBtn) {
            if (activeTabBtn.dataset.tab === 'tab-scorecard') this.loadScorecard(matchId);
            if (activeTabBtn.dataset.tab === 'tab-charts') this.loadProgressionChart(matchId);
            if (activeTabBtn.dataset.tab === 'tab-feed') this.loadExternalFeed(matchId);
        }
    }

    renderLiveScoreUpdate(score) {
        // Meta
        document.getElementById('heroSeriesTitle').textContent = score.title || 'ICC Cricket Match';
        document.getElementById('heroVenue').textContent = score.venue || '';

        const badge = document.getElementById('heroMatchStatusBadge');
        if (score.status === 'LIVE') {
            badge.className = 'badge-live pulse';
            badge.innerHTML = '<i class="fa-solid fa-circle"></i> LIVE';
        } else if (score.status === 'COMPLETED') {
            badge.className = 'badge-completed-sm';
            badge.textContent = 'COMPLETED';
        } else {
            badge.className = 'badge-upcoming-sm';
            badge.textContent = score.status;
        }

        // Batting Card
        document.getElementById('battingTeamFlag').textContent = score.battingTeamFlag || '🏏';
        document.getElementById('battingTeamName').textContent = score.battingTeamName || 'Batting Team';
        document.getElementById('inningsLabel').textContent = `${score.currentInnings === 2 ? '2nd' : '1st'} Innings • ${score.battingTeamShort}`;
        document.getElementById('liveScoreRunsWkts').textContent = `${score.runs}/${score.wickets}`;
        document.getElementById('liveScoreOvers').textContent = score.overs || '0.0';
        document.getElementById('liveTotalOvers').textContent = score.totalOvers || 20;
        document.getElementById('liveCrr').textContent = score.currentRunRate != null ? score.currentRunRate.toFixed(2) : '0.00';

        // Target & RRR
        const rrrContainer = document.getElementById('rrrContainer');
        const targetContainer = document.getElementById('targetContainer');
        if (score.currentInnings === 2 && score.targetRuns != null) {
            rrrContainer.style.display = 'flex';
            targetContainer.style.display = 'flex';
            document.getElementById('liveTarget').textContent = score.targetRuns;
            document.getElementById('liveRrr').textContent = score.requiredRunRate != null ? score.requiredRunRate.toFixed(2) : '-';
        } else {
            rrrContainer.style.display = 'none';
            targetContainer.style.display = 'none';
        }

        // Bowling Card
        document.getElementById('bowlingTeamFlag').textContent = score.bowlingTeamFlag || '🏏';
        document.getElementById('bowlingTeamName').textContent = score.bowlingTeamName || 'Bowling Team';
        document.getElementById('firstInningsScoreVal').textContent = score.firstInningsScore || 'In progress';

        // Equation Status Message
        document.getElementById('heroStatusMessage').textContent = score.statusMessage || score.resultMessage || 'Match in progress';

        // Win Probability
        const prob1 = score.battingTeamWinProbability != null ? score.battingTeamWinProbability : 50;
        const prob2 = score.bowlingTeamWinProbability != null ? score.bowlingTeamWinProbability : 50;
        document.getElementById('probTeam1Label').textContent = `${score.battingTeamShort || 'BAT'} ${prob1}%`;
        document.getElementById('probTeam2Label').textContent = `${score.bowlingTeamShort || 'BOWL'} ${prob2}%`;
        document.getElementById('probTeam1Bar').style.width = `${prob1}%`;
        document.getElementById('probTeam2Bar').style.width = `${prob2}%`;

        // Current Over Delivery Bubbles
        const bubblesContainer = document.getElementById('currentOverBubbles');
        bubblesContainer.innerHTML = '';
        if (score.currentBowler) {
            document.getElementById('bowlerOverLabel').textContent = score.currentBowler.name || '';
        }

        if (score.currentOverBalls && score.currentOverBalls.length > 0) {
            score.currentOverBalls.forEach(ball => {
                const bubble = document.createElement('span');
                let className = 'ball-bubble';
                if (ball === 'W') className += ' run-w';
                else if (ball === '4') className += ' run-4';
                else if (ball === '6') className += ' run-6';
                else if (ball === '0') className += ' run-0';
                else if (ball === 'Wd' || ball === 'Nb') className += ' run-extra';
                else className += ' run-' + ball;

                bubble.className = className;
                bubble.textContent = ball;
                bubblesContainer.appendChild(bubble);
            });
        } else {
            bubblesContainer.innerHTML = '<span class="ball-bubble empty">0</span>';
        }

        // Last Ball Flash Box
        if (score.lastEvent) {
            document.getElementById('lastBallBadge').textContent = score.lastEvent;
            document.getElementById('lastBallDesc').textContent = score.lastCommentary || '';
        }

        // Active Batsmen Table
        const batsmenTbody = document.getElementById('liveBatsmenTbody');
        batsmenTbody.innerHTML = '';

        if (score.striker) {
            batsmenTbody.appendChild(this.createBatsmanRow(score.striker, true));
        }
        if (score.nonStriker) {
            batsmenTbody.appendChild(this.createBatsmanRow(score.nonStriker, false));
        }
        if (!score.striker && !score.nonStriker) {
            batsmenTbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);">Innings break or yet to start</td></tr>';
        }

        // Active Bowler Table
        const bowlerTbody = document.getElementById('liveBowlerTbody');
        bowlerTbody.innerHTML = '';
        if (score.currentBowler) {
            const b = score.currentBowler;
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${b.name}</strong></td>
                <td class="num">${b.overs}</td>
                <td class="num">${b.maidens}</td>
                <td class="num">${b.runsConceded}</td>
                <td class="num text-accent"><strong>${b.wickets}</strong></td>
                <td class="num">${b.economy ? b.economy.toFixed(2) : '0.00'}</td>
            `;
            bowlerTbody.appendChild(tr);
        } else {
            bowlerTbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);">-</td></tr>';
        }
    }

    createBatsmanRow(b, isStriker) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <strong>${b.name}</strong>
                ${isStriker ? '<span class="striker-star" title="On Strike"><i class="fa-solid fa-star"></i></span>' : ''}
            </td>
            <td class="num"><strong>${b.runs}</strong></td>
            <td class="num">${b.balls}</td>
            <td class="num">${b.fours}</td>
            <td class="num">${b.sixes}</td>
            <td class="num">${b.strikeRate ? b.strikeRate.toFixed(1) : '0.0'}</td>
        `;
        return tr;
    }

    renderCommentaryList(balls) {
        const feed = document.getElementById('commentaryFeed');
        feed.innerHTML = '';

        if (!balls || balls.length === 0) {
            feed.innerHTML = '<div class="empty-state">No commentary available yet. Start simulation or record a ball.</div>';
            return;
        }

        balls.forEach(b => {
            feed.appendChild(this.createCommentaryElement(b));
        });
    }

    prependCommentaryBall(ball) {
        const feed = document.getElementById('commentaryFeed');
        const emptyState = feed.querySelector('.empty-state');
        if (emptyState) emptyState.remove();

        const item = this.createCommentaryElement(ball);
        feed.insertBefore(item, feed.firstChild);
    }

    createCommentaryElement(b) {
        const item = document.createElement('div');
        let modifier = '';
        if (b.isWicket) modifier = 'wicket-ball';
        else if (b.runsOffBat === 6) modifier = 'boundary-six';
        else if (b.runsOffBat === 4) modifier = 'boundary-four';

        item.className = `commentary-item ${modifier}`;
        item.innerHTML = `
            <div class="comm-ball-badge">${b.ballDisplay || (b.overNumber + '.' + b.ballNumberInOver)}</div>
            <div class="comm-content">
                <div>
                    <strong>${b.bowler ? b.bowler.name : 'Bowler'}</strong> to <strong>${b.batsman ? b.batsman.name : 'Batter'}</strong>
                    <span class="comm-score-tag">[${b.scoreAtBall || ''}]</span>
                </div>
                <div class="comm-text">${b.commentary}</div>
            </div>
        `;
        return item;
    }

    renderScorecard(card) {
        const container = document.getElementById('scorecardContainer');
        container.innerHTML = '';

        if (card.firstInnings) {
            container.appendChild(this.buildInningsScorecardSection(card.firstInnings, "1st Innings"));
        }
        if (card.secondInnings) {
            container.appendChild(this.buildInningsScorecardSection(card.secondInnings, "2nd Innings"));
        }
    }

    buildInningsScorecardSection(inn, label) {
        const div = document.createElement('div');
        div.className = 'inn-scorecard-card';

        let battingRows = '';
        if (inn.batting && inn.batting.length > 0) {
            inn.batting.forEach(b => {
                battingRows += `
                    <tr>
                        <td>
                            <strong>${b.playerName}</strong>
                            ${b.isCurrentlyBatting ? '<span class="striker-star">*</span>' : ''}
                        </td>
                        <td class="dismissal-col">${b.dismissalInfo}</td>
                        <td class="num"><strong>${b.runs}</strong></td>
                        <td class="num">${b.balls}</td>
                        <td class="num">${b.fours}</td>
                        <td class="num">${b.sixes}</td>
                        <td class="num">${b.strikeRate ? b.strikeRate.toFixed(2) : '0.00'}</td>
                    </tr>
                `;
            });
        }

        let bowlingRows = '';
        if (inn.bowling && inn.bowling.length > 0) {
            inn.bowling.forEach(b => {
                bowlingRows += `
                    <tr>
                        <td><strong>${b.bowlerName}</strong></td>
                        <td class="num">${b.overs}</td>
                        <td class="num">${b.maidens}</td>
                        <td class="num">${b.runsConceded}</td>
                        <td class="num text-accent"><strong>${b.wickets}</strong></td>
                        <td class="num">${b.economy ? b.economy.toFixed(2) : '0.00'}</td>
                        <td class="num">${b.dotBalls || 0}</td>
                    </tr>
                `;
            });
        }

        let fowHtml = '';
        if (inn.fallOfWickets && inn.fallOfWickets.length > 0) {
            const fowStrings = inn.fallOfWickets.map(f => `${f.score}-${f.wicketNumber} (${f.playerOutName}, ${f.over} ov)`).join(', ');
            fowHtml = `
                <div class="fall-of-wickets-wrap">
                    <div class="fow-title">Fall of Wickets:</div>
                    <div>${fowStrings}</div>
                </div>
            `;
        }

        div.innerHTML = `
            <div class="inn-header-bar">
                <div class="inn-title">
                    <span>${inn.teamFlag || ''}</span>
                    <span>${inn.teamName} (${label})</span>
                </div>
                <div class="inn-total-tag">${inn.totalRuns}/${inn.wickets} (${inn.overs} ov)</div>
            </div>

            <!-- Batting Table -->
            <table class="scorecard-table">
                <thead>
                    <tr>
                        <th>Batter</th>
                        <th>Dismissal</th>
                        <th class="num">R</th>
                        <th class="num">B</th>
                        <th class="num">4s</th>
                        <th class="num">6s</th>
                        <th class="num">SR</th>
                    </tr>
                </thead>
                <tbody>
                    ${battingRows}
                    <tr class="extras-row">
                        <td colspan="2">Extras</td>
                        <td colspan="5" style="text-align:right;"><strong>${inn.extras}</strong> (${inn.extrasBreakdown || ''})</td>
                    </tr>
                    <tr class="total-row">
                        <td colspan="2">Total</td>
                        <td colspan="5" style="text-align:right;"><strong>${inn.totalRuns}/${inn.wickets}</strong> (${inn.overs} Ov, RR: ${inn.runRate})</td>
                    </tr>
                </tbody>
            </table>

            ${fowHtml}

            <!-- Bowling Table -->
            <table class="scorecard-table" style="margin-top:20px;">
                <thead>
                    <tr>
                        <th>Bowler</th>
                        <th class="num">O</th>
                        <th class="num">M</th>
                        <th class="num">R</th>
                        <th class="num">W</th>
                        <th class="num">ECON</th>
                        <th class="num">DOTS</th>
                    </tr>
                </thead>
                <tbody>
                    ${bowlingRows}
                </tbody>
            </table>
        `;
        return div;
    }

    /* ==========================================================================
       Canvas HTML5 Progression Worm Chart
       ========================================================================== */
    drawProgressionWormChart(data) {
        this.lastProgressionData = data;
        const canvas = document.getElementById('wormChartCanvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;

        ctx.clearRect(0, 0, width, height);

        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';

        const padding = { top: 30, right: 40, bottom: 40, left: 55 };
        const chartWidth = width - padding.left - padding.right;
        const chartHeight = height - padding.top - padding.bottom;

        const maxOvers = 20;
        let maxRuns = 220; // Default max ceiling

        // Find highest run count in data
        if (data.innings1Progression && data.innings1Progression.length > 0) {
            const last1 = data.innings1Progression[data.innings1Progression.length - 1].cumulativeRuns;
            if (last1 > maxRuns) maxRuns = last1 + 20;
        }
        if (data.innings2Progression && data.innings2Progression.length > 0) {
            const last2 = data.innings2Progression[data.innings2Progression.length - 1].cumulativeRuns;
            if (last2 > maxRuns) maxRuns = last2 + 20;
        }

        // Theme colors
        const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0';
        const labelColor = isDark ? '#94a3b8' : '#64748b';
        const inn1Color = isDark ? '#00e5ff' : '#0284c7';
        const inn2Color = isDark ? '#ffd600' : '#f59e0b';

        // Draw Grid Lines & Axes
        ctx.strokeStyle = gridColor;
        ctx.lineWidth = 1;
        ctx.fillStyle = labelColor;
        ctx.font = '500 11px Inter, sans-serif';

        // Y-axis run intervals (every 40 runs)
        for (let r = 0; r <= maxRuns; r += 40) {
            const y = padding.top + chartHeight - (r / maxRuns) * chartHeight;
            ctx.beginPath();
            ctx.moveTo(padding.left, y);
            ctx.lineTo(width - padding.right, y);
            ctx.stroke();
            ctx.fillText(r.toString(), padding.left - 30, y + 4);
        }

        // X-axis overs intervals (every 2 overs)
        for (let ov = 0; ov <= maxOvers; ov += 2) {
            const x = padding.left + (ov / maxOvers) * chartWidth;
            ctx.beginPath();
            ctx.moveTo(x, padding.top);
            ctx.lineTo(x, padding.top + chartHeight);
            ctx.stroke();
            ctx.fillText(ov + ' ov', x - 12, padding.top + chartHeight + 22);
        }

        // Plot 1st Innings Line
        if (data.innings1Progression && data.innings1Progression.length > 0) {
            this.plotInningsLine(ctx, data.innings1Progression, inn1Color, padding, chartWidth, chartHeight, maxOvers, maxRuns, isDark, true);
        }

        // Plot 2nd Innings Line
        if (data.innings2Progression && data.innings2Progression.length > 0) {
            this.plotInningsLine(ctx, data.innings2Progression, inn2Color, padding, chartWidth, chartHeight, maxOvers, maxRuns, isDark, false);
        }
    }

    plotInningsLine(ctx, progression, color, padding, chartWidth, chartHeight, maxOvers, maxRuns, isDark, hasAreaFill) {
        if (!progression || progression.length === 0) return;

        const startX = padding.left;
        const startY = padding.top + chartHeight;

        // Optional Area Fill under line for premier look
        if (hasAreaFill) {
            ctx.save();
            ctx.beginPath();
            ctx.moveTo(startX, startY);
            progression.forEach(step => {
                const x = padding.left + (step.overNumber / maxOvers) * chartWidth;
                const y = padding.top + chartHeight - (step.cumulativeRuns / maxRuns) * chartHeight;
                ctx.lineTo(x, y);
            });
            const lastStep = progression[progression.length - 1];
            const endX = padding.left + (lastStep.overNumber / maxOvers) * chartWidth;
            ctx.lineTo(endX, startY);
            ctx.closePath();
            const grad = ctx.createLinearGradient(0, padding.top, 0, startY);
            grad.addColorStop(0, isDark ? 'rgba(0, 229, 255, 0.15)' : 'rgba(2, 132, 199, 0.12)');
            grad.addColorStop(1, 'rgba(2, 132, 199, 0)');
            ctx.fillStyle = grad;
            ctx.fill();
            ctx.restore();
        }

        // Draw Line
        ctx.beginPath();
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.moveTo(startX, startY);

        progression.forEach(step => {
            const x = padding.left + (step.overNumber / maxOvers) * chartWidth;
            const y = padding.top + chartHeight - (step.cumulativeRuns / maxRuns) * chartHeight;
            ctx.lineTo(x, y);
        });
        ctx.stroke();

        // Draw points and wicket markers
        progression.forEach(step => {
            const x = padding.left + (step.overNumber / maxOvers) * chartWidth;
            const y = padding.top + chartHeight - (step.cumulativeRuns / maxRuns) * chartHeight;

            if (step.wicketsInOver > 0) {
                // Wicket point in ruby red with crisp white border
                ctx.beginPath();
                ctx.arc(x, y, 6, 0, Math.PI * 2);
                ctx.fillStyle = '#ef4444';
                ctx.fill();
                ctx.lineWidth = 2;
                ctx.strokeStyle = '#ffffff';
                ctx.stroke();
            } else {
                ctx.beginPath();
                ctx.arc(x, y, 3, 0, Math.PI * 2);
                ctx.fillStyle = color;
                ctx.fill();
            }
        });
    }

    /* ==========================================================================
       Leaderboards Rendering
       ========================================================================== */
    renderLeaderboards(data) {
        this.renderTableRows('orangeCapTbody', data.orangeCap, (p, idx) => `
            <tr>
                <td><span class="rank-badge rank-${idx + 1}">${idx + 1}</span></td>
                <td><strong>${p.playerName}</strong> <small style="color:var(--text-muted)">(${p.teamShort})</small></td>
                <td>${p.matches}</td>
                <td class="num"><strong>${p.runs}</strong></td>
                <td class="num">${p.highestScore}</td>
                <td class="num">${p.strikeRate ? p.strikeRate.toFixed(1) : '-'}</td>
            </tr>
        `);

        this.renderTableRows('purpleCapTbody', data.purpleCap, (p, idx) => `
            <tr>
                <td><span class="rank-badge rank-${idx + 1}">${idx + 1}</span></td>
                <td><strong>${p.playerName}</strong> <small style="color:var(--text-muted)">(${p.teamShort})</small></td>
                <td>${p.matches}</td>
                <td class="num text-accent"><strong>${p.wickets}</strong></td>
                <td class="num">${p.economy ? p.economy.toFixed(2) : '-'}</td>
                <td class="num">${p.ballsBowled}</td>
            </tr>
        `);

        this.renderTableRows('strikeRateTbody', data.topStrikeRates, (p, idx) => `
            <tr>
                <td><span class="rank-badge rank-${idx + 1}">${idx + 1}</span></td>
                <td><strong>${p.playerName}</strong> <small style="color:var(--text-muted)">(${p.teamShort})</small></td>
                <td class="num" style="color:var(--accent-cyan)"><strong>${p.strikeRate ? p.strikeRate.toFixed(1) : '-'}</strong></td>
                <td class="num">${p.runs}</td>
                <td class="num">${p.fours}</td>
                <td class="num">${p.sixes}</td>
            </tr>
        `);

        this.renderTableRows('economyTbody', data.bestEconomy, (p, idx) => `
            <tr>
                <td><span class="rank-badge rank-${idx + 1}">${idx + 1}</span></td>
                <td><strong>${p.playerName}</strong> <small style="color:var(--text-muted)">(${p.teamShort})</small></td>
                <td class="num" style="color:var(--accent-green)"><strong>${p.economy ? p.economy.toFixed(2) : '-'}</strong></td>
                <td class="num">${(p.ballsBowled / 6).toFixed(1)}</td>
                <td class="num">${p.runsConceded}</td>
                <td class="num">${p.wickets}</td>
            </tr>
        `);
    }

    renderTableRows(tbodyId, list, rowTemplateFn) {
        const tbody = document.getElementById(tbodyId);
        if (!tbody) return;
        tbody.innerHTML = '';
        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted)">No data</td></tr>';
            return;
        }
        list.forEach((item, idx) => {
            tbody.innerHTML += rowTemplateFn(item, idx);
        });
    }
}

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    window.cricketApp = new CricketApp();
});
