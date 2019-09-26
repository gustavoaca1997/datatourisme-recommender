package recommender.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "relevance")
public class Relevance {

    @Id
    @Column(name = "rid")
    private Integer rid;

    @ManyToOne
    @JoinColumn(name = "cid", nullable = false)
    private ContextFactor contextFactor;

    @Column(name = "uri")
    private String uri;

    @Column(name = "value")
    private Double value;

    //TODO: relation One-To-Many to User.

}
