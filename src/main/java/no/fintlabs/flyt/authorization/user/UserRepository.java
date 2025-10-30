package no.fintlabs.flyt.authorization.user;

import no.fintlabs.flyt.authorization.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByObjectIdentifier(UUID sub);

    List<UserEntity> findAllByObjectIdentifierIn(Collection<UUID> objectIdentifiers);

}
