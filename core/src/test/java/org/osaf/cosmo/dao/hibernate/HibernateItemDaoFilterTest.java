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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.Restrictions;
import org.osaf.cosmo.model.filter.StampFilter;
import org.osaf.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibEventStamp;
import org.osaf.cosmo.model.hibernate.HibNoteItem;

/**
 * Test findItems() api in ItemDao.
 *
 */
public class HibernateItemDaoFilterTest extends AbstractHibernateDaoTestCase {

    protected ContentDaoImpl contentDao = null;

    protected UserDaoImpl userDao = null;
    
    protected final String CALENDAR_UID_1 = "calendar1";
    protected final String CALENDAR_UID_2 = "calendar2";
    protected final String NOTE_UID = "note";

    public HibernateItemDaoFilterTest() {
        super();
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception {
        // TODO Auto-generated method stub
        super.onSetUpInTransaction();
        
        CollectionItem calendar1 = generateCalendar("test1", "testuser");
        CollectionItem calendar2 = generateCalendar("test2", "testuser");
        calendar1.setUid(CALENDAR_UID_1);
        calendar2.setUid(CALENDAR_UID_2);
        
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        contentDao.createCollection(root, calendar1);
        contentDao.createCollection(root, calendar2);

        for (int i = 1; i <= 6; i++) {
            NoteItem event = generateEvent("test" + i + ".ics", "cal"
                    + i + ".ics", "testuser");
            event.setUid("calendar1_" + i);
            event.setIcalUid("icaluid" + i);
            contentDao.createContent(calendar1, event);
        }
        
        NoteItem note = generateNote("testnote", "testuser");
        note.setUid(NOTE_UID);
        note.setBody("find me");
        note.setIcalUid("find me");
        note.setDisplayName("find me");
        note.setReminderTime(new Date(123456789));
        note.getTriageStatus().setCode(TriageStatus.CODE_DONE);
        
        note = (NoteItem) contentDao.createContent(calendar1, note);
        
        NoteItem noteMod = generateNote("testnotemod", "testuser");
        noteMod.setUid(NOTE_UID + ":mod");
        noteMod.setModifies(note);
        noteMod.getTriageStatus().setCode(TriageStatus.CODE_NOW);
        noteMod = (NoteItem) contentDao.createContent(calendar1, noteMod);
        
        for (int i = 1; i <= 3; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "eventwithtimezone"
                    + i + ".ics", "testuser");
            event.setUid("calendar2_" + i);
            contentDao.createContent(calendar2, event);
        }
        
        
    }

    public void testFilterByUid() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setUid(Restrictions.eq(CALENDAR_UID_1));
        Set<Item> results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        verifyItemInSet(results, CALENDAR_UID_1);
    }
    
    public void testNoteFilter() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        Set<Item> results = contentDao.findItems(filter);
        Assert.assertEquals(11, results.size());
        
        filter.setIcalUid(Restrictions.eq("icaluid1"));
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        filter.setIcalUid(null);
        
        filter.setDisplayName(Restrictions.eq("find me not"));
        results = contentDao.findItems(filter);
        Assert.assertEquals(0, results.size());
        
        filter.setDisplayName(Restrictions.eq("find me"));
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        filter.setBody(Restrictions.like("find me not"));
        results = contentDao.findItems(filter);
        Assert.assertEquals(0, results.size());
        
        filter.setBody(Restrictions.like("find me"));
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        // find master items only
        filter = new NoteItemFilter();
        filter.setIsModification(false);
        results = contentDao.findItems(filter);
        Assert.assertEquals(10, results.size());
        
        // find master items with modifications only
        filter.setIsModification(null);
        filter.setHasModifications(true);
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        // find specific master and modifications
        filter = new NoteItemFilter();
        NoteItem note = (NoteItem) contentDao.findItemByUid(NOTE_UID);
        filter.setMasterNoteItem(note);
        results = contentDao.findItems(filter);
        Assert.assertEquals(2, results.size());
        
        // find triageStatus==DONE only, which should match one
        filter = new NoteItemFilter();
        filter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_DONE));
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        // find triageStatus==LATER only, which should match none
        filter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_LATER));
        results = contentDao.findItems(filter);
        Assert.assertEquals(0, results.size());
        
        //find notes without triage
        filter = new NoteItemFilter();
        filter.setTriageStatusCode(Restrictions.isNull());
        results = contentDao.findItems(filter);
        Assert.assertEquals(9, results.size());
        
        // limit results
        filter.setMaxResults(5);
        results = contentDao.findItems(filter);
        Assert.assertEquals(5, results.size());
        
        // find notes by reminderTime
        filter = new NoteItemFilter();
        filter.setReminderTime(Restrictions.between(new Date(12345678),new Date(1234567890)));
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        filter.setReminderTime(Restrictions.between(new Date(1000),new Date(2000)));
        results = contentDao.findItems(filter);
        Assert.assertEquals(0, results.size());
    }
    
    public void testFilterByParent() throws Exception {
        CollectionItem calendar1 = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID_1);
        ItemFilter filter = new NoteItemFilter();
        filter.setParent(calendar1);
        
        Set<Item> results = contentDao.findItems(filter);
        Assert.assertEquals(8, results.size());
    }
    
    public void testFilterByNoStamp() throws Exception {
        CollectionItem calendar1 = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID_1);
        ItemFilter filter = new NoteItemFilter();
        filter.setParent(calendar1);
        StampFilter missingStamp = new StampFilter();
        missingStamp.setStampClass(EventStamp.class);
        missingStamp.setMissing(true);
        filter.getStampFilters().add(missingStamp);
        
        Set<Item> results = contentDao.findItems(filter);
        Assert.assertEquals(2, results.size());
        verifyItemInSet(results, NOTE_UID);
    }
    
    public void testFilterByEventStamp() throws Exception {
        CollectionItem calendar1 = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID_1);
        CollectionItem calendar2 = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID_2);
        ItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        filter.getStampFilters().add(eventFilter);
        
        Set<Item> results = contentDao.findItems(filter);
        Assert.assertEquals(9, results.size());
        
        // find only recurring events
        eventFilter.setIsRecurring(true);
        results = contentDao.findItems(filter);
        Assert.assertEquals(2, results.size());
        
        eventFilter.setIsRecurring(null);
        filter.setParent(calendar1);
        results = contentDao.findItems(filter);
        Assert.assertEquals(6, results.size());
        
        DateTime start = new DateTime("20050817T115000Z");
        DateTime end = new DateTime("20050818T115000Z");
       
        Period period = new Period(start, end);
        
        eventFilter.setPeriod(period);
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        // Test that event with start==end==rangeStart (cal6.ics)
        // is returned
        start = new DateTime("20070811T164500Z");
        end = new DateTime("20070818T164500Z");
       
        period = new Period(start, end);
        
        eventFilter.setPeriod(period);
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        verifyItemInSet(results, "calendar1_6");
        
        start.setTime(new GregorianCalendar(1996, 1, 22).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        
        results = contentDao.findItems(filter);
        Assert.assertEquals(6, results.size());
        
        start.setTime(new GregorianCalendar(2007, 8, 6).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        
        results = contentDao.findItems(filter);
        Assert.assertEquals(0, results.size());
        
        // test query from calendar 2
        filter.setParent(calendar2);
        
        start = new DateTime("20070501T010000Z");
        end = new DateTime("20070601T160000Z");
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        
        results = contentDao.findItems(filter);
        Assert.assertEquals(3, results.size());
        
        start = new DateTime("20080501T010000Z");
        end = new DateTime("20080601T160000Z");
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        
        results = contentDao.findItems(filter);
        Assert.assertEquals(2, results.size());
        
        start = new DateTime("20200501T160000Z");
        end = new DateTime("20200601T160000Z");
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        
        results = contentDao.findItems(filter);
        Assert.assertEquals(1, results.size());
        
        // test expand recurring events
        eventFilter.setExpandRecurringEvents(true);
        
        start = new DateTime("20080501T010000Z");
        end = new DateTime("20080601T160000Z");
        period = new Period(start, end);
        eventFilter.setPeriod(period);
        results = contentDao.findItems(filter);
        // Should be two masters + 32 occurences for the daily + 4 occurences for 
        // the weekly event
        Assert.assertEquals(38, results.size());
        
        // configure filter to not return master items
        filter.setFilterProperty(EventStampFilter.PROPERTY_INCLUDE_MASTER_ITEMS, "false");
        results = contentDao.findItems(filter);
        // Should just be the occurrences
        Assert.assertEquals(36, results.size());
    }
    
    public void testMultipleFilters() throws Exception {
        CollectionItem calendar1 = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID_1);
 
        NoteItemFilter filter1 = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        filter1.getStampFilters().add(eventFilter);
        filter1.setParent(calendar1);
        
        NoteItemFilter filter2 = new NoteItemFilter();
        filter2.setParent(calendar1);
        filter2.setIsModification(false);
        
        
        StampFilter missingFilter = new StampFilter();
        missingFilter.setStampClass(EventStamp.class);
        missingFilter.setMissing(true);
        filter2.setParent(calendar1);
        filter2.getStampFilters().add(missingFilter);
        
        ItemFilter[] filters = new ItemFilter[] {filter1, filter2};
        
        Set<Item> results = contentDao.findItems(filters);
        Assert.assertEquals(7, results.size());
    }
    
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private CollectionItem generateCalendar(String name, String owner) {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setName(name);
        calendar.setOwner(getUser(userDao, owner));
        
        CalendarCollectionStamp ccs = new HibCalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");
        
        return calendar;
    }

    private NoteItem generateEvent(String name, String file,
            String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventStamp evs = new HibEventStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
       
        return event;
    }
    
    private NoteItem generateNote(String name,
            String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        return event;
    }
    
    private void verifyItemInSet(Set<Item> items, String uid) {
        for(Item item: items) {
            if(item.getUid().equals(uid))
                return;
        }
        
        Assert.fail("item " + uid + " not in set");   
    }

}
