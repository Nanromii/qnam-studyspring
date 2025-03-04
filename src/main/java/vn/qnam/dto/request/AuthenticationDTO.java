package vn.qnam.dto.request;

import lombok.Data;

@Data
public class AuthenticationDTO {
    private String userName;
    private String password;
}
