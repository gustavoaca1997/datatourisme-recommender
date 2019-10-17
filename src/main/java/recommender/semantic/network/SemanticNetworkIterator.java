package recommender.semantic.network;

import org.apache.jena.ontology.OntClass;
import recommender.semantic.util.constants.OntologyConstants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;


public class SemanticNetworkIterator implements Iterator<OntClass> {
    private Queue<OntClass> queue;
    private Set<OntClass> visitedSet;

    SemanticNetworkIterator(SemanticNetwork semanticNetwork) {
        queue = new LinkedList<>();
        visitedSet = new HashSet<>();
        OntClass placeOfInterestClass = semanticNetwork
                .getOntModel()
                .getOntClass(OntologyConstants.PLACE_URI);
        assert(placeOfInterestClass != null);
        queue.add(placeOfInterestClass);
        visitedSet.add(placeOfInterestClass);
    }

    public boolean hasNext() {
        return !queue.isEmpty();

    }

    public OntClass next() {
        OntClass nextClass = queue.poll();

        visitedSet.add(nextClass);
        if (nextClass != null) {
            queue.addAll(
                    nextClass.listSubClasses(true)
                            .toList()
                            .stream()
                            .filter(x -> !visitedSet.contains(x))
                            .peek(ontClass -> visitedSet.add(ontClass))
                            .collect(Collectors.toList()));
        }
        return nextClass;
    }
}
