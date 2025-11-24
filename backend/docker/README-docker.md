# RBAC System - Docker Compose Setup

This directory contains Docker Compose configuration for setting up PostgreSQL and Redis services for testing the RBAC Common Layer.

## Services

### PostgreSQL

- **Image**: `postgres:14-alpine`
- **Port**: `5432`
- **Database**: `rbac_system`
- **Username**: `postgres`
- **Password**: `postgres`
- **Volume**: `postgres_data` for data persistence
- **Health Check**: Enabled with `pg_isready` command

### Redis

- **Image**: `redis:7-alpine`
- **Port**: `6379`
- **Configuration**: Custom `redis.conf` with AOF persistence
- **Volume**: `redis_data` for data persistence
- **Health Check**: Enabled with `redis-cli ping`

## Usage

### Start Services

```bash
docker-compose up -d
```

### Stop Services

```bash
docker-compose down
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f redis
```

### Reset Data

```bash
# Stop and remove containers and volumes
docker-compose down -v

# Restart fresh
docker-compose up -d
```

## Configuration

### Application Properties

Update your `application.yml` or environment variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac_system
    username: postgres
    password: postgres

  redis:
    host: localhost
    port: 6379
```

### Environment Variables

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=rbac_system
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

export REDIS_HOST=localhost
export REDIS_PORT=6379
```

## Testing

### Integration Tests

The services are configured for use with Testcontainers in integration tests. The containers will be automatically started by Testcontainers when running tests.

### Manual Testing

For manual testing or development:

1. Start the services: `docker-compose up -d`
2. Wait for health checks to pass
3. Run your application with the above configuration
4. Test database connections and Redis operations

## Health Checks

Both services include health checks that verify:

- PostgreSQL: Database is ready to accept connections
- Redis: Server responds to PING commands

Check container health:

```bash
docker ps
# Look for "healthy" status in STATUS column
```

## Data Persistence

- **PostgreSQL**: Data is persisted in `postgres_data` volume
- **Redis**: Data is persisted using AOF (Append Only File) in `redis_data` volume

To completely reset all data:

```bash
docker-compose down -v
```

## Troubleshooting

### PostgreSQL Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Connect manually
docker exec -it rbac-postgres psql -U postgres -d rbac_system
```

### Redis Connection Issues

```bash
# Check if Redis is running
docker-compose ps redis

# Check logs
docker-compose logs redis

# Test connection
docker exec -it rbac-redis redis-cli ping
```

### Port Conflicts

If ports 5432 or 6379 are already in use, modify the `ports` section in `docker-compose.yml`:

```yaml
ports:
  - "5433:5432"  # Change host port to 5433
```

## Security Notes

This configuration is intended for **development and testing only**:

- Default passwords are used
- No authentication required for Redis
- Protected mode is disabled

For production deployments, configure proper authentication and security settings.
