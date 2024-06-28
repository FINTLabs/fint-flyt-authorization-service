package no.fintlabs.flyt.authorization.user;

import no.fintlabs.flyt.authorization.user.model.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, Long> {

    Optional<UserPermissionEntity> findByObjectIdentifier(UUID sub);

    List<UserPermissionEntity> findAllByObjectIdentifierIn(Collection<UUID> objectIdentifiers);

    interface ObjectIdentifierSelection {
        UUID getObjectIdentifier();
    }

    List<ObjectIdentifierSelection> findObjectIdentifiersByObjectIdentifierIn(Collection<UUID> objectIdentifiers);

    void deleteByObjectIdentifierNotIn(Set<UUID> objectIdentifiers);

}
