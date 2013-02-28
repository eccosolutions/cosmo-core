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
package org.osaf.cosmo.eim.schema;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.model.Item;

/**
 * Base class for generators that map to <code>Item</code>s.
 *
 * @see Item
 */
public abstract class BaseItemGenerator extends BaseGenerator {
    private static final Log log =
        LogFactory.getLog(BaseItemGenerator.class);

    /**
     * This class should not be instantiated directly.
     */
    protected BaseItemGenerator(String prefix,
                                String namespace,
                                Item item) {
        super(prefix, namespace, item);
    }

    /**
     * Copies the data from an item into one or more EIM records.
     */
    public abstract List<EimRecord> generateRecords();
}
