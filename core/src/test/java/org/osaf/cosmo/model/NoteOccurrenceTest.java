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
package org.osaf.cosmo.model;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test NoteOccurrenceItem
 */
public class NoteOccurrenceTest {

    private final EntityFactory factory = new MockEntityFactory();

    @Test
    public void testGenerateNoteOccurrence() throws Exception {

        MockNoteItem note = (MockNoteItem) factory.createNote();
        note.setUid("1");
        note.setCreationDate(new Date());
        note.setModifiedDate(new Date());
        note.setDisplayName("dn");
        note.setBody("body");
        note.addStamp(factory.createEventStamp(note));

        NoteOccurrence no = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070101"), note);
        NoteOccurrence no2 = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070102"), note);


        Assertions.assertEquals("1:20070101", no.getUid());
        Assertions.assertEquals(note, no.getMasterNote());
        Assertions.assertNotNull(no.getOccurrenceDate());

        Assertions.assertEquals(note.getCreationDate(), no.getCreationDate());
        Assertions.assertEquals("dn", no.getDisplayName());
        Assertions.assertEquals("body", no.getBody());

        Assertions.assertEquals(1, no.getStamps().size());

        Assertions.assertNotEquals(no, no2);
        Assertions.assertTrue(no.hashCode() != no2.hashCode());

        try {
            no.setUid("blah");
            Assertions.fail("able to perform unsupported op");
        } catch (UnsupportedOperationException e) {

        }
    }

}
