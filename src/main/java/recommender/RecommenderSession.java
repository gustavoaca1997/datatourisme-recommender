package recommender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.jena.ontology.OntClass;
import recommender.persistence.entity.ClassProperties;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;
import recommender.semantic.util.constants.OntologyConstants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.min;

@Data
@Builder
@AllArgsConstructor
public class RecommenderSession {
    private final UserManager userManager;
    private final ClassPropertiesManager propertiesManager;
    private final SemanticNetwork semanticNetwork;
    private final Integer uid;                    // user id

    private Double decreaseRate ;    // how much does confidence decrease in initial spreading
    private Double coopScale = 0.1;       // cooperation scale between current values and new values

    private final Set<String> higherClasses;

    public RecommenderSession(UserManager userManager, ClassPropertiesManager classPropertiesManager,
                              SemanticNetwork semanticNetwork, Integer uid) {
        this.userManager = userManager;
        this.propertiesManager = classPropertiesManager;
        this.semanticNetwork = semanticNetwork;
        this.uid = uid;
        this.decreaseRate = 0.25;
        this.coopScale = 0.1;

        this.higherClasses = semanticNetwork.getOntModel()
                .getOntClass(OntologyConstants.PLACE_URI)
                .listSubClasses(true)
                .toList()
                .stream()
                .map(OntClass::getURI)
                .collect(Collectors.toSet());
    }

    public User getUser() {
        return userManager.getUser(uid);
    }

    public void init(Map<String, Double> initialPreferences) {
        // Check all higher classes are in the map
        assert(initialPreferences.keySet()
                .equals(higherClasses));
        initialPreferences.forEach(
                (uri, pref) ->
                        propertiesManager.addClassProperties(
                                ClassProperties.builder()
                                        .uri(uri)
                                        .user(getUser())
                                        .confidence(1D)
                                        .preference(pref)
                                        .build()));
        initialSpreading();
    }

    //  TODO
    //      update from an ont class to ancestors and to descendants.

    private void initialSpreading() {
        @SuppressWarnings("unchecked") Iterable<OntClass> iterable = (Iterable<OntClass >) semanticNetwork;
        for (OntClass ontClass : iterable) {
            String namespace = ontClass.getNameSpace();

            // If it is not POI class and neither a high class
            if (!higherClasses.contains(ontClass.getURI()) &&
                    !OntologyConstants.PLACE_URI.equals(ontClass.getURI())
            ) {
                List<ClassProperties> ancestors = ontClass.listSuperClasses(true)
                        .toList().stream()
                        .filter(ontClass1 ->
                                Optional.ofNullable(ontClass1.getNameSpace())
                                        .orElse("")
                                        .equals(namespace))
                        .map(OntClass::getURI)
                        .map(uri -> propertiesManager
                                .getClassProperties(uri, getUser().getUid()))
                        .collect(Collectors.toList());

                Double confidence = initialConfidence(ancestors);
                Double preference = initialPreference(ancestors);

                propertiesManager.addClassProperties(ClassProperties.builder()
                        .uri(ontClass.getURI())
                        .user(getUser())
                        .confidence(confidence)
                        .preference(preference)
                        .build());
            }
        }
    }

    /**
     * Computes new confidence of a child node relative to its ancestors.
     *
     * @param ancestors list of ancestors of the node
     * @return (Sum of ancestors ' confidences) / (Number of ancestors) - alpha
     */
    public Double initialConfidence(List<ClassProperties> ancestors) {
        Double sumOfConfidences = ancestors.stream()
                .map(ClassProperties::getConfidence)
                .reduce((c1, c2) -> c1 + c2).orElse(0D);
        Integer numberOfAncestors = ancestors.size();
        return sumOfConfidences / numberOfAncestors - decreaseRate;
    }

    public Double initialPreference(List<ClassProperties> ancestors) {
        Double weightedSumOfPreferences = ancestors.stream()
                .map(a -> a.getConfidence() * a.getPreference())
                .reduce((a1, a2) -> a1 + a2).orElse(0D);
        Double sumOfConfidences = ancestors.stream()
                .map(ClassProperties::getConfidence)
                .reduce((c1, c2) -> c1 + c2).orElse(0D);
        return weightedSumOfPreferences / sumOfConfidences;
    }

    /**
     * Computes new preference of a node relative to an assigned new preference.
     *
     * @param oldPreference      current value of the preference
     * @param assignedPreference assigned value
     * @return new preference
     */
    public Double updatedPreference(Double oldPreference, Double assignedPreference) {
        return min(1D, oldPreference + coopScale * assignedPreference);
    }

    /**
     * Computes new confidence of a node relative to an assigned new confidence.
     *
     * @param oldConfidence      current value of the confidence
     * @param assignedConfidence assigned value
     * @return new confidence
     */
    public Double updatedConfidence(Double oldConfidence, Double assignedConfidence) {
        return (1 - coopScale) * oldConfidence + coopScale * assignedConfidence;
    }

    //  TODO get recommendations for ont classes
    //      - Parameters: Context factor level of fulfillment.
    //      - From each context factor, spread fulfillment and
    //          store in a collection each ont class/uri with its
    //          uri, activation and preference.
    //      - Return collection ordered by preference * activation
    public Set<OntClass> getRecommendation(Map<ContextFactor, Double> fulfillments) {
        return null;
    }

}
