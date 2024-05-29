package no.fintlabs.authorization.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private long id;
    @Setter
    @Column(unique = true, nullable = false)
    private String objectIdentifier;
    @ElementCollection
    @CollectionTable(
            name = "user_permission_source_application_ids",
            joinColumns = @JoinColumn(name = "user_permission_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_permission_id", "source_application_ids"})
    )
    @Setter
    @Column(name = "source_application_ids")
    private List<Integer> sourceApplicationIds;
}
