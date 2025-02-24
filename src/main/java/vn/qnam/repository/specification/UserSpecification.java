package vn.qnam.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import vn.qnam.model.User;

@Getter
@AllArgsConstructor
public class UserSpecification implements Specification<User> {
    private SpecSearchCriteria specSearchCriteria;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return switch (specSearchCriteria.getSearchOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(specSearchCriteria.getKey()), specSearchCriteria.getValue());
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(specSearchCriteria.getKey()), specSearchCriteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue().toString() + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(specSearchCriteria.getKey()), specSearchCriteria.getValue() + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(specSearchCriteria.getKey()), "%" + specSearchCriteria.getValue().toString() + "%");
        };
    }
}
