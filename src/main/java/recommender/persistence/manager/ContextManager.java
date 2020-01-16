package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.Relevance;
import recommender.persistence.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.NoSuchElementException;

public class ContextManager {
    public ContextManager() {

    }

    public Integer addContextFactor(ContextFactor contextFactor) {
        Transaction tx = null;
        Integer cid;
        Session session = HibernateUtil.openSession();
        try {
            tx = session.beginTransaction();
            cid = (Integer) session.save(contextFactor);
            tx.commit();

            return cid;
        }
        catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    public Integer addRelevance(Relevance relevance) {
        Transaction tx = null;
        Integer rid;

        Session session = HibernateUtil.openSession();
        try {
            tx = session.beginTransaction();

            Integer cid = relevance.getContextFactor().getCid();
            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            relevance.setContextFactor(contextFactor);

            Integer uid = relevance.getUser().getUid();
            User user = session.get(User.class, uid);
            relevance.setUser(user);

            contextFactor.getRelevanceSet().add(relevance);
            user.getRelevanceSet().add(relevance);

            rid = (Integer) session.save(relevance);
            session.update(contextFactor);
            session.update(user);

            tx.commit();

            return rid;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }

    }

    public ContextFactor getContextFactor(Integer cid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            if (contextFactor == null) {
                throw new NoSuchElementException(
                        String.format("Context factor with id %s not found", cid)
                );
            }
            tx.commit();
            return contextFactor;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public ContextFactor getContextFactor(String name) {
        try (Session session = HibernateUtil.openSession()) {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ContextFactor> criteriaQuery = cb.createQuery(ContextFactor.class);
            Root<ContextFactor> root = criteriaQuery.from(ContextFactor.class);
            Predicate predicate = cb.like(root.get("name"), name);
            criteriaQuery.select(root).where(predicate);
            Query<ContextFactor> query = session.createQuery(criteriaQuery);

            ContextFactor contextFactor = query.getSingleResult();

            if (contextFactor == null) {
                throw new NoSuchElementException(
                        String.format("Context factor with name %s not found", name)
                );
            }
            return contextFactor;
        }
    }

    public Relevance getRelevance(Integer rid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Relevance relevance = session.get(Relevance.class, rid);
            if (relevance == null) {
                throw new NoSuchElementException(
                        String.format("Relevance with id %s not found", rid)
                );
            }
            tx.commit();
            return relevance;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Relevance getRelevance(String uri, Integer cid, Integer uid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            User user = session.get(User.class, uid);

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Relevance> criteriaQuery = cb.createQuery(Relevance.class);
            Root<Relevance> root = criteriaQuery.from(Relevance.class);
            Predicate predicate = cb.and(
                    cb.like(root.get("uri"), uri),
                    cb.equal(root.get("contextFactor"), contextFactor),
                    cb.equal(root.get("user"), user)
            );
            criteriaQuery.select(root).where(predicate);
            Query<Relevance> query = session.createQuery(criteriaQuery);

            Relevance relevance = query.getSingleResult();

            if (relevance == null) {
                throw new NoSuchElementException(
                        String.format(
                                "Relevance for class %s and context factor %s not found",
                                uri, cid));
            }
            tx.commit();
            return relevance;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void updateRelevance(Relevance updateRelevance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Relevance relevance;
            Integer rid = updateRelevance.getRid();
            if (rid != null) {
                relevance = session.get(Relevance.class, rid);
            } else {
                String uri = updateRelevance.getUri();
                Integer cid = updateRelevance.getContextFactor().getCid();
                Integer uid = updateRelevance.getUser().getUid();
                relevance = getRelevance(uri, cid, uid);
            }
            if (relevance == null) {
                throw new NoSuchElementException("Relevance not found");
            }
            updateRelevance.setRid(relevance.getRid());
            session.merge(updateRelevance);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw  e;
        }
    }

    public void deleteContextFactor(Integer cid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            session.delete(contextFactor);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteRelevance(Integer rid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Relevance relevance = getRelevance(rid);
            relevance.getContextFactor().getRelevanceSet().remove(relevance);
            session.remove(relevance);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<ContextFactor> listContextFactors() {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            //noinspection unchecked
            List<ContextFactor> contextFactors =
                    session.createQuery("FROM ContextFactor").list();
            tx.commit();
            return contextFactors;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<Relevance> listRelevancesByUserId(Integer uid) {
        Session session = HibernateUtil.openSession();
        Query<Relevance> query;
        User user = session.get(User.class, uid);

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Relevance> criteriaQuery = cb.createQuery(Relevance.class);
        Root<Relevance> root = criteriaQuery.from(Relevance.class);
        Predicate predicate = cb.and(
                cb.equal(root.get("user"), user)
        );
        criteriaQuery.select(root).where(predicate);
        query = session.createQuery(criteriaQuery);
        List<Relevance> result = query.getResultList();
        session.close();
        return result;
    }
}
