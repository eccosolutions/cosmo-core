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
package org.osaf.cosmo.dao.hibernate.query;

import static org.osaf.cosmo.dao.hibernate.HibernateSessionSupport.getQueryString;

import jakarta.persistence.TypedQuery;
import org.junit.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.junit.Test;
import org.osaf.cosmo.dao.hibernate.AbstractHibernateDaoTestCase;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.filter.*;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibQName;

import java.util.Date;


/**
 * Test StandardItemQueryBuilder.
 */
public class StandardItemFilterProcessorTest extends AbstractHibernateDaoTestCase {

    StandardItemFilterProcessor queryBuilder = new StandardItemFilterProcessor();
    TimeZoneRegistry registry =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    public StandardItemFilterProcessorTest() {
        super();
    }

    @Test
    public void testUidQuery() {
        ItemFilter filter = new ItemFilter();
        filter.setUid(Restrictions.eq("abc"));
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.uid=:param0", getQueryString(query));
    }

    @Test
    public void testDisplayNameQuery() {
        ItemFilter filter = new ItemFilter();
        filter.setDisplayName(Restrictions.eq("test"));
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName=:param0", getQueryString(query));

        filter.setDisplayName(Restrictions.neq("test"));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName!=:param0", getQueryString(query));

        filter.setDisplayName(Restrictions.like("test"));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName like :param0", getQueryString(query));

        filter.setDisplayName(Restrictions.nlike("test"));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName not like :param0", getQueryString(query));

        filter.setDisplayName(Restrictions.isNull());
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName is null", getQueryString(query));

        filter.setDisplayName(Restrictions.ilike("test"));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) like :param0", getQueryString(query));

        filter.setDisplayName(Restrictions.nilike("test"));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) not like :param0", getQueryString(query));

    }

    @Test
    public void testParentQuery() {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.parentDetails pd where pd.primaryKey.collection=:parent", getQueryString(query));
    }

    @Test
    public void testDisplayNameAndParentQuery() {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.parentDetails pd where pd.primaryKey.collection=:parent and i.displayName=:param1", getQueryString(query));
    }

    @Test
    public void testContentItemQuery() {
        ContentItemFilter filter = new ContentItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibContentItem i join i.parentDetails pd where pd.primaryKey.collection=:parent", getQueryString(query));

    }

    @Test
    public void testNoteItemQuery() {
        NoteItemFilter filter = new NoteItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        filter.setBody(Restrictions.eq("body"));

        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibTextAttribute ta3 where pd.primaryKey.collection=:parent and i.displayName=:param1 and i.icalUid=:param2 and ta3.item=i and ta3.qname=:ta3qname and ta3.value=:param4", getQueryString(query));

        filter = new NoteItemFilter();
        filter.setIsModification(true);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where i.modifies is not null", getQueryString(query));

        filter.setIsModification(false);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where i.modifies is null", getQueryString(query));

        filter.setIsModification(null);

        filter.setHasModifications(true);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where size(i.modifications) > 0", getQueryString(query));

        filter.setHasModifications(false);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where size(i.modifications) = 0", getQueryString(query));

        filter =  new NoteItemFilter();
        filter.setMasterNoteItem(new HibNoteItem());
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where (i=:masterItem or i.modifies=:masterItem)", getQueryString(query));

        filter = new NoteItemFilter();
        Date date1 = new Date(1000);
        Date date2 = new Date(2000);
        filter.setReminderTime(Restrictions.between(date1,date2));
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i, HibTimestampAttribute tsa0 where tsa0.item=i and tsa0.qname=:tsa0qname and tsa0.value between :param1 and :param2", getQueryString(query));

    }

    @Test
    public void testEventStampQuery() {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        //filter.setBody("body");
        filter.getStampFilters().add(eventFilter);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and i.displayName=:param1 and es.item=i and i.icalUid=:param2", getQueryString(query));

        eventFilter.setIsRecurring(true);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and i.displayName=:param1 and es.item=i and (es.timeRangeIndex.isRecurring=true or i.modifies is not null) and i.icalUid=:param2", getQueryString(query));
    }

    @Test
    public void testEventStampTimeRangeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        eventFilter.setPeriod(period);
        eventFilter.setTimezone(registry.getTimeZone("America/Chicago"));

        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.getStampFilters().add(eventFilter);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and es.item=i and ( (es.timeRangeIndex.isFloating=true and es.timeRangeIndex.startDate < '20070201T040000' and es.timeRangeIndex.endDate > '20070101T040000') or (es.timeRangeIndex.isFloating=false and es.timeRangeIndex.startDate < '20070201T100000Z' and es.timeRangeIndex.endDate > '20070101T100000Z') or (es.timeRangeIndex.startDate=es.timeRangeIndex.endDate and (es.timeRangeIndex.startDate='20070101T040000' or es.timeRangeIndex.startDate='20070101T100000Z')))", getQueryString(query));
    }

    @Test
    public void testBasicStampQuery() {
        NoteItemFilter filter = new NoteItemFilter();
        StampFilter missingFilter = new StampFilter();
        missingFilter.setStampClass(EventStamp.class);
        filter.getStampFilters().add(missingFilter);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where exists (select s.id from HibStamp s where s.item=i and s.class=HibEventStamp)", getQueryString(query));
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where not exists (select s.id from HibStamp s where s.item=i and s.class=HibEventStamp)", getQueryString(query));
    }

    @Test
    public void testBasicAttributeQuery() {
        NoteItemFilter filter = new NoteItemFilter();
        AttributeFilter missingFilter = new AttributeFilter();
        missingFilter.setQname(new HibQName("ns","name"));
        filter.getAttributeFilters().add(missingFilter);
        TypedQuery<?> query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where exists (select a.id from HibAttribute a where a.item=i and a.qname=:param0)", getQueryString(query));
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(getSession(), filter);
        Assert.assertEquals("select i from HibNoteItem i where not exists (select a.id from HibAttribute a where a.item=i and a.qname=:param0)", getQueryString(query));
    }

}
