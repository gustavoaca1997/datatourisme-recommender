package recommender;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RDFResult {
    private final String label;
    private final String uri;

    private final String uriClass;
    private Double predictedRating; // Predicted Rating for its Ontology Class

    private final Double latitude;
    private final Double longitude;
    private Double distance;

    private Double agingValue;

    public String toString() {
        return String.format("%s <%s km> (rating: %s) (aging: %s) (score: %s)",
                label, distance, predictedRating, agingValue, predictedRating*agingValue);
    }
}
