package recommender.semantic.network;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import recommender.persistence.entity.ClassProperties;
import recommender.semantic.util.constants.OntologyConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.min;

@Data
@Builder
@AllArgsConstructor
public class SemanticNetwork implements Iterable {
    private final OntModel ontModel;

    private Double decreaseRate; // how much does confidence decrease in initial spreading
    private Double coopScale; // cooperation scale between current values and new values

    /*
        Constructors
     */

    public SemanticNetwork() throws IOException {
        this.decreaseRate = 0.25;
        this.coopScale = 0.1;
        ontModel = ModelFactory.createOntologyModel();
        this.read();
    }

    /**
     * Initialize Decrease Rate and Cooperation Scale
     *
     * @param decreaseRate (also called alpha) how much does confidence decrease in initial spreading
     * @param coopScale    (also called beta) cooperation scale between current values and new values
     * @throws IOException when there is an error reading file
     */
    public SemanticNetwork(Double decreaseRate, Double coopScale) throws IOException {
        this.decreaseRate = decreaseRate;
        this.coopScale = coopScale;
        ontModel = ModelFactory.createOntologyModel();
        this.read();
    }

    public SemanticNetwork(String file) throws IOException {
        this.decreaseRate = 0.5;
        this.coopScale = 0.5;
        ontModel = ModelFactory.createOntologyModel();
        this.read(file);
    }

    /*
        Iterable interface methods
     */

    public Iterator<OntClass> iterator() {
        return new SemanticNetworkIterator(this);
    }

    /*
        Utility methods
     */

    /**
     * Read default ontology model
     *
     * @return always true
     * @throws IOException when there is an error reading file
     */
    private boolean read() throws IOException {
        readOntology(OntologyConstants.MODEL_FILE, ontModel);
        return true;
    }

    /**
     * Read ontology model
     * @param file path to ontology file
     * @return always true
     * @throws IOException when there is an error reading file
     */
    private boolean read(String file) throws IOException {
        readOntology(file, ontModel);
        return true;
    }

    private static void readOntology(String file, OntModel model) throws IOException {
        InputStream in = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(file)).openStream();
        model.read(in, null, "TTL");
        in.close();
    }

    /*
        Spreading methods
     */

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

}
