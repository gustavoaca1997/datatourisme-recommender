package recommender.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "class_properties")
public class ClassProperties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Integer pid; // Properties ID

    @Column(name = "uri", nullable = false)
    private String uri; // POI Class URI

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "preference")
    private Double preference;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "activation")
    private Double activation;
}

