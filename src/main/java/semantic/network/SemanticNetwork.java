package semantic.network;

import lombok.Data;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static semantic.util.constants.OntologyConstants.MODEL_FILE;

@Data
public class SemanticNetwork implements Iterable {
    private OntModel ontModel;

    public SemanticNetwork() {
        ontModel = ModelFactory.createOntologyModel();
    }

    public boolean read() throws IOException {
        readOntology(MODEL_FILE, ontModel);
        return true;
    }

    public boolean read(String file) throws IOException {
        readOntology(file, ontModel);
        return true;
    }

    public Iterator<OntClass> iterator() {
        return new SemanticNetworkIterator(this);
    }

    public static void readOntology( String file, OntModel model ) throws IOException {
        InputStream in = ClassLoader.getSystemClassLoader().getResource(file).openStream();
        System.out.println(ClassLoader.getSystemClassLoader().getResource(file).getFile());
        model.read(in, null, "TTL");
        in.close();
    }
}
