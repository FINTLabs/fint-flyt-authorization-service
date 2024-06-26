package no.fintlabs.flyt.authorization.userpermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionEntity, Long> {

    Optional<UserPermissionEntity> findByObjectIdentifier(String sub);

    default Map<String, UserPermissionEntity> findAllAsMapWithObjectIdentifierAsKey() {
        return findAll().stream()
                .collect(Collectors.toMap(
                        UserPermissionEntity::getObjectIdentifier,
                        Function.identity()
                ));
    }

}
