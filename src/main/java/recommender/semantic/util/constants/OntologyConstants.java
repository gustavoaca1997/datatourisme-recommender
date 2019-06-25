package recommender.semantic.util.constants;

import java.util.Collections;
import java.util.List;

public final class OntologyConstants {
    public static final String POINT_OF_INTEREST_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#PointOfInterest";

    public static final String ENTERTAINMENT_AND_EVENT_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#EntertainmentAndEvent";
    public static final String PLACE_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#PlaceOfInterest";
    public static final String PRODUCT_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#Product";
    public static final String TOUR_URI =
            "https://www.datatourisme.gouv.fr/ontology/core#Tour";

    public static final String MODEL_FILE = "datatourisme.ttl";


    public static final List<String> HIGH_CLASSES_URI = Collections.singletonList(PLACE_URI);
}
