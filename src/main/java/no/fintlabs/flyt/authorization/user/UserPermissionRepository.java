package no.fintlabs.flyt.authorization.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    Optional<UserPermission> findByObjectIdentifier(String sub);

    default Map<String, UserPermission> findAllAsMapWithObjectIdentifierAsKey() {
        return findAll().stream()
                .collect(Collectors.toMap(
                        UserPermission::getObjectIdentifier,
                        Function.identity()
                ));
    }

}
