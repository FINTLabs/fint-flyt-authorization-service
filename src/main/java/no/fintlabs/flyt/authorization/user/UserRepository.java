package no.fintlabs.flyt.authorization.user;

import no.fintlabs.flyt.authorization.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByObjectIdentifier(UUID sub);

    List<UserEntity> findAllByObjectIdentifierIn(Collection<UUID> objectIdentifiers);

    void deleteByObjectIdentifierNotIn(Set<UUID> objectIdentifiers);

}
