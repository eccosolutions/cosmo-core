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
package org.osaf.cosmo.scheduler;

import java.util.HashMap;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.service.impl.StandardUserService;

/**
 * Test UserPreferencesScheduleHelper
 */
public class UserPreferencesScheduleHelperTest extends TestCase {
   
    private StandardUserService userService;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private MockDaoStorage storage;
    private TestHelper testHelper;
    private UserPreferencesScheduleHelper helper;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();
        
        helper = new UserPreferencesScheduleHelper(new MockEntityFactory());
    }

    public void testCreateUpdateSchedule() throws Exception {
       
        // create test data
        User user = testHelper.makeDummyUser("user1", "user1");
        userService.createUser(user);
       
        Assert.assertEquals(0, user.getPreferences().size());
        
        HashMap<String, String> scheduleProps = new HashMap<String, String>();
        scheduleProps.put("enabled", "true");
        scheduleProps.put("foo", "bar");
        Schedule schedule = new Schedule("1", scheduleProps);
        
        helper.addScheduleToUser(user, schedule);
        Assert.assertEquals(2, user.getPreferences().size());
        Assert.assertEquals("true", user.getPreference("cosmo.scheduler.job.1.enabled").getValue());
        Assert.assertEquals("bar", user.getPreference("cosmo.scheduler.job.1.foo").getValue());
        
        try {
            //shouldn't be able to create now
            helper.addScheduleToUser(user, schedule);
            Assert.fail("able to create same schdule twice!");
        } catch (IllegalArgumentException e) {
           
        }
        
        schedule.getProperties().put("foo2", "bar2");
        schedule.getProperties().put("enabled", "false");
        schedule.getProperties().remove("foo");
        
        helper.updateScheduleForUser(user, schedule);
        Assert.assertEquals(2, user.getPreferences().size());
        Assert.assertEquals("false", user.getPreference("cosmo.scheduler.job.1.enabled").getValue());
        Assert.assertEquals("bar2", user.getPreference("cosmo.scheduler.job.1.foo2").getValue());       
        Assert.assertNull(user.getPreference("cosmo.scheduler.job.1.foo"));
        
        try {
            //shouldn't be able to update non-existent
            schedule.setName("2");
            helper.updateScheduleForUser(user, schedule);
            Assert.fail("able to update non existent schedule!");
        } catch (IllegalArgumentException e) {
           
        }
    }
    
    public void testGetSchedules() throws Exception {
        // create test data
        User user = testHelper.makeDummyUser("user1", "user1");
        userService.createUser(user);
       
        Assert.assertEquals(0, user.getPreferences().size());
        
        HashMap<String, String> scheduleProps = new HashMap<String, String>();
        scheduleProps.put("enabled", "true");
        scheduleProps.put("foo", "bar");
        Schedule schedule = new Schedule("1", scheduleProps);
        
        helper.addScheduleToUser(user, schedule);
       
        Set<Schedule> schedules = helper.getSchedulesForUser(user);
        Assert.assertEquals(1, schedules.size());
        
        Schedule sched = schedules.iterator().next();
        Assert.assertEquals("1", sched.getName());
        Assert.assertEquals("true", sched.getProperty("enabled"));
        Assert.assertEquals("bar", sched.getProperty("foo"));
        
        sched.getProperties().put("enabled", "false");
        helper.updateScheduleForUser(user, sched);
        
        Assert.assertEquals(0, helper.getSchedulesForUser(user).size());
    }
    
}
