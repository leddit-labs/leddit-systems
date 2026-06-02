package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.service.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenRevocationService tokenRevocationService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String tokenId = jwt.getId();
            Long expirySeconds = jwt.getExpiresAt().getEpochSecond() -
                    jwt.getIssuedAt().getEpochSecond();
            tokenRevocationService.revokeToken(tokenId, expirySeconds);
        }
        return ResponseEntity.ok().build();
    }

    // Debug endpoint to check authentication status
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();

        if (authentication == null) {
            debug.put("authenticated", false);
            debug.put("message", "No authentication object found");
            debug.put("headers", "Check if Authorization header is being sent");
        } else {
            debug.put("authenticated", authentication.isAuthenticated());
            debug.put("name", authentication.getName());
            debug.put("principal_type", authentication.getPrincipal().getClass().getName());

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                debug.put("token_type", "JWT");
                debug.put("token_id", jwt.getId());
                debug.put("issuer", jwt.getIssuer().toString());
                debug.put("subject", jwt.getSubject());
                debug.put("claims", jwt.getClaims());
            }
        }

        return ResponseEntity.ok(debug);
    }

    @GetMapping("/debug/issuer")
    public ResponseEntity<Map<String, String>> debugIssuer() {
        Map<String, String> debug = new HashMap<>();

        debug.put("expected_issuer_from_config", System.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "NOT SET"));
        debug.put("actual_env_issuer_uri", System.getenv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI"));

        return ResponseEntity.ok(debug);
    }
}