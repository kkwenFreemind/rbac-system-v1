-- PostgreSQL initialization script for RBAC system testing
-- This script runs when the PostgreSQL container starts for the first time

-- Create extensions that might be needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

-- Create a test user (optional, for development)
-- CREATE USER rbac_test WITH PASSWORD 'rbac_test';
-- GRANT ALL PRIVILEGES ON DATABASE rbac_system TO rbac_test;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'RBAC System PostgreSQL database initialized successfully';
END
$$;