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
package org.osaf.cosmo.atom.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for for {@link ExpandedItemProvider} tests.
 */
public abstract class BaseExpandedItemCollectionAdapterTestCase
    extends BaseCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(BaseExpandedItemCollectionAdapterTestCase.class);

    protected BaseCollectionAdapter createAdapter() {
        ExpandedItemCollectionAdapter adapter = new ExpandedItemCollectionAdapter();
        adapter.setProcessorFactory(helper.getProcessorFactory());
        adapter.setContentService(helper.getContentService());
        return adapter;
    }
}
