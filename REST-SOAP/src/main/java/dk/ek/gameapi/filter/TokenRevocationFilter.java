package dk.ek.gameapi.filter;

import dk.ek.gameapi.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRevocationFilter extends OncePerRequestFilter {

    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("TokenRevocationFilter - Processing request: {} {}", request.getMethod(), request.getRequestURI());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            log.info("TokenRevocationFilter - Authentication found, authenticated: {}", authentication.isAuthenticated());

            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String tokenId = jwt.getId();
                boolean isRevoked = tokenRevocationService.isTokenRevoked(tokenId);

                log.info("TokenRevocationFilter - Token ID: {}, Revoked: {}", tokenId, isRevoked);

                if (isRevoked) {
                    log.warn("TokenRevocationFilter - REJECTING request with revoked token: {}", tokenId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token has been revoked\", \"status\": 401}");
                    response.getWriter().flush();
                    SecurityContextHolder.clearContext();
                    return;
                }
            }
        } else {
            log.info("TokenRevocationFilter - No authentication found");
        }

        filterChain.doFilter(request, response);
    }
}