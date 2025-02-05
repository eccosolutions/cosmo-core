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
package org.osaf.cosmo.model.hibernate;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Tombstone;

/**
 * Hibernate persistent Tombstone.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="cosmo_tombstones")
@DiscriminatorColumn(
        name="tombstonetype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
public abstract class HibTombstone extends BaseModelObject implements Tombstone {

    @Column(name = "removedate", nullable = false)
    @Type(type="long_timestamp")
    private Date timestamp = null;

    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", nullable = false)
    private Item item = null;

    public HibTombstone() {
    }

    public HibTombstone(Item item) {
        this.item = item;
        this.timestamp = new Date(System.currentTimeMillis());
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Tombstone#getTimestamp()
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Tombstone#setTimestamp(java.util.Date)
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Tombstone#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Tombstone#setItem(org.osaf.cosmo.model.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null || !(obj instanceof Tombstone))
            return false;
        return new EqualsBuilder().append(item, ((Tombstone) obj).getItem()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 23).append(item).toHashCode();
    }
}
