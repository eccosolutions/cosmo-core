/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;

import org.junit.Test;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.AttributeTombstone;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TaskStamp;
import org.osaf.cosmo.model.Tombstone;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibFileItem;
import org.osaf.cosmo.model.hibernate.HibItem;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibQName;
import org.osaf.cosmo.model.hibernate.HibStampTombstone;
import org.osaf.cosmo.model.hibernate.HibStringAttribute;
import org.osaf.cosmo.model.hibernate.HibTaskStamp;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test that HibernateContentDao correctly manages Tombstones.
 */
public class HibernateContentDaoTombstonesTest extends AbstractHibernateDaoTestCase {

    @Autowired
    protected UserDaoImpl userDao;

    @Autowired
    protected ContentDaoImpl contentDao;

    public HibernateContentDaoTombstonesTest() {
        super();
    }

    @Test
    public void testContentDaoAttributeTombstones() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        queryItem.removeAttribute(new HibQName("customattribute"));

        queryItem = contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(0, queryItem.getAttributes().size());
        Assert.assertEquals(1, queryItem.getTombstones().size());

        Tombstone ts = queryItem.getTombstones().iterator().next();
        Assert.assertTrue(ts instanceof AttributeTombstone);
        Assert.assertTrue(((AttributeTombstone) ts).getQName().equals(new HibQName("customattribute")));

        queryItem.addAttribute(new HibStringAttribute(new HibQName("customattribute"),"customattributevalue"));
        contentDao.updateContent(queryItem);
        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getAttributes().size());
        Assert.assertEquals(0, queryItem.getTombstones().size());
    }

    @Test
    public void testContentDaoItemTombstones() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        //create new collections
        CollectionItem a = new HibCollectionItem();
        a.setUid("a");
        a.setName("a");
        a.setOwner(user);
        a = contentDao.createCollection(root, a);

        //create new collections
        CollectionItem b= new HibCollectionItem();
        b.setUid("b");
        b.setName("b");
        b.setOwner(user);
        b = contentDao.createCollection(root, b);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note2.setUid("1:20070101");

        note2.setModifies(note1);

        Set<ContentItem> items = new LinkedHashSet<>();

        items.add(note1);
        items.add(note2);

        contentDao.updateCollection(a, items);
        contentDao.updateCollection(b, items);

        contentDao.removeItemFromCollection(note1, a);

        clearSession();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());

        // should be two because of master/mod
        Assert.assertNotNull(getItemTombstone(a, note1.getUid()));
        Assert.assertNotNull(getItemTombstone(a, note2.getUid()));

        // now re-add
        note1 = (NoteItem) contentDao.findItemByUid(note1.getUid());

        contentDao.addItemToCollection(note1, a);

        clearSession();
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());

        // should none now
        Assert.assertEquals(0, a.getTombstones().size());


        note1 = (NoteItem) contentDao.findItemByUid(note1.getUid());
        // remove note from all collections
        contentDao.removeItem(note1);

        clearSession();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());

        // should be two for each collection because of master/mod
        Assert.assertNotNull(getItemTombstone(a, note1.getUid()));
        Assert.assertNotNull(getItemTombstone(a, note2.getUid()));

        Assert.assertNotNull(getItemTombstone(b, note1.getUid()));
        Assert.assertNotNull(getItemTombstone(b, note2.getUid()));

    }

    @Test
    public void testContentDaoStampTombstones() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        NoteItem item = generateTestNote();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        TaskStamp task = new HibTaskStamp();
        item.addStamp(task);

        contentDao.createContent(root, item);
        clearSession();

        item = (NoteItem) contentDao.findItemByUid(item.getUid());
        Assert.assertEquals(0, item.getTombstones().size());

        item.removeStamp(item.getStamp(TaskStamp.class));

        contentDao.updateContent(item);
        item = (NoteItem) contentDao.findItemByUid(item.getUid());
        Assert.assertEquals(1, item.getTombstones().size());

        Assert.assertTrue(item.getTombstones().contains(new HibStampTombstone(item, "task")));

        // re-add
        task = new HibTaskStamp();
        item.addStamp(task);

        contentDao.updateContent(item);

        clearSession();

        item = (NoteItem) contentDao.findItemByUid(item.getUid());
        Assert.assertEquals(0, item.getTombstones().size());
    }

    private ItemTombstone getItemTombstone(Item item, String uid) {
        for(Tombstone ts: item.getTombstones()) {
            if(ts instanceof ItemTombstone)
                if(((ItemTombstone) ts).getItemUid().equals(uid))
                    return (ItemTombstone) ts;
        }

        return null;
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private FileItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private NoteItem generateTestNote() throws Exception {
        return generateTestNote("test", "testuser");
    }

    private FileItem generateTestContent(String name, String owner)
            throws Exception {
        FileItem content = new HibFileItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setContent(helper.getBytes("testdata1.txt"));
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }

    private NoteItem generateTestNote(String name, String owner) {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        return content;
    }

    private HibItem getHibItem(Item item) {
        return (HibItem) item;
    }

}
