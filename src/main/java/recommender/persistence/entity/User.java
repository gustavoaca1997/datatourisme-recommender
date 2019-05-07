package recommender.persistence.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private int uid;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private Set<ClassProperties> classPropertiesSet;
}
