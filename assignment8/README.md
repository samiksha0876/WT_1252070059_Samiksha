# 🏏 CricPulse — Real-Time Cricket Score Management System

A scalable, high-performance, and real-time cricket score management platform built with **Spring Boot 3**, **Spring WebSocket (STOMP)**, **Spring Data JPA**, **H2 In-Memory Database**, and a responsive frontend dashboard.

The application simulates a professional live cricket scoring platform (comparable to Hotstar, ESPNcricinfo, and Cricbuzz) capable of handling concurrent matches, real-time ball-by-ball score updates, full batting and bowling scorecards, player statistics, tournament leaderboards, dynamic win probability calculations, and match summaries.

---

## 🌟 Key Features

1. **⚡ Real-Time Ball-by-Ball Live Updates (WebSocket & STOMP)**
   - Instant bi-directional messaging broadcasting score changes, strike changes, over summaries, and live ball events across all connected browser clients with zero latency.
   - Live visual over-tracker (`[ 1 ] [ 4 ] [ 0 ] [ W ] [ 6 ] [ 2 ]`) with color-coded badges for boundaries, wickets, and extras.

2. **🤖 Realistic Live Match Simulation Engine**
   - Background multi-threaded simulation engine (`SimulationService`) that auto-advances deliveries at configurable speeds (1.5s fast, 2.5s normal, 4.0s slow).
   - Generates realistic cricket distributions tuned for **Powerplay** (overs 0–5), **Middle Overs** (overs 6–14), and **Death Overs** (overs 15–20).
   - Dynamic commentary engine with situational ball descriptions (e.g., yorkers, reverse-swing, lofted drives, DRS appeals).

3. **📊 Dynamic Win Probability & Match Analytics**
   - Live win probability meter calculated dynamically based on required run rate (RRR), current run rate (CRR), wickets in hand, and overs remaining.
   - **Interactive Run Rate Worm Chart** rendered on HTML5 Canvas comparing 1st innings vs 2nd innings progression with wicket markers.

4. **📋 Complete Scorecards & Summaries**
   - Full batting scorecard: Runs, Balls, 4s, 6s, Strike Rate, Dismissal info (`c Fielder b Bowler`, `not out`).
   - Full bowling scorecard: Overs, Maidens, Runs conceded, Wickets, Economy Rate, Dot balls.
   - Fall of wickets timeline and extras breakdown (`wides`, `no-balls`, `byes`, `leg-byes`).

5. **🏆 Tournament Leaderboards**
   - **Orange Cap**: Top tournament run-scorers with highest score, average, and strike rate.
   - **Purple Cap**: Top tournament wicket-takers with economy rate and balls bowled.
   - **Power Hitters**: Top strike rates (minimum 20 balls faced).
   - **Economy Bowlers**: Best bowling economies.

6. **🎮 Digital Umpire & Match Scorer Keypad**
   - Interactive scoring console allowing manual entry of runs (0, 1, 2, 3, 4, 6), extras (Wide, No Ball, Bye, Leg Bye), and dismissals (Bowled, Caught, LBW, Run Out, Stumped) with instant WebSocket broadcast.

7. **📡 External Feed Fetcher & Ingestion Pipeline**
   - REST endpoints simulating external cricket provider feed ingestion (`/api/feed/external/{id}` and `/api/feed/ingest/{id}`).

8. **🔊 Web Audio API Sound Effects**
   - Synthesized live boundary chimes, sixer fanfares, and wicket buzzers without external audio dependencies.

---

## 🏗️ Architecture & Technology Stack

```
cricket-score-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/cricket/scoreboard/
│   │   │   ├── config/             # WebSocket (STOMP + SockJS) & CORS Configuration
│   │   │   ├── controller/         # REST Controllers (Match, Scoring, Player, Feed)
│   │   │   ├── dataloader/         # DataInitializer (Teams, Players, Live Fixtures)
│   │   │   ├── dto/                # Data Transfer Objects (LiveScore, Scorecard, Leaderboard)
│   │   │   ├── entity/             # JPA Entities (Match, Innings, Team, Player, BallEvent, Stats)
│   │   │   ├── enums/              # Cricket Domain Enums (MatchStatus, DismissalType, ExtraType)
│   │   │   ├── repository/         # Spring Data JPA Repositories
│   │   │   ├── service/            # Core Business Logic (Match, Scoring, Simulation, Broadcast)
│   │   │   └── CricketScoreboardApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/             # Responsive Web Dashboard
│   │           ├── index.html      # Single-Page Dashboard
│   │           ├── css/style.css   # Dark Broadcast Theme & Glassmorphism
│   │           └── js/app.js       # WebSocket STOMP Client & UI Controller
└── pom.xml                         # Maven dependencies & build setup
```

### Backend:
- **Java 17 / 21 / 25**
- **Spring Boot 3.3.4**
- **Spring Boot Starter Web** (RESTful APIs)
- **Spring Boot Starter WebSocket** (STOMP over SockJS)
- **Spring Data JPA & Hibernate**
- **H2 Database** (Zero configuration, in-memory with Web Console)
- **Lombok** (Boilerplate reduction)

### Frontend:
- **Vanilla HTML5 & CSS3** (Dark broadcast theme, responsive flexbox/grid, micro-animations)
- **Vanilla JavaScript (ES6+)**
- **SockJS & STOMP.js**
- **HTML5 Canvas** (Run rate progression worm chart)
- **Web Audio API** (Sound alert synthesizer)

---

## 🚀 How to Run the Application

### Prerequisites:
- Java JDK 17 or higher
- Apache Maven 3.8+

### Step 1: Start the Spring Boot Application
Open terminal in the project directory:
```powershell
cd e:\WT\cricket-score-management-system
mvn spring-boot:run
```

Or run the pre-built JAR:
```powershell
java -jar target/cricket-score-management-system-1.0.0.jar
```

### Step 2: Access the Live Dashboard
Open your web browser and navigate to:
```
http://localhost:8082
```

### Step 3: Access the H2 Database Console (Optional)
```
URL:      http://localhost:8082/h2-console
JDBC URL: jdbc:h2:mem:cricketdb
User:     sa
Password: (leave blank)
```

---

## 📡 REST API Reference

### 1. Match Endpoints (`/api/matches`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/matches` | Get all matches (optional `?status=LIVE\|COMPLETED\|UPCOMING`) |
| `GET` | `/api/matches/{id}` | Get live score update, current batsmen, bowler, win probability |
| `GET` | `/api/matches/{id}/summary` | Get compact match summary |
| `GET` | `/api/matches/{id}/scorecard` | Get full detailed 1st & 2nd innings scorecard with fall of wickets |
| `GET` | `/api/matches/{id}/commentary`| Get ball-by-ball commentary stream |
| `GET` | `/api/matches/{id}/progression`| Get over-by-over run and wicket chart data |

### 2. Scoring & Simulation Endpoints (`/api/scoring`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/scoring/ball` | Record custom ball delivery (manual umpire mode) |
| `POST` | `/api/scoring/simulate-ball/{id}` | Advance exactly 1 simulated ball delivery |
| `POST` | `/api/scoring/start-simulation/{id}?intervalMs=2500` | Start background real-time auto-simulation |
| `POST` | `/api/scoring/stop-simulation/{id}` | Pause auto-simulation |
| `GET` | `/api/scoring/simulation-status/{id}` | Check if match is currently simulating |

### 3. Player & Leaderboard Endpoints (`/api/players`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/players` | List all players across all teams |
| `GET` | `/api/players/team/{teamId}` | List players by team |
| `GET` | `/api/players/{id}` | Get player profile & career stats |
| `GET` | `/api/players/leaderboard` | Get Orange Cap, Purple Cap, Strike Rate & Economy leaderboards |

### 4. External Feed Endpoints (`/api/feed`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/feed/external/{id}` | Simulate external sports provider JSON feed fetch |
| `POST` | `/api/feed/ingest/{id}` | Ingest and sync external payload into system |

---

## 🎯 Sample Fixtures Included
1. **India vs Australia** (LIVE): High-stakes T20 chase at Kensington Oval, Barbados. Target: 206. Australia 156/4 in 16.3 overs.
2. **England vs South Africa** (LIVE): Nail-biting finish at St Lucia. Target: 164. England 138/4 in 17.3 overs.
3. **Pakistan vs New Zealand** (COMPLETED): High-scoring game at Eden Park. Result: "New Zealand won by 21 runs".
4. **India vs England** (UPCOMING): Semi-Final clash at Providence Stadium, Guyana.
