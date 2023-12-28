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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.BaseModelObject;
import org.osaf.cosmo.model.hibernate.HibUser;
import org.osaf.cosmo.util.ArrayPagedList;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends HibernateSessionSupport implements UserDao {

    private static final Log log = LogFactory.getLog(UserDaoImpl.class);

    private static final QueryCriteriaBuilder<User.SortType> queryCriteriaBuilder = new UserQueryCriteriaBuilder<User.SortType>();

    public User createUser(User user) {

        try {
            if(user==null)
                throw new IllegalArgumentException("user is required");

            if(getBaseModelObject(user).getId()!=-1)
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
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logInvalidStateException(cve);
            throw cve;
        }

    }

    public User getUser(String username) {
        try {
            return findUserByUsername(username);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserById(long userId) {
        try {
            return findUserById(userId);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByUid(String uid) {
        if(uid==null)
            throw new IllegalArgumentException("uid required");

        try {
            return findUserByUid(uid);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByActivationId(String id) {
        if(id==null)
            throw new IllegalArgumentException("id required");

        try {
            return findUserByActivationId(id);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        if(email==null)
            throw new IllegalArgumentException("email required");

        try {
            return findUserByEmail(email);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<User>();
            Iterator it = currentSession().getNamedQuery("user.all").iterate();
            while (it.hasNext())
                users.add((User) it.next());

            return users;
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public PagedList getUsers(PageCriteria<User.SortType> pageCriteria) {
        try {
            Criteria crit = queryCriteriaBuilder.buildQueryCriteria(
                    currentSession(), pageCriteria);
            List<User> results = crit.list();

            // Need the total
            Long size = (Long) currentSession().getNamedQuery("user.count")
                    .uniqueResult();

            return new ArrayPagedList<User, User.SortType>(pageCriteria, results, size.intValue());
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null)
                removeUser(user);
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(User user) {
        try {
            // TODO: should probably let db take care of this with
            // cacade constaint
            deleteAllPasswordRecoveries(user);

            currentSession().delete(user);
            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User updateUser(User user) {
        try {
            // prevent auto flushing when querying for existing users
            currentSession().setFlushMode(FlushMode.MANUAL);

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
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }

    public void createPasswordRecovery(PasswordRecovery passwordRecovery){
        try {
            currentSession().save(passwordRecovery);
            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public PasswordRecovery getPasswordRecovery(String key){
        try {
            Query hibQuery = currentSession().getNamedQuery("passwordRecovery.byKey")
                    .setParameter("key", key);
            hibQuery.setCacheable(true);
            return (PasswordRecovery) hibQuery.uniqueResult();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void deletePasswordRecovery(PasswordRecovery passwordRecovery) {
        try {
            currentSession().delete(passwordRecovery);
            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    private User findUserByUsername(String username) {
        // take advantage of optimized caching with naturalId
        return (User) currentSession().bySimpleNaturalId(HibUser.class).load(username);
    }

    private User findUserByUsernameIgnoreCase(String username) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery("user.byUsername.ignorecase").setParameter(
                "username", username);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (!users.isEmpty())
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByUsernameOrEmailIgnoreCaseAndId(Long userId,
            String username, String email) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery(
                "user.byUsernameOrEmail.ignorecase.ingoreId").setParameter(
                "username", username).setParameter("email", email)
                .setParameter("userid", userId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (!users.isEmpty())
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByEmail(String email) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery("user.byEmail").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (!users.isEmpty())
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery("user.byEmail.ignorecase").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (!users.isEmpty())
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserById(long userId) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery(
                "user.byId").setParameter("userId", userId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (!users.isEmpty())
            return (User) users.get(0);
        else
            return null;
    }

    private User findUserByUid(String uid) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery("user.byUid").setParameter(
                "uid", uid);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        return (User) hibQuery.uniqueResult();
    }

    private void deleteAllPasswordRecoveries(User user) {
        Session session = currentSession();
        session.getNamedQuery("passwordRecovery.delete.byUser").setParameter(
                "user", user).executeUpdate();
    }

    private User findUserByActivationId(String id) {
        Session session = currentSession();
        Query hibQuery = session.getNamedQuery("user.byActivationId").setParameter(
                "activationId", id);
        hibQuery.setCacheable(true);
        return (User) hibQuery.uniqueResult();
    }

    private static class UserQueryCriteriaBuilder<SortType extends User.SortType> extends
            StandardQueryCriteriaBuilder<SortType> {

        public UserQueryCriteriaBuilder() {
            super(User.class);
        }

        protected List<Order> buildOrders(PageCriteria<SortType> pageCriteria) {
            List<Order> orders = new ArrayList<Order>();

            User.SortType sort = pageCriteria.getSortType();
            if (sort == null)
                sort = User.SortType.USERNAME;

            if (sort.equals(User.SortType.NAME)) {
                orders.add(createOrder(pageCriteria, "lastName"));
                orders.add(createOrder(pageCriteria, "firstName"));
            }
            else if (sort.equals(User.SortType.ADMIN))
                orders.add(createOrder(pageCriteria, "admin"));
            else if (sort.equals(User.SortType.EMAIL))
                orders.add(createOrder(pageCriteria, "email"));
            else if (sort.equals(User.SortType.CREATED))
                orders.add(createOrder(pageCriteria, "CreatedDate"));
            else if (sort.equals(User.SortType.LAST_MODIFIED))
                orders.add(createOrder(pageCriteria, "ModifiedDate"));
            else if (sort.equals(User.SortType.ACTIVATED))
                orders.add(createOrder(pageCriteria, "activationId"));
            else
                orders.add(createOrder(pageCriteria, "username"));

            return orders;
        }

        private Order createOrder(PageCriteria pageCriteria, String property) {
            return pageCriteria.isSortAscending() ?
                Order.asc(property) :
                   Order.desc(property);
        }
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

    protected void logInvalidStateException(javax.validation.ConstraintViolationException cve) {
        // log more info about the invalid state
        if(log.isDebugEnabled()) {
            log.debug(cve.getLocalizedMessage());
            for (ConstraintViolation iv : cve.getConstraintViolations()) {
                log.debug("property name: " + iv.getPropertyPath() + " value: "
                        + iv.getInvalidValue());
            }
        }
    }
}
