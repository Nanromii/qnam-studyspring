package vn.qnam.controller;

import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.qnam.dto.reponse.ResponseData;
import vn.qnam.dto.reponse.ResponseError;
import vn.qnam.dto.request.AuthenticationDTO;
import vn.qnam.dto.request.IntrospectRequest;
import vn.qnam.servie.AuthenticationService;

import java.text.ParseException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/auth")
@Validated
@Slf4j
@Tag(name = "Authentication Controller")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/log-in")
    public ResponseData<?> authenticate(@RequestBody AuthenticationDTO authDTO) {
        try {
            log.info("Authenticate successfully.");
            return new ResponseData<>(HttpStatus.OK.value(), "Authenticate successfully", authService.authenticated(authDTO));
        } catch (NoSuchElementException e) {
            log.error("User not found: {}", authDTO.getUserName(), e);
            return new ResponseError<>(HttpStatus.NOT_FOUND.value(), "User not found.");
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return new ResponseError<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Authentication error.");
        }
    }

    @PostMapping("/introspect")
    public ResponseData<?> authenticate(@RequestBody IntrospectRequest introspectRequest) throws ParseException, JOSEException {
        var result = authService.introspect(introspectRequest);
        return new ResponseData<>(HttpStatus.OK.value(), "Authenticate successfully", result);
    }
}
