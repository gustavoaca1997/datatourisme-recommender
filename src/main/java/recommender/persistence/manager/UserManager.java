package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import recommender.persistence.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class UserManager {
    public UserManager() {
    }

    /**
     * Add a new user with `username`.
     *
     * @param user an entity with the user's fields
     * @return New user's id
     */
    public Integer addUser(User user) {
        Transaction tx = null;
        Integer userID = null;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            userID = (Integer) session.save(user);
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
        }
        return userID;
    }

    /**
     * List all users.
     *
     * @return List of {@link User User}
     */
    @SuppressWarnings("unchecked")
    public List<User> listUsers() {
        Transaction tx = null;
        List<User> users = new ArrayList<>();
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            users = session.createQuery("FROM User").list();
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
        }
        return users;
    }

    /**
     * Get user by id.
     *
     * @param uid unique identifier of the user.
     * @return A {@link Optional<User> User} corresponding to the user.
     * @throws NoSuchElementException if there is not such user.
     */
    public User getUser(Integer uid) {
        Transaction tx = null;
        User user;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            user = session.get(User.class, uid);
            if (user == null) {
                throw new
                        NoSuchElementException(String.format(
                        "User with id %s not found",
                        uid
                ));
            }
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
            throw e;
        }
        return user;
    }

    /**
     * Delete user by id
     * @param uid id of user to be deleted
     */
    public void deleteUser(Integer uid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            User user = Optional.ofNullable(session.get(User.class, uid))
                    .orElseThrow(NoSuchElementException::new);
            session.delete(user);
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
        }
    }

    /**
     * Update user by id
     *
     * @param updatedUser an object with new fields
     * @throws NoSuchElementException if there is not such user
     */
    void updateUser(User updatedUser) throws NoSuchElementException {
        Transaction tx = null;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Integer uid = updatedUser.getUid();
            if (session.get(User.class, uid) == null) {
                throw new NoSuchElementException(
                        String.format(
                                "User with id %s not found",
                                uid));
            }
            session.merge(updatedUser);
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
        }
    }
}
