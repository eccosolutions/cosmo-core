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

import java.util.Date;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.hibernate.Query;
import org.osaf.cosmo.dao.hibernate.AbstractHibernateDaoTestCase;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.AttributeFilter;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.Restrictions;
import org.osaf.cosmo.model.filter.StampFilter;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibQName;


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

    public void testUidQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setUid(Restrictions.eq("abc"));
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.uid=:param0", query.getQueryString());
    }
    
    public void testDisplayNameQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setDisplayName(Restrictions.eq("test"));
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.displayName=:param0", query.getQueryString());
    
        filter.setDisplayName(Restrictions.neq("test"));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.displayName!=:param0", query.getQueryString());
    
        filter.setDisplayName(Restrictions.like("test"));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.displayName like :param0", query.getQueryString());
        
        filter.setDisplayName(Restrictions.nlike("test"));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.displayName not like :param0", query.getQueryString());
    
        filter.setDisplayName(Restrictions.isNull());
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where i.displayName is null", query.getQueryString());
    
        filter.setDisplayName(Restrictions.ilike("test"));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) like :param0", query.getQueryString());
    
        filter.setDisplayName(Restrictions.nilike("test"));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) not like :param0", query.getQueryString());
    
    }
    
    public void testParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i join i.parentDetails pd where pd.primaryKey.collection=:parent", query.getQueryString());
    }
    
    public void testDisplayNameAndParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibItem i join i.parentDetails pd where pd.primaryKey.collection=:parent and i.displayName=:param1", query.getQueryString());
    }
    
    public void testContentItemQuery() throws Exception {
        ContentItemFilter filter = new ContentItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_DONE));
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibContentItem i join i.parentDetails pd where pd.primaryKey.collection=:parent and i.triageStatus.code=:param1", query.getQueryString());
    
        filter.setTriageStatusCode(Restrictions.isNull());
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibContentItem i join i.parentDetails pd where pd.primaryKey.collection=:parent and i.triageStatus.code is null", query.getQueryString());
        
        filter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_DONE));
        filter.addOrderBy(ContentItemFilter.ORDER_BY_TRIAGE_STATUS_RANK_ASC);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibContentItem i join i.parentDetails pd where pd.primaryKey.collection=:parent and i.triageStatus.code=:param1 order by i.triageStatus.rank", query.getQueryString());
    }
    
    public void testNoteItemQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        filter.setBody(Restrictions.eq("body"));
        filter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_DONE));
        
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibTextAttribute ta4 where pd.primaryKey.collection=:parent and i.displayName=:param1 and i.triageStatus.code=:param2 and i.icalUid=:param3 and ta4.item=i and ta4.qname=:ta4qname and ta4.value=:param5", query.getQueryString());
        
        filter = new NoteItemFilter();
        filter.setIsModification(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where i.modifies is not null", query.getQueryString());
       
        filter.setIsModification(false);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where i.modifies is null", query.getQueryString());
       
        filter.setIsModification(null);
        
        filter.setHasModifications(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where size(i.modifications) > 0", query.getQueryString());
        
        filter.setHasModifications(false);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where size(i.modifications) = 0", query.getQueryString());
    
        filter =  new NoteItemFilter();
        filter.setMasterNoteItem(new HibNoteItem());
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where (i=:masterItem or i.modifies=:masterItem)", query.getQueryString());
    
        filter = new NoteItemFilter();
        Date date1 = new Date(1000);
        Date date2 = new Date(2000);
        filter.setReminderTime(Restrictions.between(date1,date2));
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i, HibTimestampAttribute tsa0 where tsa0.item=i and tsa0.qname=:tsa0qname and tsa0.value between :param1 and :param2", query.getQueryString());
    
    }
    
    public void testEventStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        //filter.setBody("body");
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and i.displayName=:param1 and es.item=i and i.icalUid=:param2", query.getQueryString());
    
        eventFilter.setIsRecurring(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and i.displayName=:param1 and es.item=i and (es.timeRangeIndex.isRecurring=true or i.modifies is not null) and i.icalUid=:param2", query.getQueryString());
    }
    
    public void testEventStampTimeRangeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        eventFilter.setPeriod(period);
        eventFilter.setTimezone(registry.getTimeZone("America/Chicago"));
        
        CollectionItem parent = new HibCollectionItem();
        filter.setParent(parent);
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i join i.parentDetails pd, HibBaseEventStamp es where pd.primaryKey.collection=:parent and es.item=i and ( (es.timeRangeIndex.isFloating=true and es.timeRangeIndex.startDate < '20070201T040000' and es.timeRangeIndex.endDate > '20070101T040000') or (es.timeRangeIndex.isFloating=false and es.timeRangeIndex.startDate < '20070201T100000Z' and es.timeRangeIndex.endDate > '20070101T100000Z') or (es.timeRangeIndex.startDate=es.timeRangeIndex.endDate and (es.timeRangeIndex.startDate='20070101T040000' or es.timeRangeIndex.startDate='20070101T100000Z')))", query.getQueryString());
    }
    
    public void testBasicStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        StampFilter missingFilter = new StampFilter();
        missingFilter.setStampClass(EventStamp.class);
        filter.getStampFilters().add(missingFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where exists (select s.id from HibStamp s where s.item=i and s.class=HibEventStamp)", query.getQueryString());
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where not exists (select s.id from HibStamp s where s.item=i and s.class=HibEventStamp)", query.getQueryString());
    }
    
    public void testBasicAttributeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        AttributeFilter missingFilter = new AttributeFilter();
        missingFilter.setQname(new HibQName("ns","name"));
        filter.getAttributeFilters().add(missingFilter);
        Query query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where exists (select a.id from HibAttribute a where a.item=i and a.qname=:param0)", query.getQueryString());
        missingFilter.setMissing(true);
        query =  queryBuilder.buildQuery(session, filter);
        Assert.assertEquals("select i from HibNoteItem i where not exists (select a.id from HibAttribute a where a.item=i and a.qname=:param0)", query.getQueryString());
    }

}
