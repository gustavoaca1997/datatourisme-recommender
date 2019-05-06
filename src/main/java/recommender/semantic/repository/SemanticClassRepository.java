package recommender.semantic.repository;

import org.apache.jena.ontology.OntClass;

import java.util.HashMap;
import java.util.Map;

public class SemanticClassRepository {
    private Map<OntClass, SemanticClassProperties> propertiesDict;

    public SemanticClassRepository() {
        propertiesDict = new HashMap<>();
    }

    public SemanticClassProperties get(OntClass ontClass) {
        return propertiesDict.get(ontClass);
    }

    public void add(OntClass ontClass, SemanticClassProperties properties) {
        propertiesDict.put(ontClass, properties);
    }

    public void remove(OntClass ontClass) {
        propertiesDict.remove(ontClass);
    }
}
