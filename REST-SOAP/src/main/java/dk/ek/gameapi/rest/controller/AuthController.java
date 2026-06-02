package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.service.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}