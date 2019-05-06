package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import recommender.persistence.entity.User;
import recommender.persistence.manager.dto.user.CreateDTO;
import recommender.persistence.manager.dto.user.GetDTO;
import recommender.persistence.manager.dto.user.UpdateDTO;
import recommender.persistence.manager.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

class UserManager extends AbstractManager {
    UserManager() {
        super(User.class);
    }

    /**
     * Add a new user with `username`.
     *
     * @param createDTO a DTO with the username
     * @return New user's id
     */
    Integer addUser(CreateDTO createDTO) {
        Transaction tx = null;
        Integer userID = null;

        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            User user = UserMapper.fromDTO(createDTO);
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
     * @return List of {@link GetDTO User DTO}
     */
    @SuppressWarnings("unchecked")
    List<GetDTO> listUsers() {
        Transaction tx = null;
        List<User> users = new ArrayList<>();
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            users = session.createQuery("FROM User").list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by id.
     *
     * @param uid unique identifier of the user.
     * @return A {@link GetDTO DTO} corresponding to the user.
     * @throws NoSuchElementException if there is not such user.
     */
    GetDTO getUser(Integer uid) throws NoSuchElementException {
        Transaction tx = null;
        User user = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            user = session.get(User.class, uid);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return Optional.ofNullable(user)
                .map(UserMapper::toDTO)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                String.format("User with id %s not found.", uid)));
    }

    /**
     * Delete user by id.
     *
     * @param uid unique identifier of the user
     * @throws NoSuchElementException if there is not such user
     */
    void deleteUser(Integer uid) throws NoSuchElementException {
        Transaction tx = null;
        User user;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            user = session.get(User.class, uid);

            if (user == null) {
                throw new NoSuchElementException(
                        String.format("User with id %s not found.", uid));
            } else {
                session.delete(user);
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Update user by id
     *
     * @param updateDTO a DTO with the new properties.
     * @throws NoSuchElementException if there is not such user
     */
    void updateUser(UpdateDTO updateDTO) throws NoSuchElementException {
        Transaction tx = null;
        User user;
        Integer uid = updateDTO.getUid();

        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            user = session.get(User.class, uid);

            if (user == null) {
                throw new NoSuchElementException(
                        String.format("User with id %s not found.", uid));
            } else {
                UserMapper.fromDTO(user, updateDTO);
                session.update(user);
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
