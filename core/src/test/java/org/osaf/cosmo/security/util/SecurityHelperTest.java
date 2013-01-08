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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.mock.MockSecurityContext;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
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
    public void testCollectionTicketAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        
        CollectionItem col = testHelper.makeDummyCalendarCollection(user1);
        
        Ticket roTicket = testHelper.makeDummyTicket();
        roTicket.setKey("1");
        roTicket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        col.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        
        CosmoSecurityContext context = getSecurityContext(roTicket);
        
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(rwTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(rwBogus);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
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
    
    /** */
    public void testContentTicketAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user1);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        Ticket roTicket = testHelper.makeDummyTicket();
        roTicket.setKey("1");
        roTicket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        col2.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col2.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        Ticket rwItemTicket = testHelper.makeDummyTicket();
        rwItemTicket.setKey("4");
        rwItemTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        note.getTickets().add(rwItemTicket);
        
        CosmoSecurityContext context = getSecurityContext(roTicket);
        
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(rwTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(rwBogus);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        // remove note from col2, so rwTicket doesn't have access
        note.removeParent(col2);
        
        context = getSecurityContext(rwTicket);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        // check item ticket
        context = getSecurityContext(rwItemTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
    }
    
    /** */
    public void testContentUserWithTicketsAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user1);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col2.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        Ticket rwItemTicket = testHelper.makeDummyTicket();
        rwItemTicket.setKey("4");
        rwItemTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        note.getTickets().add(rwItemTicket);
        
        Set<Ticket> tickets = new HashSet<Ticket>();
        tickets.add(rwTicket);
        
        CosmoSecurityContext context = getSecurityContextWithTickets(user2, tickets);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        
        tickets.clear();
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        tickets.add(rwItemTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
    }
    
    private CosmoSecurityContext getSecurityContext(User user) {
        return new MockSecurityContext(new MockUserPrincipal(user));
    }
    
    private CosmoSecurityContext getSecurityContextWithTickets(User user, Set<Ticket> tickets) {
        return new MockSecurityContext(new MockUserPrincipal(user), tickets);
    }
    
    private CosmoSecurityContext getSecurityContext(Ticket ticket) {
        return new MockSecurityContext(new MockTicketPrincipal(ticket));
    } 
}
