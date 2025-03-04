package vn.qnam.servie;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.qnam.dto.reponse.AuthenticationResponse;
import vn.qnam.dto.reponse.IntrospectResponse;
import vn.qnam.dto.request.AuthenticationDTO;
import vn.qnam.dto.request.IntrospectRequest;
import vn.qnam.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;
    PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public AuthenticationResponse authenticated(AuthenticationDTO authenticationDTO) {
        var user = userRepository.findUserByUserName(authenticationDTO.getUserName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));


        //mat khau goc truyen vao truoc mat sau ma hoa
        boolean authenticated = encoder.matches(authenticationDTO.getPassword(), user.getPassword());
        var token = generateToken(authenticationDTO.getUserName());
        return new AuthenticationResponse(authenticated, token);
    }

    private String generateToken(String userName) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userName)
                .issuer("vnqnam.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        } catch (JOSEException joseException) {
            log.info("Cannot create token", joseException);
            throw new RuntimeException(joseException);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken();
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }
}
