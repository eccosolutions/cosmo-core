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
package org.osaf.cosmo.model.mock;

import java.math.BigDecimal;

import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;

/**
 * Represents attribute with a decimal value.
 */
public class MockDecimalAttribute extends MockAttribute implements java.io.Serializable, DecimalAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 7830581788843520989L;
    
    
    private BigDecimal value;

    /** default constructor */
    public MockDecimalAttribute() {
    }

    public MockDecimalAttribute(QName qname, BigDecimal value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceDecimalAttribute#getValue()
     */
    public BigDecimal getValue() {
        return this.value;
    }

    public Attribute copy() {
        DecimalAttribute attr = new MockDecimalAttribute();
        attr.setQName(getQName().copy());
        if(value!=null)
            attr.setValue(new BigDecimal(value.toString()));
        return attr;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceDecimalAttribute#setValue(java.math.BigDecimal)
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && !(value instanceof BigDecimal))
            throw new ModelValidationException(
                    "attempted to set non BigDecimal value on attribute");
        setValue((BigDecimal) value);
    }

}
