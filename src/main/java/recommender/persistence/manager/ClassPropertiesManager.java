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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ClassPropertiesManager {

    public ClassPropertiesManager() {
    }

    public Integer addClassProperties(ClassProperties classProperties)
            throws NoSuchElementException {
        Transaction tx = null;
        Integer pid;
        Integer uid = classProperties.getUser().getUid();
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            User userEntity = session.get(User.class, uid);
            classProperties.setUser(userEntity);
            Set<ClassProperties> classPropertiesSet =
                    userEntity.getClassPropertiesSet();
            classPropertiesSet.add(classProperties);

            pid = (Integer) session.save(classProperties);
            session.update(userEntity);

            tx.commit();
        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            throw e;
        }
        return pid;
    }

    public Set<ClassProperties> listClassPropertiesByUserAsSet(Integer uid) {
        Transaction tx = null;
        //noinspection UnusedAssignment
        Set<ClassProperties> classPropertiesSet = Collections.emptySet();
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, uid);
            classPropertiesSet = user.getClassPropertiesSet();
            tx.commit();
        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            throw e;
        }
        return classPropertiesSet;
    }

    public Map<String, ClassProperties> listClassPropertiesByUserAsMap(Integer uid) {
        //noinspection UnusedAssignment
        Map<String, ClassProperties> classPropertiesMap = new HashMap<>();
        try (Session session = HibernateUtil.openSession()) {
            User user = session.get(User.class, uid);
            Set<ClassProperties> classPropertiesSet = user.getClassPropertiesSet();
            classPropertiesSet.forEach(
                    p -> classPropertiesMap.put(p.getUri(), p)
            );
        }
        return classPropertiesMap;
    }

    public ClassProperties getClassProperties(Integer pid) {
        Transaction tx = null;
        ClassProperties classProperties;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            classProperties = session.get(ClassProperties.class, pid);
            if (classProperties == null) {
                throw new NoSuchElementException(
                        String.format("Class props with id %s not found", pid)
                );
            }
            tx.commit();
        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            throw e;
        }
        return classProperties;
    }

    public ClassProperties getClassProperties(String uri, Integer uid) {
        Transaction tx = null;
        ClassProperties classProperties;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, uid);
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ClassProperties> criteriaQuery = cb.createQuery(ClassProperties.class);
            Root<ClassProperties> root = criteriaQuery.from(ClassProperties.class);
            Predicate predicate = cb.and(
                    cb.like(root.get("uri"), uri),
                    cb.equal(root.get("user"), user)
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
            throw e;
        }

        return classProperties;
    }

    public void updateClassProperties(ClassProperties updatedProperties) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            ClassProperties props;
            Integer pid = updatedProperties.getPid();
            if (pid != null) {
                props = session.get(ClassProperties.class, pid);
            } else {
                Integer uid = updatedProperties.getUser().getUid();
                String uri = updatedProperties.getUri();
                props = getClassProperties(uri, uid);
            }
            if (props == null) {
                throw new NoSuchElementException("Class props not found");
            }
            props.setPreference(updatedProperties.getPreference());
            props.setConfidence(updatedProperties.getConfidence());
            session.update(props);
            tx.commit();
        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            throw e;
        }
    }

    void deleteClassProperties(Integer pid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            ClassProperties classProperties = session.get(ClassProperties.class, pid);
            if (classProperties == null) {
                throw new NoSuchElementException(
                        String.format(
                                "Class Properties with id %s not found",
                                pid));
            }
            Integer uid = classProperties.getUser().getUid();
            User user = session.get(User.class, uid);
            user.getClassPropertiesSet().remove(classProperties);
            session.delete(classProperties);

            tx.commit();
        } catch (HibernateException e) {

            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
