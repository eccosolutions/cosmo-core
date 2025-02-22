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
package org.osaf.cosmo.eim.schema.text;

import java.text.ParseException;
import java.time.temporal.TemporalAmount;

import org.junit.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

import net.fortuna.ical4j.model.TemporalAmountAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DurationFormatTest extends TestCase {
    private static final Log log = LogFactory.getLog(DurationFormatTest.class);

    public void testGetValue() {
        // iCal's Duration() internally sets a TemporalAmountAdapter, which getValue() calls toString() on.
        // The toString() is flawed in earlier iCal versions, so we do a quick test.
        // NB iCal ends up calling getValue from usual deepCopy work (Property class in this case)

        // Test something with days and minutes - the minutes get dropped in the previous iCal version.
        assertEquals("P1DT3H40M", TemporalAmountAdapter.parse("P1DT3H40M").toString());
    }

    public void testFormat() throws Exception {
        DurationFormat df = DurationFormat.getInstance();
        TemporalAmount dur = null;

        dur = makeDur("20070512", "20070513");
        assertEquals("P1D", df.format(dur));

        dur = makeDur("20070512T103000", "20070513T103000");
        assertEquals("P1D", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T113000");
        assertEquals("PT1H", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T103500");
        assertEquals("PT5M", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T113500");
        assertEquals("PT1H5M", df.format(dur));

        dur = makeDur("20070512T103000", "20070512T103030");
        assertEquals("PT30S", df.format(dur));
    }

    public void testParse() throws Exception {
        DurationFormat df = DurationFormat.getInstance();

        Assert.assertEquals(df.parse("P5W").toString(), "P35D");
        Assert.assertEquals(df.parse("P5D").toString(), "P5D");
        Assert.assertEquals(df.parse("PT5H").toString(), "PT5H");
        Assert.assertEquals(df.parse("P5DT5H").toString(), "PT125H");
        Assert.assertEquals(df.parse("PT5H5M").toString(), "PT5H5M");
        Assert.assertEquals(df.parse("PT5M").toString(), "PT5M");
        Assert.assertEquals(df.parse("PT5M5S").toString(), "PT5M5S");
        Assert.assertEquals(df.parse("PT5S").toString(), "PT5S");

        try {
            df.parse("P");
            Assert.fail("able to parse invalid duration");
        } catch (ParseException e) {
        }

        try {
            df.parse("P5H");
            Assert.fail("able to parse invalid duration");
        } catch (ParseException e) {
        }

        try {
            df.parse("P0M");
            Assert.fail("able to parse invalid duration");
        } catch (ParseException e) {
        }

        try {
            df.parse("PT5M5H");
            Assert.fail("able to parse invalid duration");
        } catch (ParseException e) {
        }
    }

    private TemporalAmount makeDur(String start,
                                   String end)
        throws Exception {
        if (start.indexOf("T") > 0)
            return TemporalAmountAdapter.fromDateRange(new DateTime(start), new DateTime(end)).getDuration();
        return TemporalAmountAdapter.fromDateRange(new Date(start), new Date(end)).getDuration();
    }
}
