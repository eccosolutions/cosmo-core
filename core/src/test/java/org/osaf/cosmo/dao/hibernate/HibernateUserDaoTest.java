/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibPasswordRecovery;
import org.osaf.cosmo.model.hibernate.HibPreference;
import org.osaf.cosmo.model.hibernate.HibUser;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;


public class HibernateUserDaoTest extends AbstractHibernateDaoTestCase {
    
    protected UserDaoImpl userDao = null;
    
    public HibernateUserDaoTest() {
        super();
    }
    
    public void testCreateUser() {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);

        User user2 = new HibUser();
        user2.setUsername("user2");
        user2.setFirstName("User2");
        user2.setLastName("2");
        user2.setEmail("user2@user2.com");
        user2.setPassword("user2password");
        user2.setAdmin(Boolean.FALSE);

        user2 = userDao.createUser(user2);

        // find by username
        User queryUser1 = userDao.getUser("user1");
        Assert.assertNotNull(queryUser1);
        Assert.assertNotNull(queryUser1.getUid());
        verifyUser(user1, queryUser1);

        clearSession();

        // find by uid
        queryUser1 = userDao.getUserByUid(user1.getUid());
        Assert.assertNotNull(queryUser1);
        verifyUser(user1, queryUser1);

        clearSession();

        // Get all
        Set users = userDao.getUsers();
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
        verifyUserInCollection(user1, users);
        verifyUserInCollection(user2, users);

        clearSession();

        // try to create duplicate
        User user3 = new HibUser();
        user3.setUsername("user2");
        user3.setFirstName("User");
        user3.setLastName("1");
        user3.setEmail("user1@user1.com");
        user3.setPassword("user1password");
        user3.setAdmin(Boolean.TRUE);

        try {
            userDao.createUser(user3);
            Assert.fail("able to create user with duplicate username");
        } catch (DuplicateUsernameException due) {
        }

        user3.setUsername("user3");
        try {
            userDao.createUser(user3);
            Assert.fail("able to create user with duplicate email");
        } catch (DuplicateEmailException dee) {
        }

        // delete user
        userDao.removeUser("user2");
    }
    
    public void testUserProperties() {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);
        
        clearSession();

        // find by username
        User queryUser1 = userDao.getUser("user1");
        Assert.assertNotNull(queryUser1);
        Assert.assertNotNull(queryUser1.getUid());
        verifyUser(user1, queryUser1);
        
        // attempt to find by pref
        Assert.assertEquals(0, userDao.findUsersByPreference("prop1", "value1").size());
        
        queryUser1.addPreference(new HibPreference("prop1", "value1"));
        queryUser1.addPreference(new HibPreference("prop2", "value2"));

        userDao.updateUser(queryUser1);
        
        clearSession();

        // find by uid
        queryUser1 = userDao.getUserByUid(user1.getUid());
        Assert.assertNotNull(queryUser1);
        Assert.assertEquals(2, queryUser1.getPreferences().size());
        Assert.assertEquals("value1",
                            queryUser1.getPreference("prop1").getValue());
        Assert.assertEquals("value2",
                            queryUser1.getPreference("prop2").getValue());
        
        // find by preference
        Assert.assertEquals(1, userDao.findUsersByPreference("prop1", "value1").size());
        
        queryUser1.removePreference("prop2");
        queryUser1.getPreference("prop1").setValue("value1changed");
        userDao.updateUser(queryUser1);

        clearSession();

        queryUser1 = userDao.getUserByUid(user1.getUid());
        Assert.assertNotNull(queryUser1);
        Assert.assertEquals(1, queryUser1.getPreferences().size());
        Assert.assertEquals("value1changed", queryUser1.getPreference("prop1").getValue());
        
        userDao.removeUser(queryUser1);
        clearSession();
    }
    
    public void testCreateDuplicateUserEmail() {
        User user1 = new HibUser();
        user1.setUsername("uSeR1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);
        clearSession();

        User user2 = new HibUser();
        user2.setUsername("UsEr1");
        user2.setFirstName("User2");
        user2.setLastName("2");
        user2.setEmail("user2@user2.com");
        user2.setPassword("user2password");
        user2.setAdmin(Boolean.FALSE);

        try {
            user2 = userDao.createUser(user2);
            clearSession();
            Assert.fail("able to create duplicate usernames!");
        } catch (DuplicateUsernameException e) {
        }
        
        
        user2.setUsername("user2");   
        user2 = userDao.createUser(user2);
        clearSession();
        
        User user3 = new HibUser();
        user3.setUsername("user3");
        user3.setFirstName("User2");
        user3.setLastName("2");
        user3.setEmail("USER2@user2.com");
        user3.setPassword("user2password");
        user3.setAdmin(Boolean.FALSE);
        
        try {
            user3 = userDao.createUser(user3);
            clearSession();
            Assert.fail("able to create duplicate email!");
        } catch (DuplicateEmailException e) {
        }
        
        
        user3.setEmail("user3@user2.com");
        user3 = userDao.createUser(user3);
        clearSession();  
    }
    
    public void testUpdateUser() throws Exception {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);
        
        clearSession();
        
        // find by uid
        User queryUser1 = userDao.getUserByUid(user1.getUid());
        Assert.assertNotNull(queryUser1);
        verifyUser(user1, queryUser1);
        
        queryUser1.setPassword("user2password");
        userDao.updateUser(queryUser1);
        
        clearSession();
        queryUser1 = userDao.getUserByUid(user1.getUid());
        Assert.assertEquals(queryUser1.getPassword(), "user2password");
    }
    
    public void testUpdateUserDuplicate() throws Exception {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);
        
        User user2 = new HibUser();
        user2.setUsername("user2");
        user2.setFirstName("User2");
        user2.setLastName("2");
        user2.setEmail("user2@user2.com");
        user2.setPassword("user2password");
        user2.setAdmin(Boolean.FALSE);

        user2 = userDao.createUser(user2);
        
        clearSession();
        
        // find by uid
        User queryUser1 = userDao.getUserByUid(user1.getUid());
        queryUser1.setUsername("user2");
        try {
            userDao.updateUser(queryUser1);
            Assert.fail("able to update with duplicate username");
        } catch (DuplicateUsernameException e) {
        }
        
        queryUser1.setUsername("user1");
        queryUser1.setEmail("user2@user2.com");
        try {
            userDao.updateUser(queryUser1);
            Assert.fail("able to update with duplicate email");
        } catch (DuplicateEmailException e) {
        }
        
        queryUser1.setEmail("lsdfj@lsdfj.com");
        userDao.updateUser(queryUser1);
        clearSession();
    }
    
    public void testPaginatedUsers() throws Exception {
        User user1 = helper.createDummyUser(userDao, 1);
        User user2 = helper.createDummyUser(userDao, 2);
        User user3 = helper.createDummyUser(userDao, 3);
        User user4 = helper.createDummyUser(userDao, 4);

        clearSession();

        PageCriteria pageCriteria = new PageCriteria();

        pageCriteria.setPageNumber(1);
        pageCriteria.setPageSize(2);
        pageCriteria.setSortAscending(true);
        pageCriteria.setSortType(User.SortType.NAME);

        PagedList pagedList = userDao.getUsers(pageCriteria);
        List results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        verifyUserInCollection(user1, results);
        verifyUserInCollection(user2, results);

        clearSession();

        pageCriteria.setPageNumber(2);
        pagedList = userDao.getUsers(pageCriteria);
        results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        verifyUserInCollection(user3, results);
        verifyUserInCollection(user4, results);

        pageCriteria.setSortAscending(false);
        pageCriteria.setSortType(User.SortType.NAME);
        pageCriteria.setPageNumber(1);

        pagedList = userDao.getUsers(pageCriteria);
        results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        verifyUserInCollection(user3, results);
        verifyUserInCollection(user4, results);
    }

    public void testDeleteUser() throws Exception {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        userDao.createUser(user1);
        clearSession();
        
        User queryUser1 = userDao.getUser("user1");
        Assert.assertNotNull(queryUser1);
        userDao.removeUser(queryUser1);
        
        clearSession();
        
        queryUser1 = userDao.getUser("user1");
        Assert.assertNull(queryUser1);
    }

    public void testDeleteUserByUsername() throws Exception {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        userDao.createUser(user1);
        
        clearSession();
        
        User queryUser1 = userDao.getUser("user1");
        Assert.assertNotNull(queryUser1);
        userDao.removeUser(user1.getUsername());
        
        clearSession();
        
        queryUser1 = userDao.getUser("user1");
        Assert.assertNull(queryUser1);
    }
    
    public void testCreatePasswordRecovery() throws Exception {
        User user1 = new HibUser();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);

        user1 = userDao.createUser(user1);
        
        PasswordRecovery passwordRecovery = new HibPasswordRecovery(user1, "1");
        
        userDao.createPasswordRecovery(passwordRecovery);
        
        String passwordRecoveryKey = passwordRecovery.getKey();
        
        clearSession();
        
        PasswordRecovery queryPasswordRecovery = 
            userDao.getPasswordRecovery(passwordRecoveryKey);
        
        Assert.assertNotNull(queryPasswordRecovery);
        Assert.assertEquals(passwordRecovery, queryPasswordRecovery);
        
        // Test delete
        userDao.deletePasswordRecovery(queryPasswordRecovery);
        queryPasswordRecovery = 
            userDao.getPasswordRecovery(passwordRecoveryKey);
        
        Assert.assertNull(queryPasswordRecovery);
    }
    
    private void verifyUser(User user1, User user2) {
        Assert.assertEquals(user1.getUid(), user2.getUid());
        Assert.assertEquals(user1.getUsername(), user2.getUsername());
        Assert.assertEquals(user1.getAdmin(), user2.getAdmin());
        Assert.assertEquals(user1.getEmail(), user2.getEmail());
        Assert.assertEquals(user1.getFirstName(), user2.getFirstName());
        Assert.assertEquals(user1.getLastName(), user2.getLastName());
        Assert.assertEquals(user1.getPassword(), user2.getPassword());
    }

    private void verifyUserInCollection(User user, Collection users) {
        Iterator it = users.iterator();
        while (it.hasNext()) {
            User nextUser = (User) it.next();
            if (nextUser.getUsername().equals(user.getUsername())) {
                verifyUser(user, nextUser);
                return;
            }
        }
        Assert.fail("specified User doesn't exist in Set: "
                + user.getUsername());
    }

}
