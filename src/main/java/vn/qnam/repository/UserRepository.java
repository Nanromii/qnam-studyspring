package vn.qnam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.qnam.model.User;
import vn.qnam.util.UserStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findUserByUserName(String userName);
    Optional<User> findUserByFirstName(String firstName);
    List<User> findUserByStatusAndCreateAtLessThan(UserStatus status, Date time);
}
