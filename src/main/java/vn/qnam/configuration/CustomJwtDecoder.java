package vn.qnam.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import vn.qnam.dto.reponse.IntrospectResponse;
import vn.qnam.dto.request.IntrospectDTO;
import vn.qnam.service.AuthenticationService;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    private final AuthenticationService authenticationService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        validateToken(token);
        return getNimbusJwtDecoder().decode(token);
    }

    private void validateToken(String token) {
        try {
            IntrospectResponse response = authenticationService.introspect(IntrospectDTO.builder().token(token).build());
            if (!response.isValid()) {
                throw new JwtException("Token is invalid or expired.");
            }
        } catch (ParseException e) {
            throw new JwtException("Error parsing token: " + e.getMessage(), e);
        }
    }

    private synchronized NimbusJwtDecoder getNimbusJwtDecoder() {
        if (nimbusJwtDecoder == null) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return nimbusJwtDecoder;
    }
}
