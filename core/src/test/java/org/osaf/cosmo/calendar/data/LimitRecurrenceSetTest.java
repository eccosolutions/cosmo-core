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
package org.osaf.cosmo.calendar.data;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Summary;

import java.io.FileInputStream;
import java.io.StringReader;

/**
 * Test limit-recurring-events output filter
 */
public class LimitRecurrenceSetTest extends TestCase {
    protected String baseDir = "src/test/resources/testdata/";

    public void testLimitRecurrenceSet() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "limit_recurr_test.ics");
        Calendar calendar = cb.build(fis);

        Assert.assertEquals(5, calendar.getComponents().getComponents("VEVENT").size());

        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060104T010000", tz);
        DateTime end = new DateTime("20060106T010000", tz);
        start.setUtc(true);
        end.setUtc(true);

        Period period = new Period(start, end);
        filter.setLimit(period);
        filter.setAllSubComponents();
        filter.setAllProperties();

        StringBuffer buffer = new StringBuffer();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());

        Calendar filterCal = cb.build(sr);

        ComponentList<CalendarComponent> comps = filterCal.getComponents();
        Assert.assertEquals(3, comps.getComponents("VEVENT").size());
        Assert.assertEquals(1, comps.getComponents("VTIMEZONE").size());

        // Make sure 3rd and 4th override are dropped
        ComponentList<VEvent> components = comps.getComponents("VEVENT");
        for (VEvent c : components) {
            Assert.assertNotSame("event 6 changed 3", c.getProperties().<Summary>getProperty("SUMMARY").getValue());
            Assert.assertNotSame("event 6 changed 4", c.getProperties().<Summary>getProperty("SUMMARY").getValue());
        }
    }

    public void testLimitFloatingRecurrenceSet() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "limit_recurr_float_test.ics");
        Calendar calendar = cb.build(fis);

        Assert.assertEquals(3, calendar.getComponents().getComponents("VEVENT").size());

        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060102T170000");
        DateTime end = new DateTime("20060104T170000");

        start.setUtc(true);
        end.setUtc(true);

        Period period = new Period(start, end);
        filter.setLimit(period);
        filter.setAllSubComponents();
        filter.setAllProperties();

        StringBuffer buffer = new StringBuffer();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());

        Calendar filterCal = cb.build(sr);

        Assert.assertEquals(2, filterCal.getComponents().getComponents("VEVENT").size());
        // Make sure 2nd override is dropped
        for (Component c : filterCal.getComponents().getComponents("VEVENT")) {
            Assert.assertNotSame("event 6 changed 2", c.getProperties().<Summary>getProperty("SUMMARY").getValue());
        }
    }

    public void testLimitRecurrenceSetThisAndFuture() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "limit_recurr_taf_test.ics");
        Calendar calendar = cb.build(fis);

        Assert.assertEquals(4, calendar.getComponents().getComponents("VEVENT").size());

        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060108T170000", tz);
        DateTime end = new DateTime("20060109T170000", tz);
        start.setUtc(true);
        end.setUtc(true);

        Period period = new Period(start, end);
        filter.setLimit(period);
        filter.setAllSubComponents();
        filter.setAllProperties();

        StringBuffer buffer = new StringBuffer();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());

        Calendar filterCal = cb.build(sr);

        Assert.assertEquals(2, filterCal.getComponents().getComponents("VEVENT").size());
        // Make sure 2nd and 3rd override are dropped
        for (Component c : filterCal.getComponents().getComponents("VEVENT")) {
            Assert.assertNotSame("event 6 changed", c.getProperties().<Summary>getProperty("SUMMARY").getValue());
            Assert.assertNotSame("event 6 changed 2", c.getProperties().<Summary>getProperty("SUMMARY").getValue());
        }
    }

}
