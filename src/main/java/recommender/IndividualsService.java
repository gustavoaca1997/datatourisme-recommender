package recommender;


import lombok.Getter;
import lombok.Setter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be making the queries to the Fuseki server.
 */

@Getter
@Setter
public class IndividualsService {
    public static String serviceURI = "http://localhost:3030/Datatourisme/sparql";

    public static List<RDFResult> getResults(String query) {
        try (QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query)) {
            ResultSet result = q.execSelect();
            List<RDFResult> results = new ArrayList<>();
            while (result.hasNext()) {
                QuerySolution soln = result.nextSolution();
                results.add(new RDFResult(soln.get("label"), soln.get("uri")));
            }
            return results;
        }
    }
}
