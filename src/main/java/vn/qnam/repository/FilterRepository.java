package vn.qnam.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.qnam.dto.reponse.PageResponse;
import vn.qnam.model.Score;
import vn.qnam.model.User;
import vn.qnam.repository.criteria.SearchCriteria;
import vn.qnam.repository.criteria.UserSearchCriteriaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class FilterRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getUsersByFiltering(int pageNo, int pageSize, String search, String sortBy) {
        StringBuilder sqlQuery = new StringBuilder("select new vn.qnam.dto.reponse.UserDetailResponse(u.firstName, u.lastName, u.email, u.phone) from User u where 1=1");
        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" and lower(u.firstName) like lower(:firstName)");
            sqlQuery.append(" or lower(u.lastName) like lower(:lastName)");
            sqlQuery.append(" or lower(u.email) like lower(:email)");
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                sqlQuery.append(String.format(" order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", String.format("%%%s%%", search));
            selectQuery.setParameter("lastName", String.format("%%%s%%", search));
            selectQuery.setParameter("email", String.format("%%%s%%", search));
        }
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);

        StringBuilder sqlCountQuery = new StringBuilder("select count(*) from User u where 1=1");
        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" and lower(u.firstName) like lower(?1)");
            sqlCountQuery.append(" or lower(u.lastName) like lower(?2)");
            sqlCountQuery.append(" or lower(u.email) like lower(?3)");
        }

        Query countQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            countQuery.setParameter(1, String.format("%%%s%%", search));
            countQuery.setParameter(2, String.format("%%%s%%", search));
            countQuery.setParameter(3, String.format("%%%s%%", search));
        }

        Long totalTuples = (Long) countQuery.getSingleResult();
        List<?> users = selectQuery.getResultList();

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<?> page = new PageImpl<>(users, pageable, totalTuples);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(users)
                .build();
    }

    public PageResponse<?> getUsersByCriteria(int pageNo, int pageSize, String sortBy, String score, String... search) {
        List<SearchCriteria> searchCriteriaList = new ArrayList<>();
        //1 Lay list users
        if (search != null) {
            for (String s : search) {
                Pattern pattern = Pattern.compile("(\\w+?)(=|>|<)(.*)");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    searchCriteriaList.add(
                            new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3))
                    );
                }
            }
        }

        //2 Lay totalElements
        List<User> users = getUsers(pageNo, pageSize, searchCriteriaList, sortBy, score);
        Long totalElements = getTotalElements(searchCriteriaList, score);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(totalElements.intValue()) //So ban ghi
                .items(users)
                .build();
    }

    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> searchCriteriaList, String sortBy, String score) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaConsumer userSearchCriteriaConsumer = new UserSearchCriteriaConsumer(criteriaBuilder, predicate, root);
        //Search
        if (StringUtils.hasLength(score)) {
            Join<Score, User> scoreUserJoin = root.join("scoresList");
            Predicate scorePredicate = criteriaBuilder.equal(scoreUserJoin.get("scoreValue"), score);
            query.where(predicate, scorePredicate);
        } else {
            searchCriteriaList.forEach(userSearchCriteriaConsumer);
            predicate = userSearchCriteriaConsumer.getPredicate();
            query.where(predicate);
        }

        //Sort
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equals("asc")) {
                    query.orderBy(criteriaBuilder.asc(root.get(matcher.group(1))));
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get(matcher.group(1))));
                }
            }
        }

        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    private Long getTotalElements(List<SearchCriteria> searchCriteriaList, String score) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaConsumer userSearchCriteriaConsumer = new UserSearchCriteriaConsumer(criteriaBuilder, predicate, root);
        //Search
        if (StringUtils.hasLength(score)) {
            Join<Score, User> scoreUserJoin = root.join("scoresList");
            Predicate scorePredicate = criteriaBuilder.equal(scoreUserJoin.get("scoreValue"), Integer.parseInt(score));
            query.select(criteriaBuilder.countDistinct(root));
            query.where(predicate, scorePredicate);
        } else {
            searchCriteriaList.forEach(userSearchCriteriaConsumer);
            predicate = userSearchCriteriaConsumer.getPredicate();
            query.select(criteriaBuilder.count(root));
            query.where(predicate);
        }


        return entityManager.createQuery(query).getSingleResult();
    }
}
