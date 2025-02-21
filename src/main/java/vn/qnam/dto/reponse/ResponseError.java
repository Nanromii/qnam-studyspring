package vn.qnam.dto.reponse;

public class ResponseError<Long> extends ResponseData<Long>{
    public ResponseError(int status, String message) {
        super(status, message);
    }
}
