import recommender.RecommenderSession;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.Relevance;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
import recommender.persistence.manager.ContextManager;
import recommender.persistence.manager.UserManager;
import recommender.semantic.network.SemanticNetwork;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static RecommenderSession recommenderSession;
    private static Scanner scanner;
    private static ClassPropertiesManager propertiesManager;
    private static SemanticNetwork semanticNetwork;
    private static ContextManager contextManager;
    private static User user;
    private static Map<ContextFactor, Double> fulfillment;

    public static void main(String[] args) throws IOException {

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

        recommenderSession = new RecommenderSession(
                userManager, propertiesManager,
                contextManager, semanticNetwork, uid);

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

                case 5:     addContextFactor();
                            break;

                case 6:     setFulfillment();
                            break;

                case 7:     setRelevance();
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
                "5: Add context factor\n" +
                "6: Set fulfillment of a context factor\n" +
                "7: Set relevance to context factor");
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

    private static void addContextFactor() {
        System.out.print("Context Factor name: ");
        String name = scanner.next();
        contextManager.addContextFactor(ContextFactor.builder()
                .name(name)
                .build());
    }

    private static void setRelevance() {
        System.out.print("Context factor name: ");
        String name = scanner.next();
        System.out.print("Relevance value: ");
        Double value = scanner.nextDouble();
        contextManager.addRelevance(Relevance.builder()
                .value(value)
                .user(user)
                .contextFactor(contextManager.getContextFactor(name))
                .build());
    }

    private static void setFulfillment() {
        System.out.print("Context Factor name: ");
        String name = scanner.next();
        System.out.print("Fulfillment value: ");
        Double value = scanner.nextDouble();
        fulfillment.put(contextManager.getContextFactor(name), value);
    }
}
