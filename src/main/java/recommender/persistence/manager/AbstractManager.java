package recommender.persistence.manager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

abstract class AbstractManager {
    SessionFactory factory;

    /**
     * Instantiate the session factory with the corresponded class.
     *
     * @param annotatedClass Entity class
     */
    AbstractManager(Class annotatedClass) {
        try {
            factory = new Configuration()
                    .configure()
                    .addAnnotatedClass(annotatedClass)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
}
