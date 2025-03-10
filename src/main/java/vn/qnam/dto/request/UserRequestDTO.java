package vn.qnam.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import vn.qnam.util.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static vn.qnam.util.Gender.*;

@Data
public class UserRequestDTO implements Serializable {
    @NotBlank(message = "firstName must be not blank")
    private String firstName;

    @NotNull(message = "lastName must be not null")
    private String lastName;

    @Email(message = "email invalid format")
    private String email;

    @PhoneNumber
    private String phone;

    @NotNull(message = "dateOfBirth must be not null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;

    @NotEmpty
    private List<ScoreDTO> score;

    @EnumPattern(name = "status", regexp = "ACTIVE|INACTIVE|NONE")
    private UserStatus status;

    @GenderSubset(anyOf = {MALE, FEMALE, OTHER})
    private Gender gender;

    @NotNull(message = "type must be not null")
    @EnumValue(name = "type", enumClass = Scope.class)
    private String type;

    @NotNull(message = "username must be not null")
    private String username;

    @NotNull(message = "password must be not null")
    private String password;

    public UserRequestDTO(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public void setFirstName(@NotBlank(message = "firstName must be not blank") String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(@NotNull(message = "lastName must be not null") String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(@Email(message = "email invalid format") String email) {
        this.email = email;
    }

    public void setPhone(@Pattern(regexp = "^\\d{10}$", message = "phone invalid format") String phone) {
        this.phone = phone;
    }

    public void setDateOfBirth(@NotBlank(message = "dateOfBirth must be not blank") Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setScore(@NotEmpty List<ScoreDTO> score) {
        this.score = score;
    }

    public void setStatus(@EnumPattern(name = "status", regexp = "ACTIVE|INACTIVE|NONE") UserStatus status) {
        this.status = status;
    }
}
