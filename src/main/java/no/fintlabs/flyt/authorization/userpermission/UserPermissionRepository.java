package no.fintlabs.flyt.authorization.userpermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, Long> {

    Optional<UserPermissionEntity> findByObjectIdentifier(String sub);

}
