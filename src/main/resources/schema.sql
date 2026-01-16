-- Drop tables if they exist (for clean re-initialization)
DROP TABLE IF EXISTS users CASCADE^
DROP TABLE IF EXISTS user_roles CASCADE^

-- Users table with custom attributes
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    department VARCHAR(100),
    job_title VARCHAR(100),
    phone_number VARCHAR(20),
    employee_id VARCHAR(50) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
)^

-- User roles table for additional role management
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_name)
)^

-- Indexes to prevent deadlocks and improve query performance
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id)^
CREATE INDEX idx_users_username ON users(username)^
CREATE INDEX idx_users_email ON users(email)^
CREATE INDEX idx_users_employee_id ON users(employee_id)^
CREATE INDEX idx_users_is_active ON users(is_active)^
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id)^
CREATE INDEX idx_user_roles_role_name ON user_roles(role_name)^

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql'^

-- Trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()^
