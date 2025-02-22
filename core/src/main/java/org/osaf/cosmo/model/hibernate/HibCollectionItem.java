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
package org.osaf.cosmo.model.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent CollectionItem.
 */
@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem implements CollectionItem {

    /**
     *
     */
    private static final long serialVersionUID = 2873258323314048223L;

    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        new HibQName(CollectionItem.class, "excludeFreeBusyRollup");

    public static final QName ATTR_HUE =
        new HibQName(CollectionItem.class, "hue");

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="primaryKey.collection", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE })
    private final Set<CollectionItemDetails> childDetails = new HashSet<>(0);

    private transient Set<Item> children = null;

    public HibCollectionItem() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildren()
     */
    @Override
    public Set<Item> getChildren() {
        if(children!=null)
            return children;

        children = new HashSet<>();
        for(CollectionItemDetails cid: childDetails)
            children.add(cid.getItem());

        children = Collections.unmodifiableSet(children);

        return children;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildDetails(org.osaf.cosmo.model.Item)
     */
    @Override
    public CollectionItemDetails getChildDetails(Item item) {
        for(CollectionItemDetails cid: childDetails)
            if(cid.getItem().equals(item))
                return cid;

        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    @Override
    public Item getChild(String uid) {
        for (Item child : getChildren()) {
            if (child.getUid().equals(uid))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    @Override
    public Item getChildByName(String name) {
        for (Item child : getChildren()) {
            if (child.getName().equals(name))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#isExcludeFreeBusyRollup()
     */
    @Override
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv =  HibBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if(bv==null)
            return false;
        else
            return bv.booleanValue();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    @Override
    public void setExcludeFreeBusyRollup(boolean flag) {
       HibBooleanAttribute.setValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP, flag);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getHue()
     */
    @Override
    public Long getHue() {
        return HibIntegerAttribute.getValue(this, ATTR_HUE);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#setHue(java.lang.Long)
     */
    @Override
    public void setHue(Long value) {
        HibIntegerAttribute.setValue(this, ATTR_HUE, value);
    }

    /**
     * Remove ItemTombstone with an itemUid equal to a given Item's uid
     * @param item
     * @return true if a tombstone was removed
     */
    public boolean removeTombstone(Item item) {
        ItemTombstone ts = new HibItemTombstone(this, item);
        return tombstones.remove(ts);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#generateHash()
     */
    @Override
    public int generateHash() {
        return getVersion();
    }

    @Override
    public Item copy() {
        CollectionItem copy = new HibCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
