/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.util;

import org.junit.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Test TimeZoneUtils
 */
public class TimeZoneUtilsTest {

    @Test
    public void testLoadTimeZone() {
        VTimeZone vtz = TimeZoneUtils.getVTimeZone("America/Chicago");
        Assertions.assertNotNull(vtz);
        Assertions.assertEquals("America/Chicago", vtz.getTimeZoneId().getValue());
        Assertions.assertEquals(4, vtz.getObservances().size());

        vtz = TimeZoneUtils.getVTimeZone("blah");
        Assertions.assertNull(vtz);
    }

    @Test
    public void testLoadSimpleTimeZone() throws Exception {
        DateTime dt1 = new DateTime("20080101T100000", TimeZoneUtils.getTimeZone("America/Chicago"));
        DateTime dt2 = new DateTime("20060101T100000", TimeZoneUtils.getTimeZone("America/Chicago"));

        VTimeZone vtz1 = TimeZoneUtils.getSimpleVTimeZone("America/Chicago", dt1.getTime());
        Assertions.assertNotNull(vtz1);
        Assertions.assertEquals(2, vtz1.getObservances().size());

        VTimeZone vtz2 = TimeZoneUtils.getSimpleVTimeZone("America/Chicago", dt2.getTime());
        Assertions.assertNotNull(vtz2);
        Assertions.assertEquals(2, vtz2.getObservances().size());

        Assertions.assertFalse(vtz1.equals(vtz2));
    }
}
