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
package org.osaf.cosmo.calendar;

import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test RecurrenceExpander.
 *
 */
public class RecurrenceExpanderTest extends TestCase {
    
    private static final Log log = LogFactory.getLog(RecurrenceExpanderTest.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    public void testRecurrenceExpanderAllDay() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
      
        Calendar calendar = getCalendar("allday_recurring1.ics");
        
        Date[] range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101", range[0].toString());
        Assert.assertEquals("20070120", range[1].toString());
        
        calendar = getCalendar("allday_recurring2.ics");
        
        range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101", range[0].toString());
        Assert.assertNull(range[1]);
    }
    
    public void testRecurrenceExpanderFloating() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("floating_recurring1.ics");
        
        Date[] range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20070119T120000", range[1].toString());
        
        calendar = getCalendar("floating_recurring2.ics");
        range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertNull(range[1]);
    }
    
    public void testRecurrenceExpanderTimezone() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("tz_recurring1.ics");
        
        Date[] range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20070119T120000", range[1].toString());
        
        Assert.assertEquals(((DateTime) range[0]).getTimeZone().getID(), "America/Chicago");
        Assert.assertEquals(((DateTime) range[1]).getTimeZone().getID(), "America/Chicago");
        
        calendar = getCalendar("tz_recurring2.ics");
        
        range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertNull(range[1]);
        
        Assert.assertEquals(((DateTime) range[0]).getTimeZone().getID(), "America/Chicago");
    }
    
    public void testRecurrenceExpanderLongEvent() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("tz_recurring3.ics");
        
        Date[] range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20091231T120000", range[1].toString());
    }
    
    public void testRecurrenceExpanderRDates() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("floating_recurring3.ics");
        
        Date[] range = expander.calculateRecurrenceRange(calendar);
        
        Assert.assertEquals("20061212T100000", range[0].toString());
        Assert.assertEquals("20101212T120000", range[1].toString());
    }
    
    public void testRecurrenceExpanderSingleOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("floating_recurring4.ics");
        
        InstanceList instances = expander.getOcurrences(calendar, new DateTime("20080101T100000"), new DateTime("20080101T100001"), null);
        
        Assert.assertEquals(1, instances.size());
    }
    
    
    public void testIsOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        Calendar calendar = getCalendar("floating_recurring3.ics");
        
        
        Assert.assertTrue(expander.isOccurrence(calendar, new DateTime("20070102T100000")));
        Assert.assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T110000")));
        Assert.assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T100001")));
    
        // test DATE
        calendar = getCalendar("allday_recurring3.ics");
        
        Assert.assertTrue(expander.isOccurrence(calendar, new Date("20070101")));
        Assert.assertFalse(expander.isOccurrence(calendar, new Date("20070102")));
        Assert.assertTrue(expander.isOccurrence(calendar, new Date("20070108")));
        
        // test DATETIME with timezone
        calendar = getCalendar("tz_recurring3.ics");
        TimeZone ctz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        Assert.assertTrue(expander.isOccurrence(calendar, new DateTime("20070102T100000", ctz)));
        Assert.assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T110000", ctz)));
        Assert.assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T100001", ctz)));
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream("expander/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }        
        Calendar calendar = cb.build(in);
        return calendar;
    }
    
}
