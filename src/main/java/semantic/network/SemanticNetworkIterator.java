package semantic.network;

import org.apache.jena.ontology.OntClass;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static semantic.util.constants.OntologyConstants.POINT_OF_INTEREST_URI;


public class SemanticNetworkIterator implements Iterator<OntClass> {
    private Queue<OntClass> queue;

    public SemanticNetworkIterator(SemanticNetwork semanticNetwork) {
        queue = new LinkedList<>();
        OntClass pointOfInterestClass = semanticNetwork
                .getOntModel()
                .getOntClass(POINT_OF_INTEREST_URI);
        assert(pointOfInterestClass != null);
        queue.add(pointOfInterestClass);
    }

    public boolean hasNext() {
        return !queue.isEmpty();

    }

    public OntClass next() {
        OntClass nextClass = queue.poll();
        queue.addAll(nextClass.listSubClasses().toList());
        return nextClass;
    }
}
