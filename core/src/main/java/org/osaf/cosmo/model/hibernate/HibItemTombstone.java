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

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;

/**
 * Hibernate persistent ItemTombstone.
 */
@Entity
@DiscriminatorValue("item")
public class HibItemTombstone extends HibTombstone implements ItemTombstone {

    @Column(name="itemuid", length=255)
    private String itemUid = null;

    public HibItemTombstone() {
    }

    public HibItemTombstone(CollectionItem parent, Item item) {
        super(parent);
        itemUid = item.getUid();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ItemTombstone#getItemUid()
     */
    public String getItemUid() {
        return this.itemUid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ItemTombstone#setItemUid(java.lang.String)
     */
    public void setItemUid(String itemUid) {
        this.itemUid = itemUid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ItemTombstone))
            return false;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                itemUid, ((ItemTombstone) obj).getItemUid()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 27).appendSuper(super.hashCode())
                .append(itemUid.hashCode()).toHashCode();
    }
}
