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
package org.osaf.cosmo.dav.caldav.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavCollectionBase;
import org.osaf.cosmo.dav.impl.DavFile;
import org.osaf.cosmo.dav.impl.mock.MockCalendarResource;
import org.osaf.cosmo.dav.report.BaseReportTestCase;

/**
 * Test case for <code>QueryReport</code>.
 */
public class QueryReportTest extends BaseReportTestCase {
    private static final Log log =
        LogFactory.getLog(QueryReportTest.class);

    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("query");

        QueryReport report = new QueryReport();
        try {
            report.init(dcc, makeReportInfo("freebusy1.xml", DEPTH_1));
            fail("Non-query report info initalized");
        } catch (Exception e) {}
    }

    public void testQuerySelfCalendarResource() throws Exception {
        MockCalendarResource test = (MockCalendarResource)
            makeTarget(MockCalendarResource.class);
        test.setMatchFilters(true);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Self query failed for calendar resource");
        }
        assertTrue("Calendar resource not found in results",
                   report.getResults().contains(test));
    }

    public void testQuerySelfNonCalendarResource() throws Exception {
        DavResource test = makeTarget(DavFile.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
            fail("Self query succeeded for non-calendar resource");
        } catch (UnprocessableEntityException e) {}
    }

    public void testQuerySelfCalendarCollection() throws Exception {
        DavResource test = makeTarget(DavCalendarCollection.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Self query failed for calendar collection");
        }
    }

    public void testQuerySelfNonCalendarCollection() throws Exception {
        DavResource test = makeTarget(DavCollectionBase.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Self query failed for non-calendar collection");
        }
    }

    public void testQueryChildrenCalendarCollection() throws Exception {
        DavCollection test = (DavCollection)
            makeTarget(DavCalendarCollection.class);
        QueryReport report = makeReport("query1.xml", DEPTH_1, test);
        try {
            report.doQueryChildren(test);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Children query failed for calendar collection");
        }
    }

    public void testQueryChildrenNonCalendarCollection() throws Exception {
        DavCollection test = (DavCollection)
            makeTarget(DavCollectionBase.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQueryChildren(test);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Children query failed for non-calendar collection");
        }
    }

    private QueryReport makeReport(String reportXml,
                                   int depth,
                                   DavResource target)
        throws Exception {
        return (QueryReport)
            super.makeReport(QueryReport.class, reportXml, depth, target);
    }
}
