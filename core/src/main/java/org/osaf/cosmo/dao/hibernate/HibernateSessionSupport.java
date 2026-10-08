package org.osaf.cosmo.dao.hibernate;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.hibernate.FlushMode;
import org.hibernate.Session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import java.util.function.Supplier;

public class HibernateSessionSupport {

    @PersistenceContext
    protected EntityManager entityManager;

    Session currentSession() {
        return entityManager.unwrap(Session.class);
    }

    public static <T> void setCacheable(TypedQuery<T> hibQuery) {
        ((Query<T>) hibQuery).setCacheable(true);
    }

    public static <T> void setManualFlush(TypedQuery<T> query) {
        ((org.hibernate.query.Query<T>)query).setHibernateFlushMode(FlushMode.MANUAL);
    }

    /**
     * Does the work with the session's flush mode MANUAL (eg so a lookup doesn't auto flush), and restores the flush
     * mode afterwards - as setManualFlush does for a query. The session is shared with the caller's transaction, so
     * leaving it MANUAL would mean the rest of that transaction isn't flushed at commit, losing its changes.
     */
    <T> T withManualFlush(Supplier<T> work) {
        Session session = currentSession();
        FlushMode flushMode = session.getHibernateFlushMode();
        session.setHibernateFlushMode(FlushMode.MANUAL);
        try {
            return work.get();
        } finally {
            session.setHibernateFlushMode(flushMode);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getUniqueResult(TypedQuery<T> hibQuery) {
        try {
            return hibQuery.getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
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
