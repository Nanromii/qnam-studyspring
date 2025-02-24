package vn.qnam.repository.specification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecSearchCriteria {
    private String key;
    private SearchOperation searchOperation;
    private Object value;
    private Boolean orPredicate;

    public SpecSearchCriteria(final String key, final SearchOperation operation, final Object value) {
        super();
        this.key = key;
        this.searchOperation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(String orPredicate, String key, SearchOperation searchOperation, String value) {
        super();
        this.key = key;
        this.orPredicate = orPredicate != null && orPredicate.equals(SearchOperation.OR_PREDICATE_FLAG);
        this.searchOperation = searchOperation;
        this.value = value;
    }

    public SpecSearchCriteria(String key, String operation, String value, String prefix, String suffix) {
        SearchOperation searchOp = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOp != null) {
            if (searchOp == SearchOperation.EQUALITY) {
                boolean startWith = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                boolean endWith = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                if (startWith && endWith) {
                    searchOp = SearchOperation.CONTAINS;
                } else if (startWith) {
                    searchOp = SearchOperation.ENDS_WITH;
                } else if (endWith) {
                    searchOp = SearchOperation.STARTS_WITH;
                }
            }
        }
        this.key = key;
        this.searchOperation = searchOp;
        this.value = value;
    }
}
