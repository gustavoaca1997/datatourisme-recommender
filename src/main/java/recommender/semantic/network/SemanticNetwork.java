package recommender.semantic.network;

import lombok.Data;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import recommender.semantic.util.constants.OntologyConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

@Data
public class SemanticNetwork implements Iterable {
    private OntModel ontModel;

    SemanticNetwork() throws IOException {
        ontModel = ModelFactory.createOntologyModel();
        this.read();
    }

    public SemanticNetwork(String file) throws IOException {
        ontModel = ModelFactory.createOntologyModel();
        this.read(file);
    }

    private boolean read() throws IOException {
        readOntology(OntologyConstants.MODEL_FILE, ontModel);
        return true;
    }

    private boolean read(String file) throws IOException {
        readOntology(file, ontModel);
        return true;
    }

    public Iterator<OntClass> iterator() {
        return new SemanticNetworkIterator(this);
    }

    private static void readOntology(String file, OntModel model) throws IOException {
        InputStream in = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(file)).openStream();
        model.read(in, null, "TTL");
        in.close();
    }
}
