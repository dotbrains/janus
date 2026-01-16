package com.dotbrains.janus.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 *
 * Configures Swagger UI with API documentation and OAuth2 security scheme
 * for Keycloak integration.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Janus API",
        version = "1.0.0",
        description = """
            Janus is a federated authentication service that acts as a gateway between
            client applications and Keycloak, enriching JWTs with custom user data from
            a PostgreSQL database.

            Named after the Roman god of gates, transitions, and beginnings, Janus provides:
            - OAuth2/OIDC authentication with Keycloak
            - Token enhancement with database attributes
            - SpEL-based dynamic claim generation
            - Secure credential management

            ## Authentication Flow

            1. Client initiates OAuth2 login via `/oauth2/authorization/keycloak`
            2. User authenticates with Keycloak
            3. Janus receives OAuth2 callback
            4. Janus enriches token with database attributes
            5. Client receives enhanced JWT with custom claims

            ## Custom Claims

            Enhanced tokens include:
            - User roles and permissions
            - Department and job title
            - Employee ID
            - Custom application attributes
            """,
        contact = @Contact(
            name = "Janus Support",
            email = "nicholas.adamou@outlook.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:9090",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://janus.example.com",
            description = "Production Server"
        )
    }
)
@SecurityScheme(
    name = "oauth2",
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "http://localhost:8080/realms/janus/protocol/openid-connect/auth",
            tokenUrl = "http://localhost:8080/realms/janus/protocol/openid-connect/token",
            scopes = {
                @OAuthScope(name = "openid", description = "OpenID Connect scope"),
                @OAuthScope(name = "profile", description = "User profile information"),
                @OAuthScope(name = "email", description = "User email address")
            }
        )
    )
)
public class OpenApiConfig {
    // Configuration is done via annotations
}
