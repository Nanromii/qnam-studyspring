package vn.qnam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.qnam.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
