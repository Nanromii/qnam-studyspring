package vn.qnam.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerValidationException(Exception e, WebRequest webRequest) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        if (e instanceof MethodArgumentNotValidException) {
            errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            errorResponse.setPath(webRequest.getDescription(false).replace("uri=", ""));
            errorResponse.setError("Payload Invalid");
            String message = e.getMessage();
            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            errorResponse.setMessage(message.substring(start + 1, end - 1));
        }
        return errorResponse;
    }
}
