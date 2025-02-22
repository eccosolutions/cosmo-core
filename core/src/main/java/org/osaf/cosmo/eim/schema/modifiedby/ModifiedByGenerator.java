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
package org.osaf.cosmo.eim.schema.modifiedby;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates modifidBy records from content items.
 *
 * @see ContentItem
 */
public class ModifiedByGenerator extends BaseItemGenerator
    implements ModifiedByConstants {
    private static final Log log =
        LogFactory.getLog(ModifiedByGenerator.class);

    /** */
    public ModifiedByGenerator(Item item) {
        super(PREFIX_MODIFIEDBY, NS_MODIFIEDBY, item);
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("item " + item.getUid() + " not a content item");
    }

    /**
     * Copies contentItem properties and attributes into a contentItem
     * record.
     */
    public List<EimRecord> generateRecords() {
        ContentItem contentItem = (ContentItem) getItem();

        ArrayList<EimRecord> records = new ArrayList<>();

//        if (contentItem.getClientModifiedDate() == null)
            return records;

/*
        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID, contentItem.getUid()));

        record.addKeyField(new TextField(FIELD_USERID,
                                         contentItem.getLastModifiedBy()));

        long timestamp = contentItem.getClientModifiedDate().getTime() / 1000;
        record.addKeyField(new DecimalField(FIELD_TIMESTAMP,
                                            new BigDecimal(timestamp),
                                            DIGITS_TIMESTAMP, DEC_TIMESTAMP));

        record.addKeyField(new IntegerField(FIELD_ACTION,
                                            contentItem.getLastModification()));

        records.add(record);

        return records;
*/
    }
}
