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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import vn.qnam.dto.reponse.AuthenticationResponse;
import vn.qnam.dto.reponse.IntrospectResponse;
import vn.qnam.dto.request.AuthenticationDTO;
import vn.qnam.dto.request.IntrospectDTO;
import vn.qnam.dto.request.LogoutDTO;
import vn.qnam.dto.request.RefreshDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.model.InvalidatedToken;
import vn.qnam.model.Role;
import vn.qnam.model.User;
import vn.qnam.repository.InvalidatedRepository;
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
    private final InvalidatedRepository invalidatedRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    @Transactional
    public AuthenticationResponse authenticated(AuthenticationDTO authenticationDTO) {
        var user = userRepository.findUserByUserName(authenticationDTO.getUserName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        //mat khau goc truyen vao truoc mat sau ma hoa
        boolean authenticated = encoder.matches(authenticationDTO.getPassword(), user.getPassword());
        var token = generateToken(user);
        return new AuthenticationResponse(authenticated, token);
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("vnqnam.com")
                .issueTime(new Date())
                .claim("Scope", buildScope(user))
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
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

    public IntrospectResponse introspect(IntrospectDTO introspectRequest) throws ParseException {
        var token = introspectRequest.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (JOSEException  | SecurityException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
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

    private InvalidatedToken invalidatedToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(token, true);
        String jid = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        return InvalidatedToken.builder().id(jid).expiryTime(expiryTime).build();
    }

    public void logout(LogoutDTO request) throws ParseException, JOSEException {
        try {
            InvalidatedToken invalidatedToken = invalidatedToken(request.getToken());
            invalidatedRepository.save(invalidatedToken);
        } catch (SecurityException e) {
            log.info("Token already expired.");
        }

    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshDTO request) throws ParseException, JOSEException {
        InvalidatedToken invalidatedToken = invalidatedToken(request.getToken());

        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findUserByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("User with username=" + username + " not exists"));
        String token = generateToken(user);

        invalidatedRepository.save(invalidatedToken);
        return new AuthenticationResponse(true, token);
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? Date.from(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS))
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) {
            throw new JOSEException("JWT token is invalid or expired.");
        }
        if (invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new SecurityException("Token has been invalidated.");
        }
        return signedJWT;
    }
}
