package recommender.persistence.manager;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import recommender.persistence.entity.ClassProperties;
import recommender.persistence.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class ClassPropertiesManager {
    private UserManager userManager;

    ClassPropertiesManager() {
        userManager = new UserManager();
    }

    Optional<Integer> addClassProperties(ClassProperties classProperties, Integer uid)
            throws NoSuchElementException {
        Transaction tx = null;
        Integer pid = null;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            // Retrieve user
            Optional<User> user = userManager.getUser(uid);
            if (user.isPresent()) {
                User userEntity = user.get();
                // Create relation between both entities
                classProperties.setUser(userEntity);

                Set<ClassProperties> classPropertiesSet =
                        userEntity.getClassPropertiesSet();

                classPropertiesSet.add(classProperties);
                userEntity.setClassPropertiesSet(classPropertiesSet);

                pid = (Integer) session.save(classProperties);
                session.update(user);
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return Optional.ofNullable(pid);
    }

    Set<ClassProperties> getClassPropertiesByUser(Integer uid) {
        Transaction tx = null;
        Set<ClassProperties> classPropertiesSet = Collections.emptySet();

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Optional<User> user = userManager.getUser(uid);
            if (user.isPresent()) {
                classPropertiesSet = user.get().getClassPropertiesSet();
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return classPropertiesSet;
    }

    Optional<ClassProperties> getClassPropertiesByUriAndUser(String uri, Integer uid) {
        Transaction tx = null;
        ClassProperties classProperties = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ClassProperties> criteriaQuery = cb.createQuery(ClassProperties.class);
            Root<ClassProperties> root = criteriaQuery.from(ClassProperties.class);
            Predicate predicate = cb.and(
                    cb.like(root.get("uri"), uri),
                    cb.equal(root.get("uid"), uid)
            );
            criteriaQuery.select(root).where(predicate);
            Query<ClassProperties> query = session.createQuery(criteriaQuery);
            classProperties = query.getSingleResult();

            if (classProperties == null) {
                throw new NoSuchElementException(
                        String.format(
                                "Class Properties for class %s and user %s not found",
                                uri, uid));
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }

        return Optional.ofNullable(classProperties);
    }
}
