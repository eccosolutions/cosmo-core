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
package org.osaf.cosmo.security.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.mock.MockSecurityContext;
import org.osaf.cosmo.security.mock.MockUserPrincipal;

/**
 * Test Case for <code>SecurityHelper/code> which uses mock
 * model objects.
 */
public class SecurityHelperTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(SecurityHelperTest.class);

    private TestHelper testHelper;

    private SecurityHelper securityHelper;
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private MockUserDao userDao;

    /** */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        securityHelper = new SecurityHelper(contentDao, userDao);
    }

    /** */
    public void testCollectionUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        CollectionItem col = testHelper.makeDummyCalendarCollection(user1);
        CosmoSecurityContext context = getSecurityContext(user1);

        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
    }


    /** */
    public void testContentUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User user3 = testHelper.makeDummyUser("user3","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user2);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);

        CosmoSecurityContext context = getSecurityContext(user1);

        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user2);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user3);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));

        // remove note from col2, so user2 doesn't have access
        note.removeParent(col2);

        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));

    }

    private CosmoSecurityContext getSecurityContext(User user) {
        return new MockSecurityContext(new MockUserPrincipal(user));
    }
}
