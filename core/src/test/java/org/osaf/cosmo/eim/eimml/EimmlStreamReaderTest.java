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
package org.osaf.cosmo.eim.eimml;

import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.assertj.core.api.Assertions;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.eim.EimRecordSet;

/**
 * Test Case for {@link EimmlStreamReader}.
 */
public class EimmlStreamReaderTest extends TestCase
    implements EimmlConstants {

    private TestHelper testHelper;

    protected void setUp() {
        testHelper = new TestHelper();
    }

    public void testReadChandlerUpdate() throws Exception {
        InputStreamReader in = new InputStreamReader(testHelper.
            getInputStream("eimml/chandler-update.xml"));
        EimmlStreamReader reader = new EimmlStreamReader(in);

        assertEquals("Collection uuid incorrect",
                     "6b97edbe-bb44-4a72-860c-d4905732e060",
                     reader.getCollectionUuid());
        assertNull("Collection name not null", reader.getCollectionName());

        Assertions.assertThat(reader.getCollectionHue()).isEqualTo(1);

        assertTrue("Did not find next recordset", reader.hasNext());

        EimRecordSet recordset = reader.nextRecordSet();
        assertNotNull("Next recordset is null", recordset);

        assertFalse("Reader thinks it has another recordset", reader.hasNext());
    }
}
