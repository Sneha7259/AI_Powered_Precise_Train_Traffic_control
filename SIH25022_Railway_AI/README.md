# SIH25022 — AI-Powered Precise Train Traffic Control

A Java-first, simulation-based decision-support prototype for railway section traffic management.

## Safety boundary
This is an educational simulation and decision-support prototype. It does **not** connect to railway signalling, interlocking, GPS/track-circuit infrastructure, or control real trains. All recommendations require human review. The optimizer must never be treated as a certified railway safety system.

## Stack
- Java 21, Spring Boot 3.3, Maven
- React + TypeScript + Vite
- Python FastAPI prediction service (baseline heuristic; replace with trained XGBoost model)
- PostgreSQL (compose service; persistence integration is a later phase)
- Docker Compose

## Run locally
Requirements: Java 21, Maven 3.9+, Node 20+, Python 3.11+.

### Backend
```bash
cd backend
mvn spring-boot:run
```
API: http://localhost:8080/api

### Frontend
```bash
cd frontend
npm install
npm run dev
```
UI: http://localhost:5173

### ML service
```bash
cd ml-service
python -m venv .venv
# Windows: .venv\Scripts\activate
# macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --reload --port 8001
```
API docs: http://localhost:8001/docs

### Docker
```bash
docker compose up --build
```

## Current MVP capabilities
- Seeded synthetic railway section with two stations and two loop points
- Train list and section state API
- Discrete-time train movement simulation
- Single-line block conflict detection
- FCFS baseline and priority-aware recommendation
- Controller accept/reject action (simulation only)
- Dashboard with train status and recommendation cards
- Baseline delay/conflict scoring endpoint in Python

## API
- `GET /api/health`
- `GET /api/section`
- `GET /api/trains`
- `POST /api/simulation/tick`
- `POST /api/scheduling/recommend`
- `POST /api/recommendations/{id}/decision` body: `{"decision":"ACCEPT"}` or `{"decision":"REJECT"}`

## Next engineering milestones
1. Persist trains, blocks, timetables and decisions in PostgreSQL using Spring Data JPA.
2. Add topology editor and configurable block/loop/platform constraints.
3. Replace baseline predictor with a trained XGBoost model and export ONNX.
4. Integrate OR-Tools CP-SAT for constrained multi-train scheduling.
5. Add WebSocket push updates, role-based authentication, audit logs and scenario replay.
6. Benchmark against FCFS using throughput, average delay, total delay and conflicts avoided.

## Repository structure
```text
backend/       Spring Boot API, simulation and scheduling domain
frontend/      React controller dashboard
ml-service/    FastAPI prediction service
docker-compose.yml
```
