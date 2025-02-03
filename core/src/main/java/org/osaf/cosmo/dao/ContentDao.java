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
package org.osaf.cosmo.dao;

import java.util.Date;
import java.util.Set;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for DAO that provides base operations for content items.
 *
 * A content item is either a piece of content (or file) or a collection
 * containing content items or other collection items.
 *
 */
public interface ContentDao extends ItemDao {

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
     * Update collection and children.  The set of children can contain
     * new items, existing items, and item removals.  An item removal
     * is recognized by Item.isActive==false.
     * @param collection collection to update
     * @param children children to updated
     * @return updated collection
     */
    CollectionItem updateCollection(CollectionItem collection,
            Set<ContentItem> children);

    /**
     * Update an existing collection.
     *
     * @param collection
     *            collection to update
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
    ContentItem createContent(CollectionItem parent, ContentItem content);


    /**
     * Create new content item and associate with multiple parent collections.
     *
     * @param parents
     *            parent collections of content.
     * @param content
     *            content to create
     * @return newly created content
     */
    ContentItem createContent(Set<CollectionItem> parents, ContentItem content);

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
     * Remove all content owned by a user
     *
     * @param user
     *            user to remove content for
     */
    void removeUserContent(User user);

    /**
     * Remove collection item
     *
     * @param collection
     *            collection item to remove
     */
    void removeCollection(CollectionItem collection);

    /**
     * Update timestamp on collection.
     * @param collection collection to update
     * @return updated collection
     */
    CollectionItem updateCollectionTimestamp(CollectionItem collection);

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
}
