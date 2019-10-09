import recommender.RecommenderSession;
import recommender.persistence.entity.User;
import recommender.persistence.manager.ClassPropertiesManager;
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

    public static void main(String[] args) throws IOException {
        // Ask for username
        scanner = new Scanner(System.in);
        System.out.print("User: ");
        String username = scanner.next();

        // Get user or create it
        UserManager userManager = new UserManager();
        Integer uid;
        boolean newUser = false;
        try {
            User user = userManager.getUser(username);
            uid = user.getUid();
        } catch (NoSuchElementException | NoResultException e) {
            uid = userManager.addUser(User.builder().username(username).build());
            newUser = true;
        }

        // Create new recommender session
        ClassPropertiesManager propertiesManager = new ClassPropertiesManager();
        SemanticNetwork semanticNetwork = new SemanticNetwork();

        recommenderSession = new RecommenderSession(userManager, propertiesManager,
                semanticNetwork, uid);

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

                default:    System.out.println("Invalid option.");
            }
        }
    }

    private static void printOptions() {
        System.out.println("0: Exit\n" +
                "1: Show options\n" +
                "2: Update preference of a POI\n" +
                "3: Export JSON\n" +
                "4: Initial spreading");
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
}
