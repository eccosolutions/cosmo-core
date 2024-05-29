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
package org.osaf.cosmo.eim.schema.contentitem;

import org.junit.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.model.mock.MockQName;
import org.osaf.cosmo.model.mock.MockStringAttribute;

import java.util.List;

/**
 * Test Case for {@link ContentItemGenerator}.
 */
public class ContentItemGeneratorTest extends BaseGeneratorTestCase
    implements ContentItemConstants {
    private static final Log log =
        LogFactory.getLog(ContentItemGeneratorTest.class);

    public void testGenerateRecord() {
        String uid = "deadbeef";
        String name = "3inchesofblood";
        String displayName = "3 Inches of Blood";
        Boolean sent = Boolean.TRUE;
        Boolean needsReply = Boolean.TRUE;

        ContentItem contentItem = new MockNoteItem();
        contentItem.setUid(uid);
        contentItem.setName(name);
        contentItem.setDisplayName(displayName);
        contentItem.setSent(sent);
        contentItem.setNeedsReply(needsReply);
//        contentItem.setClientCreationDate(clientCreationDate);

        StringAttribute unknownAttr = makeStringAttribute();
        contentItem.addAttribute(unknownAttr);

        ContentItemGenerator generator = new ContentItemGenerator(contentItem);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_ITEM, NS_ITEM);
        checkUuidKey(record.getKey(), uid);

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 5, fields.size());

        EimRecordField titleField = fields.get(0);
        checkTextField(titleField, FIELD_TITLE, displayName);

        EimRecordField sentField = fields.get(1);
        checkBooleanField(sentField, FIELD_HAS_BEEN_SENT, sent);

        EimRecordField needsReplyField = fields.get(2);
        checkBooleanField(needsReplyField, FIELD_NEEDS_REPLY, needsReply);

        EimRecordField unknownField = fields.get(4);
        checkTextField(unknownField, unknownAttr.getName(),
                       unknownAttr.getValue());
    }

    public void testInactiveNotDeleted() {
        // inactive items are not deleted via item records but via
        // recordset
        ContentItem contentItem = new MockNoteItem();
        contentItem.setIsActive(false);

        ContentItemGenerator generator = new ContentItemGenerator(contentItem);

        checkNotDeleted(generator.generateRecords().get(0));
    }

    public void testGenerateMissingField() {
        NoteItem modification = new MockNoteItem();
        NoteItem parent = new MockNoteItem();
        modification.setUid("1");
        parent.setDisplayName("test");
        modification.setDisplayName(null);
        modification.setModifies(parent);

        ContentItemGenerator generator = new ContentItemGenerator(modification);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_ITEM, NS_ITEM);
        checkUuidKey(record.getKey(), modification.getUid());

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 4, fields.size());

        EimRecordField titleField = fields.get(0);
        Assert.assertTrue(titleField.isMissing());
    }

    private StringAttribute makeStringAttribute() {
        StringAttribute attr = new MockStringAttribute();
        attr.setQName(new MockQName(NS_ITEM, "Blues Traveler"));
        attr.setValue("Sweet talkin' hippie");
        return attr;
    }
}
