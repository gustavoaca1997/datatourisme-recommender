package recommender.semantic.util.constants;

import java.util.Arrays;
import java.util.List;

public final class OntologyConstants {
    public static final String POINT_OF_INTEREST_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#PointOfInterest";

    private static final String ENTERTAINMENT_AND_EVENT_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#EntertainmentAndEvent";
    private static final String PLACE_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#PlaceOfInterest";
    private static final String PRODUCT_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#Product";
    private static final String TOUR_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#Tour";

    public static final String MODEL_FILE = "datatourisme.ttl";

    public static final List<String> HIGH_CLASSES_URI = Arrays.asList(ENTERTAINMENT_AND_EVENT_URI,
            PLACE_URI, PRODUCT_URI, TOUR_URI);
}
