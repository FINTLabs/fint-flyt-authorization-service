package no.novari.flyt.authorization.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private long id;

    @Setter
    @NaturalId
    @Column(nullable = false, unique = true)
    private UUID objectIdentifier;

    @Setter
    private String email;

    @Setter
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "user_entity_source_application_ids",
            joinColumns = @JoinColumn(name = "user_entity_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_entity_id", "source_application_ids"})
    )
    @Setter
    @Column(name = "source_application_ids")
    @Builder.Default
    private List<Long> sourceApplicationIds = new ArrayList<>();
}
