# IDP Development Setup Guide

## Architecture Overview

The IDP system consists of multiple services that work together:

### Services
1. **idp** (Backend): Spring Boot application on port 8081
2. **stock-monitor** (Backend): Spring Boot application on port 8080  
3. **finance-data-service** (Backend): Spring Boot application on port 8082
4. **idp-frontend** (Frontend): React app served on port 3000
5. **stock-monitor-frontend** (Frontend): React app served on port 3001
6. **PostgreSQL Database**: Shared database on port 5432

### Configuration

#### Option 1: Docker Compose (Recommended for Production)

**Setup:**
```bash
cd /home/msun/projects/idp
docker-compose up -d
```

**Services will be available at:**
- idp-frontend: `http://localhost:3000`
- stock-monitor-frontend: `http://localhost:3001`
- idp backend: `http://localhost:8081` (internal)
- stock-monitor backend: `http://localhost:8080` (internal)
- finance-data-service: `http://localhost:8082` (internal)

**Frontend proxy configuration (Nginx):**
- IDP Frontend proxies `/api/` → `idp:8081`
- Stock-Monitor Frontend proxies `/api/` → `stock-monitor:8080`

---

#### Option 2: Local Development (Frontend only)

**When to use:** You're developing the frontend locally and want fast reloads

**Prerequisites:**
- Backends running in Docker: `docker-compose up -d idp stock-monitor finance-data-service postgres-shared`
- **OR** Backends running locally on ports 8080, 8081, 8082

**Setup:**

1. **IDP Frontend** (local dev):
```bash
cd /home/msun/projects/idp/src/frontend
npm run dev
# Frontend will run on port 3002 (or next available)
```

2. **Stock-Monitor Frontend** (local dev):
```bash
cd /home/msun/projects/stock-monitor/src/frontend
npm run dev
# Frontend will run on port 3003 (or next available)
```

**Vite Configuration (`vite.config.js`):**
```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8081',  // idp backend
        changeOrigin: true,
      },
    },
  },
});
```

**Important:** Update `target` to match your backend:
- For IDP frontend dev: `http://localhost:8081`
- For Stock-Monitor frontend dev: `http://localhost:8080`

---

#### Option 3: Full Local Setup (All services locally)

**Prerequisites:**
- Java 17+ installed
- PostgreSQL running on port 5432
- Maven installed

**Backend Setup:**

```bash
# Terminal 1: Start postgres
# (Assuming already running from Docker)

# Terminal 2: Start finance-data-service
cd /home/msun/projects/finance-data-service/src/backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/finance_data"

# Terminal 3: Start stock-monitor
cd /home/msun/projects/stock-monitor/src/backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/stock_monitor"

# Terminal 4: Start idp
cd /home/msun/projects/idp/src/backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/idp"
```

**Frontend Setup (separate terminals):**

```bash
# Terminal 5: IDP Frontend
cd /home/msun/projects/idp/src/frontend
npm run dev

# Terminal 6: Stock-Monitor Frontend
cd /home/msun/projects/stock-monitor/src/frontend
npm run dev
```

---

## Current Setup

Based on docker-compose.yml, the intended setup is:

### Service Port Mapping

| Service | Docker Port | Host Port | Container Name |
|---------|------------|-----------|-----------------|
| PostgreSQL | 5432 | 5432 | postgres-shared |
| IDP Backend | 8081 | 8081 | idp |
| Stock-Monitor Backend | 8080 | 8080 | stock-monitor |
| Finance Data Service | 8082 | 8082 | finance-data-service |
| IDP Frontend | 80 | 3000 | idp-frontend |
| Stock-Monitor Frontend | 80 | 3001 | stock-monitor-frontend |

### Environment Variables

**IDP Backend:**
```
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/idp
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
FINANCE_DATA_BASE_URL: http://finance-data-service:8082
```

**Stock-Monitor Backend:**
```
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/stock_monitor
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
FINANCE_DATA_BASE_URL: http://finance-data-service:8082
```

**Finance Data Service:**
```
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/finance_data
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
```

---

## Troubleshooting

### Issue: 502 Bad Gateway

**Cause:** Frontend can't reach backend because API proxy is not configured or pointing to wrong port

**Solution:**
1. Check `vite.config.js` proxy configuration
2. Verify backend is running on the correct port
3. For IDP dev frontend: should proxy to `http://localhost:8081`
4. For Stock-Monitor dev frontend: should proxy to `http://localhost:8080`

### Issue: Port already in use

**Solution:**
```bash
# Find what's using the port
lsof -i :3000  # or :8080, :8081, etc

# Kill the process
kill -9 <PID>

# Or let Vite use the next available port (3001, 3002, etc)
```

### Issue: Docker containers fail to start

**Cause:** Backends might be taking time to start or there's a build error

**Solution:**
```bash
# Rebuild images without cache
docker-compose build --no-cache

# Start services with logs visible
docker-compose up

# Check individual service logs
docker-compose logs idp
docker-compose logs stock-monitor
```

### Issue: Database connection refused

**Cause:** PostgreSQL not running or credentials wrong

**Solution:**
```bash
# Check PostgreSQL is running
docker-compose ps | grep postgres

# Verify database created correctly
docker exec postgres-shared psql -U postgres -l

# Check initialized databases
docker exec postgres-shared psql -U postgres -d postgres -c "\l"
```

---

## Quick Start Commands

### Start Everything with Docker
```bash
cd /home/msun/projects/idp
docker-compose up -d
# Wait for startup: docker-compose ps
# Access: http://localhost:3000 (idp) or http://localhost:3001 (stock-monitor)
```

### Stop Everything
```bash
docker-compose down
```

### Local Frontend Development
```bash
# Start backends in Docker
docker-compose up -d idp stock-monitor finance-data-service postgres-shared

# Start frontend dev server
cd /home/msun/projects/idp/src/frontend
npm run dev

# Access: http://localhost:3002 (or next available port)
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f idp
docker-compose logs -f stock-monitor-frontend
```

---

## Configuration Files Reference

- **Backend**: `application.yml` in each service's `src/main/resources/`
- **Frontend (Docker)**: `entrypoint.sh` handles Nginx proxy configuration
- **Frontend (Dev)**: `vite.config.js` handles dev server proxy configuration
- **Compose**: `docker-compose.yml` defines all services and networking

---

## Network Details

Services running in Docker use a shared bridge network called `shared-network`. This allows:
- IDP container to call `http://idp:8081`
- Stock-Monitor container to call `http://stock-monitor:8080`
- Frontends to call `http://finance-data-service:8082`

From the host machine, use `localhost` or IP address instead of container names.
