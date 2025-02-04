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
package org.osaf.cosmo.service;

import net.fortuna.ical4j.model.DateTime;
import org.osaf.cosmo.model.*;
import org.osaf.cosmo.model.filter.ItemFilter;

import java.util.Date;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for services that manage access to user content.
 */
@Transactional
public interface ContentService extends Service {

    /**
     * Get the root item for a user
     *
     * @param user
     */
    @Transactional(readOnly = true)
    HomeCollectionItem getRootItem(User user);

    /**
     * Find an item with the specified uid. The return type will be one of
     * ContentItem, CollectionItem, CalendarCollectionItem, CalendarItem.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    @Transactional(readOnly = true)
    Item findItemByUid(String uid);

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    @Transactional(readOnly = true)
    Item findItemByPath(String path);

    /**
     * Find content item by path relative to the identified parent
     * item.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    @Transactional(readOnly = true)
    Item findItemByPath(String path,
                        String parentUid);

    /**
     * Find content item's parent by path. Path is of the format:
     * /username/parent1/parent2/itemname.  In this example,
     * the item at /username/parent1/parent2 would be returned.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    @Transactional(readOnly = true)
    Item findItemParentByPath(String path);

    /**
     * Add an item to a collection.
     *
     * @param item
     *            item to add to collection
     * @param collection
     *            collection to add item to
     */
    void addItemToCollection(Item item, CollectionItem collection);

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param existingParent existing source collection
     * @param target existing destination collection
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    void copyItem(Item item, CollectionItem targetParent,
                  String path, boolean deepCopy);

    /**
     * Move item from one collection to another
     * @param item item to move
     * @param oldParent parent to remove item from
     * @param newParent parent to add item to
     */
    void moveItem(Item item, CollectionItem oldParent, CollectionItem newParent);

    /**
     * Remove an item. Removes item from all collections.
     *
     * @param item
     *            item to remove
     */
    void removeItem(Item item);

    /**
     * Remove an item from a collection.  The item will be removed if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    void removeItemFromCollection(Item item, CollectionItem collection);


    /**
     * Load all children for collection that have been updated since a
     * given timestamp.  If no timestamp is specified, then return all
     * children.
     * @param collection collection
     * @param timestamp timestamp
     * @return children of collection that have been updated since
     *         timestamp, or all children if timestamp is null
     */
    @Transactional(readOnly = true)
    Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp);

    /**
     * Create a new collection.
     *
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    CollectionItem createCollection(CollectionItem parent,
                                    CollectionItem collection);

    /**
     * Create a new collection with an initial set of items.
     * The initial set of items can include new items and
     * existing items.  New items will be created and associated
     * to the new collection and existing items will be updated
     * and associated to the new collection.
     *
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @param children
     *            collection children
     * @return newly created collection
     */
    CollectionItem createCollection(CollectionItem parent,
                                    CollectionItem collection,
                                    Set<Item> children);

    /**
     * Update a collection and set child items.  The set of
     * child items to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false for an existing Item.  When an item is marked
     * for removal, it is removed from the collection and
     * removed from the server only if the item has no parent
     * collections.
     *
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     * @return updated collection
     */
    CollectionItem updateCollection(CollectionItem collection,
                                    Set<Item> children);

    /**
     * Remove collection item
     *
     * @param collection
     *            collection item to remove
     */
    void removeCollection(CollectionItem collection);

    /**
     * Update collection item
     *
     * @param collection
     *            collection item to update
     * @return updated collection
     */
    CollectionItem updateCollection(CollectionItem collection);

    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     *
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    ContentItem createContent(CollectionItem parent,
                              ContentItem content);

    /**
     * Create new content items in a parent collection.
     *
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    void createContentItems(CollectionItem parent,
                            Set<ContentItem> contentItems);

    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     *
     * @param parents
     *            parents that new content items will be added to.
     * @param contentItems to update
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    void updateContentItems(Set<CollectionItem> parents, Set<ContentItem> contentItems);

    /**
     * Update an existing content item.
     *
     * @param content
     *            content item to update
     * @return updated content item
     */
    ContentItem updateContent(ContentItem content);


    /**
     * Remove content item
     *
     * @param content
     *            content item to remove
     */
    void removeContent(ContentItem content);


    /**
     * Find items by filter.
     *
     * @param filter
     *            filter to use in search
     * @return set items matching specified
     *         filter.
     */
    @Transactional(readOnly = true)
    Set<Item> findItems(ItemFilter filter);


    /**
     * Find calendar events by time range.
     *
     * @param collection
     *            collection to search
     * @param rangeStart time range start
     * @param rangeEnd time range end
     * @param expandRecurringEvents if true, recurring events will be expanded
     *        and each occurrence will be returned as a NoteItemOccurrence.
     * @return set ContentItem objects that contain EventStamps that occur
     *         int the given timeRange.
     */
    @Transactional(readOnly = true)
    Set<ContentItem> findEvents(CollectionItem collection,
                                DateTime rangeStart, DateTime rangeEnd,
                                boolean expandRecurringEvents);

}
