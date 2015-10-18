package org.osaf.cosmo.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;

/**
 * Replaces the hibernate 3 class which was once provided by Spring, until we upgraded to Hibernate 4 and didn't
 * upgrade Spring to 4.1. TODO: Upgrade Spring to 4.1, and get rid of this class.
 *
 * @since 17/10/15
 */
public class HibernateDaoSupport {
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected final DataAccessException convertHibernateAccessException(HibernateException ex) {
        return SessionFactoryUtils.convertHibernateAccessException(ex);
    }

}
