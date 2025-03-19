package vn.qnam.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ResponseData<T>{
    private final int status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    //Neu null thi se khong in ra o frontend
    private T data;

    public ResponseData(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
