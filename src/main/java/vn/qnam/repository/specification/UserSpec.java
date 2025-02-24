package vn.qnam.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.qnam.model.User;
import vn.qnam.util.Gender;

public class UserSpec {
    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("firstName"), firstName + "%");
    }

    public static Specification<User> equalGender(Gender gender) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("gender"), gender);
    }
}
