package recommender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.rdf.model.RDFNode;

@Getter
@Setter
@AllArgsConstructor
public class RDFResult {
    private RDFNode label;
    private RDFNode uri;
    public String toString() {
        return label.toString();
    }
}
