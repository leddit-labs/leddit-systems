package dk.ek.gameapi.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.web.client.RestOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CustomJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder delegate;

    public CustomJwtDecoder(String jwkSetUri) throws Exception {
        this.delegate = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(Duration.ofSeconds(60)));
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(validators);
        this.delegate.setJwtValidator(validator);
    }

    public CustomJwtDecoder(String jwkSetUri, RestOperations restOperations) throws Exception {
        NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri);
        if (restOperations != null) {
            builder.restOperations(restOperations);
        }
        this.delegate = builder.build();

        // Create validators without issuer validation
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(Duration.ofSeconds(60)));

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(validators);
        this.delegate.setJwtValidator(validator);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        return delegate.decode(token);
    }
}