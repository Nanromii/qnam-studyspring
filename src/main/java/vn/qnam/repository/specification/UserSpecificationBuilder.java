package vn.qnam.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.qnam.model.User;

import java.util.ArrayList;
import java.util.List;

import static vn.qnam.repository.specification.SearchOperation.*;

public class UserSpecificationBuilder {
    private final List<SpecSearchCriteria> param;

    public UserSpecificationBuilder() {
        param = new ArrayList<>();
    }

    public UserSpecificationBuilder with(String key, String operation, String value, String prefix, String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public UserSpecificationBuilder with(String orPredicate, String key, String operation, String value, String prefix, String suffix) {
        SearchOperation searchOp = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOp != null) {
            if (searchOp == EQUALITY) {
                final boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    searchOp = CONTAINS;
                } else if (startWithAsterisk) {
                    searchOp = ENDS_WITH;
                } else if (endWithAsterisk) {
                    searchOp = STARTS_WITH;
                }
            }
            param.add(new SpecSearchCriteria(orPredicate, key, searchOp, value));
        }
        return this;
    }

    public Specification<User> build() {
        if (param.isEmpty()) return null;

        Specification<User> spec = new UserSpecification(param.get(0));
        for (int i = 1; i < param.size(); i++) {
            spec = param.get(i).getOrPredicate()
                    ? Specification.where(spec).or(new UserSpecification(param.get(i)))
                    : Specification.where(spec).and(new UserSpecification(param.get(i)));
        }
        return spec;
    }
}
