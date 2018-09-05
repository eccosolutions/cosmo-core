package org.osaf.cosmo.dao.hibernate;

import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class HibernateSessionSupport {

    @PersistenceContext
    private EntityManager entityManager;

    Session currentSession() {
        return entityManager.unwrap(Session.class);
    }
}
