/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.util.Iterator;
import java.util.List;

import org.osaf.cosmo.eim.EimException;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.mock.MockItem;

/**
 * Iterator that translates items to EIM records.
 *
 * @see MockItem
 * @see EimRecord
 */
public class ItemTranslationIterator implements EimRecordSetIterator {

    private Iterator<ContentItem> decorated;
    private long timestamp;

    public ItemTranslationIterator(List<ContentItem> items) {
        this(items, -1);
    }

    public ItemTranslationIterator(List<ContentItem> items,
                                   long timestamp) {
        this.decorated = items.iterator();
        this.timestamp = timestamp;
    }

    public boolean hasNext()
        throws EimException {
        return decorated.hasNext();
    }

    public EimRecordSet next()
        throws EimException {
        return new ItemTranslator(decorated.next()).generateRecords(timestamp);
    }
}
