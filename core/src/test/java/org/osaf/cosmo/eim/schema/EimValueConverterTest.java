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
package org.osaf.cosmo.eim.schema;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EimValueConverterTest {
    private static final Log log =
        LogFactory.getLog(EimValueConverterTest.class);

    @Test
    public void testToICalRecurs() throws Exception {
        String recur0 = "FREQ=YEARLY;INTERVAL=2;BYMONTH=1;BYDAY=SU;BYHOUR=8,9;BYMINUTE=30";
        String recur1 = "FREQ=DAILY;COUNT=10;INTERVAL=2";
        String test = recur0 + ":" + recur1;

        List recurs = EimValueConverter.toICalRecurs(test);
        assertEquals("wrong number of recurs", 2, recurs.size());
    }

    @Test
    public void testFromICalRecurs() throws Exception {
        ArrayList recurs = new ArrayList();
        String recur0 = "FREQ=YEARLY;INTERVAL=2;BYMONTH=1;BYDAY=SU;BYHOUR=8,9;BYMINUTE=30";
        recurs.add(new Recur(recur0));
        String recur1 = "FREQ=DAILY;COUNT=10;INTERVAL=2";
        recurs.add(new Recur(recur1));

        String test = recurs.get(0) + ":" + recurs.get(1);
        String result = EimValueConverter.fromICalRecurs(recurs);
        assertNotNull("null result", result);
        assertEquals("wrong serialized recur list", test, result);
    }

    // NB TemporalAmountAdapter handles the awkwardness now that Dur is deprecated
    // and we need to use Period OR Duration - but not sure which.
    // The return of these methods is slightly different to Dur, but they are the suggested replacement.
    @Test
    public void testToIcalTrigger() throws Exception {
        String strTrigger = ";RELATED=END:P7W";
        Trigger trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals(";RELATED=END:P49D", EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = "PT15M";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = "-PT15M15S";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals("PT-15M-15S", EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = ";VALUE=DATE-TIME:19970317T133000Z";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = "P15W";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals("P105D", EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = "-P2D";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        assertEquals("P-2D", EimValueConverter.fromIcalTrigger(trigger));

        strTrigger = ";RELATED=END:19970317T133000Z";
        try {
            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }

        strTrigger = "foo";
        try {
            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }

        strTrigger = ";VALUE=DATE:-PT15M";
        try {

            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }

        strTrigger = ";VALUE=DATE-TIME:-PT15M";
        try {

            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }
    }
}
