package recommender;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;
import recommender.semantic.util.constants.OntologyConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecommenderSessionTests {
    private static RecommenderSession recommenderSession;

    @BeforeClass
    public static void init() throws IOException {
        UserManager userManager = new UserManager();
        Integer uid = userManager.addUser(User.builder()
                .username("gustavoaca")
                .build());

        ClassPropertiesManager propertiesManager = new ClassPropertiesManager();
        SemanticNetwork semanticNetwork = new SemanticNetwork();

        recommenderSession = new RecommenderSession(userManager, propertiesManager, semanticNetwork, uid);
    }

    @AfterClass
    public static void cleanUp() {
        recommenderSession
                .getUserManager().deleteUser(recommenderSession.getUid());
    }


    @Test
    public void initRecommenderSessionTest() {
        Map<String, Double> initialPreferences = new HashMap<>();
        initialPreferences.put(OntologyConstants.ENTERTAINMENT_AND_EVENT_URI, 0.75);
        initialPreferences.put(OntologyConstants.PLACE_URI, 0.8);
        initialPreferences.put(OntologyConstants.PRODUCT_URI, 0.25);
        initialPreferences.put(OntologyConstants.TOUR_URI, 0.35);

        recommenderSession.init(initialPreferences);

        ClassPropertiesManager propertiesManager = recommenderSession.getPropertiesManager();
        Integer uid = recommenderSession.getUid();

        initialPreferences.forEach(
                (uri, pref) -> {
                    Assert.assertEquals(String.format("Preferences don't match for %s", uri),
                            pref, propertiesManager.getClassProperties(uri, uid).getPreference());
                    Assert.assertEquals(String.format("Confidences don't match for %s", uri),
                            Double.valueOf(1D), propertiesManager.getClassProperties(uri, uid).getConfidence());
                }
        );
        Double prefOfArcheologicalSite =
                propertiesManager.getClassProperties("https://www.datatourisme.gouv.fr/ontology/core#ArcheologicalSite",
                        uid).getPreference();
        Assert.assertTrue("Preference is <= 0 or == 1",
                prefOfArcheologicalSite > 0 && prefOfArcheologicalSite < 1);
    }
}
