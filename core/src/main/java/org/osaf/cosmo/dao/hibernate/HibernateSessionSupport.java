package org.osaf.cosmo.dao.hibernate;

import org.hibernate.Session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class HibernateSessionSupport {

    @PersistenceContext
    private EntityManager entityManager;

    Session currentSession() {
        return entityManager.unwrap(Session.class);
    }
}
