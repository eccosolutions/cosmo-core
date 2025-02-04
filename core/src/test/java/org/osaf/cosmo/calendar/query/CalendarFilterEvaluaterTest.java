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
package org.osaf.cosmo.calendar.query;

import java.io.InputStream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test CalendarFilterEvaluater
 */
public class CalendarFilterEvaluaterTest {

    @BeforeEach
    protected void setUp() throws Exception {
        System.setProperty("ical4j.unfolding.relaxed", "true");
        System.setProperty("ical4j.parsing.relaxed", "true");
        System.setProperty("ical4j.validation.relaxed", "true");
    }

    @AfterEach
    protected void tearDown() throws Exception {
        System.clearProperty("ical4j.unfolding.relaxed");
        System.clearProperty("ical4j.parsing.relaxed");
        System.clearProperty("ical4j.validation.relaxed");
    }

    @Test
    public void testEvaluateFilterPropFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("cal1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        TextMatchFilter textFilter = new TextMatchFilter("Visible");
        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("ViSiBle");
        textFilter.setCollation(textFilter.COLLATION_OCTET);
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        textFilter.setCollation(null);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("XXX");
        textFilter.setNegateCondition(true);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        propFilter.setTextMatchFilter(null);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        propFilter.setName("RRULE");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateFilterParamFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("cal1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("DTSTART");
        ParamFilter paramFilter = new ParamFilter("VALUE");
        TextMatchFilter textFilter = new TextMatchFilter("DATE-TIME");
        paramFilter.setTextMatchFilter(textFilter);
        propFilter.getParamFilters().add(paramFilter);
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("XXX");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        textFilter.setNegateCondition(true);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        paramFilter.setTextMatchFilter(null);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        paramFilter.setName("BOGUS");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        paramFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateFilterEventTimeRangeFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("cal1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        DateTime start = new DateTime("20050816T115000Z");
        DateTime end = new DateTime("20050916T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20050818T115000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateFilterRecurringEventTimeRangeFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("eventwithtimezone1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        DateTime start = new DateTime("20070514T115000Z");
        DateTime end = new DateTime("20070516T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20070515T205000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateFilterPropertyTimeRangeFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("cal1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("DTSTAMP");

        DateTime start = new DateTime("20060517T115000Z");
        DateTime end = new DateTime("20060717T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);

        propFilter.setTimeRangeFilter(timeRangeFilter);
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20060717T115000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateComplicated() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("cal1.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        DateTime start = new DateTime("20050816T115000Z");
        DateTime end = new DateTime("20050916T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        TextMatchFilter textFilter = new TextMatchFilter("Visible");
        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        PropertyFilter propFilter2 = new PropertyFilter("DTSTART");
        ParamFilter paramFilter2 = new ParamFilter("VALUE");
        TextMatchFilter textFilter2 = new TextMatchFilter("DATE-TIME");
        paramFilter2.setTextMatchFilter(textFilter2);
        propFilter2.getParamFilters().add(paramFilter2);

        DateTime start2 = new DateTime("20060517T115000Z");
        DateTime end2 = new DateTime("20060717T115000Z");

        Period period2 = new Period(start, end);
        TimeRangeFilter timeRangeFilter2 = new TimeRangeFilter(period2);
        propFilter2.setTimeRangeFilter(timeRangeFilter2);

        eventFilter.getPropFilters().add(propFilter2);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        // change one thing
        paramFilter2.setName("XXX");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateVAlarmFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("event_with_alarm.ics");


        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        filter.setFilter(compFilter);

        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        ComponentFilter alarmFilter = new ComponentFilter("VALARM");
        PropertyFilter propFilter = new PropertyFilter("ACTION");
        TextMatchFilter textMatch = new TextMatchFilter("AUDIO");
        propFilter.setTextMatchFilter(textMatch);

        compFilter.getComponentFilters().add(eventFilter);
        eventFilter.getComponentFilters().add(alarmFilter);
        alarmFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textMatch.setValue("EMAIL");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        alarmFilter.getPropFilters().clear();
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        // time-range filter on VALARM

        // find alarm relative to start
        DateTime start = new DateTime("20060101T220000Z");
        DateTime end = new DateTime("20060101T230000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        alarmFilter.setTimeRangeFilter(timeRangeFilter);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        // find alarm relative to end
        start = new DateTime("20060101T050000Z");
        end = new DateTime("20060101T190000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        // find absolute repeating alarm
        start = new DateTime("20051230T050000Z");
        end = new DateTime("20051230T080000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        // find no alarms
        start = new DateTime("20060101T020000Z");
        end = new DateTime("20060101T030000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));

        alarmFilter.setTimeRangeFilter(null);
        alarmFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateFilterPropFilterAgainstException() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("event_with_exception.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("DESCRIPTION");
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateVJournalFilterPropFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("vjournal.ics");


        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VJOURNAL");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        TextMatchFilter textFilter = new TextMatchFilter("Staff");
        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("bogus");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateVToDoFilterPropFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("vtodo.ics");


        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VTODO");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        TextMatchFilter textFilter = new TextMatchFilter("Income");
        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("bogus");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateVToDoTimeRangeFilter() throws Exception {

        Calendar calendar1 = getCalendar("vtodo/vtodo.ics");
        Calendar calendar2 = getCalendar("vtodo/vtodo_due_only.ics");

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter vtodoFilter = new ComponentFilter("VTODO");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(vtodoFilter);

        // Verify VTODO that has DTSTART matches
        DateTime start = new DateTime("19970414T133000Z");
        DateTime end = new DateTime("19970416T133000Z");
        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        vtodoFilter.setTimeRangeFilter(timeRangeFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar1, filter));

        // Verify VTODO that has DTSTART doesn't match
        start = new DateTime("19970420T133000Z");
        end = new DateTime("19970421T133000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertFalse(evaluater.evaluate(calendar1, filter));

        // Verify VTODO that has DUE doesn't match
        Assertions.assertFalse(evaluater.evaluate(calendar2, filter));

        // Verify VTODO that has DUE matches
        start = new DateTime("20080401T133000Z");
        end = new DateTime("20080421T133000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        Assertions.assertTrue(evaluater.evaluate(calendar2, filter));
    }

    @Test
    public void testEvaluateVFreeBusyFilterFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("vfreebusy.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter vfbFilter = new ComponentFilter("VFREEBUSY");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(vfbFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        PropertyFilter propFilter = new PropertyFilter("ORGANIZER");
        TextMatchFilter textFilter = new TextMatchFilter("Joe");
        propFilter.setTextMatchFilter(textFilter);
        vfbFilter.getPropFilters().add(propFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("bogus");
        Assertions.assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    public void testEvaluateVFreeBusyFilterFilterTimeRange() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar1 = getCalendar("vfreebusy.ics");
        Calendar calendar2 = getCalendar("vfreebusy_no_dtstart.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter vfbFilter = new ComponentFilter("VFREEBUSY");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(vfbFilter);

        DateTime start = new DateTime("20060102T115000Z");
        DateTime end = new DateTime("20060109T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        vfbFilter.setTimeRangeFilter(timeRangeFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar1, filter));
        Assertions.assertTrue(evaluater.evaluate(calendar2, filter));

        start = new DateTime("20070102T115000Z");
        end = new DateTime("20070109T115000Z");

        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);

        Assertions.assertFalse(evaluater.evaluate(calendar1, filter));
        Assertions.assertFalse(evaluater.evaluate(calendar2, filter));

    }

    @Test
    public void testEvaluateVAvailabilityFilter() throws Exception {

        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        Calendar calendar = getCalendar("vavailability.ics");

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter vfbFilter = new ComponentFilter("VAVAILABILITY");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(vfbFilter);

        Assertions.assertTrue(evaluater.evaluate(calendar, filter));
    }

    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        Calendar calendar = cb.build(in);
        return calendar;
    }
}
