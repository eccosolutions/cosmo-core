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

import org.junit.Assert;

import org.junit.Test;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateContentDaoModificationsTest extends AbstractHibernateDaoTestCase {

    @Autowired
    protected UserDaoImpl userDao;
    @Autowired
    protected ContentDaoImpl contentDao;

    public HibernateContentDaoModificationsTest() {
        super();
    }

    @Test
    public void testModificationsCreate() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem itemA = generateTestContent("A", "testuser");
        NoteItem itemB = generateTestContent("BModifesA", "testuser");

        itemA = (NoteItem) contentDao.createContent(root, itemA);
        itemB.setModifies(itemA);
        itemB = (NoteItem) contentDao.createContent(root, itemB);
        clearSession();

        itemA = (NoteItem) contentDao.findItemByUid(itemA.getUid());
        itemB = (NoteItem) contentDao.findItemByUid(itemB.getUid());

        Assert.assertEquals(1, itemA.getModifications().size());
        Assert.assertTrue(itemA.getModifications().contains(itemB));
        Assert.assertNotNull(itemB.getModifies());
        Assert.assertEquals(itemB.getModifies().getUid(), itemA.getUid());

        // add another mod/remove old
        NoteItem itemC = generateTestContent("CModifesA", "testuser");
        itemC.setModifies(itemA);
        root = contentDao.getRootItem(user);

        contentDao.removeItem(itemB);
        contentDao.createContent(root, itemC);

        clearSession();
        itemA = (NoteItem) contentDao.findItemByUid(itemA.getUid());
        itemB = (NoteItem) contentDao.findItemByUid(itemB.getUid());
        itemC = (NoteItem) contentDao.findItemByUid(itemC.getUid());

        Assert.assertEquals(1, itemA.getModifications().size());
        Assert.assertTrue(itemA.getModifications().contains(itemC));
        Assert.assertNull(itemB);
        Assert.assertNotNull(itemC.getModifies());
        Assert.assertEquals(itemC.getModifies().getUid(), itemA.getUid());
    }

    @Test
    public void testModificationsDelete() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem itemA = generateTestContent("A", "testuser");
        NoteItem itemB = generateTestContent("BModifesA", "testuser");
        NoteItem itemC = generateTestContent("CModifesA", "testuser");

        itemA = (NoteItem) contentDao.createContent(root, itemA);
        itemB.setModifies(itemA);
        itemC.setModifies(itemA);
        itemB = (NoteItem) contentDao.createContent(root, itemB);
        itemC = (NoteItem) contentDao.createContent(root, itemC);
        clearSession();

        itemA = (NoteItem) contentDao.findItemByUid(itemA.getUid());

        Assert.assertEquals(2, itemA.getModifications().size());
        contentDao.removeContent(itemA);
        clearSession();

        itemA = (NoteItem) contentDao.findItemByUid(itemA.getUid());
        itemB = (NoteItem) contentDao.findItemByUid(itemB.getUid());
        itemC = (NoteItem) contentDao.findItemByUid(itemC.getUid());

        Assert.assertNull(itemA);
        Assert.assertNull(itemB);
        Assert.assertNull(itemC);
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private NoteItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private NoteItem generateTestContent(String name, String owner) {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        content.setIcalUid("icaluid:" + name);
        content.setBody("this is a body");
        return content;
    }

}
