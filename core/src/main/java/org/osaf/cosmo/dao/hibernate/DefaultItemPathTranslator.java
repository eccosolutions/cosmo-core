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
package org.osaf.cosmo.dao.hibernate;

import org.hibernate.Session;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Default implementation for ItempPathTranslator. This implementation expects
 * paths to be of the format: /username/parent1/parent2/itemname
 *
 */
public class DefaultItemPathTranslator implements ItemPathTranslator {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.hibernate.ItemPathTranslator#findItemByPath(org.hibernate.Session,
     *      java.lang.String)
     */
    @Transactional
    public Item findItemByPath(final String path) {
        final Session session = getCurrentSession();
        return findItemByPath(session, path);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.hibernate.ItemPathTranslator#findItemByPath(java.lang.String, org.osaf.cosmo.model.CollectionItem)
     */
    @Transactional
    public Item findItemByPath(final String path, final CollectionItem root) {
        final Session session = getCurrentSession();
        return findItemByPath(session, path, root);
    }

    public Item findItemParent(String path) {
        if(path==null)
            return null;

        int lastIndex = path.lastIndexOf("/");
        if(lastIndex==-1)
            return null;

        if((lastIndex+1) >= path.length())
            return null;

        String parentPath =  path.substring(0,lastIndex);

        return findItemByPath(parentPath);
    }

    public String getItemName(String path) {
        if(path==null)
            return null;

        int lastIndex = path.lastIndexOf("/");
        if(lastIndex==-1)
            return null;

        if((lastIndex+1) >= path.length())
            return null;

        return path.substring(lastIndex+1);
    }

    protected Item findItemByPath(Session session, String path) {

        if(path == null || path.isEmpty())
            return null;

        if (path.charAt(0) == '/')
            path = path.substring(1);

        String[] segments = path.split("/");
        String username = segments[0];

        String rootName = segments[0];
        Item rootItem = findRootItemByOwnerAndName(session, username,
                rootName);

        // If parent item doesn't exist don't go any further
        if (rootItem == null)
            return null;

        Item parentItem = rootItem;
        for (int i = 1; i < segments.length; i++) {
            parentItem = findItemByParentAndName(session, parentItem,
                    segments[i]);
            // if any parent item doesn't exist then bail now
            if (parentItem == null)
                return null;
        }

        return parentItem;
    }

    protected Item findItemByPath(Session session, String path, CollectionItem root) {

        if(path == null || path.isEmpty())
            return null;

        if (path.charAt(0) == '/')
            path = path.substring(1);

        String[] segments = path.split("/");

        if (segments.length == 0)
            return null;

        Item parentItem = root;
        for (String segment : segments) {
            parentItem = findItemByParentAndName(session, parentItem, segment);
            // if any parent item doesn't exist then bail now
            if (parentItem == null)
                return null;
        }

        return parentItem;
    }

    protected Item findRootItemByOwnerAndName(Session session,
            String username, String name) {
        var hibQuery = session.createNamedQuery(
                "item.by.ownerName.name.nullParent", Item.class)
                .setParameter("username", username).setParameter("name", name);

        var results = hibQuery.getResultList();
        return results.isEmpty() ? null : (Item) results.get(0);
    }

    protected Item findItemByParentAndName(Session session, Item parent,
            String name) {
        var hibQuery = session.createNamedQuery("item.by.parent.name", Item.class)
                .setParameter("parent", parent).setParameter("name", name);

        var results = hibQuery.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

}
