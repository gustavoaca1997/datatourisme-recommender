import recommender.RDFResult;
import recommender.RecommenderSession;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.Relevance;
import recommender.persistence.entity.User;
import recommender.persistence.manager.AgingManager;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.ContextManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static RecommenderSession recommenderSession;
    private static Scanner scanner;
    private static ClassPropertiesManager propertiesManager;
    private static SemanticNetwork semanticNetwork;
    private static ContextManager contextManager;
    private static AgingManager agingManager;
    private static User user;
    private static Map<String, Double> fulfillment;
    private static double latitude;
    private static double longitude;
    private static double distance;

    private static List<String> baseUris = Arrays.asList(
            "https://www.datatourisme.gouv.fr/ontology/core#Museum",
            "https://www.datatourisme.gouv.fr/ontology/core#InterpretationCentre",
            "https://www.datatourisme.gouv.fr/ontology/core#Library",
            "https://www.datatourisme.gouv.fr/ontology/core#ParkAndGarden",
            "https://www.datatourisme.gouv.fr/ontology/core#ArcheologicalSite",
            "https://www.datatourisme.gouv.fr/ontology/core#ReligiousSite",
            "https://www.datatourisme.gouv.fr/ontology/core#RemarkableBuilding",
            "https://www.datatourisme.gouv.fr/ontology/core#CityHeritage",
            "https://www.datatourisme.gouv.fr/ontology/core#DefenceSite",
            "https://www.datatourisme.gouv.fr/ontology/core#RemembranceSite",
            "https://www.datatourisme.gouv.fr/ontology/core#TechnicalHeritage",
            "https://www.datatourisme.gouv.fr/ontology/core#FoodEstablishment",
            "https://www.datatourisme.gouv.fr/ontology/core#NaturalHeritage",
            "https://www.datatourisme.gouv.fr/ontology/core#SportsAndLeisurePlace",
            "https://www.datatourisme.gouv.fr/ontology/core#Store"
    );
    private static List<Double> relevances = Arrays.asList(1D, 1.394117647, 1.029411765, 1.082352941, 1.488235294, 1.064705882,
            1.517647059, 0.7882352941, 0.1941176471, 0.8705882353, 1.311764706, 0.8294117647, 1.029411765,
            1.423529412, 1.029411765, 1.382352941, 0.7705882353, 0.1352941176, 1.270588235, 1.388235294,
            1.188235294, 1.311764706, 1.158823529, 1.323529412, 1.452941176, 0.7294117647, 0.2705882353,
            0.3352941176, 1.847058824, 0.8941176471, 1.364705882, 1.664705882, 1.388235294, 1.629411765,
            0.9235294118, 0.4941176471, 0.4764705882, 1.488235294, 0.6705882353, 0.7823529412, 1.470588235,
            1.235294118, 1.423529412, 0.6823529412, 0.3352941176, 0.8, 1.017647059, 0.8411764706, 0.6411764706,
            1.105882353, 1.017647059, 0.9705882353, 0.5294117647, 0.1, 0.9117647059, 1.576470588, 0.9941176471,
            1.164705882, 1.505882353, 1.241176471, 1.452941176, 1.005882353, 0.3235294118, 0.6647058824,
            1.629411765,0.9470588235, 1.088235294, 1.617647059, 1.329411765, 1.564705882, 1.270588235, 0.5,
            0.7117647059, 1.411764706, 0.8058823529, 0.8352941176, 1.376470588, 1.252941176, 1.388235294,
            0.8235294118, 0.2647058824, 0.5882352941, 1.317647059, 0.8117647059, 0.9352941176, 1.305882353,
            1.129411765, 1.294117647, 0.6, 0.3235294118, 0.5823529412, 1.370588235, 0.6823529412, 0.8411764706,
            1.3,	1.264705882,	1.3,	0.7411764706	,0.3	,1.488235294	,1.623529412	,1.494117647	,1.635294118	,1.758823529	,1.352941176	,1.670588235	,1.676470588	,0.6882352941	,0.4941176471	,1.776470588	,0.8352941176	,1.088235294	,1.658823529	,1.535294118	,1.523529412	,0.9529411765	,0.4647058824	,1.088235294	,1.541176471	,1.005882353	,1.117647059	,1.588235294	,1.023529412	,1.505882353	,1.417647059	,0.5882352941	,1.311764706	,1.535294118	,1.241176471	,1.388235294	,1.435294118	,1.058823529	,1.547058824	,1.164705882	,0.3);

    private static List<String> contextFactors = Arrays.asList(
            "rainy",
            "sunny",
            "snowy",
            "workday",
            "weekend",
            "morning",
            "afternoon",
            "night",
            "early_morning"
    );

    public static void main(String[] args) throws IOException, InterruptedException {

        fulfillment = new HashMap<>();

        // Ask for username
        scanner = new Scanner(System.in);
        System.out.print("User: ");
        String username = scanner.next();

        // Get user or create it
        UserManager userManager = new UserManager();
        Integer uid;
        boolean newUser = false;
        try {
            user = userManager.getUser(username);
            uid = user.getUid();
        } catch (NoSuchElementException | NoResultException e) {
            uid = userManager.addUser(User.builder().username(username).build());
            user = userManager.getUser(username);
            newUser = true;
        }

        // Create new recommender session
        propertiesManager = new ClassPropertiesManager();
        semanticNetwork = new SemanticNetwork();
        contextManager = new ContextManager();
        agingManager = new AgingManager();

        recommenderSession = new RecommenderSession(
                userManager, propertiesManager,
                contextManager, agingManager, semanticNetwork, uid);

        // If it is a new user, initial spreading is required
        if (newUser) {
            initialSpreading();
        }

        // User feedback
        printOptions();
        while (true) {
            System.out.print("Option: ");
            int option = scanner.nextInt();

            switch (option) {
                case 0:     return;

                case 1:     printOptions();
                            break;

                case 2:     askForPreference();
                            break;

                case 3:     exportToJSON();
                            break;

                case 4:     initialSpreading();
                            break;

                case 5:     setFulfillment();
                            break;

                case 6:     loadContextFactors();
                            break;

                case 7:     loadRelevances();
                            break;

                case 8:     List<RDFResult> results =
                                recommenderSession
                                        .getRecommendedIndividuals(fulfillment, latitude, longitude, distance);
                            results.forEach(System.out::println);
                            break;

                case 9:     System.out.print("Type your latitude, longitude and then the maximum distance: ");
                            latitude = scanner.nextDouble();
                            longitude = scanner.nextDouble();
                            distance = scanner.nextDouble();
                            break;

                default:    System.out.println("Invalid option.");
            }
        }
    }

    private static void printOptions() {
        System.out.println("0: Exit\n" +
                "1: Show options\n" +
                "2: Update preference of a POI\n" +
                "3: Export JSON\n" +
                "4: Initial spreading\n" +
                "5: Set fulfillment of context factors\n" +
                "6: Load context factors\n" +
                "7: Load relevances\n" +
                "8: Get recommendation\n" +
                "9: Set location");
    }

    private static void askForPreference() {
        System.out.println("Please type a valid URI and your preference:");
        String uri = scanner.next();
        Double preference = scanner.nextDouble();

        recommenderSession.updateAndPropagate(uri, preference);
    }

    private static void initialSpreading() {
        Map<String, Double> initialPreference = new HashMap<>();
        for (String uri : recommenderSession.getHigherClasses()) {
            System.out.print("Preference for " + uri + ": ");
            Double preference = scanner.nextDouble();
            initialPreference.put(uri, preference);
        }
        recommenderSession.init(initialPreference);
    }

    private static void exportToJSON() throws IOException {
        System.out.print("File path: ");
        String filename = scanner.next();
        recommenderSession.exportJSON(filename);
    }

    private static void setFulfillment() {
        for (String name : contextFactors) {
            System.out.println("Context factor: " + name);
            System.out.print("Fulfillment value: ");
            Double value = scanner.nextDouble();
            fulfillment.put(name, value);
        }
    }

    private static void loadContextFactors() {
        // Context factors
        contextFactors.forEach(name -> contextManager.addContextFactor(ContextFactor.builder()
                .name(name).build()));
    }

    private static void loadRelevances() {

        // Relevances
        for (int i=0; i<baseUris.size(); i++) {
            String uri = baseUris.get(i);
            for (int j=0; j<contextFactors.size(); j++) {
                String name = contextFactors.get(j);
                contextManager.addRelevance(Relevance.builder()
                        .uri(uri)
                        .value(relevances.get(j*contextFactors.size()+i))
                        .contextFactor(contextManager.getContextFactor(name))
                        .user(user)
                        .build());
            }
        }

    }
}
