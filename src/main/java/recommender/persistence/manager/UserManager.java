package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import recommender.persistence.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

class UserManager {
    UserManager() {
    }

    /**
     * Add a new user with `username`.
     *
     * @param user an entity with the user's fields
     * @return New user's id
     */
    Integer addUser(User user) {
        Transaction tx = null;
        Integer userID = null;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            userID = (Integer) session.save(user);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return userID;
    }

    /**
     * List all users.
     *
     * @return List of {@link User User}
     */
    @SuppressWarnings("unchecked")
    List<User> listUsers() {
        Transaction tx = null;
        List<User> users = new ArrayList<>();
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            users = session.createQuery("FROM User").list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
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
    Optional<User> getUser(Integer uid) {
        Transaction tx = null;
        User user = null;
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
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return Optional.ofNullable(user);
    }

    /**
     * Delete user by id.
     *
     * @param uid unique identifier of the user
     * @throws NoSuchElementException if there is not such user
     */
    void deleteUser(Integer uid) throws NoSuchElementException {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Optional<User> user = getUser(uid);
            user.ifPresent(session::delete);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
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
        Integer uid = updatedUser.getUid();

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Optional<User> user = getUser(uid);
            if (user.isPresent()) {
                User entity = user.get();
                entity.setUsername(updatedUser.getUsername());
                session.update(entity);
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
