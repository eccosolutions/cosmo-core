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

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BooleanAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent BooleanAtttribute.
 */
@Entity
@DiscriminatorValue("boolean")
public class HibBooleanAttribute extends HibAttribute implements java.io.Serializable, BooleanAttribute {

    /**
     *
     */
    private static final long serialVersionUID = -8393344132524216261L;

    @Column(name = "booleanvalue")
    private Boolean value;

    /** default constructor */
    public HibBooleanAttribute() {
    }

    public HibBooleanAttribute(QName qname, Boolean value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.hibernate.HibAttribute#getValue()
     */
    public Boolean getValue() {
        return this.value;
    }

    public Attribute copy() {
        BooleanAttribute attr = new HibBooleanAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value);
        return attr;
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.BooleanAttribute#setValue(java.lang.Boolean)
     */
    public void setValue(Boolean value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Boolean))
            throw new ModelValidationException(
                    "attempted to set non Boolean value on attribute");
        setValue((Boolean) value);
    }

    /**
     * Convienence method for returning a Boolean value on a BooleanAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch BooleanAttribute from
     * @param qname QName of attribute
     * @return Boolean value of IntegerAttribute
     */
    public static Boolean getValue(Item item, QName qname) {
        BooleanAttribute ba = (BooleanAttribute) item.getAttribute(qname);
        if(ba==null)
            return null;
        else
            return ba.getValue();
    }

    /**
     * Convienence method for setting a Boolean value on a BooleanAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch BooleanAttribute from
     * @param qname QName of attribute
     * @param value value to set on BooleanAttribute
     */
    public static void setValue(Item item, QName qname, Boolean value) {
        BooleanAttribute attr = (BooleanAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibBooleanAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if(value==null)
            item.removeAttribute(qname);
        else
            attr.setValue(value);
    }

}
