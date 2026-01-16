# Contributing

Thank you for your interest in contributing! This document provides guidelines for contributing to this project.

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- A clear and descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Environment details (OS, Java version, etc.)
- Relevant logs or screenshots

Use the bug report template when creating a new issue.

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- A clear and descriptive title
- Detailed description of the proposed feature
- Use cases and benefits
- Possible implementation approach

Use the feature request template when creating a new issue.

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the coding style** of the project
3. **Write tests** for your changes
4. **Update documentation** if needed
5. **Ensure tests pass** locally
6. **Submit a pull request** with a clear description

#### Pull Request Process

1. Update the README.md or relevant documentation with details of changes
2. Update the CHANGELOG.md following the existing format
3. Ensure your code follows the project's coding standards
4. Add or update tests as needed
5. Make sure all tests pass: `mvn test`
6. Update environment variable examples if you add new configuration
7. The PR will be merged once you have approval from a maintainer

## Development Setup

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Setting Up Development Environment

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/janus.git
cd janus

# Start infrastructure
docker-compose up -d

# Build the project
mvn clean package

# Run tests
mvn test

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Coding Standards

### Java Code Style

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep methods focused and concise (Single Responsibility Principle)
- Add comments for complex logic
- Use Lombok annotations to reduce boilerplate
- Follow the existing code structure (Vertical Slice Architecture)

### Code Organization

- Place new features in appropriate packages following vertical slice architecture
- API interfaces go in `api/v{version}/`
- Implementation goes in feature-specific packages (e.g., `auth/v{version}/`, `user/v{version}/`)
- Shared domain logic in root feature package (e.g., `user/`)
- Configuration in `config/`

### Testing

- Write unit tests for business logic
- Write integration tests for API endpoints
- Aim for meaningful test coverage, not just high percentages
- Use descriptive test method names
- Follow Arrange-Act-Assert pattern

```java
@Test
void shouldReturnUserWhenValidKeycloakIdProvided() {
    // Arrange
    String keycloakId = "test-id";
    User expectedUser = createTestUser(keycloakId);
    
    // Act
    User actualUser = userService.findByKeycloakId(keycloakId);
    
    // Assert
    assertThat(actualUser).isEqualTo(expectedUser);
}
```

### Documentation

- Update README.md for user-facing changes
- Update relevant docs/ files for architectural changes
- Add inline comments for complex logic
- Update API documentation (Swagger annotations)
- Keep CHANGELOG.md current

### Commit Messages

Write clear, concise commit messages:

```
feat: add user profile endpoint

- Add GET /api/v1/users/profile endpoint
- Add UserProfileDTO for response
- Add tests for new endpoint

Closes #123
```

Format: `type: subject`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

## API Versioning

When making breaking changes:

1. Create a new API version (e.g., `v2`)
2. Copy relevant interfaces from previous version
3. Implement new controllers
4. Update documentation
5. Keep old version running alongside new one

See [docs/API_VERSIONING.md](docs/API_VERSIONING.md) for details.

## Security

- Never commit sensitive data (passwords, secrets, keys)
- Use environment variables for configuration
- Follow OWASP guidelines for web security
- Report security vulnerabilities privately (see SECURITY.md)
- Keep dependencies up to date

## Questions?

Feel free to open an issue for questions or join discussions in existing issues.

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

## Recognition

Contributors will be recognized in the project's documentation and releases.

Thank you for contributing! 🎉
