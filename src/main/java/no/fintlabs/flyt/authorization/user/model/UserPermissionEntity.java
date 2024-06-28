package no.fintlabs.flyt.authorization.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_permission")
public class UserPermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private long id;

    @Setter
    @NaturalId
    @Column(nullable = false, unique = true)
    private UUID objectIdentifier;

    @ElementCollection
    @CollectionTable(
            name = "user_permission_source_application_ids",
            joinColumns = @JoinColumn(name = "user_permission_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_permission_id", "source_application_ids"})
    )
    @Setter
    @Column(name = "source_application_ids")
    @Builder.Default
    private List<Long> sourceApplicationIds = new ArrayList<>();
}
