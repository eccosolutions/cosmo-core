/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.mock;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ScheduleDao;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.scheduler.Schedule;
import org.osaf.cosmo.scheduler.UserPreferencesScheduleHelper;

/**
 * Mock implementation of {@link ScheduleDao} useful for testing.
 */
public class MockScheduleDao implements ScheduleDao {
    private static final Log log = LogFactory.getLog(MockScheduleDao.class);

    MockUserDao userDao;
    UserPreferencesScheduleHelper helper = new UserPreferencesScheduleHelper(new MockEntityFactory());
    
    public MockScheduleDao(MockUserDao userDao) {
        this.userDao = userDao;
    }

    public Schedule createScheduleForUser(Schedule schedule, User user) {
        helper.addScheduleToUser(user, schedule);
        return schedule;
    }

    public void deleteScheduleForUser(Schedule schedule, User user) {
        helper.removeScheduleFromUser(user, schedule);
    }

    public void enableScheduleForUser(Schedule schedule, User user,
            boolean enabled) {
        helper.enableScheduleForUser(user, schedule, enabled);
    }

    public Set<Schedule> getSchedulesForUser(User user) {
        return helper.getSchedulesForUser(user);
    }

    public Set<User> getUsersWithSchedules() {
        HashSet<User> results = new HashSet<User>();
        for(User user: userDao.getUsers())
            for(Preference p: user.getPreferences())
                if(p.getKey().matches("cosmo\\.scheduler\\.job\\..*\\.enabled") && p.getValue().equals("true"))
                    results.add(user);
        
        return results;
    }

    public Schedule updateScheduleForUser(Schedule schedule, User user) {
       helper.updateScheduleForUser(user, schedule);
       return schedule;
    }

    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    public void init() {
        // TODO Auto-generated method stub
        
    }

}
