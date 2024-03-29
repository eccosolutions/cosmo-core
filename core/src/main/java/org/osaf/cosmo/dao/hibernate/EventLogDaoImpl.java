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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.dao.EventLogDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemChangeRecord;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.event.EventLogEntry;
import org.osaf.cosmo.model.event.ItemAddedEntry;
import org.osaf.cosmo.model.event.ItemEntry;
import org.osaf.cosmo.model.event.ItemRemovedEntry;
import org.osaf.cosmo.model.event.ItemUpdatedEntry;
import org.osaf.cosmo.model.hibernate.HibEventLogEntry;
import org.osaf.cosmo.model.hibernate.HibItem;
import org.osaf.cosmo.model.hibernate.HibUser;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

/**
 * Implementation of EventLogDao using Hibernate persistence objects.
 */
public class EventLogDaoImpl extends HibernateSessionSupport implements EventLogDao {

    private static final Log log = LogFactory.getLog(EventLogDaoImpl.class);


    public void addEventLogEntry(EventLogEntry entry) {
        try {
            addEventLogEntryInternal(entry);
            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void addEventLogEntries(List<EventLogEntry> entries) {

        try {
            for(EventLogEntry entry: entries)
                addEventLogEntryInternal(entry);

            currentSession().flush();
        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    public List<ItemChangeRecord> findChangesForCollection(
            CollectionItem collection, Date start, Date end) {
        try {
            Query hibQuery = currentSession().getNamedQuery("logEntry.by.collection.date");
            hibQuery.setParameter("parentId", ((HibItem) collection).getId());
            hibQuery.setParameter("startDate", start);
            hibQuery.setParameter("endDate", end);
            List<HibEventLogEntry> results = hibQuery.list();

            ArrayList<ItemChangeRecord> changeRecords = new ArrayList<ItemChangeRecord>();

            for(HibEventLogEntry result: results)
                changeRecords.add(convertToItemChangeRecord(result));

            return changeRecords;

        } catch (HibernateException e) {
            currentSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    private ItemChangeRecord convertToItemChangeRecord(HibEventLogEntry entry) {
        ItemChangeRecord record = new ItemChangeRecord();
        record.setAction(ItemChangeRecord.toAction(entry.getType()));
        record.setDate(entry.getDate());
        record.setItemUuid(entry.getUid1());
        record.setItemDisplayName(entry.getStrval1());
        record.setModifiedBy(entry.getStrval2());

        return record;
    }

    private void addEventLogEntryInternal(EventLogEntry entry) {

        if(entry instanceof ItemAddedEntry)
            addItemAddedEntry((ItemAddedEntry) entry);
        else if(entry instanceof ItemRemovedEntry)
            addItemRemovedEntry((ItemRemovedEntry) entry);
        else if(entry instanceof ItemUpdatedEntry)
            addItemUpdatedEntry((ItemUpdatedEntry) entry);

    }

    // translate ItemAddedEntry to HibEventLogEntry
    private void addItemAddedEntry(ItemAddedEntry entry) {
        HibEventLogEntry hibEntry = createBaseHibEntry(entry);
        hibEntry.setType("ItemAdded");
        setBaseItemEntryAttributes(hibEntry, entry);
    }

    // translate ItemRevmoedEntry to HibEventLogEntry
    private void addItemRemovedEntry(ItemRemovedEntry entry) {
        HibEventLogEntry hibEntry = createBaseHibEntry(entry);
        hibEntry.setType("ItemRemoved");
        setBaseItemEntryAttributes(hibEntry, entry);
    }

    // translate ItemUpdatedEntry to HibEventLogEntry(s)
    private void addItemUpdatedEntry(ItemUpdatedEntry entry) {
        HibEventLogEntry hibEntry = createBaseHibEntry(entry);
        hibEntry.setType("ItemUpdated");
        setBaseItemEntryAttributes(hibEntry, entry);
    }

    private HibEventLogEntry createBaseHibEntry(EventLogEntry entry) {
        HibEventLogEntry hibEntry = new HibEventLogEntry();

        if(entry.getDate()!=null)
            hibEntry.setDate(entry.getDate());

        if(entry.getUser()!=null) {
            hibEntry.setAuthType("user");
            hibEntry.setAuthId(((HibUser) entry.getUser()).getId());
        }

        return hibEntry;
    }

    private void setBaseItemEntryAttributes(HibEventLogEntry hibEntry, ItemEntry entry) {
        hibEntry.setId1(((HibItem) entry.getCollection()).getId());
        hibEntry.setId2(((HibItem) entry.getItem()).getId());
        hibEntry.setUid1(entry.getItem().getUid());
        updateDisplayName(hibEntry, entry);
        updateLastModifiedBy(hibEntry, entry);
        currentSession().save(hibEntry);
    }

    private void updateLastModifiedBy(HibEventLogEntry hibEntry, ItemEntry entry) {
        Item item = entry.getItem();
        if(item instanceof ContentItem)
            hibEntry.setStrval2(((ContentItem) item).getLastModifiedBy());

        if(hibEntry.getStrval2()==null) {
            if(entry.getUser()!=null)
                hibEntry.setStrval2(entry.getUser().getEmail());
            else
                hibEntry.setStrval2("ticket: anonymous");
        }
    }

    private void updateDisplayName(HibEventLogEntry hibEntry, ItemEntry entry) {
        Item item = entry.getItem();
        String displayName = item.getDisplayName();

        // handle case of "missing" displayName
        if(displayName==null) {
            if(item instanceof NoteItem) {
                NoteItem note = (NoteItem) item;
                if(note.getModifies()!=null)
                   displayName = note.getModifies().getDisplayName();
            }
        }

        // limit to 255 chars
        hibEntry.setStrval1(StringUtils.substring(displayName, 0, 255));
    }
}
