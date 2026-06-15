package org.dubini.gestion.repository;

import org.dubini.gestion.model.UserPermission;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPermissionRepository extends ListCrudRepository<UserPermission, Long> {
    Optional<UserPermission> findByEmail(String email);
}
