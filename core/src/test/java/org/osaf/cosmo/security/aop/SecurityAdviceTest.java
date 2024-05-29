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
package org.osaf.cosmo.security.aop;

import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.security.Permission;
import org.osaf.cosmo.security.mock.MockSecurityContext;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

/**
 * Test Case for <code>SecurityAdvice/code>
 */
public class SecurityAdviceTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(SecurityAdviceTest.class);

    private StandardContentService service;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    private ContentService proxyService;
    private MockSecurityManager securityManager;


    /** */
    protected void setUp() {
        testHelper = new TestHelper();
        securityManager = new MockSecurityManager();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        service = new StandardContentService();
        lockManager = new SingleVMLockManager();
        service.setCalendarDao(calendarDao);
        service.setContentDao(contentDao);
        service.setLockManager(lockManager);
        service.init();

        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service);

        // add aspect
        SecurityAdvice sa = new SecurityAdvice();
        sa.setEnabled(true);
        sa.setSecurityManager(securityManager);
        sa.setContentDao(contentDao);
        sa.setUserDao(userDao);
        sa.init();
        factory.addAspect(sa);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }

    /** */
    public void testSecuredApiWithUser() {
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        // login as user1
        initiateContext(user1);

        // should work fine
        proxyService.findItemByUid("1");

        // now set security context to user2
        initiateContext(user2);
        // should fail
        try {
            proxyService.findItemByUid("1");
            Assert.fail("able to view item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.READ, e.getPermission());
        }

        // try to update item
        // should fail
        try {
            proxyService.updateContent(dummyContent);
            Assert.fail("able to update item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }

        // login as user1
        initiateContext(user1);

        // should succeed
        proxyService.updateContent(dummyContent);
    }

    /** */

    private void initiateContext(User user) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user)));
    }

}
