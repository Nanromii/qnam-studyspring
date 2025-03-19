package vn.qnam.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum Error {
    USER_EXISTS(409, "User already exists.", HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(9999, "Unknown error. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    Error(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
