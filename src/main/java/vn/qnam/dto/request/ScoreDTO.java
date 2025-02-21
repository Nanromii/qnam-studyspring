package vn.qnam.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.io.Serializable;

@Data
public class ScoreDTO implements Serializable {
    private int score;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ScoreDTO(Integer score) {
        this.score = score;
    }
}
