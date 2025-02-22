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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Hibernate persistent CollectionSubscription.
 */
@Entity
//Define a unique constraint on user and name
//because we don't want two subscriptions with the same name
//to be associated with the same user
@Table(name="cosmo_subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames={"ownerid", "displayname"})})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibCollectionSubscription extends HibAuditableObject implements CollectionSubscription {

    /**
     *
     */
    private static final long serialVersionUID = 1376628118792909419L;

    @ManyToOne(targetEntity=HibUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerid", nullable = false)
    @NotNull
    private User owner;

    @Column(name = "displayname", nullable = false, length = 255)
    @NotNull
    private String displayName;


    @Column(name = "collectionuid", nullable = false, length = 255)
    @NotNull
    private String collectionUid;

    /**
     */
    public HibCollectionSubscription() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#getCollectionUid()
     */
    public String getCollectionUid() {
        return collectionUid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#setCollectionUid(java.lang.String)
     */
    public void setCollectionUid(String collectionUid) {
        this.collectionUid = collectionUid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#setCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public void setCollection(CollectionItem collection) {
        this.collectionUid = collection.getUid();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionSubscription#setOwner(org.osaf.cosmo.model.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }
}
