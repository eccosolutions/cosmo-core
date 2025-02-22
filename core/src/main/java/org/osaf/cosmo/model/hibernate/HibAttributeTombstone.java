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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Target;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.AttributeTombstone;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent AttributeTombstone.
 */
@Entity
@DiscriminatorValue("attribute")
public class HibAttributeTombstone extends HibTombstone implements AttributeTombstone {

    @Embedded
    @Target(HibQName.class)
    @AttributeOverrides( {
            @AttributeOverride(name="namespace", column = @Column(name="namespace", length=255) ),
            @AttributeOverride(name="localName", column = @Column(name="localname", length=255) )
    } )
    private QName qname = null;

    public HibAttributeTombstone() {
    }

    public HibAttributeTombstone(Item item, Attribute attribute) {
        super(item);
        qname = attribute.getQName();
    }

    public HibAttributeTombstone(Item item, QName qname) {
        super(item);
        this.qname = qname;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.AttributeTombstone#getQName()
     */
    public QName getQName() {
        return qname;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.AttributeTombstone#setQName(org.osaf.cosmo.model.QName)
     */
    public void setQName(QName qname) {
        this.qname = qname;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AttributeTombstone))
            return false;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                qname, ((AttributeTombstone) obj).getQName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 31).appendSuper(super.hashCode())
                .append(qname.hashCode()).toHashCode();
    }


}
