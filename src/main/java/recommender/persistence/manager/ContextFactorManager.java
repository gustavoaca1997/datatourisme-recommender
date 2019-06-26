package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.Relevance;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class ContextFactorManager {
    public ContextFactorManager() {

    }

    public Integer addContextFactor(ContextFactor contextFactor) {
        Transaction tx = null;
        Integer cid;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            cid = (Integer) session.save(contextFactor);
            tx.commit();

            return cid;
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Integer addRelevance(Relevance relevance) {
        Transaction tx = null;
        Integer rid;

        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Integer cid = relevance.getContextFactor().getCid();
            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            relevance.setContextFactor(contextFactor);

            Set<Relevance> relevanceSet =
                    contextFactor.getRelevanceSet();
            relevanceSet.add(relevance);
            contextFactor.setRelevanceSet(relevanceSet);

            rid = (Integer) session.save(relevance);
            session.update(contextFactor);
            tx.commit();

            return rid;
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
            throw e;
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

    public Relevance getRelevance(String uri, Integer cid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Relevance> criteriaQuery = cb.createQuery(Relevance.class);
            Root<Relevance> root = criteriaQuery.from(Relevance.class);
            Predicate predicate = cb.and(
                    cb.like(root.get("uri"), uri),
                    cb.equal(root.get("contextFactor"), contextFactor)
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
                relevance = getRelevance(uri, cid);
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

    public void deleteRelevance(Integer rid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Relevance relevance = session.get(Relevance.class, rid);
            if (relevance == null) {
                throw new NoSuchElementException(
                        String.format("Relevance with id %s not found", rid)
                );
            }
            Integer cid = relevance.getContextFactor().getCid();
            ContextFactor contextFactor = session.get(ContextFactor.class, cid);
            contextFactor.getRelevanceSet().remove(relevance);
            session.delete(relevance);

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
            List<ContextFactor> contextFactors =
                    session.createQuery("FROM ContextFactor").list();
            tx.commit();
            return contextFactors;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
