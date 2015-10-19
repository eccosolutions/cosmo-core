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

import junit.framework.Assert;

import org.junit.Test;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibPreference;
import org.osaf.cosmo.model.hibernate.HibUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test UserPreferencesScheduleDao
 */
@ContextConfiguration({
                "/applicationContext-test-sessionFactory.xml",
                "/applicationContext.xml",
                "/applicationContext-scheduler.xml",
                "/applicationContext-test.xml",
            })
public class HibernateScheduleDaoTest extends AbstractHibernateDaoTestCase {
    
    @Autowired
    protected UserPreferencesScheduleDao scheduleDao;
    @Autowired
    protected UserDaoImpl userDao;
    
    public HibernateScheduleDaoTest() {
        super();
    }
    
    @Test
    public void testGetUsersWithSchedules() {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        
        user1 = userDao.createUser(user1);
        
        Assert.assertEquals(0, scheduleDao.getUsersWithSchedules().size());
        
        user1.addPreference(new HibPreference("cosmo.scheduler.job.1.enabled", "true"));
        
        userDao.updateUser(user1);
        
        Assert.assertEquals(1, scheduleDao.getUsersWithSchedules().size());
        
        user1.getPreference("cosmo.scheduler.job.1.enabled").setValue("false");
        
        userDao.updateUser(user1);
        
        Assert.assertEquals(0, scheduleDao.getUsersWithSchedules().size());
    }

}
