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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.proxy.HibernateProxy;
import org.osaf.cosmo.dao.ItemDao;
import org.osaf.cosmo.dao.hibernate.query.ItemFilterProcessor;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.hibernate.BaseModelObject;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibEventStamp;
import org.osaf.cosmo.model.hibernate.HibHomeCollectionItem;
import org.osaf.cosmo.model.hibernate.HibItem;
import org.osaf.cosmo.model.hibernate.HibItemTombstone;

/**
 * Implementation of ItemDao using Hibernate persistent objects.
 *
 */
public abstract class ItemDaoImpl extends HibernateSessionSupport implements ItemDao {

    private static final Log log = LogFactory.getLog(ItemDaoImpl.class);

    private ItemPathTranslator itemPathTranslator = null;
    private ItemFilterProcessor itemFilterProcessor = null;

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#findItemByPath(java.lang.String)
     */
    public Item findItemByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemByPath(path);
            return dbItem;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#findItemByPath(java.lang.String, java.lang.String)
     */
    public Item findItemByPath(String path, String parentUid) {
        try {
            Item parent = findItemByUid(parentUid);
            if(parent==null)
                return null;
            Item item = itemPathTranslator.findItemByPath(path, (CollectionItem) parent);
            return item;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#findItemParentByPath(java.lang.String)
     */
    public Item findItemParentByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemParent(path);
            return dbItem;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#findItemByUid(java.lang.String)
     */
    public Item findItemByUid(String uid) {
        try {
            // prevent auto flushing when looking up item by uid
            currentSession().setHibernateFlushMode(FlushMode.MANUAL);

            // take advantage of optimized caching with naturalId
            Item item = currentSession().bySimpleNaturalId(HibItem.class).load(uid);

            // Prevent proxied object from being returned
            if (item instanceof HibernateProxy)
                item = (Item) ((HibernateProxy) item).getHibernateLazyInitializer().getImplementation();

            return item;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#removeItem(org.osaf.cosmo.model.Item)
     */
    public void removeItem(Item item) {
        try {

            if(item==null)
                throw new IllegalArgumentException("item cannot be null");

            if(item instanceof HomeCollectionItem)
                throw new IllegalArgumentException("cannot remove root item");

            removeItemInternal(item);
            currentSession().flush();

        } catch(UnresolvableObjectException uoe) {
            throw new ItemNotFoundException("item not found");
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#getRootItem(org.osaf.cosmo.model.User)
     */
    public HomeCollectionItem getRootItem(User user) {
        try {
            return findRootItem(getBaseModelObject(user).getId());
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#createRootItem(org.osaf.cosmo.model.User)
     */
    public HomeCollectionItem createRootItem(User user) {
        try {

            if(user==null)
                throw new IllegalArgumentException("invalid user");

            if( findRootItem(getBaseModelObject(user).getId()) != null )
                throw new RuntimeException("user already has root item");

            HomeCollectionItem newItem = new HibHomeCollectionItem();

            newItem.setOwner(user);
            newItem.setName(user.getUsername());
            newItem.setDisplayName(newItem.getName());
            setBaseItemProps(newItem);
            currentSession().save(newItem);
            currentSession().flush();
            return newItem;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void addItemToCollection(Item item, CollectionItem collection) {
        try {
            addItemToCollectionInternal(item, collection);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    public void removeItemFromCollection(Item item, CollectionItem collection) {
        try {
            removeItemFromCollectionInternal(item, collection);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#removeItemByPath(java.lang.String)
     */
    public void removeItemByPath(String path) {
        try {
            Item item = itemPathTranslator.findItemByPath(path);
            if(item==null)
                throw new ItemNotFoundException("item at " + path
                        + " not found");
            removeItem(item);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#removeItemByUid(java.lang.String)
     */
    public void removeItemByUid(String uid) {
        try {
            Item item = findItemByUid(uid);
            if (item == null)
                throw new ItemNotFoundException("item with uid " + uid
                        + " not found");
            removeItem(item);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    public void copyItem(Item item, String destPath, boolean deepCopy) {
        try {
            String copyName = itemPathTranslator.getItemName(destPath);

            if(copyName == null || copyName.isEmpty())
                throw new IllegalArgumentException("path must include name");

            if(item instanceof HomeCollectionItem)
                throw new IllegalArgumentException("cannot copy root collection");

            CollectionItem newParent = (CollectionItem) itemPathTranslator.findItemParent(destPath);

            if(newParent==null)
                throw new ItemNotFoundException("parent collection not found");

            verifyNotInLoop(item, newParent);

            Item newItem = copyItemInternal(item, newParent, deepCopy);
            newItem.setName(copyName);
            currentSession().flush();

        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#moveItem(java.lang.String, java.lang.String)
     */
    public void moveItem(String fromPath, String toPath) {
        try {

            // Get current item
            Item item = itemPathTranslator.findItemByPath(fromPath);

            if(item==null)
                throw new ItemNotFoundException("item " + fromPath + " not found");

            if(item instanceof HomeCollectionItem)
                throw new IllegalArgumentException("cannot move root collection");

            // Name of moved item
            String moveName = itemPathTranslator.getItemName(toPath);

            if(moveName == null || moveName.isEmpty())
                throw new IllegalArgumentException("path must include name");

            // Parent of moved item
            CollectionItem parent = (CollectionItem) itemPathTranslator.findItemParent(toPath);

            if(parent==null)
                throw new ItemNotFoundException("parent collecion not found");

            // Current parent
            CollectionItem oldParent = (CollectionItem) itemPathTranslator.findItemParent(fromPath);

            verifyNotInLoop(item, parent);

            item.setName(moveName);
            if(!parent.getUid().equals(oldParent.getUid())) {
                ((HibCollectionItem)parent).removeTombstone(item);

                // Copy over existing CollectionItemDetails
                CollectionItemDetails cid = item.getParentDetails(oldParent);
                ((HibItem) item).addParent(parent);

                // Remove item from old parent collection
                getHibItem(oldParent).addTombstone(new HibItemTombstone(oldParent, item));
                ((HibItem) item).removeParent(oldParent);
            }

            currentSession().flush();

        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#refreshItem(org.osaf.cosmo.model.Item)
     */
    public void refreshItem(Item item) {
        try {
           currentSession().refresh(item);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#initializeItem(org.osaf.cosmo.model.Item)
     */
    public void initializeItem(Item item) {
        try {
            // initialize all the proxied-associations, to prevent
            // lazy-loading of this data
            Hibernate.initialize(item.getAttributes());
            Hibernate.initialize(item.getStamps());
            Hibernate.initialize(item.getTombstones());
         } catch (PersistenceException e) {
             currentSession().clear();
            throw convertJpaAccessException(e);
         }
    }

    /**
     * Find a set of items using an ItemFilter.
     * @param filter criteria to filter items by
     * @return set of items matching ItemFilter
     */
    public Set<Item> findItems(ItemFilter filter) {
        try {
            return itemFilterProcessor.processFilter(currentSession(), filter);
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /**
     * Find a set of items using a set of ItemFilters.  The set of items
     * returned includes all items that match any of the filters.
     * @param filters criteria to filter items by
     * @return set of items matching any of the filters
     */
    public Set<Item> findItems(ItemFilter[] filters) {
        try {
            HashSet<Item> returnSet = new HashSet<>();
            for (ItemFilter filter : filters)
                returnSet.addAll(itemFilterProcessor.processFilter(
                        currentSession(), filter));
            return returnSet;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /**
     * Generates a unique ID. Provided for consumers that need to
     * manipulate an item's UID before creating the item.
     */
    public String generateUid() {
        return UUID.randomUUID().toString();
    }

    public ItemPathTranslator getItemPathTranslator() {
        return itemPathTranslator;
    }

    /**
     * Set the path translator. The path translator is responsible for
     * translating a path to an item in the database.
     */
    public void setItemPathTranslator(ItemPathTranslator itemPathTranslator) {
        this.itemPathTranslator = itemPathTranslator;
    }


    public ItemFilterProcessor getItemFilterProcessor() {
        return itemFilterProcessor;
    }

    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.Dao#init()
     */
    public void init() {
        if (itemPathTranslator == null) {
            throw new IllegalStateException("itemPathTranslator is required");
        }

        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

    }

    protected Item copyItemInternal(Item item, CollectionItem newParent, boolean deepCopy) {

        Item item2 = item.copy();
        item2.setName(item.getName());

        // copy base Item fields
        setBaseItemProps(item2);

        ((HibItem) item2).addParent(newParent);

        // save Item before attempting deep copy
        currentSession().save(item2);
        currentSession().flush();

        // copy children if collection and deepCopy = true
        if(deepCopy && (item instanceof CollectionItem collection) ) {
            for(Item child: collection.getChildren())
                copyItemInternal(child, (CollectionItem) item2,true);
        }

        return item2;
    }

    /**
     * Checks to see if a parent Item is currently a child of a target item. If
     * so, then this would put the hierarchy into a loop and is not allowed.
     *
     * @throws ModelValidationException
     *             if newParent is child of item
     */
    protected void verifyNotInLoop(Item item, CollectionItem newParent) {
        // need to verify that the new parent is not a child
        // of the item, otherwise we get a loop
        if (getBaseModelObject(item).getId().equals(getBaseModelObject(newParent).getId()))
            throw new ModelValidationException(newParent,
                    "Invalid parent - will cause loop");

        // If item is not a collection then all is good
        if(!(item instanceof CollectionItem collection))
            return;

        currentSession().refresh(collection);

        for (Item nextItem: collection.getChildren())
            verifyNotInLoop(nextItem, newParent);
    }

    /**
     * Verifies that name is unique in collection, meaning no item exists
     * in collection with the same item name.
     * @param item item to check
     * @param collection collection to check against
     * @throws DuplicateItemNameException if item with same name exists
     *         in collection
     */
    protected void verifyItemNameUnique(Item item, CollectionItem collection) {
        var hibQuery = entityManager.createNamedQuery("itemId.by.parentId.name", Long.class);
        hibQuery.setParameter("name", item.getName()).setParameter("parentid",
                ((HibItem) collection).getId());
        var results = hibQuery.getResultList();
        if(!results.isEmpty()) {
            throw new DuplicateItemNameException(item, "item name " + item.getName() +
                    " already exists in collection " + collection.getUid());
        }
    }

    /**
     * Find the DbItem with the specified dbId
     *
     * @param dbId
     *            dbId of DbItem to find
     * @return DbItem with specified dbId
     */
    protected Item findItemByDbId(Long dbId) {
        return currentSession().get(Item.class, dbId);
    }

    // Set server generated item properties
    protected void setBaseItemProps(Item item) {
        if (item.getUid() == null)
            item.setUid(UUID.randomUUID().toString());
        if (item.getName() == null)
            item.setName(item.getUid());
        if (item instanceof ICalendarItem ical) {
            if (ical.getIcalUid() == null) {
                ical.setIcalUid(item.getUid());
                EventStamp es = HibEventStamp.getStamp(ical);
                if (es != null)
                    es.setIcalUid(ical.getIcalUid());
            }
        }
    }

    protected Item findItemByParentAndName(Long userDbId, Long parentDbId, String name) {
        var hibQuery = parentDbId != null
            ? entityManager.createNamedQuery("item.by.ownerId.parentId.name", Item.class)
                .setParameter("ownerid", userDbId)
                .setParameter("parentid", parentDbId)
                .setParameter("name", name)
            : entityManager.createNamedQuery("item.by.ownerId.nullParent.name", Item.class)
                .setParameter("ownerid", userDbId)
                .setParameter("name", name);
        setManualFlush(hibQuery);
        return getUniqueResult(hibQuery);
    }

    protected Item findItemByParentAndNameMinusItem(Long userDbId, Long parentDbId,
            String name, Long itemId) {
        var hibQuery = parentDbId != null
            ? entityManager.createNamedQuery("item.by.ownerId.parentId.name.minusItem", Item.class)
                .setParameter("itemid", itemId)
                .setParameter("ownerid", userDbId)
                .setParameter("parentid", parentDbId)
                .setParameter("name", name)
            : entityManager.createNamedQuery("item.by.ownerId.nullParent.name.minusItem", Item.class)
                .setParameter("itemid", itemId)
                .setParameter("ownerid", userDbId)
                .setParameter("name", name);
        setManualFlush(hibQuery);
        return getUniqueResult(hibQuery);
    }

    protected HomeCollectionItem findRootItem(Long dbUserId) {
        var hibQuery = currentSession().createNamedQuery(
                "homeCollection.by.ownerId", HomeCollectionItem.class)
            .setParameter("ownerid", dbUserId);
        setCacheable(hibQuery);
        setManualFlush(hibQuery);

        return getUniqueResult(hibQuery);
    }

    protected void checkForDuplicateUid(Item item) {
        // verify uid not in use
        if (item.getUid() != null) {

            // Lookup item by uid
            var hibQuery = currentSession().createNamedQuery("itemid.by.uid", Long.class)
                    .setParameter("uid", item.getUid());
            setManualFlush(hibQuery);

            Long itemId = getUniqueResult(hibQuery);

            // if uid is in use throw exception
            if (itemId != null) {
                throw new UidInUseException(item.getUid(), "uid " + item.getUid()
                        + " already in use");
            }
        }
    }

    protected void attachToSession(Item item) {
        if(currentSession().contains(item))
            return;
        currentSession().lock(item, LockMode.NONE);
    }

    protected void logConstraintViolationException(ConstraintViolationException ise) {
        // log more info about the invalid state
        if(log.isDebugEnabled()) {
            log.debug(ise.getLocalizedMessage());

            for (ConstraintViolation<?> iv : ise.getConstraintViolations())
                log.debug("property name: " + iv.getPropertyPath() + " value: "
                        + iv.getInvalidValue());
        }
    }

    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {

        currentSession().update(collection);
        currentSession().update(item);

        // do nothing if item doesn't belong to collection
        if(!item.getParents().contains(collection))
            return;

        getHibItem(collection).addTombstone(new HibItemTombstone(collection, item));
        ((HibItem) item).removeParent(collection);

        // If the item belongs to no collection, then it should
        // be purged.
        if(item.getParents().isEmpty())
            removeItemInternal(item);
    }

    protected void addItemToCollectionInternal(Item item,
            CollectionItem collection) {
        verifyItemNameUnique(item, collection);
        currentSession().update(item);
        currentSession().update(collection);
        ((HibCollectionItem)collection).removeTombstone(item);
        ((HibItem) item).addParent(collection);
    }

    protected void removeItemInternal(Item item) {
        currentSession().delete(item);
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

    protected HibItem getHibItem(Item item) {
        return (HibItem) item;
    }

    protected HibCollectionItem getHibCollectionItem(CollectionItem item) {
        return (HibCollectionItem) item;
    }

}
