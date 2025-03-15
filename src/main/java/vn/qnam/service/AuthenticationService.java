package vn.qnam.service;

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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import vn.qnam.dto.reponse.AuthenticationResponse;
import vn.qnam.dto.reponse.IntrospectResponse;
import vn.qnam.dto.request.AuthenticationDTO;
import vn.qnam.dto.request.IntrospectDTO;
import vn.qnam.model.Role;
import vn.qnam.model.User;
import vn.qnam.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        var token = generateToken(user);
        return new AuthenticationResponse(authenticated, token);
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("vnqnam.com")
                .issueTime(new Date())
                .claim("Scope", buildScope(user))
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

    public IntrospectResponse introspect(IntrospectDTO introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken();
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    private List<String> buildScope(User user) {
        List<String> scopes = new ArrayList<>();
        if (StringUtils.hasLength(user.getRole().toString())) {
            /*
                !CollectionUtils.isEmpty(user.getRole()) de join cac role lai,
                tuy nhien o demo nay dang de role la Role chu khong phai Set<Role>
                nen dung StringUtils.hasLength
            */

            Role role = user.getRole();
            scopes.add("ROLE_" + role.getName());
            if (!CollectionUtils.isEmpty(role.getPermissions())) {
                role.getPermissions().forEach(permission -> scopes.add(permission.getName()));
            }
        }
        return scopes;
    }
}
