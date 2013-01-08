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
package org.osaf.cosmo.model.mock;

import java.util.Date;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
import org.osaf.cosmo.model.Item;

/**
 * Mock CollectionItemDetails
 */
public class MockCollectionItemDetails implements CollectionItemDetails {

    private CollectionItem collection = null;
    private Item item = null;
    private Date createDate = new Date();
    
    public MockCollectionItemDetails(CollectionItem collection, Item item) {
        this.collection = collection;
        this.item = item;
    }
    
    public CollectionItem getCollection() {
        return collection;
    }
    public Item getItem() {
       return item;
    }
    public Date getTimestamp() {
        return createDate;
    }
}
