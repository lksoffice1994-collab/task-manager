-- Create schemas
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS task_service;

-- Create users
CREATE USER user_service_user WITH PASSWORD 'pass123';
CREATE USER task_service_user WITH PASSWORD 'pass123';

-- Schema usage
GRANT USAGE ON SCHEMA user_service TO user_service_user;
GRANT USAGE ON SCHEMA task_service TO task_service_user;

-- Allow object creation
GRANT CREATE ON SCHEMA user_service TO user_service_user;
GRANT CREATE ON SCHEMA task_service TO task_service_user;

-- Grant privileges on existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA user_service TO user_service_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA task_service TO task_service_user;

-- Grant privileges on future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA user_service
GRANT ALL PRIVILEGES ON TABLES TO user_service_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA task_service
GRANT ALL PRIVILEGES ON TABLES TO task_service_user;
