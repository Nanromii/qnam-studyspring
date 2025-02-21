package vn.qnam.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Score")
@Table(name = "scores")
public class Score extends AbstractEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "score_value")
    private Integer scoreValue;
}
