package vn.qnam.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.qnam.model.User;

import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchCriteriaConsumer implements Consumer<SearchCriteria> {
    private CriteriaBuilder criteriaBuilder;
    private Predicate predicate;
    private Root<User> root;

    @Override
    public void accept(SearchCriteria parameter) {
        if (parameter.getOperation().equals(">")) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get(parameter.getKey()), parameter.getValue().toString()));
        } else if (parameter.getOperation().equals("<")) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(parameter.getKey()), parameter.getValue().toString()));
        } else {
            if (root.get(parameter.getKey()).getJavaType() == String.class) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(parameter.getKey()), "%" + parameter.getValue().toString() + "%"));
            } else {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(parameter.getKey()), parameter.getValue().toString()));
            }
        }
    }
}
