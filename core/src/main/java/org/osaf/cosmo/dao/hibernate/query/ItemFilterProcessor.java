/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate.query;

import java.util.Set;

import jakarta.persistence.EntityManager;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.filter.ItemFilter;

/**
 * Defines api for applying a <code>ItemFilter</code> and
 * returning matching <code>Item</code> instances.
 */
public interface ItemFilterProcessor {

    /**
     * @param session hibernate session
     * @param filter item filter
     * @return set of items that match filter
     */
    public Set<Item> processFilter(EntityManager session, ItemFilter filter);
}
