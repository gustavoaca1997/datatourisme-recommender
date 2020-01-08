package recommender.persistence.manager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import recommender.persistence.entity.Aging;
import recommender.persistence.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class AgingManager {
    public AgingManager() {

    }

    public Integer addAging(Aging aging) {
        Transaction tx = null;
        Integer aid;
        Integer uid = aging.getUser().getUid();
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            User userEntity = session.get(User.class, uid);
            aging.setUser(userEntity);
            userEntity.getAgingSet().add(aging);

            aid = (Integer) session.save(aging);
            session.update(userEntity);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
        return aid;
    }

    public Optional<Aging> getAging(String puri, Integer uid) {
        Aging aging = null;
        try (Session session = HibernateUtil.openSession()) {
            User user = session.get(User.class, uid);
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Aging> criteriaQuery = cb.createQuery(Aging.class);
            Root<Aging> root = criteriaQuery.from(Aging.class);
            Predicate predicate = cb.and(
                    cb.like(root.get("puri"), puri),
                    cb.equal(root.get("user"), user)
            );
            criteriaQuery.select(root).where(predicate);
            Query <Aging> query = session.createQuery(criteriaQuery);
            List<Aging> resultList = query.getResultList();
            if (resultList.size() == 1) {
                aging = resultList.get(0);
            }
        }
        return Optional.ofNullable(aging);
    }

    public Double getAgingValue(String puri, Integer uid) {
        Optional<Aging> optionalAging = getAging(puri, uid);
        if (optionalAging.isPresent()) {
            return optionalAging.get().getValue();
        }
        return 1D;
    }

    public void updateAging(Aging updatedAging) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Aging aging;
            Integer aid = updatedAging.getAid();
            if (aid != null) {
                aging = session.get(Aging.class, aid);
            } else {
                Integer uid = updatedAging.getUser().getUid();
                String puri = updatedAging.getPuri();
                Optional<Aging> opAging = getAging(puri, uid);

                aging = opAging.orElseThrow(() -> new NoSuchElementException("Aging not found"));
            }

            aging.setValue(updatedAging.getValue());
            session.update(aging);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteAging(Integer aid) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Aging aging = session.get(Aging.class, aid);
            if (aging == null) {
                throw new NoSuchElementException(
                        String.format(
                                "Aging with id %s not found",
                                aid));
            }
            Integer uid = aging.getUser().getUid();
            User user = session.get(User.class, uid);
            user.getAgingSet().remove(aging);
            session.delete(aging);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
