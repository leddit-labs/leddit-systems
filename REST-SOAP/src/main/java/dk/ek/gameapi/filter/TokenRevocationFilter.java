package dk.ek.gameapi.filter;

import dk.ek.gameapi.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
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

        String method = request.getMethod();
        String path = request.getRequestURI();

        // Check if this is a public GET endpoint (should skip revocation check)
        boolean isPublicGetEndpoint = HttpMethod.GET.matches(method) && (
                path.equals("/api/v1/games") ||
                        path.matches("^/api/v1/games/\\d+$") ||
                        path.equals("/api/v1/games/available")
        );

        // Skip revocation check for public GET endpoints
        if (isPublicGetEndpoint) {
            log.debug("Skipping revocation check for public GET: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        // For protected endpoints, check if token is revoked
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String tokenId = jwt.getId();
                boolean isRevoked = tokenRevocationService.isTokenRevoked(tokenId);

                if (isRevoked) {
                    log.warn("Rejected {} request on {} with revoked token: {}", method, path, tokenId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token has been revoked\", \"status\": 401}");
                    response.getWriter().flush();
                    SecurityContextHolder.clearContext();
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}