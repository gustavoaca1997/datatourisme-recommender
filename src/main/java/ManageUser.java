import jpa.entity.User;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Iterator;
import java.util.List;

public class ManageUser {
    private static SessionFactory factory;
    public static void main(String[] args) {
        try {
            factory = new Configuration()
                    .configure()
                    .addAnnotatedClass(User.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        ManageUser MU = new ManageUser();

        Integer userID1 = MU.addUser("gustavoaca");
        Integer userID2 = MU.addUser("lisalfonzo");
        Integer userID3 = MU.addUser("gcastellanos");

        MU.listUsers();
    }

    private Integer addUser(String username) {
        Transaction tx = null;
        Integer userID = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            User user = new User();
            user.setUsername(username);
            userID = (Integer) session.save(user);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return userID;
    }

    private void listUsers() {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            List users = session.createQuery("FROM User").list();
            for (Iterator iterator = users.iterator(); iterator.hasNext(); ) {
                User user = (User) iterator.next();
                System.out.println(String.format(
                        "Id: %s, Username: %s",
                        user.getUid(), user.getUsername()
                ));
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
