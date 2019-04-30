package semantic.network;


import org.apache.jena.ontology.OntClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static semantic.util.constants.OntologyConstants.HIGH_CLASSES_URI;
import static semantic.util.constants.OntologyConstants.POINT_OF_INTEREST_URI;

public class SemanticNetworkTests {
    private SemanticNetwork semanticNetwork;

    @Before
    public void setUp() {
        semanticNetwork = new SemanticNetwork();
    }

    @Test
    public void readModelTest() throws IOException  {
        Assert.assertTrue("Failed to read ontology", semanticNetwork.read());
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
                HIGH_CLASSES_URI, root.listSubClasses());
    }
}
