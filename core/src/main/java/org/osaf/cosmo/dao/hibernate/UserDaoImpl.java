/*
 * Copyright 2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.dao.hibernate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.BaseModelObject;
import org.osaf.cosmo.model.hibernate.HibUser;

/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends HibernateSessionSupport implements UserDao {

    private static final Log log = LogFactory.getLog(UserDaoImpl.class);

    public User createUser(User user) {

        try {
            if(user==null)
                throw new IllegalArgumentException("user is required");

            if(getBaseModelObject(user).getId() != null)
                throw new IllegalArgumentException("new user is required");

            if (findUserByUsernameIgnoreCase(user.getUsername()) != null)
                throw new DuplicateUsernameException(user);

            if (findUserByEmailIgnoreCase(user.getEmail()) != null)
                throw new DuplicateEmailException(user);

            if (user.getUid() == null || user.getUid() != null && user.getUid().isEmpty())
                user.setUid(UUID.randomUUID().toString());

            currentSession().save(user);
            currentSession().flush();
            return user;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException cve) {
            logInvalidStateException(cve);
            throw cve;
        }

    }

    public User getUser(String username) {
        try {
            return findUserByUsername(username);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public User getUserById(long userId) {
        try {
            return findUserById(userId);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public User getUserByUid(String uid) {
        if(uid==null)
            throw new IllegalArgumentException("uid required");

        try {
            return findUserByUid(uid);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public User getUserByActivationId(String id) {
        if(id==null)
            throw new IllegalArgumentException("id required");

        try {
            return findUserByActivationId(id);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        if(email==null)
            throw new IllegalArgumentException("email required");

        try {
            return findUserByEmail(email);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<>();
            var it = currentSession().createNamedQuery("user.all", User.class).stream().iterator();
            while (it.hasNext())
                users.add(it.next());

            return users;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null)
                removeUser(user);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public void removeUser(User user) {
        try {
            currentSession().delete(user);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public User updateUser(User user) {
        try {
            // prevent auto flushing when querying for existing users
            currentSession().setHibernateFlushMode(FlushMode.MANUAL);

            User findUser = findUserByUsernameOrEmailIgnoreCaseAndId(getBaseModelObject(user)
                    .getId(), user.getUsername(), user.getEmail());

            if (findUser != null) {
                if (findUser.getEmail().equals(user.getEmail()))
                    throw new DuplicateEmailException(user);
                else
                    throw new DuplicateUsernameException(user);
            }

            user.updateTimestamp();
            currentSession().update(user);
            currentSession().flush();

            return user;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }

    private User findUserByUsername(String username) {
        // take advantage of optimized caching with naturalId
        return currentSession().bySimpleNaturalId(HibUser.class).load(username);
    }

    private User findUserByUsernameIgnoreCase(String username) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery("user.byUsername.ignorecase", User.class).setParameter(
                "username", username);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        var users = hibQuery.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private User findUserByUsernameOrEmailIgnoreCaseAndId(Long userId,
            String username, String email) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery(
                "user.byUsernameOrEmail.ignorecase.ingoreId", User.class).setParameter(
                "username", username).setParameter("email", email)
                .setParameter("userid", userId);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        var users = hibQuery.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private User findUserByEmail(String email) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery("user.byEmail", User.class).setParameter(
                "email", email);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        var users = hibQuery.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery("user.byEmail.ignorecase", User.class).setParameter(
                "email", email);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        var users = hibQuery.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private User findUserById(long userId) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery(
                "user.byId", User.class).setParameter("userId", userId);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        var users = hibQuery.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private User findUserByUid(String uid) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery("user.byUid", User.class).setParameter(
                "uid", uid);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);
        return getUniqueResult(hibQuery);
    }

    private User findUserByActivationId(String id) {
        Session session = currentSession();
        var hibQuery = session.createNamedQuery("user.byActivationId", User.class).setParameter(
                "activationId", id);
        setCacheable(hibQuery);
        return getUniqueResult(hibQuery);
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

    protected void logInvalidStateException(jakarta.validation.ConstraintViolationException cve) {
        // log more info about the invalid state
        if(log.isDebugEnabled()) {
            log.debug(cve.getLocalizedMessage());
            for (ConstraintViolation<?> iv : cve.getConstraintViolations()) {
                log.debug("property name: " + iv.getPropertyPath() + " value: "
                        + iv.getInvalidValue());
            }
        }
    }
}
