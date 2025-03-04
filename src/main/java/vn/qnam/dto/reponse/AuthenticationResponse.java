package vn.qnam.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private boolean authenticated;
    private String token;
}
