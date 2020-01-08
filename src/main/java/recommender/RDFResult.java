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
    private final Double latitude;
    private final Double longitude;
    private Double distance;
    private Double agingValue;
    public String toString() {
        return String.format("%s <%s km> (aging: %s)", label, distance, agingValue);
    }
}
