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
package org.osaf.cosmo.calendar;

import java.io.File;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.ParamFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test CalendarQueryFilter
 */
public class CalendarQueryFilterTest {
    protected String baseDir = "src/test/resources/testdata/queries";

    @Test
    public void testComponentFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test1.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
    }

    @Test
    public void testComponentFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test4.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getIsNotDefinedFilter());
    }

    @Test
    public void testPropertyFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test2.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        Assertions.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);

        Assertions.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assertions.assertNotNull(textMatch);
        Assertions.assertEquals("ABC",textMatch.getValue());
    }

    @Test
    public void testPropertyFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test5.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        Assertions.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);

        Assertions.assertEquals("SUMMARY", propFilter.getName());
        Assertions.assertNotNull(propFilter.getIsNotDefinedFilter());
    }

    @Test
    public void testParamFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test3.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        Assertions.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);

        Assertions.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assertions.assertNotNull(textMatch);
        Assertions.assertEquals("ABC",textMatch.getValue());

        Assertions.assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        Assertions.assertEquals("PARAM1", paramFilter.getName());

        textMatch = paramFilter.getTextMatchFilter();
        Assertions.assertNotNull(textMatch);
        Assertions.assertEquals("DEF", textMatch.getValue());
        Assertions.assertTrue(textMatch.isCaseless());
    }

    @Test
    public void testParamFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test6.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        Assertions.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);

        Assertions.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assertions.assertNotNull(textMatch);
        Assertions.assertEquals("ABC",textMatch.getValue());

        Assertions.assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        Assertions.assertEquals("PARAM1", paramFilter.getName());


        Assertions.assertNotNull(paramFilter.getIsNotDefinedFilter());
    }

    @Test
    public void testMultiplePropFilters() throws Exception {
        Element element = parseFile(new File(baseDir + "/test7.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();

        Assertions.assertNotNull(compFilter);
        Assertions.assertEquals("VCALENDAR", compFilter.getName());
        Assertions.assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);

        Assertions.assertEquals("VEVENT", compFilter.getName());
        Assertions.assertNotNull(compFilter.getTimeRangeFilter());

        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assertions.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assertions.assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        Assertions.assertEquals(2, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);

        Assertions.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assertions.assertNotNull(textMatch);
        Assertions.assertEquals("ABC",textMatch.getValue());

        propFilter = (PropertyFilter) compFilter.getPropFilters().get(1);
        Assertions.assertEquals("DESCRIPTION", propFilter.getName());
        Assertions.assertNotNull(propFilter.getIsNotDefinedFilter());
    }

    @Test
    public void testComponentFilterError() throws Exception {

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test4.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test5.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test6.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test7.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test8.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}

    }

    @Test
    public void testPropertyFilterError() throws Exception {

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test9.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
    }

    @Test
    public void testParamFilterError() throws Exception {

        try
        {
            Element element = parseFile(new File(baseDir + "/error-test10.xml"));
            CalendarFilter filter = new CalendarFilter(element);
            Assertions.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
    }

    protected Element parseFile(File file) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(file);
        return (Element) dom.getFirstChild();
    }

}
