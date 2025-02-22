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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.model.ItemTombstone;

/**
 * Handles the translation of EIM recordsets from a
 * <code>ItemTombstone</code>.
 *
 * @see EimRecordSet
 * @see ItemTombstone
 */
public class ItemTombstoneTranslator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(ItemTombstoneTranslator.class);

    private final ItemTombstone tombstone;

    /** */
    public ItemTombstoneTranslator(ItemTombstone tombstone) {
        this.tombstone = tombstone;
    }

    /**
     * Generates a deleted recordset from the tombstone.
     */
    public EimRecordSet generateRecordSet() {
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(tombstone.getItemUid());
        recordset.setDeleted(true);

        return recordset;
    }

    /** */
    public ItemTombstone getTombstone() {
        return tombstone;
    }
}
