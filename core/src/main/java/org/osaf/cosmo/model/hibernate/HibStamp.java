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

import java.util.Date;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;

/**
 * Hibernate persistent Stamp.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "stamptype", 
                     discriminatorType = DiscriminatorType.STRING, length = 16)
// Unique constraint for stamptype and itemid to prevent items
// having more than one of the same stamp
@Table(name = "cosmo_stamp", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "itemid", "stamptype" }) })
// Define index on discriminator
@org.hibernate.annotations.Table(appliesTo = "cosmo_stamp", 
        indexes = { @Index(name = "idx_stamptype", columnNames = { "stamptype" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class HibStamp extends HibAuditableObject implements
        java.io.Serializable, Stamp {

    // Fields
    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "itemid", nullable = false)
    private Item item;
    
    // Constructors
    /** default constructor */
    public HibStamp() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#setItem(org.osaf.cosmo.model.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }

    
    /**
     * Convenience method for retrieving an attribute on the underlying
     * item.
     * @param qname QName of attribute
     * @return attribute value
     */
    protected Attribute getAttribute(QName qname) {
        return getItem().getAttribute(qname);
    }
    
    /**
     * Convenience method for adding an attribute to the underlying item
     * @param attribute attribute to add
     */
    protected void addAttribute(Attribute attribute) {
        getItem().addAttribute(attribute);
    }
    
    /**
     * Convenience method for removing an attribute to the underlying item
     * @param qname QName of attribute to remove
     */
    protected void removeAttribute(QName qname) {
        getItem().removeAttribute(qname);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.hibernate.HibAuditableObject#updateTimestamp()
     */
    @Override
	public void updateTimestamp() {
        setModifiedDate(new Date());
    }
}
