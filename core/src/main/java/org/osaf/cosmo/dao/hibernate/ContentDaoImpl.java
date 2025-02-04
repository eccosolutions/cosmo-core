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

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.hibernate.Hibernate;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.*;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibItem;
import org.osaf.cosmo.model.hibernate.HibItemTombstone;

import javax.validation.ConstraintViolationException;
import java.util.*;

/**
 * Implementation of ContentDao using hibernate persistence objects
 *
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    private boolean shouldUpdateCollectionTimestamp = Boolean.getBoolean("cosmo.updateCollectionTimestamp");

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#createCollection(org.osaf.cosmo.model.CollectionItem,
     *      org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItem createCollection(CollectionItem parent,
            CollectionItem collection) {

        if(parent==null)
            throw new IllegalArgumentException("parent cannot be null");

        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");

        if (collection.getOwner() == null)
            throw new IllegalArgumentException("collection must have owner");

        if (getBaseModelObject(collection).getId()!=-1)
            throw new IllegalArgumentException("invalid collection id (expected -1)");


        try {
            // verify uid not in use
            checkForDuplicateUid(collection);

            setBaseItemProps(collection);
            ((HibItem) collection).addParent(parent);

            currentSession().save(collection);
            currentSession().flush();

            return collection;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#updateCollection(org.osaf.cosmo.model.CollectionItem, java.util.Set, java.util.Map)
     */
    public CollectionItem updateCollection(CollectionItem collection, Set<ContentItem> children) {

        // Keep track of duplicate icalUids because we don't flush
        // the db until the end so we need to handle the case of
        // duplicate icalUids in the same request.
        HashMap<String, NoteItem> icalUidMap = new HashMap<>();

        try {
            updateCollectionInternal(collection);

            // Either create, update, or delete each item
            for (ContentItem item : children) {

                // Because we batch all the db operations, we must check
                // for duplicate icalUid within the same request
                if (item instanceof NoteItem note && note.getIcalUid() != null) {
                    if (item.getIsActive()) {
                        NoteItem dup = icalUidMap.get(note.getIcalUid());
                        if (dup != null && !dup.getUid().equals(item.getUid()))
                            throw new IcalUidInUseException("iCal uid"
                                    + note.getIcalUid()
                                    + " already in use for collection "
                                    + collection.getUid(), item.getUid(), dup
                                    .getUid());
                    }

                    icalUidMap.put(note.getIcalUid(), note);
                }

                // create item
                if(getBaseModelObject(item).getId()==-1) {
                    createContentInternal(collection, item);
                }
                // delete item
                else if(item.getIsActive()==false) {
                    // If item is a note modification, only remove the item
                    // if its parent is not also being removed.  This is because
                    // when a master item is removed, all its modifications are
                    // removed.
                    if(item instanceof NoteItem note) {
                        if(note.getModifies()!=null && !note.getModifies().getIsActive())
                            continue;
                    }
                    removeItemFromCollectionInternal(item, collection);
                }
                // update item
                else {
                    if(!item.getParents().contains(collection)) {

                        // If item is being added to another collection,
                        // we need the ticket/perms to add that item.
                        // If ticket exists, then add with ticket and ticket perms.
                        // If ticket doesn't exist, but item uuid is present in
                        // itemPerms map, then add with read-only access.

                        addItemToCollectionInternal(item, collection);
                    }

                    updateContentInternal(item);
                }
            }

            currentSession().flush();

            // clear the session to improve subsequent flushes
            currentSession().clear();

            // load collection to get it back into the session
            currentSession().load(collection, getBaseModelObject(collection).getId());

            return collection;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logConstraintViolationException(ise);
            throw ise;
        }
    }



    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#createContent(org.osaf.cosmo.model.CollectionItem,
     *      org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem createContent(CollectionItem parent, ContentItem content) {

        try {
            createContentInternal(parent, content);
            currentSession().flush();
            return content;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logConstraintViolationException(ise);
            throw ise;
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#createContent(java.util.Set, org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {

        try {
            createContentInternal(parents, content);
            currentSession().flush();
            return content;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logConstraintViolationException(ise);
            throw ise;
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#updateCollectionTimestamp(org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        if (!shouldUpdateCollectionTimestamp) {
            return collection;
        }

        try {
            if(!currentSession().contains(collection))
                collection = (CollectionItem) currentSession().merge(collection);
            collection.updateTimestamp();
            currentSession().flush();
            return collection;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#updateCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        try {

            updateCollectionInternal(collection);
            currentSession().flush();

            return collection;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logConstraintViolationException(ise);
            throw ise;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#updateContent(org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem updateContent(ContentItem content) {
        try {
            updateContentInternal(content);
            currentSession().flush();
            return content;
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        } catch (ConstraintViolationException ise) {
            logConstraintViolationException(ise);
            throw ise;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#removeCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public void removeCollection(CollectionItem collection) {

        if(collection==null)
            throw new IllegalArgumentException("collection cannot be null");

        try {
            currentSession().refresh(collection);
            removeCollectionRecursive(collection);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ContentDao#removeContent(org.osaf.cosmo.model.ContentItem)
     */
    public void removeContent(ContentItem content) {

        if(content==null)
            throw new IllegalArgumentException("content cannot be null");

        try {
            currentSession().refresh(content);
            removeContentRecursive(content);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#removeUserContent(org.osaf.cosmo.model.User)
     */
    public void removeUserContent(User user) {
        try {
            var query = entityManager.createNamedQuery("contentItem.by.owner", ContentItem.class)
                .setParameter("owner", user);

            List<ContentItem> results = query.getResultList();
            for(ContentItem content: results)
                removeContentRecursive(content);
            currentSession().flush();
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#loadChildren(org.osaf.cosmo.model.CollectionItem, java.util.Date)
     */
    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp) {
        try {
            Set<ContentItem> children = new HashSet<>();
            TypedQuery<ContentItem> query =
                timestamp == null ? entityManager.createNamedQuery("contentItem.by.parent",
                        ContentItem.class)
                    .setParameter("parent", collection)
                    : entityManager.createNamedQuery("contentItem.by.parent.timestamp",
                            ContentItem.class)
                        .setParameter("parent", collection).setParameter(
                            "timestamp", timestamp);

            // use custom HQL query that will eager fetch all associations

            setManualFlush(query);
            List<ContentItem> results = query.getResultList();
            for (ContentItem content : results) {
                initializeItem(content);
                children.add(content);
            }

            return children;

        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }
    }


    @Override
    public void initializeItem(Item item) {
        super.initializeItem(item);

        // Initialize master NoteItem if applicable
        try {
           if(item instanceof NoteItem note) {
               if(note.getModifies()!=null) {
                   Hibernate.initialize(note.getModifies());
                   initializeItem(note.getModifies());
               }
           }
        } catch (PersistenceException e) {
            currentSession().clear();
            throw convertJpaAccessException(e);
        }

    }

    @Override
    public void removeItem(Item item) {
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }


    @Override
    public void removeItemByPath(String path) {
        Item item = this.findItemByPath(path);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }

    @Override
    public void removeItemByUid(String uid) {
        Item item = this.findItemByUid(uid);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }


    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        super.init();
    }

    private void removeContentRecursive(ContentItem content) {
        removeContentCommon(content);

        // Remove modifications
        if(content instanceof NoteItem note) {
            if(note.getModifies()!=null) {
                // remove mod from master's collection
                note.getModifies().removeModification(note);
                note.getModifies().updateTimestamp();
            } else {
                // mods will be removed by Hibernate cascading rules, but we
                // need to add tombstones for mods
                for(NoteItem mod: note.getModifications())
                    removeContentCommon(mod);
            }
        }

        currentSession().delete(content);
    }

    /**
     * NOTE: We now default this to off to avoid perf impacts on modifying a "collection" join table.
     * We don't use the timestamps but the update causes a conditional merge, update and flush
     */
    public void setShouldUpdateCollectionTimestamp(boolean shouldUpdateCollectionTimestamp) {
        this.shouldUpdateCollectionTimestamp = shouldUpdateCollectionTimestamp;
    }

    private void removeContentCommon(ContentItem content) {
        // Add a tombstone to each parent collection to track
        // when the removal occurred.
        for (CollectionItem parent : content.getParents()) {
            getHibItem(parent).addTombstone(new HibItemTombstone(parent,content));
            currentSession().update(parent);
        }
    }

    private void removeCollectionRecursive(CollectionItem collection) {
        // Removing a collection does not automatically remove
        // its children.  Instead, the association to all the
        // children is removed, and any children who have no
        // parent collection are then removed.
        for(Item item: collection.getChildren()) {
            if(item instanceof CollectionItem) {
                removeCollectionRecursive((CollectionItem) item);
            } else if(item instanceof ContentItem) {
                ((HibItem) item).removeParent(collection);
                if(item.getParents().isEmpty())
                    currentSession().delete(item);
            } else {
                currentSession().delete(item);
            }
        }

        currentSession().delete(collection);
    }


    private void removeNoteItemFromCollectionInternal(NoteItem note, CollectionItem collection) {
        currentSession().update(collection);
        currentSession().update(note);

        // do nothing if item doesn't belong to collection
        if(!note.getParents().contains(collection))
            return;

        getHibItem(collection).addTombstone(new HibItemTombstone(collection, note));
        ((HibItem) note).removeParent(collection);

        for(NoteItem mod: note.getModifications())
            removeNoteItemFromCollectionInternal(mod, collection);

        // If the item belongs to no collection, then it should
        // be purged.
        if(note.getParents().isEmpty())
            removeItemInternal(note);

    }


    protected void createContentInternal(CollectionItem parent, ContentItem content) {

        if(parent==null)
            throw new IllegalArgumentException("parent cannot be null");

        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (getBaseModelObject(content) .getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");

        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");

        // verify uid not in use
        checkForDuplicateUid(content);

        // verify icaluid not in use for collection
        if (content instanceof ICalendarItem)
            checkForDuplicateICalUid((ICalendarItem) content, parent);

        setBaseItemProps(content);


        // When a note modification is added, it must be added to all
        // collections that the parent note is in, because a note modification's
        // parents are tied to the parent note.
        if(isNoteModification(content)) {
            NoteItem note = (NoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if(!note.getModifies().getParents().contains(parent))
                throw new ModelValidationException(note, "cannot create modification "
                        + note.getUid() + " in collection " + parent.getUid()
                        + ", master must be created or added first");

            // Add modification to all parents of master
            for (CollectionItem col : note.getModifies().getParents()) {
                if (((HibCollectionItem) col).removeTombstone(content) == true)
                    currentSession().update(col);
                ((HibItem) note).addParent(col);
            }
        } else {
            // add parent to new content
            ((HibItem) content).addParent(parent);

            // remove tombstone (if it exists) from parent
            if(((HibCollectionItem)parent).removeTombstone(content)==true)
                currentSession().update(parent);
        }


        currentSession().save(content);
    }

    protected void createContentInternal(Set<CollectionItem> parents, ContentItem content) {

        if(parents==null)
            throw new IllegalArgumentException("parent cannot be null");

        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (getBaseModelObject(content).getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");

        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");


        if(parents.isEmpty())
            throw new IllegalArgumentException("content must have at least one parent");

        // verify uid not in use
        checkForDuplicateUid(content);

        // verify icaluid not in use for collections
        if (content instanceof ICalendarItem)
            checkForDuplicateICalUid((ICalendarItem) content, content.getParents());

        setBaseItemProps(content);

        // Ensure NoteItem modifications have the same parents as the
        // master note.
        if (isNoteModification(content)) {
            NoteItem note = (NoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if (!note.getModifies().getParents().equals(parents)) {
                StringBuilder modParents = new StringBuilder();
                StringBuilder masterParents = new StringBuilder();
                for(CollectionItem p: parents)
                    modParents.append(p.getUid()).append(",");
                for (CollectionItem p : note.getModifies().getParents())
                    masterParents.append(p.getUid()).append(",");
                throw new ModelValidationException(note,
                        "cannot create modification " + note.getUid()
                                + " in collections " + modParents.toString()
                                + " because master's parents are different: "
                                + masterParents.toString());
            }
        }

        for(CollectionItem parent: parents) {
            ((HibItem) content).addParent(parent);
            if(((HibCollectionItem)parent).removeTombstone(content)==true)
                currentSession().update(parent);
        }


        currentSession().save(content);
    }

    protected void updateContentInternal(ContentItem content) {

        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if(content.getIsActive()==Boolean.FALSE)
            throw new IllegalArgumentException("content must be active");

        currentSession().update(content);

        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");

        content.updateTimestamp();

        if(isNoteModification(content)) {
            // ensure master is dirty so that etag gets updated
            ((NoteItem) content).getModifies().updateTimestamp();
        }

    }

    protected void updateCollectionInternal(CollectionItem collection) {
        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");

        currentSession().update(collection);

        if (collection.getOwner() == null)
            throw new IllegalArgumentException("collection must have owner");

        collection.updateTimestamp();
    }

    /**
     * Override so we can handle NoteItems. Adding a note to a collection
     * requires verifying that the icaluid is unique within the collection.
     */
    @Override
    protected void addItemToCollectionInternal(Item item,
            CollectionItem collection) {

        // Don't allow note modifications to be added to a collection
        // When a master is added, all the modifications are added
        if (isNoteModification(item))
            throw new ModelValidationException(item, "cannot add modification "
                    + item.getUid() + " to collection " + collection.getUid()
                    + ", only master");

        if (item instanceof ICalendarItem)
            // verify icaluid is unique within collection
            checkForDuplicateICalUid((ICalendarItem) item, collection);


        super.addItemToCollectionInternal(item, collection);

        // Add all modifications
        if(item instanceof NoteItem noteItem) {
            for(NoteItem mod: noteItem.getModifications())
                super.addItemToCollectionInternal(mod, collection);
        }
    }

    @Override
    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {
        if(item instanceof NoteItem note) {
            // When a note modification is removed, it is really removed from
            // all collections because a modification can't live in one collection
            // and not another.  It is tied to the collections that the master
            // note is in.  Therefore you can't just remove a modification from
            // a single collection when the master note is in multiple collections.
            if(note.getModifies() != null)
                removeContentRecursive((ContentItem) item);
            else
                removeNoteItemFromCollectionInternal(note, collection);
        }
        else
            super.removeItemFromCollectionInternal(item, collection);
    }

    protected void checkForDuplicateICalUid(ICalendarItem item, CollectionItem parent) {

        // TODO: should icalUid be required?  Currrently its not and all
        // items created by the webui dont' have it.
        if (item.getIcalUid() == null)
            return;

        // ignore modifications
        if(item instanceof NoteItem && ((NoteItem) item).getModifies()!=null)
            return;

        // Lookup item by parent/icaluid
        TypedQuery<Long> hibQuery = item instanceof NoteItem ? entityManager.createNamedQuery(
            "noteItemId.by.parent.icaluid", Long.class).setParameter("parentid",
            getBaseModelObject(parent).getId()).setParameter("icaluid", item.getIcalUid())
            : entityManager.createNamedQuery(
                "icalendarItem.by.parent.icaluid", Long.class).setParameter("parentid",
                getBaseModelObject(parent).getId()).setParameter("icaluid", item.getIcalUid());

        HibernateSessionSupport.setManualFlush(hibQuery);

        Long itemId = getUniqueResult(hibQuery);

        // if icaluid is in use throw exception
        if (itemId != null) {
            // If the note is new, then its a duplicate icaluid
            if (getBaseModelObject(item).getId() == -1) {
                Item dup = currentSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
            // If the note exists and there is another note with the same
            // icaluid, then its a duplicate icaluid
            if (getBaseModelObject(item).getId().equals(itemId)) {
                Item dup = currentSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
        }
    }

    protected void checkForDuplicateICalUid(ICalendarItem item,
            Set<CollectionItem> parents) {

        if (item.getIcalUid() == null)
            return;

        // ignore modifications
        if(isNoteModification(item))
            return;

        for (CollectionItem parent : parents)
            checkForDuplicateICalUid(item, parent);
    }

    private boolean isNoteModification(Item item) {
        return item instanceof NoteItem noteItem && noteItem.getModifies() != null;

    }
}
