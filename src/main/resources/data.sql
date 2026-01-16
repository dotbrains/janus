-- Insert sample users
INSERT INTO users (keycloak_id, username, email, first_name, last_name, department, job_title, phone_number, employee_id, is_active)
VALUES
    ('kc-user-001', 'john.doe', 'john.doe@dotbrains.com', 'John', 'Doe', 'Engineering', 'Senior Software Engineer', '+1-555-0101', 'EMP001', true),
    ('kc-user-002', 'jane.smith', 'jane.smith@dotbrains.com', 'Jane', 'Smith', 'Product', 'Product Manager', '+1-555-0102', 'EMP002', true),
    ('kc-user-003', 'bob.johnson', 'bob.johnson@dotbrains.com', 'Bob', 'Johnson', 'Engineering', 'DevOps Engineer', '+1-555-0103', 'EMP003', true),
    ('kc-user-004', 'alice.williams', 'alice.williams@dotbrains.com', 'Alice', 'Williams', 'Design', 'UX Designer', '+1-555-0104', 'EMP004', true),
    ('kc-user-005', 'charlie.brown', 'charlie.brown@dotbrains.com', 'Charlie', 'Brown', 'Engineering', 'Junior Developer', '+1-555-0105', 'EMP005', true),
    ('kc-user-006', 'diana.prince', 'diana.prince@dotbrains.com', 'Diana', 'Prince', 'Security', 'Security Architect', '+1-555-0106', 'EMP006', true),
    ('kc-user-007', 'admin.user', 'admin@dotbrains.com', 'Admin', 'User', 'IT', 'System Administrator', '+1-555-0100', 'EMP000', true)^

-- Insert user roles
INSERT INTO user_roles (user_id, role_name)
VALUES
    -- John Doe - Senior Engineer
    ((SELECT id FROM users WHERE username = 'john.doe'), 'USER'),
    ((SELECT id FROM users WHERE username = 'john.doe'), 'DEVELOPER'),
    ((SELECT id FROM users WHERE username = 'john.doe'), 'SENIOR'),
    
    -- Jane Smith - Product Manager
    ((SELECT id FROM users WHERE username = 'jane.smith'), 'USER'),
    ((SELECT id FROM users WHERE username = 'jane.smith'), 'PRODUCT_MANAGER'),
    
    -- Bob Johnson - DevOps
    ((SELECT id FROM users WHERE username = 'bob.johnson'), 'USER'),
    ((SELECT id FROM users WHERE username = 'bob.johnson'), 'DEVELOPER'),
    ((SELECT id FROM users WHERE username = 'bob.johnson'), 'DEVOPS'),
    
    -- Alice Williams - Designer
    ((SELECT id FROM users WHERE username = 'alice.williams'), 'USER'),
    ((SELECT id FROM users WHERE username = 'alice.williams'), 'DESIGNER'),
    
    -- Charlie Brown - Junior Dev
    ((SELECT id FROM users WHERE username = 'charlie.brown'), 'USER'),
    ((SELECT id FROM users WHERE username = 'charlie.brown'), 'DEVELOPER'),
    
    -- Diana Prince - Security
    ((SELECT id FROM users WHERE username = 'diana.prince'), 'USER'),
    ((SELECT id FROM users WHERE username = 'diana.prince'), 'SECURITY'),
    ((SELECT id FROM users WHERE username = 'diana.prince'), 'ADMIN'),
    
    -- Admin User - Full access
    ((SELECT id FROM users WHERE username = 'admin.user'), 'USER'),
    ((SELECT id FROM users WHERE username = 'admin.user'), 'ADMIN'),
    ((SELECT id FROM users WHERE username = 'admin.user'), 'SUPER_ADMIN')^
