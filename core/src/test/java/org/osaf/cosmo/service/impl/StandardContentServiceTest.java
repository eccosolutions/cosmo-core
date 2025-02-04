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
package org.osaf.cosmo.service.impl;

import org.junit.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.model.*;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockEventStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Test Case for <code>StandardContentService</code> which uses mock
 * data access objects.
 *
 * @see StandardContentService
 * @see MockContentDao
 */
public class StandardContentServiceTest {
    private static final Log log =
        LogFactory.getLog(StandardContentServiceTest.class);

    private StandardContentService service;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;

    protected String baseDir = "src/test/resources/testdata/";

    /** */
    protected void setUp() {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        service = new StandardContentService();
        lockManager = new SingleVMLockManager();
        service.setCalendarDao(calendarDao);
        service.setContentDao(contentDao);
        service.setLockManager(lockManager);
        service.init();
    }

    /** */
    @Test
    public void testFindItemByPath() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNotNull(item);
        assertEquals(dummyContent, item);

        contentDao.removeContent(dummyContent);
    }

    /** */
    @Test
    public void testInvalidModUid() {

        Item item = service.findItemByUid("uid" + ModificationUid.RECURRENCEID_DELIMITER + "bogus");

        // bogus mod uid should result in no item found, not a ModelValidationException
        assertNull(item);
    }

    /** */
    @Test
    public void testFindNonExistentItemByPath() {
        String path = "/foo/bar/baz";
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    /** */
    @Test
    public void testRemoveItem() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        service.removeItem(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);

        // cannot remove HomeCollection
        try {
            service.removeItem(rootCollection);
            Assertions.fail("able to remove root!");
        } catch (IllegalArgumentException e) {
        }
    }

    /** */
    @Test
    public void testCreateContent() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        ContentItem content = new MockNoteItem();
        content.setName("foo");
        content.setOwner(user);
        content = service.createContent(rootCollection, content);

        assertNotNull(content);
        assertEquals("foo", content.getName());
        assertEquals(user, content.getOwner());
    }

    /** */
    @Test
    public void testRemoveContent() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        service.removeContent(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }


    @Test
    public void testCreateCollectionWithChildren() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);

        NoteItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar");
        dummyContent.setOwner(user);

        HashSet<Item> children = new HashSet<>();
        children.add(dummyContent);

        dummyCollection =
            service.createCollection(rootCollection, dummyCollection, children);

        assertNotNull(dummyCollection);
        assertEquals(1, dummyCollection.getChildren().size());
        assertEquals("bar",
                dummyCollection.getChildren().iterator().next().getName());
    }

    @Test
    public void testUpdateCollectionWithChildren() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);

        ContentItem dummyContent1 = new MockNoteItem();
        dummyContent1.setName("bar1");
        dummyContent1.setOwner(user);

        ContentItem dummyContent2 = new MockNoteItem();
        dummyContent2.setName("bar2");
        dummyContent2.setOwner(user);

        HashSet<Item> children = new HashSet<>();
        children.add(dummyContent1);
        children.add(dummyContent2);

        dummyCollection =
            service.createCollection(rootCollection, dummyCollection, children);

        assertEquals(2, dummyCollection.getChildren().size());

        ContentItem bar1 =
            getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        ContentItem bar2 =
            getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        assertNotNull(bar1);
        assertNotNull(bar2);

        bar1.setIsActive(false);

        ContentItem bar3 = new MockNoteItem();
        bar3.setName("bar3");
        bar3.setOwner(user);

        children.clear();
        children.add(bar1);
        children.add(bar2);
        children.add(bar3);

        dummyCollection = service.updateCollection(dummyCollection, children);

        assertEquals(2, dummyCollection.getChildren().size());

        bar1 = getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        bar2 = getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        bar3 = getContentItemFromSet(dummyCollection.getChildren(), "bar3");

        assertNull(bar1);
        assertNotNull(bar2);
        assertNotNull(bar3);
    }

    @Test
    public void testCollectionHashGetsUpdated() {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);

        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar1");
        dummyContent.setOwner(user);

        dummyCollection =
            service.createCollection(rootCollection, dummyCollection);

        dummyContent =
            service.createContent(dummyCollection, dummyContent);

        assertEquals(1, dummyCollection.generateHash());

        dummyContent = service.updateContent(dummyContent);

        assertEquals(2, dummyCollection.generateHash());

        dummyContent = service.updateContent(dummyContent);
        assertEquals(3, dummyCollection.generateHash());
    }

    /** */
    @Test
    public void testUpdateEvent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        NoteItem masterNote = new MockNoteItem();
        masterNote.setName("foo");
        masterNote.setOwner(user);

        Calendar calendar = getCalendar("event_with_exceptions1.ics");

        EventStamp eventStamp = new MockEventStamp(masterNote);
        masterNote.addStamp(eventStamp);
        contentDao.createContent(rootCollection, masterNote);

        EntityConverter converter = new EntityConverter(testHelper.getEntityFactory());
        Set<ContentItem> toUpdate = new HashSet<>();
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents(), toUpdate);

        Calendar masterCal = eventStamp.getEventCalendar();
        VEvent masterEvent = eventStamp.getMasterEvent();

        Assertions.assertEquals(1, masterCal.getComponents().getComponents(Component.VEVENT).size());
        Assertions.assertNull(eventStamp.getMasterEvent().getRecurrenceId());

        Assertions.assertEquals(masterNote.getModifications().size(), 4);
        for(NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp eventException = StampUtils.getEventExceptionStamp(mod);
            VEvent exceptionEvent = eventException.getExceptionEvent();
            Assertions.assertEquals(mod.getModifies(), masterNote);
            Assertions.assertEquals(masterEvent.getUid().getValue(), exceptionEvent.getUid().getValue());
        }

        Calendar fullCal = converter.convertNote(masterNote);

        Assertions.assertNotNull(getEvent("20060104T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060105T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060106T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060107T140000", fullCal));

        Assertions.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060107T140000", masterNote.getModifications()));

        Assertions.assertEquals(fullCal.getComponents().getComponents(Component.VEVENT).size(), 5);

        // now update
        calendar = getCalendar("event_with_exceptions2.ics");
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents(), toUpdate);

        fullCal = converter.convertNote(masterNote);

        // should have removed 1, added 2 so that makes 4-1+2=5
        Assertions.assertEquals(masterNote.getModifications().size(), 5);
        Assertions.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assertions.assertNull(getEventException("20060107T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060108T140000", masterNote.getModifications()));
        Assertions.assertNotNull(getEventException("20060109T140000", masterNote.getModifications()));

        Assertions.assertNotNull(getEvent("20060104T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060105T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060106T140000", fullCal));
        Assertions.assertNull(getEvent("20060107T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060108T140000", fullCal));
        Assertions.assertNotNull(getEvent("20060109T140000", fullCal));
    }


    /** */
    @Test
    public void testNullContentDao() {
        service.setContentDao(null);
        try {
            service.init();
            fail("Should not be able to initialize service without contentDao");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private ContentItem getContentItemFromSet(Set<Item> items, String name) {
        for(Item item : items)
            if(item.getName().equals(name))
                return (ContentItem) item;
        return null;
    }

    private EventExceptionStamp getEventException(String recurrenceId, Set<NoteItem> items) {
        for(NoteItem mod : items) {
            EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
            if(ees.getRecurrenceId().toString().equals(recurrenceId))
                return ees;
        }
        return null;
    }

    private Calendar getCalendar(String filename) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + filename);
        Calendar calendar = cb.build(fis);
        return calendar;
    }

    private VEvent getEvent(String recurrenceId, Calendar calendar) {
        ComponentList events = calendar.getComponents().getComponents(Component.VEVENT);
        for(Iterator<VEvent> it = events.iterator(); it.hasNext();) {
            VEvent event = it.next();
            if(event.getRecurrenceId()!=null && event.getRecurrenceId().getDate().toString().equals(recurrenceId))
                return event;
        }
        return null;
    }
}
