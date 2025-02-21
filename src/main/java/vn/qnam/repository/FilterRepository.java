package vn.qnam.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.qnam.dto.reponse.PageResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
