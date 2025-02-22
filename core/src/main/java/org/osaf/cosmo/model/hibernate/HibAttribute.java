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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Target;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent Attribute.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
// Define a unique constraint on item, namespace, and localname
@Table(name="cosmo_attribute", uniqueConstraints = {
        @UniqueConstraint(columnNames={"itemid", "namespace", "localname"})},
        indexes={@Index(name="idx_attrtype", columnList="attributetype"),
                 @Index(name="idx_attrname", columnList="localname"),
                 @Index(name="idx_attrns", columnList="namespace")})
@DiscriminatorColumn(
        name="attributetype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class HibAttribute extends HibAuditableObject implements java.io.Serializable, Attribute {

    // Fields
    @Embedded
    @Target(HibQName.class)
    @AttributeOverrides( {
			// MED LENGTH CHANGED TO FIT PRODUCTION DB - from 255 to 128
    		@AttributeOverride(name="namespace", column = @Column(name="namespace", nullable = false, length=128) ),
            @AttributeOverride(name="localName", column = @Column(name="localname", nullable = false, length=128) )
    } )
    private HibQName qname;

    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", nullable = false)
    private Item item;

    // Constructors
    /** default constructor */
    public HibAttribute() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#getQName()
     */
    public HibQName getQName() {
        return qname;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#setQName(org.osaf.cosmo.model.QName)
     */
    public void setQName(QName qname) {
        this.qname = (HibQName) qname;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#getName()
     */
    public String getName() {
        if(qname==null)
            return null;

        return qname.getLocalName();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#setItem(org.osaf.cosmo.model.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#copy()
     */
    public abstract Attribute copy();

    /**
     * Return string representation
     */
    @Override
	public String toString() {
        Object value = getValue();
        if(value==null)
            return "null";
        return value.toString();
    }

    public void validate() {
        // subclasses can override
    }

}
