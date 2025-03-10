package vn.qnam.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class UserDetailResponse implements Serializable {
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
