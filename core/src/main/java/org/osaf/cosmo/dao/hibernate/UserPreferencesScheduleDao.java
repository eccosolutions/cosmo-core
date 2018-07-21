/*
 * Copyright 2008 Open Source Applications Foundation
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.dao.ScheduleDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibEntityFactory;
import org.osaf.cosmo.scheduler.Schedule;
import org.osaf.cosmo.scheduler.UserPreferencesScheduleHelper;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * Implementation of ScheduleDao using user preferences.
 */
public class UserPreferencesScheduleDao extends HibernateDaoSupport implements ScheduleDao {

    private static final Log log = LogFactory
            .getLog(UserPreferencesScheduleDao.class);
    private UserPreferencesScheduleHelper helper = new UserPreferencesScheduleHelper(
            new HibEntityFactory());

    public Schedule createScheduleForUser(Schedule schedule, User user) {
        helper.addScheduleToUser(user, schedule);
        updateUser(user);
        return schedule;
    }

    public void deleteScheduleForUser(Schedule schedule, User user) {
        helper.removeScheduleFromUser(user, schedule);
        updateUser(user);
    }

    public void enableScheduleForUser(Schedule schedule, User user, boolean enabled) {
        helper.enableScheduleForUser(user, schedule, enabled);
        updateUser(user);
    }

    public Set<Schedule> getSchedulesForUser(User user) {
        return helper.getSchedulesForUser(user);
    }

    public Set<User> getUsersWithSchedules() {
        Set<User> users = new HashSet<User>();

        try {
            Query hibQuery = currentSession().getNamedQuery("users.withSchedules");
            hibQuery.setCacheable(true);
            users.addAll(hibQuery.list());
            return users;
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Schedule updateScheduleForUser(Schedule schedule, User user) {
        helper.updateScheduleForUser(user, schedule);
        updateUser(user);
        return schedule;
    }

    public void destroy() {

    }

    public void init() {

    }

    protected void updateUser(User user) {
        try {
            user.updateTimestamp();
            currentSession().update(user);
            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logInvalidStateException(cve);
            throw cve;
        }
    }

    protected void logInvalidStateException(ConstraintViolationException cve) {
        // log more info about the invalid state
        if(log.isDebugEnabled()) {
            log.debug(cve.getLocalizedMessage());
            for (ConstraintViolation iv : cve.getConstraintViolations())
                log.debug("property name: " + iv.getPropertyPath() + " value: "
                        + iv.getInvalidValue());
        }
    }
}
