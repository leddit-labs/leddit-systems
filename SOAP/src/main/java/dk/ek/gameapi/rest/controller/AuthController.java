package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.service.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenRevocationService tokenRevocationService;

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        Map<String, String> response = new HashMap<>();

        log.debug("Logout endpoint called");

        if (authentication == null) {
            log.warn("Logout called with no authentication");
            response.put("error", "No authentication found");
            response.put("status", "error");
            return ResponseEntity.status(401).body(response);
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("Logout called with non-JWT principal: {}", authentication.getPrincipal().getClass());
            response.put("error", "Invalid token type");
            response.put("status", "error");
            return ResponseEntity.status(401).body(response);
        }

        String tokenId = jwt.getId();

        if (tokenRevocationService.isTokenRevoked(tokenId)) {
            log.warn("Logout called with already revoked token: {}", tokenId);
            response.put("error", "Token already revoked");
            response.put("status", "error");
            return ResponseEntity.status(401).body(response);
        }

        Long expiresAt = jwt.getExpiresAt().getEpochSecond();
        Long issuedAt = jwt.getIssuedAt().getEpochSecond();
        Long expirySeconds = expiresAt - issuedAt;

        log.info("Revoking token: {}, expiry: {} seconds", tokenId, expirySeconds);
        tokenRevocationService.revokeToken(tokenId, expirySeconds);

        SecurityContextHolder.clearContext();

        response.put("message", "Successfully logged out");
        response.put("token_id", tokenId);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("authenticated", false);
            response.put("valid", false);
            response.put("message", "No valid token provided");
            return ResponseEntity.status(401).body(response);
        }

        response.put("authenticated", true);
        response.put("name", authentication.getName());

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String tokenId = jwt.getId();
            boolean isRevoked = tokenRevocationService.isTokenRevoked(tokenId);

            response.put("token_id", tokenId);
            response.put("revoked", isRevoked);
            response.put("expires_at", jwt.getExpiresAt().toString());
            response.put("issued_at", jwt.getIssuedAt().toString());

            if (isRevoked) {
                response.put("valid", false);
                response.put("message", "Token has been revoked");
                return ResponseEntity.status(401).body(response);
            }

            response.put("valid", true);
            response.put("message", "Token is valid");
        }

        return ResponseEntity.ok(response);
    }
}