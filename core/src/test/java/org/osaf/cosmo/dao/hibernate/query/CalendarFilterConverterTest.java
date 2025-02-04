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
package org.osaf.cosmo.dao.hibernate.query;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.FilterCriteria;
import org.osaf.cosmo.model.filter.FilterExpression;
import org.osaf.cosmo.model.filter.ILikeExpression;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.LikeExpression;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.StampFilter;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;


/**
 * Test CalendarFilterConverter.
 */
public class CalendarFilterConverterTest {

    CalendarFilterConverter converter = new CalendarFilterConverter();

    public CalendarFilterConverterTest() {
        super();
    }

    @Test
    public void testTranslateItemToFilter() throws Exception {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setUid("calendar");
        CalendarFilter calFilter = new CalendarFilter();
        ComponentFilter rootComp = new ComponentFilter();
        rootComp.setName("VCALENDAR");
        calFilter.setFilter(rootComp);
        ComponentFilter eventComp = new ComponentFilter();
        eventComp.setName("VEVENT");
        rootComp.getComponentFilters().add(eventComp);

        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        eventComp.setTimeRangeFilter(timeRangeFilter);

        PropertyFilter uidFilter = new PropertyFilter();
        uidFilter.setName("UID");
        TextMatchFilter uidMatch = new TextMatchFilter();
        uidMatch.setValue("uid");
        uidMatch.setCaseless(false);
        uidFilter.setTextMatchFilter(uidMatch);
        eventComp.getPropFilters().add(uidFilter);

        PropertyFilter summaryFilter = new PropertyFilter();
        summaryFilter.setName("SUMMARY");
        TextMatchFilter summaryMatch = new TextMatchFilter();
        summaryMatch.setValue("summary");
        summaryMatch.setCaseless(false);
        summaryFilter.setTextMatchFilter(summaryMatch);
        eventComp.getPropFilters().add(summaryFilter);

        PropertyFilter descFilter = new PropertyFilter();
        descFilter.setName("DESCRIPTION");
        TextMatchFilter descMatch = new TextMatchFilter();
        descMatch.setValue("desc");
        descMatch.setCaseless(true);
        descFilter.setTextMatchFilter(descMatch);
        eventComp.getPropFilters().add(descFilter);

        ItemFilter itemFilter = converter.translateToItemFilter(calendar, calFilter);

        Assertions.assertTrue(itemFilter instanceof NoteItemFilter);
        NoteItemFilter noteFilter = (NoteItemFilter) itemFilter;
        Assertions.assertEquals(calendar.getUid(), noteFilter.getParent().getUid());
        Assertions.assertTrue(noteFilter.getDisplayName() instanceof LikeExpression);
        verifyFilterExpressionValue(noteFilter.getDisplayName(), "summary");
        Assertions.assertTrue(noteFilter.getIcalUid() instanceof LikeExpression);
        verifyFilterExpressionValue(noteFilter.getIcalUid(), "uid");
        Assertions.assertTrue(noteFilter.getBody() instanceof ILikeExpression);
        verifyFilterExpressionValue(noteFilter.getBody(), "desc");

        EventStampFilter sf = (EventStampFilter) noteFilter.getStampFilter(EventStampFilter.class);
        Assertions.assertNotNull(sf);
        Assertions.assertNotNull(sf.getPeriod());
        Assertions.assertEquals(sf.getPeriod().getStart().toString(), "20070101T100000Z");
        Assertions.assertEquals(sf.getPeriod().getEnd().toString(), "20070201T100000Z");
    }

    @Test
    public void testGetFirstPassFilter() {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setUid("calendar");
        CalendarFilter calFilter = new CalendarFilter();
        ComponentFilter rootComp = new ComponentFilter();
        rootComp.setName("VCALENDAR");
        calFilter.setFilter(rootComp);
        ComponentFilter taskComp = new ComponentFilter();
        taskComp.setName("VTODO");
        rootComp.getComponentFilters().add(taskComp);

        try {
            converter.translateToItemFilter(calendar, calFilter);
            Assertions.fail("shouldn't get here");
        } catch(IllegalArgumentException e) {}


        ItemFilter itemFilter = converter.getFirstPassFilter(calendar, calFilter);
        Assertions.assertNotNull(itemFilter);
        Assertions.assertTrue(itemFilter instanceof NoteItemFilter);
        NoteItemFilter noteFilter = (NoteItemFilter) itemFilter;

        Assertions.assertFalse(noteFilter.getIsModification().booleanValue());
        Assertions.assertEquals(1, noteFilter.getStampFilters().size());

        StampFilter sf = noteFilter.getStampFilters().get(0);
        Assertions.assertEquals(EventStamp.class, sf.getStampClass());
        Assertions.assertEquals(true, sf.isMissing());
    }

    private void verifyFilterExpressionValue(FilterCriteria fc, Object value) {
        FilterExpression fe = (FilterExpression) fc;
        Assertions.assertTrue(fe.getValue().equals(value));
    }

}
