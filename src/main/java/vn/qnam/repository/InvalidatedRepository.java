package vn.qnam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.qnam.model.InvalidatedToken;

import java.util.Date;
import java.util.List;

@Repository
public interface InvalidatedRepository extends JpaRepository<InvalidatedToken, String> {
    List<InvalidatedToken> findByExpiryTimeBefore(Date oneHourAgo);
    List<InvalidatedToken> findByExpiryTimeAfter(Date now);
    List<InvalidatedToken> findByExpiryTimeLessThan(Date now);
}
