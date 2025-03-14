package vn.qnam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.qnam.model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
