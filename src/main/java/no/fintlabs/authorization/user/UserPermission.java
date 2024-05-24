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
    private String sub;
    @ElementCollection
    private List<Integer> sourceApplicationIds;
}
