package recommender.semantic.network;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.util.iterator.ExtendedIterator;
import recommender.semantic.util.constants.OntologyConstants;

import java.util.*;


public class SemanticNetworkIterator implements Iterator<OntClass> {
    private Queue<OntClass> queue;

    SemanticNetworkIterator(SemanticNetwork semanticNetwork) {
        queue = new LinkedList<>();
        OntClass pointOfInterestClass = semanticNetwork
                .getOntModel()
                .getOntClass(OntologyConstants.POINT_OF_INTEREST_URI);
        assert(pointOfInterestClass != null);
        queue.add(pointOfInterestClass);
    }

    public boolean hasNext() {
        return !queue.isEmpty();

    }

    public OntClass next() {
        OntClass nextClass = queue.poll();
        queue.addAll(
                Optional.ofNullable(nextClass)
                        .map(OntClass::listSubClasses)
                        .map(ExtendedIterator::toList)
                        .orElse(Collections.emptyList()));
        return nextClass;
    }
}
