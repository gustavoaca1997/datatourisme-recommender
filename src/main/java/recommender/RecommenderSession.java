package recommender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import recommender.persistence.entity.ClassProperties;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;
import recommender.semantic.util.constants.OntologyConstants;

import javax.persistence.NoResultException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        downwardsPropagation();
    }

    /**
     * Computes new confidence of a child node relative to its ancestors.
     *
     * @param ancestors list of ancestors of the node
     * @return (Sum of ancestors' confidences) / (Number of ancestors) - alpha
     */
    public Double aggregatedConfidence(List<ClassProperties> ancestors) {
        Double sumOfConfidences = ancestors.stream()
                .map(ClassProperties::getConfidence)
                .reduce((c1, c2) -> c1 + c2).orElse(0D);
        Integer numberOfAncestors = ancestors.size();
        return sumOfConfidences / numberOfAncestors - decreaseRate;
    }

    /**
     * Computes new preference of a child node relative to its ancestors.
     *
     * @param ancestors list of ancestors of the node
     * @return (Weighted sum of preferences with confidence)/(sum of confidences)
     */
    public Double aggregatedPreference(List<ClassProperties> ancestors) {
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
     * @param newConfidence     new or aggregated value of the condifende
     * @return new confidence
     */
    public Double updatedConfidence(Double oldConfidence, Double newConfidence) {
        return (1 - coopScale) * oldConfidence + coopScale*newConfidence;
    }

    //  TODO: test

    /**
     * From user feedback, update properties in the semantic network.
     * @param uri                URI of the origin node
     * @param newPreference     new preference set to the node
     */
    public void updateAndPropagate(String uri, Double newPreference) {
        // Update preference and confidence
        OntClass ontClass = semanticNetwork.getOntModel().getOntClass(uri);
        ClassProperties properties = propertiesManager.getClassProperties(ontClass.getURI(), uid);
        properties.setPreference(updatedPreference(properties.getPreference(), newPreference));
        properties.setConfidence(updatedConfidence(properties.getConfidence(), 1D));
        propertiesManager.updateClassProperties(properties);

        upwardsPropagation(ontClass, new HashSet<>());
        downwardsPropagation();
    }


    private void downwardsPropagation() {
        @SuppressWarnings("unchecked") Iterable<OntClass> iterable = (Iterable<OntClass >) semanticNetwork;
        for (OntClass ontClass : iterable) {
            propagate(ontClass, ontClass.listSuperClasses(true));
        }
    }


    public void upwardsPropagation(OntClass ontClass, Set<OntClass> visitedSet) {
        for (OntClass superClass : ontClass.listSuperClasses(true).toList()) {
            String namespace = ontClass.getNameSpace();
            if (namespace.equals(superClass.getNameSpace()) &&
                    !superClass.getURI().equals(OntologyConstants.PLACE_URI) &&
                    !visitedSet.contains(superClass)
            ) {
                visitedSet.add(superClass);
                propagate(superClass, superClass.listSubClasses(true));
                upwardsPropagation(superClass, visitedSet);
            }
        }
    }

    public void propagate(OntClass ontClass, ExtendedIterator<OntClass> ancestorClasses) {
        String namespace = ontClass.getNameSpace();
        if (!higherClasses.contains(ontClass.getURI()) &&
                !OntologyConstants.PLACE_URI.equals(ontClass.getURI())
        ) {
            List<ClassProperties> ancestors = ancestorClasses
                    .toList()
                    .stream()
                    .filter(ontClass1 -> namespace.equals(ontClass1.getNameSpace()))
                    .map(OntClass::getURI)
                    .map(uri -> propertiesManager
                            .getClassProperties(uri, getUser().getUid()))
                    .collect(Collectors.toList());

            Double confidence = aggregatedConfidence(ancestors);
            Double preference = aggregatedPreference(ancestors);

            try {
                ClassProperties existentProperties =
                        propertiesManager.getClassProperties(ontClass.getURI(), uid);

                existentProperties
                        .setConfidence(updatedConfidence(
                                existentProperties.getConfidence(),
                                confidence));
                existentProperties
                        .setPreference(updatedPreference(
                                existentProperties.getPreference(),
                                preference));

                propertiesManager.updateClassProperties(existentProperties);

            } catch (NoSuchElementException | NoResultException e) {
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
     * Write to a file state of the semantic network as a JSON.
     * @throws IOException
     */
    public void exportJSON(String filename) throws IOException {
        JSONObject json = toJSONObject(
                semanticNetwork.getOntModel().getOntClass(OntologyConstants.PLACE_URI),
                new HashSet<>());

        try (FileWriter file = new FileWriter(filename)) {
            file.write(json.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
        }
    }

    private JSONObject toJSONObject(OntClass ontClass, Set<OntClass> visitedSet) {
        JSONObject obj = new JSONObject();
        obj.put("name", ontClass.getURI().split("#")[1]);

        JSONArray children = new JSONArray();
        for (OntClass subClass : ontClass.listSubClasses(true).toList()) {
            String namespace = ontClass.getNameSpace();
            if (subClass.getNameSpace().equals(namespace) &&
                    !visitedSet.contains(subClass)
            ) {
                visitedSet.add(subClass);
                JSONObject child = toJSONObject(subClass, visitedSet);
                children.add(child);
            }
        }

        obj.put("subclasses", children);

        if (!ontClass.getURI().equals(OntologyConstants.PLACE_URI)) {
            ClassProperties properties = propertiesManager.getClassProperties(ontClass.getURI(), uid);
            obj.put("preference", properties.getPreference());
            obj.put("confidence", properties.getConfidence());
        }

        return obj;
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
