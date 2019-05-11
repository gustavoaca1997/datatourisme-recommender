package recommender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.jena.ontology.OntClass;
import recommender.persistence.entity.ClassProperties;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;
import recommender.semantic.util.constants.OntologyConstants;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class RecommenderSession {
    private final UserManager userManager;
    private final ClassPropertiesManager propertiesManager;
    private final SemanticNetwork semanticNetwork;
    private Integer uid;

    public User getUser() {
        return userManager.getUser(uid);
    }

    public Double getAlpha() {
        return semanticNetwork.getDecreaseRate();
    }

    public void setAlpha(Double alpha) {
        semanticNetwork.setDecreaseRate(alpha);
    }

    public Double getBeta() {
        return semanticNetwork.getCoopScale();
    }

    public void setBeta(Double beta) {
        semanticNetwork.setCoopScale(beta);
    }

    @Transactional(rollbackOn = Exception.class)
    public void init(Map<String, Double> initialPreferences) {
        initialPreferences.forEach(
                (uri, pref) ->
                        propertiesManager.addClassProperties(
                                ClassProperties.builder()
                                        .uri(uri)
                                        .user(getUser())
                                        .confidence(1D)
                                        .preference(pref)
                                        .activation(0D)
                                        .build()));
        initialSpreading();
    }

    @Transactional(rollbackOn = Exception.class)
    private void initialSpreading() {
        @SuppressWarnings("unchecked") Iterable<OntClass> iterable = (Iterable<OntClass >) semanticNetwork;
        for (OntClass ontClass : iterable) {
            String namespace = ontClass.getNameSpace();

            // If it is not POI class and neither a high class
            if (!OntologyConstants.HIGH_CLASSES_URI.contains(ontClass.getURI()) &&
                    !OntologyConstants.POINT_OF_INTEREST_URI.equals(ontClass.getURI())
            ) {
                List<ClassProperties> ancestors = ontClass.listSuperClasses(true)
                        .toList().stream()
                        .filter(ontClass1 ->
                                Optional.ofNullable(ontClass1.getNameSpace())
                                        .orElse("")
                                        .equals(namespace))
                        .map(OntClass::getURI)
                        .map(
                                uri -> propertiesManager
                                        .getClassProperties(uri, getUser().getUid()))
                        .collect(Collectors.toList());

                Double confidence = semanticNetwork.initialConfidence(ancestors);
                Double preference = semanticNetwork.initicalPreference(ancestors);

                propertiesManager.addClassProperties(ClassProperties.builder()
                        .uri(ontClass.getURI())
                        .user(getUser())
                        .confidence(confidence)
                        .preference(preference)
                        .activation(0D)
                        .build());
            }
        }
    }
}
