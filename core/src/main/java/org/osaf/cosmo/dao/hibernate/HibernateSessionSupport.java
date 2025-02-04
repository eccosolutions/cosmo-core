package org.osaf.cosmo.dao.hibernate;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.hibernate.FlushMode;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

public class HibernateSessionSupport {

    @PersistenceContext
    private EntityManager entityManager;

    Session currentSession() {
        return entityManager.unwrap(Session.class);
    }

    public static <T> void setCacheable(TypedQuery<T> hibQuery) {
        ((Query<T>) hibQuery).setCacheable(true);
    }

    public static <T> void setManualFlush(TypedQuery<T> query) {
        ((org.hibernate.query.Query<T>)query).setHibernateFlushMode(FlushMode.MANUAL);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getUniqueResult(TypedQuery<T> hibQuery) {
        try {
            return hibQuery.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    public static <T> String getQueryString(TypedQuery<T> query) {
        return ((org.hibernate.query.Query<T>)query).getQueryString();
    }

    protected static @NonNull DataAccessException convertJpaAccessException(PersistenceException e) {
        // Always returns nonnull for PersistenceException
        var dataAccessException = EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(e);
        assert dataAccessException != null;
        return dataAccessException;
    }
}
