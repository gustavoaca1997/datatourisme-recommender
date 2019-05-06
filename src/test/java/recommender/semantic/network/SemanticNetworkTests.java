package recommender.semantic.network;


import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import static recommender.semantic.util.constants.OntologyConstants.HIGH_CLASSES_URI;
import static recommender.semantic.util.constants.OntologyConstants.POINT_OF_INTEREST_URI;

public class SemanticNetworkTests {
    static private SemanticNetwork semanticNetwork;

    static {
        try {
            semanticNetwork = new SemanticNetwork();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readModelTest()  {
        Assert.assertNotNull("Failed to read ontology", semanticNetwork.getOntModel());
    }

    @Test
    public void createIteratorTest() {
        Iterator<OntClass> iterator = semanticNetwork.iterator();
        Assert.assertNotNull("Iterator is null", iterator);
        Assert.assertTrue("Iterator is empty", iterator.hasNext());
    }

    @Test
    public void rootClassIsPOITest() {
        Iterator<OntClass> iterator = semanticNetwork.iterator();
        OntClass root = iterator.next();
        Assert.assertEquals("Ontology Classes don't match", POINT_OF_INTEREST_URI, root.getURI());
    }

    @Test
    public void rootClassSubClassesTest() {
        Iterator<OntClass> iterator = semanticNetwork.iterator();
        OntClass root = iterator.next();
        Assert.assertEquals("High classes don't match",
                new HashSet<>(HIGH_CLASSES_URI),
                root.listSubClasses(true)
                        .toSet()
                        .stream()
                        .map(Resource::getURI)
                        .collect(Collectors.toSet()));
    }
}
